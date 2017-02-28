package com.tofvesson.async;

/**
 * A Runnable-esque interface that allows for running code without try-catch blocks.
 */
public abstract class IgnorantRunnable implements Runnable{
    public void run(){ try { irun(); } catch (Throwable throwable) { throw new RuntimeException(throwable); } }
    abstract void irun() throws Throwable;
}
