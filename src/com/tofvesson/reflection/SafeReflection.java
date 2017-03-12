package com.tofvesson.reflection;

import sun.misc.Unsafe;
import java.lang.reflect.*;
import java.util.*;
import java.util.Collections;

/**
 * Safe tools to help simplify code when dealing with reflection.
 */
@SuppressWarnings({"unused", "SameParameterValue", "UnusedReturnValue", "ConstantConditions"})
public final class SafeReflection {


    public static final Unsafe unsafe;
    private static final Method newInstance, aConAccess;
    private static final Field modifiers;
    private static final String version;
    private static final long override;
    private static final boolean hasAConAccess;

    static{
        Unsafe u = null;
        Method m = null, m1 = null;
        Field f = null;
        long l = 0;
        String ver = "";
        boolean b = true;
        //Get package based on java version (Java 9+ use "jdk.internal.reflect" while "sun.reflect" is used by earlier versions)
        try{
            ClassLoader.getSystemClassLoader().loadClass("jdk.internal.reflect.DelegatingConstructorAccessorImpl");
        }catch(Throwable ignored){
            ver="sun.reflect"; // If class can't be found in sun.reflect; we know that user is running Java 9+
        }
        try{
            f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            u = (Unsafe) f.get(null);
            try {
                f = Field.class.getDeclaredField("modifiers");
                f.setAccessible(true);
            }catch(Exception e){
                try { f = Field.class.getDeclaredField("artField").getType().getDeclaredField("accessFlags"); }
                catch(Exception ignored){ f = Field.class.getDeclaredField("accessFlags"); }
                f.setAccessible(true);
            }
            try{
                l = u.objectFieldOffset(AccessibleObject.class.getDeclaredField("override")); // Most desktop versions of Java
            }catch(Exception e){
                l = u.objectFieldOffset(AccessibleObject.class.getDeclaredField("flag")); // God-damned Android
            }
            try {
                m1 = Constructor.class.getDeclaredMethod("acquireConstructorAccessor");
                m1.setAccessible(true);
                m = Class.forName(ver + ".DelegatingConstructorAccessorImpl").getDeclaredMethod("newInstance", Object[].class);
                u.putInt(m, l, 1);
            }catch(Exception e){
                b = false;
            }
            u.putInt(f, l, 1);
        }catch(Exception ignored){ ignored.printStackTrace(); }                                                         // Exception should never be thrown
        unsafe = u;
        newInstance = m;
        aConAccess = m1;
        override = l;
        version = ver;
        hasAConAccess = b;
        modifiers = f;
    }

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
            unsafe.putInt(c1, override, 1);
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
            @SuppressWarnings("unchecked")
            Constructor<T> c1 = (Constructor<T>) c.getDeclaredConstructors()[0];
            unsafe.putInt(c1, override, 1);
            return c1;
        }catch (Exception e){}
        return null;
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
            unsafe.putInt(m, override, 1);
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
        if(m!=null) unsafe.putInt(m, override, 1);
        try{ return m!=null?m.invoke(inst, params):null; }catch(Exception e){}
        return null;
    }

    /**
     * Finds first available class with name given by array elements. (Useful when dealing with backward-compatibility and cross-version support).
     * @param possibleNames Classes to look for (fully qualified class names).
     * @return First existing class or null if no class was found.
     */
    public static Class<?> forNames(String... possibleNames){
        for(String s : possibleNames) try{ return Class.forName(s); }catch(Exception e){}
        return null;
    }

    /**
     * Gets the first existing instance of a class with a given fully qualified name.
     * @param possibleNames Names to search through.
     * @return Position in the array referring to the first existing class.
     */
    public static int forNamesPos(String... possibleNames){
        for(int i = 0; i<possibleNames.length; ++i) try{ Class.forName(possibleNames[i]); return i; }catch(Exception e){}
        return -1;
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
            for (Method aM : m) if(aM.getName().equals(name)){ unsafe.putInt(aM, override, 1); return aM;}
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
            unsafe.putInt(f, override, 1);
            return f;
        }catch(Exception e){}
        return null;
    }

    /**
     * Gets the object stored in the field with the specified name in the class of the defined object.
     * @param from Object to find object in.
     * @param c Class to get field from (if <b>from</b> is null). This is used for static fields.
     * @param name Name of field.
     * @return Object or null if object is null or field doesn't exist.
     */
    public static Object getValue(Object from, Class<?> c, String name){
        try{ return getField(from!=null?from.getClass():c, name).get(from); }catch(Throwable ignored){}
        return null;
    }

    /**
     * Gets the object stored in the static field with the specified name in the class of the defined object.
     * @param c Class to get field from (if <b>from</b> is null). This is used for static fields.
     * @param name Name of field.
     * @return Object or null if object is null or field doesn't exist.
     */
    public static Object getValue(Class<?> c, String name){ return getValue(null, c, name); }

    /**
     * Set field to specified value.
     * <br><h1 style="text-align: center; color: red;">Please note:</h1>A JIT compiler may inline private final fields in methods which will prevent the actual value
     * from changing at runtime.<br>This means that the value stored in the field <i>will</i> be changed, but any methods referring directly
     * (not by java.lang.reflect.Field or sun.misc.Unsafe) to the field in the source will not be affected.
     * This should only happen, though, if the field isn't set during runtime i.e. in a static block or constructor. This means that only fields that are truly constant
     * like<br>"<i>public static final boolean b = false;</i>"<br>might be problematic.
     * @param inv Object whose field to set the value of. Can be null.
     * @param in Class to find field in. (If inv is null)
     * @param name Name of field to modify.
     * @param value Value to set the field to.
     * @param vol Whether or not the operation should be volatile.
     * @return True if setting value succeeded.
     */
    public static boolean setValue(Object inv, Class<?> in, String name, Object value, boolean vol){
        try{
            Field f = getField(inv==null?in:inv.getClass(), name);
            getPutMethod(f.getType(), vol).invoke(unsafe, createUnsafePutParams(inv, f, value));
            return true;
        }catch(Exception e){ e.printStackTrace(); }
        return false;
    }

    /**
     * Set field to specified value.
     * <br><h1 style="text-align: center; color: red;">Please note:</h1>A JIT compiler may inline private final fields in methods which will prevent the actual value
     * from changing at runtime.<br>This means that the value stored in the field <i>will</i> be changed, but any methods referring directly
     * (not by java.lang.reflect.Field or sun.misc.Unsafe) to the field in the source will not be affected.
     * This should only happen, though, if the field isn't set during runtime i.e. in a static block or constructor. This means that only fields that are truly constant
     * like<br>"<i>public static final boolean b = false;</i>"<br>might be problematic.
     * @param inv Object whose field to set the value of. Can be null.
     * @param in Class to find field in. (If inv is null)
     * @param name Name of field to modify.
     * @param value Value to set the field to.
     * @return True if setting value succeeded.
     */
    public static boolean setValue(Object inv, Class<?> in, String name, Object value){ return setValue(inv, in, name, value, false); }

    /**
     * Set field to specified value as a volatile operations.
     * <br><h1 style="text-align: center; color: red;">Please note:</h1>A JIT compiler may inline private final fields in methods which will prevent the actual value
     * from changing at runtime.<br>This means that the value stored in the field <i>will</i> be changed, but any methods referring directly
     * (not by java.lang.reflect.Field or sun.misc.Unsafe) to the field in the source will not be affected.
     * This should only happen, though, if the field isn't set during runtime i.e. in a static block or constructor. This means that only fields that are truly constant
     * like<br>"<i>public static final boolean b = false;</i>"<br>might be problematic.
     * @param inv Object whose field to set the value of. Can be null.
     * @param in Class to find field in. (If inv is null)
     * @param name Name of field to modify.
     * @param value Value to set the field to.
     * @return True if setting value succeeded.
     */
    public static boolean setValueVolatile(Object inv, Class<?> in, String name, Object value){ return setValue(inv, in, name, value, true); }

    /**
     * Set static field to specified value.
     * <br><h1 style="text-align: center; color: red;">Please note:</h1>A JIT compiler may inline private final fields in methods which will prevent the actual value
     * from changing at runtime.<br>This means that the value stored in the field <i>will</i> be changed, but any methods referring directly
     * (not by java.lang.reflect.Field or sun.misc.Unsafe) to the field in the source will not be affected.
     * This should only happen, though, if the field isn't set during runtime i.e. in a static block or constructor. This means that only fields that are truly constant
     * like<br>"<i>public static final boolean b = false;</i>"<br>might be problematic.
     * @param in Class to find field in.
     * @param name Name of field to modify.
     * @param value Value to set the field to.
     * @return True if setting value succeeded.
     */
    public static boolean setValue(Class<?> in, String name, Object value){ return setValue(null, in, name, value, false); }

    /**
     * Set static field to specified value as a volatile operation.
     * <br><h1 style="text-align: center; color: red;">Please note:</h1>A JIT compiler may inline private final fields in methods which will prevent the actual value
     * from changing at runtime.<br>This means that the value stored in the field <i>will</i> be changed, but any methods referring directly
     * (not by java.lang.reflect.Field or sun.misc.Unsafe) to the field in the source will not be affected.
     * This should only happen, though, if the field isn't set during runtime i.e. in a static block or constructor. This means that only fields that are truly constant
     * like<br>"<i>public static final boolean b = false;</i>"<br>might be problematic.
     * @param in Class to find field in.
     * @param name Name of field to modify.
     * @param value Value to set the field to.
     * @return True if setting value succeeded.
     */
    public static boolean setValueVolatile(Class<?> in, String name, Object value){ return setValue(null, in, name, value, true); }

    /**
     * Gets a reference to the enclosing class from a defined inner (nested) object.
     * @param nested Object instance of a nested class.
     * @return "this" reference to the outer class or null if class of object instance is static or isn't nested.
     */
    public static Object getEnclosingClassObjectRef(Object nested){
        try{
            Field f = nested.getClass().getDeclaredField("this$0");
            unsafe.putInt(f, override, 1);
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
            if(def==null) def = new EnumDefinition();
            // Get a reference to the static method values() and get values
            Method v = clazz.getDeclaredMethod("values");
            unsafe.putInt(v, override, 1);
            T[] values = (T[]) v.invoke(null);

            // Return object if it already exists
            for(T u2 : values) if(u2.name().equals(name)) return u2;

            // Generate enum parameter definition
            Class[] paramList = new Class[def.params.size()+2];
            paramList[0] = String.class; // Name
            paramList[1] = int.class; // Ordinal
            int iterator = paramList.length;
            for(Class c : def.params.values()) paramList[--iterator] = c; // The stuff is reversed

            // Get enum constructor (inherited from Enum.class)
            Constructor<T> c = clazz.getDeclaredConstructor(paramList);
            if(hasAConAccess) unsafe.putInt(c, override, 1);
            else c.setAccessible(true);

            Object[] parameters = new Object[def.params.size()+2];
            parameters[0] = name;
            parameters[1] = values.length;
            iterator = parameters.length;
            for(Object o : def.params.keySet()) parameters[--iterator] = o;

            // Create new instance of enum with valid name and ordinal
            if(hasAConAccess) u = (T) newInstance.invoke(aConAccess.invoke(c), (Object) parameters);
            else u = c.newInstance(parameters);

            // Get the final name field from Enum.class and make it temporarily modifiable
            Field f = Enum.class.getDeclaredField("name");
            f.setAccessible(true);

            // Rename the newly created enum to the requested name
            f.set(u, name);

            if(!addToValuesArray) return u; // Stops here if

            // Get the current values field from Enum (a female dog to modify)
            f = clazz.getDeclaredField("$VALUES");
            f.setAccessible(true);
            T[] $VALUES = (T[]) Array.newInstance(clazz, values.length+1);
            System.arraycopy(values, 0, $VALUES, 0, values.length); // Copy over values from old array
            $VALUES[values.length] = u; // Add out custom enum to our local array

            unsafe.putObject(clazz, unsafe.staticFieldOffset(f), $VALUES);
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
    @SuppressWarnings("UnusedReturnValue")
    public static final class EnumDefinition {
        HashMap<Object, Class> params = new HashMap<Object, Class>(); // Assign a specific type to each parameter

        public EnumDefinition(){}
        public EnumDefinition(Object... params){ for(Object o : params) putObject(o); }

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
        public EnumDefinition putInt(boolean b){
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

    /**
     * Gets the class that called the method where this is called from.
     * @return Class from which your method was called.
     */
    public static Class<?> getCallerClass(){
        ArrayList<StackTraceElement> s = getStacktraceWithoutReflection();
        try { return Class.forName(s.get(s.size()==3?2:s.size()==2?1:0).getClassName()); } catch (ClassNotFoundException e) { }
        assert false:"Unreachable code reached";
        return null;
    }

    public static Method getCallerMethod(){
        ArrayList<StackTraceElement> s = getStacktraceWithoutReflection();
        try{
            Method m = SafeReflection.class.getDeclaredMethod("getCallerMethod");
            
            return null;
        }catch(Exception e){}
        return null;
    }

    /**
     * Get a stacktrace without listing reflection methods.
     * @return Ordered list of stacktrace without method reflections steps.
     */
    public static ArrayList<StackTraceElement> getStacktraceWithoutReflection(){
        ArrayList<StackTraceElement> s = new ArrayList<StackTraceElement>();
        StackTraceElement[] s1 = Thread.currentThread().getStackTrace();
        Collections.addAll(s, s1);
        s.remove(0);
        Iterator<StackTraceElement> i = s.iterator();
        String s2;
        while(i.hasNext()){
            if((s2=i.next().toString()).contains("java.lang.reflect.Method.invoke")
                    || s2.contains(version+".NativeMethodAccessorImpl.invoke")
                    || s2.contains(version+".DelegatingMethodAccessorImpl.invoke"))
                i.remove();
        }
        return s;
    }

    public static Method getSetMethod(Class<?> c){
        try {
            Method m = Field.class.getDeclaredMethod(getSetMethodName(c), Object.class, isAutoBoxedClass(c)?unbox(c):c.isPrimitive()?c:Object.class);
            m.setAccessible(true);
            return m;
        } catch (Exception e) { e.printStackTrace(); }
        return null; // Not object version of primitive
    }

    public static String getSetMethodName(Class<?> c){
        try {
            String s;
            boolean b;
            Field f = null;
            try{
                f = c.getDeclaredField("value");
            }catch(Exception e){}
            if(f!=null) f.setAccessible(true);
            return"set"+
                    (f!=null && Number.class.isAssignableFrom(f.getType()) && f.getType().isPrimitive()?
                            (s=f.getType().getSimpleName()).replaceFirst(s.charAt(0)+"", Character.toUpperCase(s.charAt(0))+""):
                            c.isPrimitive()?(s=c.getSimpleName()).replaceFirst(s.charAt(0)+"", Character.toUpperCase(s.charAt(0))+"")
                                    :"");
        } catch (Exception e) { e.printStackTrace(); }
        return null; // Not object version of primitive
    }

    public static boolean isAutoBoxedClass(final Class<?> c){
        return Number.class.isAssignableFrom(c) && com.tofvesson.collections.Collections.arrayContains(c.getDeclaredFields(),
                new com.tofvesson.collections.Collections.PredicateCompat() {
                    public boolean apply(Object o) {
                        return ((Field) o).getName().equals("value") && box(((Field) o).getType())==c;
                    }
                });
    }

    public static Method getPutMethod(Class<?> c, boolean vol){
        try{
            Method m = Unsafe.class.getDeclaredMethod(
                    getSetMethodName(c).replaceFirst("set", "put") + (isAutoBoxedClass(c)||c.isPrimitive()?"":"Object")+(vol?"Volatile":""),
                    Object.class,
                    long.class,
                    c.isPrimitive()?c:Object.class
            );
            m.setAccessible(true);
            return m;
        }catch(Exception ignored){ ignored.printStackTrace(); }
        return null; // Something went wrong...
    }

    public static Object[] createUnsafePutParams(Object invokee, Field field, Object value){
        return new Object[]{ invokee==null?field.getDeclaringClass():invokee, invokee==null?unsafe.staticFieldOffset(field):unsafe.objectFieldOffset(field), value };
    }

    public static Class<?> box(Class<?> c){
        if(!c.isPrimitive()) return c;
        if(c==int.class) return Integer.class;
        if(c==char.class) return Character.class;
        if(c==byte.class) return Byte.class;
        if(c==float.class) return Float.class;
        if(c==long.class) return Long.class;
        if(c==boolean.class) return Boolean.class;
        return Double.class;
    }

    public static Class<?> unbox(Class<?> c){
        if(c.isPrimitive()) return c;
        if(c==Integer.class) return int.class;
        if(c==Character.class) return char.class;
        if(c==Byte.class) return byte.class;
        if(c==Float.class) return float.class;
        if(c==Long.class) return long.class;
        if(c==Boolean.class) return boolean.class;
        return double.class;
    }
}
