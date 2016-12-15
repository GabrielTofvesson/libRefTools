package com.tofvesson.async;

import java.util.HashMap;

/**
 * Creates a batch of similar async instructions.
 */
@SuppressWarnings("unused")
public class AsyncBatch {

    private final HashMap<Integer, Async> all = new HashMap<Integer, Async>();

    public AsyncBatch(int count, final BatchRunnable r) { for(int i = 0; i<count; ++i) add(r, i); }

    private void add(final BatchRunnable r, final int idx){ all.put(idx, new Async(new Runnable() { public void run() { r.run(idx); } })); }
    public HashMap<Integer, Async> getAll(){ return all; }
    public HashMap<Integer, Object> awaitAll(){
        HashMap<Integer, Object> al = new HashMap<Integer, Object>();
        for(Integer a : all.keySet())
            al.put(a, all.get(a).await());
        return al;
    }
    public int getFinished(){ int i = 0; for(Async a : all.values()) if(!a.isAlive()) ++i; return i; }
    public int size(){ return all.size(); }
    public boolean allFinished(){ return getFinished()==all.size(); }
    public void cancelAll(){ for(Async a : all.values()) a.cancel(); }
}
