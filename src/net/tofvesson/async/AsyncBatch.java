package net.tofvesson.async;

import java.util.HashMap;

/**
 * Creates a batch of similar async instructions.
 */
@SuppressWarnings("unused")
public class AsyncBatch<T> {

    private final HashMap<Integer, Async<T>> all = new HashMap<Integer, Async<T>>();

    public AsyncBatch(int count, final BatchRunnable r) { for(int i = 0; i<count; ++i) add(r, i); }

    private void add(final BatchRunnable r, final int idx){ all.put(idx, new Async<T>(new Runnable() { public void run() { r.run(idx); } })); }
    public HashMap<Integer, Async<T>> getAll(){ return all; }
    public HashMap<Integer, T> awaitAll(){
        HashMap<Integer, T> al = new HashMap<Integer, T>();
        for(Integer a : all.keySet())
            al.put(a, all.get(a).await());
        return al;
    }
    public int getFinished(){ int i = 0; for(Async<T> a : all.values()) if(!a.isAlive()) ++i; return i; }
    public int size(){ return all.size(); }
    public boolean allFinished(){ return getFinished()==all.size(); }
    public void cancelAll(){ for(Async<T> a : all.values()) a.cancel(); }
}
