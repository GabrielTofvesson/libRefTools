package com.tofvesson.async;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Async {

    Thread task;
    volatile Object ret; // Assigned using native method
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
        //Lambdas are compiled into more methods than anonymous class and don't decrease overhead
        //noinspection Convert2Lambda
        task = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) { try { ret = method.invoke(o, params); complete = true; }
                catch (Throwable t1) { if(!failed) { failed = true; t=t1; } } }
            }
        });
        task.setDaemon(true);
        task.start();
    }

    /**
     * Create a new async task for instantiating an object.
     * @param c Constructor to use when instantiating object.
     * @param params Parameters to use when instantiaing object.
     */
    public Async(final Constructor c, final Object... params){
        c.setAccessible(true);
        //Lambdas are compiled into more methods than anonymous class and don't decrease overhead
        //noinspection Convert2Lambda
        task = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) { try { ret = c.newInstance(params); } catch (Throwable t1) { if(!failed) { failed = true; t=t1; } } }
            }
        });
        task.setDaemon(true);
        task.start();
    }

    Async() {task = null;} // Only package-scoped because it should only be used when overriding standard construction

    /**
     * Await completion of async task. Blocks thread if task isn't complete.
     * @return Return value from async method call. Return is null if {@link #cancel()} is called before this method and async task wan't finished.
     */
    public Object await(){
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
            task.interrupt();
        }
    }
}
