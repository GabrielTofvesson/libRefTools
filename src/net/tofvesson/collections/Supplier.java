package net.tofvesson.collections;

/**
 * Compat version of Java 8 java.util.function.Supplier
 * @param <T>
 */
public interface Supplier<T> {
    T get();
}
