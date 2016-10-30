package com.tofvesson.collections;

import java.util.*;

import static com.tofvesson.collections.ShiftingSet.empty;

public class ShiftingMap<K, V> implements Map<K, V> {

    protected final ShiftingSet<K> keys;
    protected final ShiftingSet<V> values;
    protected final ShiftingSet<Entry<K, V>> entries;

    public ShiftingMap(int maxSize, double load){
        keys = new ShiftingSet<>(maxSize, load);
        values =  new ShiftingSet<>(maxSize, load);
        entries = new ShiftingSet<>(maxSize, load);
    }

    public ShiftingMap(int maxSize){
        this(maxSize, 0.75);
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return keys.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return keys.contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return values.contains(value);
    }

    @Override
    public V get(Object key) {
        int i = keys.indexOf(key);
        return i!=-1?(V)values.entries[i]:null;
    }

    @Override
    public V put(K key, V value) {
        V v=null;
        int i = keys.indexOf(key);
        if(i!=-1){
            v = values.entries[i]!= empty?(V)values.entries[i]:null;
            values.entries[i] = value!=null?value:empty;
        }else{
            for(Entry e : entries) ++((ShiftingEntry)e).pos;
            entries.add(new ShiftingEntry<>(keys, values, 0));
            keys.add(key);
            values.add(value);
        }
        return v;
    }

    @Override
    public V remove(Object key) {
        V v = get(key);
        if(keys.contains(key)){
            values.entries[keys.indexOf(key)] = null;
            values.shift();
            values.adaptLoad(-1);
            keys.remove(key);
            entries.entries[entries.size()-1] = null;
            entries.adaptLoad(-1);
        }
        return v;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        keys.stream().filter(m::containsKey).forEach(k -> {
            put(k, m.get(k));
            m.remove(k);
        });
        if(m.size()<entries.maxSize) for(Entry e : entries) ((ShiftingEntry)e).pos += m.size();
        for(int i = 0; i<m.size(); ++i) entries.add(new ShiftingEntry<>(keys, values, m.size()-i));
        keys.addAll(m.keySet());
        values.addAll(m.values());
    }

    @Override
    public void clear() {
        entries.clear();
        keys.clear();
        values.clear();
    }

    @Override
    public Set<K> keySet() {
        return keys;
    }

    @Override
    public Collection<V> values() {
        return values;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return entries;
    }
}
