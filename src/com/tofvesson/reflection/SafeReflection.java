package com.tofvesson.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SafeReflection {
    public static Method getMethod(Class<?> c, String name, Class<?>... params){
        try{
            Method m = c.getDeclaredMethod(name, params);
            m.setAccessible(true);
            return m;
        }catch(Exception e){}
        return null;
    }
    public static Field getField(Class<?> c, String name){
        try{
            Field f = c.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        }catch(Exception e){}
        return null;
    }
    public static Object getEnclosingClassObjectRef(Object nested){
        try{
            Field f = nested.getClass().getDeclaredField("this$0");
            f.setAccessible(true);
            return f.get(nested);
        }catch(Exception e){}
        return null;
    }
    public static boolean isNestedClass(Class<?> c){
        return c.getEnclosingClass()!=null;
    }
}
