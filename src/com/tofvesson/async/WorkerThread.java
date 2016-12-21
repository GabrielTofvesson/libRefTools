package com.tofvesson.async;

import com.tofvesson.collections.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * A thread tasked with accepting multiple instructions. This is useful for people who don't want to constantly create new threads for heavy work.
 * Also good if the fields in an object instantiated in this thread aren't volatile.
 */
public class WorkerThread extends Thread {

    List<Long> ids = new ArrayList<Long>();
    volatile Queue<Pair<Long, Pair<Method, Pair<Object, Object>>>> queue;
    volatile Map<Long, Object> output = new HashMap<Long, Object>();
    volatile boolean alive=true, completed=false;

    /**
     * Create a WorkerThread.
     * @param queueSize Maximum amount of instructions to be queued.
     */
    public WorkerThread(int queueSize){
        super();
        try{
            Field f = Thread.class.getDeclaredField("target");
            f.setAccessible(true);
            f.set(this, new Runnable()
            {
                public void run(){
                    synchronized (Thread.currentThread()) {
                        while (alive) {
                            if (queue.size() != 0) {
                                Pair<Long, Pair<Method, Pair<Object, Object>>> q = queue.poll();
                                Pair<Method, Pair<Object, Object>> instr = q.getValue();
                                try {
                                    output.put(q.getKey(), instr.getKey().invoke(instr.getValue().getKey(), (Object[]) instr.getValue().getValue()));
                                    completed = true;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            });
        }catch(Exception e){}
        queue = new ArrayBlockingQueue<Pair<Long, Pair<Method, Pair<Object, Object>>>>(queueSize);
    }

    /**
     * Add a new instruction for the worker thread.
     * @param invokeOn Object to invoke method on.
     * @param m Method to invoke.
     * @param params Parameters for method.
     * @return A UID corresponding to the queued instruction.
     */
    public long push(Object invokeOn, Method m, Object... params){
        m.setAccessible(true);
        long id;
        Random r = new Random();
        do{ id = r.nextLong(); }while(ids.contains(id));
        queue.add(new Pair<Long, Pair<Method, Pair<Object, Object>>>(id, new Pair<Method, Pair<Object, Object>>(m, new Pair<Object, Object>(invokeOn, params))));
        ids.add(id);
        return id;
    }

    /**
     * Waits for instruction to be processed and return value to be acquired.
     * @param id UID of the supplied instruction.
     * @return Return value from instruction called in worker thread.
     */
    public Object pop(long id){
        if(!ids.contains(id)) return null;
        if(Thread.currentThread() == this) throw new RuntimeException("Attempting to await result in worker thread! This causes the thread to lock.");
        //noinspection StatementWithEmptyBody
        while(!output.containsKey(id)) ; // Block caller thread until result is received
        Object o = output.get(id);
        output.remove(id);
        ids.remove(id);
        return o;
    }

    /**
     * Waits for current method invocation to finish before stopping thread.
     */
    public void stopGraceful(){
        alive = false;
    }

    /**
     * Interrupts thread to kill it. NOTE: This method is unsafe and could cause issues if it is performing an I/O operation.
     * Only call this if you are 100% sure that it won't break something or if are ready to deal with the consequences.
     */
    public void stopForced(){
        alive = false;
        //noinspection deprecation
        stop();
    }
}
