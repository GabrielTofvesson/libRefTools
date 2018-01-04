package net.tofvesson.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Enums {
    public static <T extends Enum<T>> boolean isFlagSet(int flags, T flag){
        return (flags&flag.ordinal())!=0;
    }
    public static void asFlags(){
        Class<?> c = SafeReflection.getCallerClass();
        if(c==null || !c.isEnum()) return;
        //noinspection unchecked
        asFlags((Class<? extends Enum<?>>)c);
    }
    public static void asFlags(Class<? extends Enum<?>> type){
        int increment = -1;
        for(Field f : type.getDeclaredFields())
            if(Modifier.isStatic(f.getModifiers()) && f.getType().equals(type))
                try {
                    SafeReflection.setValue(f.get(null), Enum.class, "ordinal", (int) Math.pow(2, ++increment));
                }catch(Exception e){ e.printStackTrace(); }
    }
    public static void asOrdinals(Class<? extends Enum<?>> type){
        int increment = -1;
        for(Field f : type.getDeclaredFields())
            if(Modifier.isStatic(f.getModifiers()) && f.getType().equals(type))
                try {
                    SafeReflection.setValue(f.get(null), Enum.class, "ordinal", ++increment);
                }catch(Exception e){ e.printStackTrace(); }
    }
}
