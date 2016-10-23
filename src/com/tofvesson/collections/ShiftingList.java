package com.tofvesson.collections;


import java.util.*;

public class ShiftingList<K, V> implements Map<K, V> {

    final ShiftingSet<K> keys;
    final ShiftingSet<V> values;


    public ShiftingList(int maxSize){
        this(maxSize, 0.75f);
    }

    public ShiftingList(int maxSize, float load){
        keys = new ShiftingSet<>(maxSize, load);
        values = new ShiftingSet<>(maxSize, load);
    }


    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(Object key) {
        return null;
    }

    @Override
    public V remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<K> keySet() {

        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }

    @Override
    public V put(K key, V value) {
        return null;
    }


    /**
     * Entries dynamically update as underlying sets change
     * @param <K>
     * @param <V>
     */
    static class ShiftingEntry<K, V> implements Entry<K, V>{

        private ShiftingSet<K> keys;
        private ShiftingSet<V> values;
        private int index = 0;

        public ShiftingEntry(ShiftingSet<K> keys, ShiftingSet<V> values, int index){
            this.keys = keys;
            this.values = values;
            this.index = index;
        }

        @Override
        public K getKey() {
            return (K) keys.set[index];
        }

        @Override
        public V getValue() {
            return (V) values.set[index];
        }

        @Override
        public V setValue(V value) {
            V v=getValue();
            values.set[index]=value;
            return v;
        }
    }

    static class ShiftingSet<E> implements Set<E>{

        Object[] set = new Object[1];
        final int maxSize;
        final float loadFactor;
        int populatedEntries = 0;

        public ShiftingSet(int maxSize, float loadFactor){
            this.maxSize = maxSize<=0?20:maxSize;
            this.loadFactor = loadFactor<=0.1?0.75f:loadFactor;
        }

        @Override
        public int size() {
            return set.length;
        }

        @Override
        public boolean isEmpty() {
            return set.length==1 && set[0]==null;
        }

        @Override
        public boolean contains(Object o) {
            if(o==null) return false;
            for(Object o1 : set)
                if(o.equals(o1))
                    return true;
            return false;
        }

        @Override
        public Iterator<E> iterator() {
            return new ShiftingIterator<>(this);
        }

        @Override
        public Object[] toArray() {
            return set;
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return (T[]) set;
        }

        @Override
        public boolean add(E e) {
            if(contains(e)) return false;
            populatedEntries=populatedEntries<maxSize?populatedEntries+1:populatedEntries;
            Object[] o = new Object[Math.min((int)(100/(100/populatedEntries*loadFactor)), maxSize)];                   // Dynamically update array size according to loadFactor and max Size
            System.arraycopy(set, 1, o, 2, o.length - 2);
            o[0] = e;
            return true;
        }

        @Override
        public boolean remove(Object o1) {
            boolean b = false;
            for(int i = 0; i<populatedEntries; ++i) if(o1.equals(set[i])){ set[i] = null; b = true; --populatedEntries; break; }
            adaptLoad();
            shift();
            return b;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            int i = 0;
            int j = 0;
            for(Object o : c) {
                ++j;
                for (Object o1 : set)
                    if (o.equals(o1)) ++i;
            }
            return j==i;
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            ArrayList<Object> l = new ArrayList<>();
            for(Object e : c)
                for (Object o : set) {
                    if (e.equals(o)){
                        l.remove(e);
                        break;
                    }
                    l.add(e);
                }
            if(l.size()==0) return false;
            int tmp = populatedEntries;
            populatedEntries=populatedEntries+l.size()<maxSize?populatedEntries+l.size():maxSize;
            Object[] o = new Object[Math.min((int)(100/(100/populatedEntries*loadFactor)), maxSize)];                   // Create new array
            for(int i = l.size(); i>0; --i) if(i<o.length-1) o[l.size()-i] = l.get(i);                                  // Move new values to start of array relative to their position
            if(l.size()<tmp) for(int i = 0; i<tmp-l.size(); ++i) if(l.size()+i<o.length) o[l.size()] = set[i];          // Move old values to relative location
            set = o;                                                                                                    // Update reference
            return true;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            int i = 0;                                                                                                  // Tracker for how many entries were removed
            for(Object o : c)                                                                                           // Check against every element to remove
                for(int j = 0; j<populatedEntries; ++j)                                                                 // Check again all populated entries
                    if(o.equals(set[j])){
                        ++i;
                        set[j] = null;
                    }
            if(i==0) return false;
            shift();
            return true;
        }

        @Override
        public void clear() {
            populatedEntries = 0;
            set = new Object[1];
        }

        /**
         * Shifts the values to fill unpopulated array slots.
         */
        void shift(){
            for(int i = 0; i<populatedEntries; ++i) if(set[i]==null) System.arraycopy(set,i+1,set,i,populatedEntries-i);// Shift populated slots towards the start of the array
        }

        /**
         * Adapt array size according to populated elements and load factor.
         */
        void adaptLoad(){
            if(((int)(100/(100/populatedEntries*loadFactor)))==set.length) return;
            Object[] o = new Object[Math.min((int)(100/(100/populatedEntries*loadFactor)), maxSize)];
            System.arraycopy(set, 0, o, 0, o.length);
            set = o;
        }
    }

    static class ShiftingIterator<E> implements Iterator<E>{

        private final ShiftingSet<E> s;
        private int ctr = -1;

        public ShiftingIterator(ShiftingSet<E> s){
            this.s=s;
        }

        @Override
        public boolean hasNext() {
            return ctr+1<=s.set.length-1;
        }

        @Override
        public E next() {
            if(!hasNext()) return null;
            return (E) s.set[++ctr];
        }
    }
}
