package com.tofvesson.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
}
