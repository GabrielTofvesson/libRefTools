package com.tofvesson.async;

/**
 * A Runnable-esque interface that allows for running code without try-catch blocks.
 */
public abstract class IgnorantRunnable implements Runnable{
    public final void run(){ try { irun(); } catch (Throwable throwable) { throw new RuntimeException(throwable); } }
    public abstract void irun() throws Throwable;
}
