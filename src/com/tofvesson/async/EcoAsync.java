package com.tofvesson.async;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EcoAsync extends Async {

    private static final Field threadTarget, runnableObjToCall, runnableMethod, runnableParams;
    private Thread previousThread;

    static{
        Field f4 = null, f3 = null, f2 = null, f1 = null;
        try{
            f1 = Thread.class.getDeclaredField("target");
            f1.setAccessible(true);
            f2 = EcoAsync$1.class.getDeclaredField("val$o");
            f2.setAccessible(true);
            f3 = EcoAsync$1.class.getDeclaredField("val$method");
            f3.setAccessible(true);
            f4 = EcoAsync$1.class.getDeclaredField("val$params");
            f4.setAccessible(true);
        }catch(Exception ignored){}
        threadTarget = f1;
        runnableObjToCall = f2;
        runnableMethod = f3;
        runnableParams = f4;
    }

    /**
     * Initiates an economic version of async task that invokes the defined method. If object is null, the method must be static.
     * Note that though this is optimized for larger tasks id est tasks that take more than 5 milliseconds to process, this class was designed with re-usability in mind and, as such, doesn't have to be re-instantiated
     * to call the same method asynchronously.
     * All-in-all, this means that this class is meant to be instantiated once but invoked multiple times.
     * This memory efficiency has the minor draw-back of executing a couple more (2-3) lines of code to allow for re-usability.
     *
     * @param o      Object to invoke method on.
     * @param method Method to invoke.
     * @param params Required parameters.
     * @param runOnConstruction If true, object will run async task after construction.
     */
    public EcoAsync(boolean runOnConstruction, Object o, Method method, Object... params) {
        super();
        method.setAccessible(true);
        // Don't call super since we are constructing a custom version
        newThread(o, method, params);
        if(runOnConstruction) try{ task.start(); }catch(Exception ignored){}
    }

    /**
     * Initiates an economic version of async task that constructs an object using the supplied constructor.
     * Note that though this is optimized for larger tasks id est tasks that take more than 5 milliseconds to process, this class was designed with
     * re-usability in mind and, as such, doesn't have to be re-instantiated to call the same constructor asynchronously.
     * All-in-all, this means that this class is meant to be instantiated once but invoked multiple times.
     * This memory efficiency has the minor draw-back of executing a couple more (2-3) lines of code to allow for re-usability.
     *
     * @param runOnConstruction Whether or not to construct object during construction time.
     * @param c Constructor to use when constructing.
     * @param params Parameter to use when constructing.
     */
    public EcoAsync(boolean runOnConstruction, Constructor c, Object... params){
        super();
        c.setAccessible(true);
        try {
            Method m = EcoAsync.class.getMethod("constructObject", Constructor.class, Object[].class);
            newThread(null, m, c, params);
        } catch (NoSuchMethodException e) { }
        if(runOnConstruction) try{ task.start(); }catch(Exception ignored){}
    }

    @Override
    public Object await() {
        //noinspection StatementWithEmptyBody
        while(!failed && !complete);
        if(ret==null && t!=null){
            Throwable t = super.t; // Added for re-usability
            super.t = null; // Added for re-usability
            throw new RuntimeException(t);
        }
        return ret;
    }

    @Override
    public boolean isAlive() {
        return !super.failed && !complete && previousThread!=null; // Due to the overridden operation, we need another way of checking if worker is alive.
    }

    @Override
    public void cancel() {
        previousThread = task;// Store a reference to the previous thread
        super.cancel();
        while(previousThread.isAlive()) ;
        complete = false;
        failed = false;
        ret = null;
    }

    /**
     * Used to start/restart execution of supplied method. Cancels previous operation it it's still alive.
     */
    public void start(){
        if(isAlive()) cancel();
        // Dig parameters out from memory rather than wasting space with our own copy
        try {
            EcoAsync$1 t_run = (EcoAsync$1) threadTarget.get(task);
            newThread(runnableObjToCall.get(t_run),
                    (Method) runnableMethod.get(t_run),
                    (Object[]) runnableParams.get(t_run));
            task.start();
        } catch (IllegalAccessException ignored) { }
    }

    void newThread(Object o, Method method, Object... params){
        previousThread = null;
        try {
            task = new Thread(new EcoAsync$1(this, o, method, params), "Worker_"+method.getDeclaringClass().getName()+"_"+method.getName());
            task.setDaemon(true);
        } catch (Exception ignored) { }
    }

    private static Object constructObject(Constructor c, Object[] params)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return c.newInstance(params);
    }
}
