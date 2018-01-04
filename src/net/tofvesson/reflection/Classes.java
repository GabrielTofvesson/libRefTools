package net.tofvesson.reflection;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.Random;
import java.util.Vector;

@SuppressWarnings("unchecked")
public class Classes {
    private static final Field classLoader_loadedClasses = SafeReflection.getField(ClassLoader.class, "classes");
    private static final char[] randomSet =
            {
                    '1', '2', '3', '4', '5',
                    '6', '7', '8', '9', '0',
                    '$', '_', '£', '¤', 'a',
                    'b', 'c', 'd', 'e', 'f',
                    'g', 'h', 'i', 'j', 'k',
                    'l', 'm', 'o', 'o', 'p',
                    'q', 'r', 's', 't', 'u',
                    'v', 'w', 'x', 'y', 'z',
            };


    public static String generateNewRandomClassName(ClassLoader loader, String prefix, String suffix, int minLength, int maxLength){
        final StringBuilder rand = new StringBuilder();
        final int range = maxLength-minLength;
        if(range<=0) throw new IndexOutOfBoundsException("Range must be a positive, non-zero value!");

        // Select an appropriate randomizer
        Random r;
        try{
            r = (Random) SafeReflection.invokeStaticMethod(SafeReflection.getMethod(Class.forName("java.util.concurrent.ThreadLocalRandom"), "current"));
        }catch (ClassNotFoundException e){
            r = new Random(System.currentTimeMillis());
        }
        assert r != null;

        // Generate name
        do{
            // Reset builder
            rand.setLength(0);

            // Generate a target length
            int targetLength = (r.nextInt()%range) + minLength;
            for(int i = 0; i<targetLength; ++i){
                // Select and appropriate index
                int rIdx = r.nextInt();
                rand.append(randomSet[rand.length()==0?(rIdx%(randomSet.length-10))+10:rIdx%randomSet.length]);
            }

            // Continue until an appropriate name has been found
        }while(classExists(loader, prefix+rand.toString()+suffix));

        return prefix+rand.toString()+suffix;
    }

    public static boolean classExists(ClassLoader loader, String name){
        if(
                containsClassWithSignature(
                        (Vector<Class<?>>) SafeReflection.getFieldValue(classLoader_loadedClasses, loader),
                        name.replace('/', '.')
                )) return true;
        try {
            loader.loadClass(name);
            return true;
        } catch (ClassNotFoundException e) { }
        return false;
    }

    public static boolean containsClassWithSignature(Collection<Class<?>> collection, String signature){
        for(Class<?> c : collection) if(c.getName().equals(signature)) return true;
        return false;
    }

    public static Field reference(ClassLoader loader, Object value){
        Class<?> synthetic = createClassWithStaticField(loader, generateNewRandomClassName(loader, "", "", 1, 64), "synth$value", Object.class);
        SafeReflection.setValue(synthetic, "synth$value", value);
        return SafeReflection.getField(synthetic, "synth$value");
    }

    public static Class<?> createClassWithStaticField(ClassLoader loader, String className, String fieldName, Class<?> classType){
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        writer.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, className.replace('.', '/'), null, null, null);
        writer.visitField(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, fieldName, classType.getName().replace('.', '/'), null, null).visitEnd();
        writer.visitEnd();
        byte[] bytecode = writer.toByteArray();
        return (Class<?>) SafeReflection.invokeMethod(
                loader,
                SafeReflection.getMethod(ClassLoader.class, "defineClass", String.class, byte[].class, int.class, int.class, ProtectionDomain.class),
                className.replace('.', '/'), bytecode, 0, bytecode.length, null
        );
    }
}
