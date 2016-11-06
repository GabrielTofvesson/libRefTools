package com.tofvesson.async;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@SuppressWarnings({"WeakerAccess", "unused", "unchecked"})
public class Async<T> {

    Thread task;
    volatile T ret; // Assigned using native method
    volatile boolean complete = false, failed = false; // Used by anonymous class, therefore not private
    volatile Throwable t;

    /**
     * Initiates an async task that invokes the defined method. If object is null, the method must be static.
     * Note that this is optimized for larger tasks id est tasks that take more than 5 milliseconds to process (preferably a minimum of 10 ms).
     * @param o Object to invoke method on.
     * @param method Method to invoke.
     * @param params Required parameters.
     */
    public Async(final Object o, final Method method, final Object... params){
        method.setAccessible(true);
        task = new Thread(()-> {
            try { ret = (T) method.invoke(o, params); complete = true; } catch (Throwable t1) { if(!failed) { failed = true; t=t1; } }
        });
        task.setDaemon(true);
        task.start();
    }

    /**
     * Dispatches an asynchronous call to supplied method on specified object with no parameters.
     * @param o Object to call method on.
     * @param m Method to call. Must be parameter-less!
     */
    public Async(Object o, Method m){ this(o, m, (Object[]) null); }

    /**
     * Dispatches an asynchronous call to supplied static method.
     * @param m Static method to call.
     * @param params Parameters to supply to method.
     */
    public Async(Method m, Object... params){ this(null, m, params); }

    /**
     * Dispatches an asynchronous, static, parameter-less method call.
     * @param m Method to call.
     */
    public Async(Method m){ this(null, m, (Object[]) null); }

    /**
     * Create a new async task for instantiating an object.
     * @param c Constructor to use when instantiating object.
     * @param params Parameters to use when instantiaing object.
     */
    public Async(final Constructor<T> c, final Object... params){
        c.setAccessible(true);
        task = new Thread(() -> {
            try { ret = c.newInstance(params); complete = true; } catch (Throwable t1) { if(!failed) { failed = true; t=t1; } }
        });
        task.setDaemon(true);
        task.start();
    }

    /**
     * Dispatch an asynchronous construction of the object corresponding to the passed constructor with no parameters.
     * @param c Constructor to call.
     */
    public Async(Constructor<T> c){ this(c, (Object[]) null); }

    Async() {task = null;} // Only package-scoped because it should only be used when overriding standard construction

    /**
     * Await completion of async task. Blocks thread if task isn't complete.
     * @return Return value from async method call. Return is null if {@link #cancel()} is called before this method and async task wan't finished.
     */
    public T await(){
        //noinspection StatementWithEmptyBody
        while(!failed && !complete); // Check using variables rather than checking if worker thread is alive since method calls are more computationally expensive
        if(ret==null && t!=null) throw new RuntimeException(t); // Detect a unique error state, get error and throw in caller thread
        //noinspection unchecked
        return ret; // Don't bother resetting values since this object is only intended to be recycled after value is gotten
    }

    /**
     * Checks if async task is still running.
     * @return True if it's still running.
     */
    public boolean isAlive(){ return task.isAlive(); }

    /**
     * Cancels async operation if it's still alive.
     */
    public void cancel(){
        if(task.isAlive()){
            // Set values before interrupting to prevent InterruptedException from
            // being propagated and thrown in the main thread
            t=null;
            failed = true; // Creates a unique and identifiable state
            task.stop();
        }
    }
}
