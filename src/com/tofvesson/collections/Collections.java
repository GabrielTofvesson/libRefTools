package com.tofvesson.collections;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unchecked")
public final class Collections {
    public static <T> ArrayList<T> flip(Collection<T> c){
        ArrayList<T> a = new ArrayList<T>();
        T[] t = (T[]) c.toArray();
        for(int i = c.size()-1; i>0; --i) a.add(t[i]);
        return a;
    }

    public static boolean arrayContains(Object[] o, PredicateCompat p){ for(Object o1 : o) if(p.apply(o1)) return true; return false; }

    public interface PredicateCompat<T>{ boolean apply(T t); }
}
