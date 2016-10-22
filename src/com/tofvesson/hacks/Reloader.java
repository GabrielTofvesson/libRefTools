package com.tofvesson.hacks;

import sun.misc.Unsafe;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import static java.lang.reflect.Modifier.STATIC;

public class Reloader {
    private static final List<Object> reload = new ArrayList<>();
    public void reloadAll(){}
    public void reloadClasses(Class<?>... c) throws ClassNotFoundException {
        final boolean[] b = {false};
        reload.stream().filter(o->{
            for(Class<?> c1 : c) b[0] = b[0] || (b[0]= o!=null && c1.isAssignableFrom(o.getClass()));
            return b[0];
        }); // Check if class needs reloading
        if(!b[0]) return;
        Unsafe u = null;
        try {
            Field f  = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            u = (Unsafe) f.get(null);
        } catch (Exception ignored) {}
        if(u==null) throw new RuntimeException("Something went very wrong! Unsafe reference is null.");
        List<Object> reloaded = new ArrayList<>();
        reload.stream().filter(o->{
            for(Class<?> c1 : c) if(c1.isAssignableFrom(o.getClass())) return true;
            return false;
        }).forEach(reloaded::add); // Move all objects to be reloaded to a holder
        reload.removeAll(reloaded);
        for(Class<?> c1 : c) c1.getClassLoader().loadClass(c1.getName()); // Re load class in classloader
        for(Object o : reloaded){
            try{
                Class<?> reloadedClass = Class.forName(o.getClass().getName());
                Object o1 = u.allocateInstance(reloadedClass);
                for(Field f : o.getClass().getDeclaredFields()){
                    f.setAccessible(true);
                    try{
                        Field f1 = reloadedClass.getDeclaredField(f.getName());
                        f1.setAccessible(true);
                        f1.set((f1.getModifiers()&STATIC)==STATIC?null:o1, f.get((f.getModifiers()&STATIC)==STATIC?null:o));
                    }catch(Exception ignored){}
                }

            }catch(Exception ignored){}
        }
    }
}
