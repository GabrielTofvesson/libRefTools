package com.tofvesson.async;

public interface Awaitable<T> {
    T await();
    boolean isAlive();
}
