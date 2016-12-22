package com.tofvesson.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("ALL")
/**
 * Map that shifts (and deletes overflowing values) as new values are added.
 * Does not support null keys.
 */
public class ShiftingMap<K, V> implements Map<K, V> {

    private final ShiftingList<Pair<K, V>> entries;

    public ShiftingMap(int maxSize, float load){ entries = new ShiftingList<Pair<K, V>>(maxSize, load); }
    public ShiftingMap(int maxSize){ this(maxSize, 0.75f); }

    public int size() { return entries.pop; }

    public boolean isEmpty() { return entries.pop==0; }

    public boolean containsKey(Object key) {

        return false;
    }

    public boolean containsValue(Object value) {
        return false;
    }

    public V get(Object key) {
        return null;
    }

    public V put(K key, V value) {
        return null;
    }

    public V remove(Object key) {
        return null;
    }

    public void putAll(Map<? extends K, ? extends V> m) {

    }

    public void clear() {

    }

    public Set<K> keySet() {
        return null;
    }

    public Collection<V> values() {
        return null;
    }

    public Set<Entry<K, V>> entrySet() {
        return null;
    }
}
