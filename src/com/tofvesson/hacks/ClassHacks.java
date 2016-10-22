package com.tofvesson.hacks;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import sun.misc.Unsafe;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

public class ClassHacks {
    public static <T> Class<T> injectField(Class<T> target, Object toInject, String name) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try{ // Get unsafe
            Field f = target.getDeclaredField(name);
            f.setAccessible(true);

            Field f1 = Unsafe.class.getDeclaredField("theUnsafe");
            f1.setAccessible(true);

            f.set(((Unsafe)f1.get(null)).allocateInstance(target), toInject); // Check if field exists
        }catch(Exception e){} // Will never happen

        // Read from class
        ClassReader cr = new ClassReader(target.getClassLoader().getResourceAsStream(target.getName().replace(".", "/")+".class"));
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS); // Prepare to write

        // Get all fully defined interface class names
        String[] itf = new String[target.getInterfaces().length];
        for(int i = 0; i<itf.length; ++i) itf[i] = target.getInterfaces()[i].getName().replace(".", "/");

        // Create class definition
        cw.visit(V1_8, ACC_PUBLIC, target.getSimpleName(), "L"+target.getName().replace(".", "/")+";",
                target.getSuperclass().getName().replace(".", "/"), itf);

        // Define blank constructor
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", "L"+target.getName().replace(".", "/")+";", null);
        mv.visitVarInsn(ALOAD, 0); // Load "this" reference
        mv.visitMethodInsn(INVOKESPECIAL, target.getSuperclass().getName().replace(".", "/"), "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
        // Define field
        cw.visitField(ACC_PUBLIC, name, "L"+toInject.getClass().getName().replace(".", "/")+";", null, toInject).visitEnd();

        // Finish modifications
        cw.visitEnd();

        try{
            Method m = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            m.setAccessible(true);
            byte[] b;
            return (Class<T>) m.invoke(target.getClassLoader(), target.getSimpleName(), b=cw.toByteArray(), 0, b.length); // Redefine class
        }catch(Exception e){ e.printStackTrace(); }
        throw new RuntimeException("Something went very wrong");
    }
}
