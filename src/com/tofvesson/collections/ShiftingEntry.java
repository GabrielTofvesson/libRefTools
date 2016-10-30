package com.tofvesson.collections;

import java.util.Map;

import static com.tofvesson.collections.ShiftingSet.empty;

public class ShiftingEntry<K, V> implements Map.Entry<K, V>{

    private final ShiftingSet<K> keys;
    private final ShiftingSet<V> values;
    int pos;

    public ShiftingEntry(ShiftingSet<K> keys, ShiftingSet<V> values, int pos){
        this.keys = keys;
        this.values = values;
        this.pos = pos;
    }

    @Override
    public K getKey() {
        return keys.entries[pos]==empty?null:(K)keys.entries[pos];
    }

    @Override
    public V getValue() {
        return values.entries[pos]==empty?null:(V)values.entries[pos];
    }

    @Override
    public V setValue(V value) {
        V v = getValue();
        if(keys.entries[pos]!=null) values.entries[pos] = value;
        return v;
    }
}
