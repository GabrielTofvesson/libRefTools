package com.tofvesson.collections;

public class Pair<K, V> {

    private final K k;
    private V v;

    public Pair(K k, V v){
        this.k = k;
        this.v = v;
    }

    public K getKey(){ return k; }
    public V getValue(){ return v; }
    public void setValue(V v){ this.v = v; }
}
