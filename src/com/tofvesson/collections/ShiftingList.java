package com.tofvesson.collections;


import java.util.*;
import java.util.function.Predicate;

public class ShiftingList<K, V> implements Map<K, V> {

    final ShiftingSet<K> keys;
    final ShiftingSet<V> values;
    final UnchangingSet<Entry<K, V>> entrySet;


    public ShiftingList(int maxSize){
        this(maxSize, 0.75f);
    }

    public ShiftingList(int maxSize, float load){
        keys = new ShiftingSet<>(maxSize, load);
        values = new ShiftingSet<>(maxSize, load);
        entrySet = new UnchangingSet<>(maxSize, load);
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
        return i!=-1?(V) values.set[i]:null;
    }

    @Override
    public V remove(Object key) {
        int i = keys.indexOf(key);
        if(i==-1) return null;
        V v;
        keys.remove(key);
        values.remove(v=(V)values.set[i]);
        return v;
    }

    @SuppressWarnings({"unchecked", "MismatchedQueryAndUpdateOfCollection"})
    @Override
    public void putAll(Map m) {
        ArrayList l = new ArrayList();
        ArrayList l1 = new ArrayList();
        m.keySet().stream().filter(this::isKey).forEach(l::add);
        m.values().stream().filter(this::isValue).forEach(l1::add);
        K[] k;
        V[] v;
        if(l.size()!=l1.size()){
            if(l.size()<l1.size()){
                v = (V[]) Arrays.copyOf(l1.toArray(), l.size());
                k = (K[]) l.toArray();
            }
            else{
                k = (K[]) Arrays.copyOf(l.toArray(), l1.size());
                v = (V[]) l1.toArray();
            }
        }else{
            k = (K[]) l.toArray();
            v = (V[]) l1.toArray();
        }
        keys.addAll(Arrays.asList(k));
        values.addAll(Arrays.asList(v));
        ArrayList<ShiftingEntry<K, V>> l2 = new ArrayList<>();
        for(int i = k.length-1; i>0; --i) l2.add(new ShiftingEntry<>(keys, values, i));
        entrySet.addAll(l2);
    }

    @Override
    public void clear() {
        keys.clear();
        values.clear();
        entrySet.clear();
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
        return entrySet;
    }

    @Override
    public V put(K key, V value) {
        V v = null;
        if(keys.contains(key)) v = (V) values.set[keys.indexOf(key)];
        else keys.add(key);
        values.add(value);
        return v;
    }

    public boolean isKey(Object o){ try{ K k = (K) o; return true; }catch(Exception e){} return false; }
    public boolean isValue(Object o){ try{ V v = (V) o; return true; }catch(Exception e){} return false; }


    /**
     * Entries dynamically update as underlying sets change
     * @param <K>
     * @param <V>
     */
    protected static class ShiftingEntry<K, V> implements Entry<K, V>{

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

    protected static class UnchangingSet<E> extends ShiftingSet<E>{

        public UnchangingSet(int maxSize, float loadFactor) {
            super(maxSize, loadFactor);
        }

        @Override
        public boolean remove(Object o1) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            return false;
        }
    }

    protected static class ShiftingSet<E> implements Set<E>{

        Object[] set = new Object[1];
        final int maxSize;
        final float loadFactor;
        int populatedEntries = 0, load = 1; // Defines currently populated entries
        double avgLoad = 1;                 // Used to optimize allocation algorithm to -
                                            // most adequately fit the average amount of elements
                                            // stored during any one operation

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
            ++load;
            avgLoad = (avgLoad+1)/2;
            populatedEntries=populatedEntries<maxSize?populatedEntries+1:populatedEntries;
            Object[] o = new Object[Math.min((int)(100/(100/(populatedEntries+avgLoad)*loadFactor)), maxSize)];                   // Dynamically update array size according to loadFactor and max Size
            System.arraycopy(set, 0, o, 1, o.length!=maxSize?populatedEntries:populatedEntries-1);
            o[0] = e;
            set = o;
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
            ++load;
            avgLoad = (avgLoad+l.size()*avgLoad/load)/2;     // Improve prediction using relative applied load (to a point)
            int tmp = populatedEntries, cal;
            populatedEntries=populatedEntries+l.size()<maxSize?populatedEntries+l.size():maxSize;
            cal = (int) (100 / (100 / (populatedEntries + avgLoad) * loadFactor));
            if(populatedEntries==tmp){ // Just use the pre-allocated space determined by the load factor
                System.arraycopy(set, 0, set, l.size(), set.length-l.size());
                System.arraycopy(l.toArray(), 0, set, 0, l.size()-1);
            } else {
                Object[] o = new Object[cal>=maxSize?maxSize:cal];                   // Create new array
                for (int i = l.size(); i > 0; --i)
                    if (i < o.length - 1)
                        o[l.size() - i] = l.get(i);                                  // Move new values to start of array relative to their position
                if (l.size() < tmp) for (int i = 0; i < tmp - l.size(); ++i)
                    if (l.size() + i < o.length) o[l.size()] = set[i];          // Move old values to relative location
                set = o;                                                                                                    // Update reference
            }
            return true;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            ArrayList<Object> l = new ArrayList<>();
            for(Object e : c)
                for (Object o : set)
                    if (e.equals(o)){
                        l.add(e);
                        break;
                    }
            if(l.size() == 0 || set.length == l.size()) return false;
            clear();
            addAll((Collection<? extends E>) c);
            return true;
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
            for(int i = populatedEntries; i<set.length; ++i) set[i] = null;                                             // Remove accidental redundancies created when shifting
        }

        /**
         * Adapt array size according to populated elements and load factor.
         */
        void adaptLoad(){
            if(((int)(100/(100/(populatedEntries+avgLoad)*loadFactor)))>=set.length &&
                    ((int)(100/(100/populatedEntries+(avgLoad/2)*loadFactor)))<=set.length) return; // Array is roughly the right size
            Object[] o = new Object[Math.min((int)(100/(100/populatedEntries*loadFactor)), maxSize)];
            System.arraycopy(set, 0, o, 0, o.length);
            set = o;
        }

        /**
         * Reset prediction algorithm judgement.
         * (Makes newly stored values more valuable when accounting for free space)
         */
        public void resetLoadCount(){ load = 0; }

        /**
         * Reset whole prediction algorithm.
         * May cause RAM to suffer at the expense of better processing times.
         */
        public void resetLoadAlgo(){ load = 0; avgLoad = 2; }


        public int indexOf(Object o){
            for(int i = 0; i<populatedEntries; ++i)
                if(o.equals(set[i])) return i;
            return -1;
        }
    }

    protected static class ShiftingIterator<E> implements Iterator<E>{

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
