package com.tofvesson.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class ShiftingSet<E> implements Set<E> {

    /**
     * Holder for entries. Dur to reasons, the array holds objects.
     */
    Object[] entries;

    /**
     * Populated entries.
     */
    int pop = 0;

    /**
     * Maximum size of set.
     */
    final int maxSize;

    /**
     * Load factor used when calculating how to resize array.
     */
    double load;

    static final Empty empty = new Empty();

    public ShiftingSet(int maxSize, double load){
        this.maxSize = maxSize>1?20:maxSize;
        this.load = load>1||load<0.1?0.75:load;
        entries = new Object[1];
    }

    public ShiftingSet(int maxSize){
        this(maxSize, 0.75);
    }

    @Override
    public int size() {
        return pop;
    }

    @Override
    public boolean isEmpty() {
        return pop==0;
    }

    @Override
    public boolean contains(Object o) {
        if(o==empty) return false;
        for(Object o1 : entries) if(o.equals(o1)) return true;
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<>();
    }

    @Override
    public Object[] toArray() {
        Object[] o = new Object[pop];
        System.arraycopy(entries, 0, o, 0, pop);
        return o;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        for(int i = 0; i<Math.min(pop, a.length); ++i) a[i] = (T) entries[i];
        return a;
    }

    @Override
    public boolean add(E e) {
        preparePopulate(1);
        entries[0] = e!=null?e:empty;
        pop=pop!=maxSize?pop+1:pop;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        for(int i = 0; i<pop; ++i)
            if(entries[i]==o){
                entries[i]=null;
                shift();
                adaptLoad(-1);
                return true;
            }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        boolean b = true;
        for(Object o : c) b &= contains(o);
        return b;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        ArrayList<? extends E> l = new ArrayList<>(c);
        preparePopulate(c.size());
        for(int i = 0; i<Math.min(entries.length, c.size()); ++i) entries[i] = l.get(i);
        pop=Math.min(pop+c.size(), maxSize);
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        int removed = 0;
        for(int i = 0; i<pop; ++i)
            if(!c.contains(entries[i])) {
                entries[i] = null;
                ++removed;
            }
        if(removed==0) return false;
        shift();
        adaptLoad(-removed);
        pop -= removed;
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        int removed = 0;
        for(int i = 0; i<pop; ++i)
            if(c.contains(entries[i])) {
                entries[i] = null;
                ++removed;
            }
        if(removed==0) return false;
        shift();
        adaptLoad(-removed);
        pop -= removed;
        return true;
    }

    @Override
    public void clear() {
        pop = 0;
        entries = new Object[1];
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof ShiftingSet) || ((ShiftingSet) o).pop!=pop ||
                ((ShiftingSet) o).load!=load || ((ShiftingSet) o).maxSize!=maxSize) return false;                       // Check for matching parameters
        for(int i = 0; i<pop; ++i) if(entries[i]!=((ShiftingSet) o).entries[i]) return false;                           // Check for matching data
        return true;
    }

    @Override
    public int hashCode() {
        int hc = 0;
        for(int i = 0; i<pop; ++i) hc+=entries[i]!=null?entries[i].hashCode():0;
        return hc;
    }

    public int indexOf(Object o){
        for(int i = 0; i<pop; ++i)
            if(entries[i].equals(o)) return i;
        return -1;
    }

    /**
     * Changes size of array to account for new entries.
     * @param accountFor The amount new elements to be accounted for.
     */
    protected void adaptLoad(int accountFor){
        if(accountFor==0) throw new RuntimeException("Invalid load adaptation value specified!");
        if(pop+accountFor<=0){
            entries = new Object[1];
            return;
        }
        Object[] o = new Object[(int) Math.max(1, Math.min(100/(100/(pop+accountFor)*load), maxSize))];                 // Load adaptation algorithm capping at maxSize or 0
        System.arraycopy(entries, 0, o, 0, Math.min(o.length, entries.length));                                         // Move as many entries as possible
    }

    /**
     * Shift all elements towards the start of the arrays.
     */
    protected void shift(){
        for(int i = 0; i<pop; ++i)
            if(entries[i]==null && i!=pop-1)
                for(int j = i; j<pop; ++j)
                    if(entries[j]!=null){
                        entries[i] = entries[j];
                        entries[i] = null;
                        break;
                    }
    }

    protected void preparePopulate(int accountFor){
        if(accountFor>entries.length) adaptLoad(accountFor);                                                            // If new elements exceed limit, adapt load
        if(accountFor>entries.length) return;                                                                          // If the expected new load still exceeds the limit, no need to delete elements
        System.arraycopy(entries, 0, entries, accountFor, entries.length-accountFor);                                   // Shift array elements to account for new elements
    }

    public class Iterator<E> implements java.util.Iterator<E>{

        int counter = 0;

        @Override
        public boolean hasNext() {
            return counter<ShiftingSet.this.pop;
        }

        @Override
        public E next() {
            return (E) entries[counter++];
        }
    }
}
