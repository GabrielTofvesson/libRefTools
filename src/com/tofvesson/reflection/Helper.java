package com.tofvesson.reflection;

import sun.misc.Unsafe;
import sun.reflect.ConstructorAccessor;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Helper {
    /**
     * Allows you to create a completely custom Enum value. If the supplied value already exists, it will be returned.
     * will not be created; the value of the existing one will be updated.
     * @param clazz The class to attribute the new enum to.
     * @param addToValuesArray Whether or not to update the internal, "immutable" values array with the new value (ignored if value already exists).
     * @return A new/existing enum.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T customEnum(Class<T> clazz, boolean addToValuesArray, String name, EnumDefinition def){
        T u;
        try {
            // Get a reference to the static method values() and get values
            Method v = clazz.getDeclaredMethod("values");
            v.setAccessible(true);
            T[] values = (T[]) v.invoke(null);

            // Return object if it already exists
            for(T u2 : values) if(u2.name().equals(name)) return u2;

            // Generate enum parameter definition
            Class[] paramList = new Class[def.params.size()+2];
            paramList[0] = String.class; // Name
            paramList[1] = int.class; // Ordinal
            int iterator = paramList.length;
            for(Class c : def.params.values()) paramList[--iterator] = c; // Shit's fucking reversed

            // Get enum constructor (inherited from Enum.class)
            Constructor c = clazz.getDeclaredConstructor(paramList);
            c.setAccessible(true);
            Method m = Constructor.class.getDeclaredMethod("acquireConstructorAccessor");
            m.setAccessible(true);

            // Get constructor accessor since Constructor.newInstance throws an exception because Enums are "immutable"
            ConstructorAccessor access = (ConstructorAccessor) m.invoke(c);

            Object[] parameters = new Object[def.params.size()+2];
            parameters[0] = name;
            parameters[1] = values.length;
            iterator = parameters.length;
            for(Object o : def.params.keySet()) parameters[--iterator] = o;

            // Create new instance of enum with valid name and ordinal
            u = (T) access.newInstance(parameters);

            // Get the final name field from Enum.class and make it temporarily modifiable
            Field f = Enum.class.getDeclaredField("name");
            f.setAccessible(true);

            // Rename the newly created enum to the requested name
            f.set(u, name);

            if(!addToValuesArray) return u; // Stops here if

            // Get the current values field from Enum (a bitch to modify)
            f = clazz.getDeclaredField("$VALUES");
            f.setAccessible(true);
            T[] $VALUES = (T[]) Array.newInstance(clazz, values.length+1);
            System.arraycopy(values, 0, $VALUES, 0, values.length); // Copy over values from old array
            $VALUES[values.length] = u; // Add out custom enum to our local array

            // Start doing magic by getting an instance of sun.reflect.UnsafeQualifiedStaticObjectFieldAccessorImpl.class
            // Class is package-local so we can't reference it by anything other than Object
            m = Field.class.getDeclaredMethod("getFieldAccessor", Object.class);
            m.setAccessible(true);
            Object UQSOFAImpl = m.invoke(f, u);

            // Get "isReadOnly" flag ($VALUES is always read-only even if Field.setAccessible(true) is called)
            // Flag is located in superclass (sun.reflect.UnsafeQualifiedStaticFieldAccessorImpl.class (also fucking package-local))
            // Set flag to 'false' to allow for modification against Java's will
            Field f1 = UQSOFAImpl.getClass().getSuperclass().getDeclaredField("isReadOnly");
            f1.setAccessible(true);
            f1.setBoolean(UQSOFAImpl, false);

            // Invoke set() method on UnsafeQualifiedStaticObjectFieldAccessorImpl object which sets the
            // private field $VALUES to our new array
            m = UQSOFAImpl.getClass().getDeclaredMethod("set", Object.class, Object.class);
            m.setAccessible(true);
            m.invoke(UQSOFAImpl, f, $VALUES);
        } catch (Exception wrongParams) { throw new RuntimeException(wrongParams); }
        return u;
    }

    /**
     * Create a new object without the hassle of having to construct it. WARNING: Not usually a good idea.
     * @param clazz Class to instantiate.
     * @return An object instance of the supplied class that hasn't been constructed.
     * @throws InstantiationException
     */
    public static <T> T create(Class<T> clazz) throws InstantiationException{
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            //noinspection unchecked
            return (T) ((Unsafe) f.get(null)).allocateInstance(clazz);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Whatever you did, it was VERY wrong! Stop it!");
            e.printStackTrace();
            return null;
        }
    }
}
