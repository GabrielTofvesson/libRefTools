package net.tofvesson.async;

import net.tofvesson.collections.Pair;
import net.tofvesson.reflection.Classes;
import net.tofvesson.reflection.SafeReflection;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;

import java.lang.reflect.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

@SuppressWarnings({"WeakerAccess", "unused", "unchecked", "SameParameterValue", "JavaReflectionMemberAccess"})
public class Async<T> implements Awaitable{

    private static final Field threadLocals;
    private static final boolean android;

    static{
        Field f = null;
        boolean $android = false;
        try{
            try{
                f = Thread.class.getDeclaredField("threadLocals");
            }catch(Exception ignored){ f = Thread.class.getDeclaredField("localValues"); $android = true; }
        }catch(Exception e){ e.printStackTrace(); }
        threadLocals = f;
        android = $android;
    }


    /**
     * Thread running background task.
     */
    Thread task;

    /**
     * Return value/ constructed object.
     */
    volatile T ret;

    /**
     * Whether or not a posted value can be overwritten.
     */
    volatile boolean ovw = true;

    /**
     * Status indicators.
     */
    volatile boolean complete = false, failed = false; // Used by anonymous class, therefore not private

    /**
     * Exception to throw in case something goes wrong.
     */
    volatile Throwable t;

    private final LinkedHashMap<String, Object> asyncLocals = new LinkedHashMap<String, Object>();

    /**
     * Create Async object for sleeping for a while.
     * @param millis Milliseconds to wait.
     * @param micros Microseconds to wait.
     */
    private Async(final long millis, final int micros){
        task = new Thread(new Runnable(){
            public void run(){
                new ThreadLocal<Async>().set(Async.this);
                try {
                    Thread.sleep(millis, micros);
                    Async.this.complete = true;
                } catch (InterruptedException t1) {
                    if(!Async.this.failed) {
                        Async.this.failed = true;
                        Async.this.t=t1;
                    }
                }
            }
        });
        task.setDaemon(true);
        task.start();
    }

    /**
     * Create Async process with runnable.
     * @param r Runnable to execute as new task.
     */
    public Async(final Runnable r){
        task = new Thread(new Runnable(){
            public void run(){
                try {
                    new ThreadLocal<Async>()
                            .set(Async.this);           // Store ThreadLocal reference to Async object
                    r.run();                            // Execute runnable
                    complete = true;                    // Notify all threads who are checking
                } catch (Throwable t1) {                // Prepare for failure
                    if(!failed) {                       // Checks if task was canceled
                        failed = true;                  // Notifies all threads that task failed
                        t=t1;                           // Makes error accessible to be thrown
                    }
                }
            }
        });                                         // Execute thread with runnable
        task.setDaemon(true);                       // Ensure that process dies with program
        task.start();                               // Start task
    }

    /**
     * Initiates an async task that invokes the defined method. If object is null, the method must be static.
     * Note that this is optimized for larger tasks id est tasks that take more than 5 milliseconds to process (preferably a minimum of 10 ms).
     * @param o Object to invoke method on.
     * @param method Method to invoke.
     * @param params Required parameters.
     */
    public Async(final Object o, final Method method, final Object... params){
        method.setAccessible(true); // Ensure that no crash occurs
        task = new Thread(new Runnable(){ // Create a new thread
            public void run(){
                try {
                    new ThreadLocal<Async>().set(Async.this);
                    T ret = (T)method.invoke(o, params);// Invoke given method
                    if(ovw) Async.this.ret = ret;       // Checks if a sticky value already has been posted
                    complete = true;                    // Notify all threads who are checking
                } catch (Throwable t1) {                // Prepare for failure
                    if(!failed) {                       // Checks if task was canceled
                        failed = true;                  // Notifies all threads that task failed
                        t=t1;                           // Makes error accessible to be thrown
                    }
                }
            }
        });
        task.setDaemon(true);                       // Ensure that process dies with program
        task.start();                               // Start task
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
        c.setAccessible(true);                  // Ensure that constructor can be called
        task = new Thread(new Runnable() {               // Creates a new thread for asynchronous execution
            public void run(){
                new ThreadLocal<Async>().set(Async.this);
                try {
                    T ret = c.newInstance(params);  // Create a new instance: invoke "<init>" method
                    if(ovw) Async.this.ret = ret;   // Checks if a sticky value already has been posted
                    complete = true;                // Notify all threads that async is finished
                } catch (Throwable t1) {            // Handle crash
                    if(!failed) {                   // Ensure that crash wasn't called by cancel()
                        failed = true;              // Notify all threads that error has been encountered
                        t=t1;                       // Make throwable accessible to be thrown in caller thread
                    }
                }
            }
        });
        task.setDaemon(true);                   // Ensure that thread dies with program
        task.start();                           // Start construction
    }

    /**
     * Dispatch an asynchronous construction of the object corresponding to the passed constructor with no parameters.
     * @param c Constructor to call.
     */
    public Async(Constructor<T> c){ this(c, (Object[]) null); }

    /**
     * Should probably only be used in debugging applications when a preset return value should be given and there is no need to start a new thread.
     * This won't even start a new thread. All it does is set it's current state to "finished" and allow for the return value to be read.
     * @param o Return value.
     */
    public Async(T o){ complete = true; ret = o; }
    /**
     * WARNING: Package-scoped because it should only be used when overriding standard construction. Should not bw used haphazardly!
     */
    Async() { task = null; }


    /**
     * Await completion of async task. Blocks thread if task isn't complete.
     * @return Return value from async method call. Return is null if {@link #cancel()} is called before this method and async task wan't finished.
     */
    public T await(){
        checkDangerousThreadedAction();
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
    public boolean isAlive(){ return task.isAlive(); } // Check if thread is still alive which directly determines if process is alive since Async is a wrapper of Thread


    /**
     * Get async instance pertaining to current thread.
     * @return Async owning current thread or null if thread isn't Async.
     */
    public static <T> Async<T> current(){
        try{
            Object holder;
            Field f = threadLocals;
            f = (holder= SafeReflection.getFieldValue(f, Thread.currentThread())).getClass().getDeclaredField("table");
            for(Object o : (Object[]) SafeReflection.getFieldValue(f, holder))
                if(o != null) {
                    if(android) holder = o;
                    else f = o.getClass().getDeclaredField("value");
                    if((android?holder:(holder=SafeReflection.getFieldValue(f, o))) instanceof Async) return (Async<T>) holder;
                }
        }catch(Exception e){}
        return null;
    }

    /**
     * Method that must be called by the async thread of any class that
     * extends this one if they want to support {@link #current()} for their class.
     */
    protected void setLocal(){ new ThreadLocal<Async>().set(this); }

    /**
     * Cancels async operation if it's still alive.
     */
    public void cancel(){
        if(task.isAlive()){
                            // Set values before interrupting to prevent InterruptedException from
                            // being propagated and thrown in the main thread
            t=null;
            failed = true;  // Creates a unique and identifiable state
                            //noinspection deprecation
            task.stop();    // Force-stop thread
        }
    }

    /**
     * Post a return value to Async object.
     * Meant to be used from inside an instance of Async as a way to prematurely provide a return value without stopping said async process.
     * Posting a value does not inform the Async object that it has completed operations.
     * @param value Value to post.
     * @param allowOverwrite If the return type of the async process should be overwritten when said process is completed. Calling postReturn overrides this.
     */
    public void postReturn(T value, boolean allowOverwrite){
        ret = value;
        ovw = allowOverwrite;
    }

    /**
     * Post a return value to Async object.
     * Meant to be used from inside an instance of Async as a way to prematurely provide a return value without stopping said async process.
     * Posting a value does not inform the Async object that it has completed operations.
     * @param value Value to post.
     */
    public void postReturn(T value){ postReturn(value, false); }

    /**
     * Get return type posted by object.
     * Meant to be used after a value has been posted, not when it has been returned.
     */
    public T getReturn(){
        return ret;
    }

    /**
     * Get a map of values local to this async object.
     * @return Map of values if called from this async task, otherwise throw exception
     */
    public Map<String, Object> getLocals(){
        if(!this.equals(Async.<T>current())) throw new IllegalCallerThreadException("Cannot get locals from another thread!");
        return asyncLocals;
    }

    /**
     * Checks for dangerous calls to Async methods such as awaiting oneself (which would cause a freeze in that thread).
     */
    protected final void checkDangerousThreadedAction(){
        if(this.equals(current())) throw new IllegalCallerThreadException("Calling dangerous method from inside thread is forbidden!");
    }

    /**
     * Safe method for delaying the current thread.
     * @param millis Milliseconds to delay.
     * @param micros Microseconds to delay.
     */
    public static void sleep(long millis, int micros){ new Async(millis, micros).await(); }

    /**
     * Safe method for delaying the current thread.
     * @param millis Milliseconds to delay.
     */
    public static void sleep(long millis){ sleep(millis, 0); }

    public static void iSleep(long millis, int micros){ try{ Thread.sleep(millis, micros); } catch(Exception ignored) {} }

    public static void iSleep(long millis){ iSleep(millis, 0); }
}
