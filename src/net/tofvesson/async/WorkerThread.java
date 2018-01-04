package net.tofvesson.async;

import net.tofvesson.collections.Pair;
import net.tofvesson.reflection.SafeReflection;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A thread tasked with accepting multiple instructions. This is useful for people who don't want to constantly create new threads for heavy work.
 * Also good if the fields in an object instantiated in this thread aren't volatile.
 */
public class WorkerThread extends Thread {

    protected final List<Long> ids = new ArrayList<Long>();
    protected final Queue<Pair<Long, Pair<Method, Pair<Object, Object>>>> queue;
    protected final Map<Long, Object> output = new HashMap<Long, Object>();
    protected final AtomicBoolean alive = new AtomicBoolean(true);

    /**
     * Create a WorkerThread.
     * @param queueSize Maximum amount of instructions to be queued.
     */
    public WorkerThread(int queueSize){
        super();
        SafeReflection.setValue(this, Thread.class, "target", new Runnable()
        {
            public void run(){
                while (getAlive()) {
                    if (queue.size() != 0) {
                        final Pair<Long, Pair<Method, Pair<Object, Object>>> q;
                        synchronized (queue){
                            q = queue.poll();
                        }
                        final Pair<Method, Pair<Object, Object>> instr = q.getValue();
                        try {
                            synchronized (output) {
                                output.put(q.getKey(), instr.getKey().invoke(instr.getValue().getKey(), (Object[]) instr.getValue().getValue()));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) { }
                }
            }
        });
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
        synchronized (queue) {
            queue.add(new Pair<Long, Pair<Method, Pair<Object, Object>>>(id, new Pair<Method, Pair<Object, Object>>(m, new Pair<Object, Object>(invokeOn, params))));
        }
        ids.add(id);
        return id;
    }

    /**
     * Waits for instruction to be processed and return value to be acquired.
     * @param id UID of the supplied instruction.
     * @return Return value from instruction called in worker thread.
     */
    public Object pop(long id){
        if(!isAlive()) throw new IllegalStateException("Cannot pop value from inactive thread");
        if(!ids.contains(id)) return null;
        if(Thread.currentThread() == this) throw new RuntimeException("Attempting to await result in worker thread! This causes the thread to lock.");
        //noinspection StatementWithEmptyBody
        while(!output.containsKey(id)) // Block caller thread until result is received
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        Object o;
        synchronized (output) {
            o = output.get(id);
            output.remove(id);
        }
        ids.remove(id);
        return o;
    }

    /**
     * Waits for current method invocation to finish before stopping thread.
     */
    public void stopGraceful(){
        synchronized (alive) { alive.set(false); }
    }

    protected final boolean getAlive(){
        boolean b;
        synchronized (alive) { b = alive.get(); }
        return b;
    }

    /**
     * Interrupts thread to kill it. NOTE: This method is unsafe and could cause issues if it is performing an I/O operation.
     * Only call this if you are 100% sure that it won't break something or if are ready to deal with the consequences.
     */
    public void stopForced(){
        stopGraceful();
        //noinspection deprecation
        stop();
    }
}
