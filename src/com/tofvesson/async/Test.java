package com.tofvesson.async;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

public class Test {

    public Test(){}

    public static void main(String[] args) throws Exception {





        // Create new class with an automatically calculated max variable and constant pool
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        // Start visiting class
        cw.visit(V1_8 /* Version of Java to compile with */,
                ACC_PUBLIC /* Access permissions */,
                "TClass", /* Simple class name */
                "Lcom/tofvesson/async/TClass;", /* Fully qualified class signature */
                "Ljava/lang/Object;", /* Superclass */
                null /* Interfaces */
        );

        // Start visiting constructor "TClass()"
        MethodVisitor mv = cw.visitMethod(
                ACC_PUBLIC, /* Access permissions */
                "<init>", /* Method name */
                "Lcom/tofvesson/async/TClass;", /* Method descriptor */
                "()V", /* Method signature */
                null /* Exceptions */
        );

        // Invoke super() on inherited Object
        mv.visitVarInsn(ALOAD, 0); // Load "this" from local constant pool and put on stack
        mv.visitMethodInsn(
                INVOKESPECIAL, /* How to invoke method ("Special" for invoking super()) */
                "java/lang/Object", /* Class where method is defined */
                "<init>", /* Method to invoke */
                "()V", /* Target method signature */
                false /* Is target object an object generified to an interface (interface method) */
        );

        mv.visitMethodInsn(INVOKESTATIC, "com/tofvesson/async/Test", "test", "()V", false);

        // Return
        mv.visitInsn(RETURN);

        // Stop visiting constructor
        mv.visitEnd();

        // Stop visiting class
        cw.visitEnd();


    }

    public void run(){
        System.out.println("Hello");
    }

    private static void test(){ System.out.println("Called incorrectly"); }


}
