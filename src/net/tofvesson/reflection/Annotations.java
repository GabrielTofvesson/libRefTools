package net.tofvesson.reflection;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Annotations {
    public static List<Class<?>> getAllAnnotatedClasses(Class<? extends Annotation> annotation){
        @SuppressWarnings("unchecked")
        Vector<Class<?>> classes = (Vector<Class<?>>) SafeReflection.getValue(annotation.getClassLoader(), ClassLoader.class, "classes");
        ArrayList<Class<?>> a = new ArrayList<Class<?>>();
        if(classes==null) return a;
        for(Class<?> c : classes)
            if(c.isAnnotationPresent(annotation))
                a.add(c);
        return a;
    }
}
