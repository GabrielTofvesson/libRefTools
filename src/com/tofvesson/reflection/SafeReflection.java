package com.tofvesson.reflection;

import java.lang.reflect.Constructor;
import sun.misc.Unsafe;
import sun.reflect.ConstructorAccessor;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Safe tools to help simplify code when dealing with reflection.
 */
@SuppressWarnings("unused")
public class SafeReflection {

    /**
     * Gets the constructor from the defined class with the specified parameters.
     * @param c Class to get constructor from.
     * @param params Definition of parameters that the constructor requires.
     * @param <T> Return-type of constructor.
     * @return Constructor with specified parameter requirements or null if constructor doesn't exist.
     */
    public static <T> Constructor<T> getConstructor(Class<T> c, Class<?>... params){
        try{
            Constructor<T> c1 = c.getConstructor(params);
            c1.setAccessible(true);
            return c1;
        }catch(Exception e){}
        return null;
    }

    /**
     * Gets the first constructor available from the given class.
     * @param c Class to get constructor from.
     * @param <T> Return-type of constructor.
     * @return Constructor or null if something goes horribly wrong.
     */
    public static <T> Constructor<T> getFirstConstructor(Class<T> c){
        try {
            Constructor<T> c1 = (Constructor<T>) c.getDeclaredConstructors()[0];
            c1.setAccessible(true);
            return c1;
        }catch (Exception e){}
        return null;
    }


    private static final Unsafe unsafe;

    static{
        Unsafe u = null;
        try{
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            u = (Unsafe) f.get(null);
        }catch(Exception ignored){}                                                                                     // Exception is never thrown
        unsafe = u;
    }

    /**
     * Gets the method from the defined class by name and parameters.
     * Method is accessible.
     * @param c Class to find method in.
     * @param name Name of method.
     * @param params Parameters of method.
     * @return Method or null if specified method wasn't found.
     */
    public static Method getMethod(Class<?> c, String name, Class<?>... params){
        try{
            Method m = c.getDeclaredMethod(name, params);
            m.setAccessible(true);
            return m;
        }catch(Exception e){}
        return null;
    }

    /**
     * Attempts to invoke a supplied static method with the given parameters.
     * @param m Method to invoke.
     * @param params Parameters to supply to method.
     * @return Return value of method or null if method is null.
     */
    public static Object invokeStaticMethod(Method m, Object... params){
        try{ return invokeMethod(null, m, params); }catch(Exception e){}
        return null;
    }

    /**
     * Attempts to invoke a supplied method with the given parameters on the supplied object.
     * @param inst Object to invoke method in.
     * @param m Method to invoke.
     * @param params Parameters to supply to method.
     * @return Return value of method or null if method is null.
     */
    public static Object invokeMethod(Object inst, Method m, Object... params){
        if(m!=null) m.setAccessible(true);
        try{ return m!=null?m.invoke(inst, params):null; }catch(Exception e){}
        return null;
    }

    /**
     * Finds the first method available with the specified name from the class.
     * Meant to be used in cases where a class only has one version of a method.
     * Method is accessible.
     * @param c Class to find method in.
     * @param name Name of method.
     * @return Method or null if no method with given name exists.
     */
    public static Method getFirstMethod(Class<?> c, String name){
        try{
            Method[] m = c.getDeclaredMethods();
            for (Method aM : m) if(aM.getName().equals(name)){ aM.setAccessible(true); return aM;}
        }catch(Exception e){}
        return null;
    }

    /**
     * Gets field object referring to field with specified name in defined class.
     * @param c Class to find field in.
     * @param name Name of field.
     * @return Field or null if no field with specified name exists.
     */
    public static Field getField(Class<?> c, String name){
        try{
            Field f = c.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        }catch(Exception e){}
        return null;
    }

    /**
     * Gets the static object stored in the field with the specified name in the defined class.
     * @param c Class to find object in.
     * @param name Name of field.
     * @return Object or null if object is null or field doesn't exist.
     */
    public static Object getStaticFieldObject(Class<?> c, String name){
        Field f = getField(c, name);
        try { return f!=null?f.get(null):null; } catch (Exception e) { }
        return null;
    }

    /**
     * Gets the object stored in the field with the specified name in the class of the defined object.
     * @param o Object to find object in.
     * @param name Name of field.
     * @return Object or null if object is null or field doesn't exist.
     */
    public static Object getFieldObject(Object o, String name){
        Field f = getField(o.getClass(), name);
        try{ return f!=null?f.get(o):null; }catch(Exception e){}
        return null;
    }

    /**
     * Gets a reference to the enclosing class from a defined inner (nested) object.
     * @param nested Object instance of a nested class.
     * @return "this" reference to the outer class or null if class of object instance is static or isn't nested.
     */
    public static Object getEnclosingClassObjectRef(Object nested){
        try{
            Field f = nested.getClass().getDeclaredField("this$0");
            f.setAccessible(true);
            return f.get(nested);
        }catch(Exception e){}
        return null;
    }

    /**
     * Checks whether a given class is an inner (nested) class.
     * @param c Class to check.
     * @return True if class is nested otherwise false.
     */
    public static boolean isNestedClass(Class<?> c){ return c.getEnclosingClass()!=null; }

    /**
     * Get class file based on class name.
     * @param c Class to find file of.
     * @return File if class file exists.
     */
    public static java.io.File getClassFile(Class c){ return new java.io.File(c.getResource(c.getSimpleName()+".class").getFile()); }

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
     * @throws InstantiationException Thrown if instantiating the object fails for some reason.
     */
    public static <T> T createNewObject(Class<T> clazz) throws InstantiationException{
        //noinspection unchecked
        return (T) unsafe.allocateInstance(clazz);
    }


    /**
     * A definition for custom enum creation.
     */
    public static class EnumDefinition {
        HashMap<Object, Class> params = new HashMap<>(); // Assign a specific type to each parameter

        /**
         * Put an object in the parameter list.
         * @param value The parameter to supply. (Type is derived automatically)
         * @return A reference to the EnumDefinition object this method was called on (for chaining).
         */
        public EnumDefinition putObject(Object value){
            params.put(value, value.getClass());
            return this;
        }

        /**
         * Put a primitive value in the parameter list.
         * @param autoBoxed An autoboxed version of the parameter. (For example putPrimitive(5) will automatically become Integer)
         * @return A reference to the EnumDefinition object this method was called on (for chaining).
         * @throws NotAutoBoxedException Thrown if a value expected to be autoboxed isn't autoboxed.
         */
        public EnumDefinition putPrimitive(Object autoBoxed) throws NotAutoBoxedException{
            // All autoboxed versions of primitives have a reference to their boxed primitive
            try {
                params.put(autoBoxed, autoBoxed.getClass().getDeclaredField("value").getType());
            }catch(Exception e){ throw new NotAutoBoxedException(); }
            return this;
        }

        /**
         * Safe wrapper of {@link #putPrimitive(Object)} for boolean.
         * @param b Boolean parameter.
         * @return A reference to the EnumDefinition object this method was called on (for chaining).
         */
        public EnumDefinition putBoolean(boolean b){
            try { return putPrimitive(b);  } catch (NotAutoBoxedException e) { }
            return this;
        }

        /**
         * Safe wrapper of {@link #putPrimitive(Object)} for byte.
         * @param b Byte parameter.
         * @return A reference to the EnumDefinition object this method was called on (for chaining).
         */
        public EnumDefinition putByte(byte b){
            try { return putPrimitive(b); } catch (NotAutoBoxedException e) { }
            return this;
        }

        /**
         * Safe wrapper of {@link #putPrimitive(Object)} for char.
         * @param c Character parameter.
         * @return A reference to the EnumDefinition object this method was called on (for chaining).
         */
        public EnumDefinition putChar(char c){
            try { return putPrimitive(c); } catch (NotAutoBoxedException e) { }
            return this;
        }

        /**
         * Safe wrapper of {@link #putPrimitive(Object)} for short.
         * @param s Short parameter.
         * @return A reference to the EnumDefinition object this method was called on (for chaining).
         */
        public EnumDefinition putShort(short s){
            try { return putPrimitive(s); } catch (NotAutoBoxedException e) { }
            return this;
        }

        /**
         * Safe wrapper of {@link #putPrimitive(Object)} for int.
         * @param i Integer parameter.
         * @return A reference to the EnumDefinition object this method was called on (for chaining).
         */
        public EnumDefinition putInt(int i){
            try { return putPrimitive(i); } catch (NotAutoBoxedException e) { }
            return this;
        }

        /**
         * Safe wrapper of {@link #putPrimitive(Object)} for long.
         * @param l Long parameter.
         * @return A reference to the EnumDefinition object this method was called on (for chaining).
         */
        public EnumDefinition putLong(long l){
            try { return putPrimitive(l); } catch (NotAutoBoxedException e) { }
            return this;
        }

        /**
         * Safe wrapper of {@link #putPrimitive(Object)} for float.
         * @param f Floating point parameter.
         * @return A reference to the EnumDefinition object this method was called on (for chaining).
         */
        public EnumDefinition putFloat(float f){
            try { return putPrimitive(f); } catch (NotAutoBoxedException e) { }
            return this;
        }

        /**
         * Safe wrapper of {@link #putPrimitive(Object)} for double.
         * @param d Double parameter.
         * @return A reference to the EnumDefinition object this method was called on (for chaining).
         */
        public EnumDefinition putDouble(double d){
            try { return putPrimitive(d); } catch (NotAutoBoxedException e) { }
            return this;
        }
    }

}
