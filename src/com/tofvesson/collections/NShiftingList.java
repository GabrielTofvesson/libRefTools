package com.tofvesson.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

@SuppressWarnings("ALL")
public class NShiftingList<E> implements List<E> {

    private final float load;
    private final int maxSize;
    private int pop = 0;
    private Object[] entries = new Object[1];



    public NShiftingList(int maxSize, float load){ this.maxSize = maxSize; this.load = load; }
    public NShiftingList(int maxSize){ this(maxSize, 0.75f); }



    public int size() { return pop; }
    public boolean isEmpty() { return pop==0; }

    public boolean contains(Object o) {
        if(o==null) return false;
        for(int i = entries.length-1; i>entries.length-pop; --i) if(o==entries[i]) return true;
        return false;
    }

    public Iterator<E> iterator() {
        //TODO: Implement
        return null;
    }

    public Object[] toArray() {
        Object[] o = new Object[pop];
        System.arraycopy(entries, entries.length-pop, o, 0, pop);
        return o;
    }

    public <T> T[] toArray(T[] a) {
        System.arraycopy(entries, entries.length-pop, a, 0, pop>a.length?a.length:pop);
        return a;
    }

    public boolean add(E e) {
        if(entries.length==pop) preparePopulate(1);
        entries[entries.length-(pop=pop==maxSize?maxSize:pop+1)] = e;
        return true;
    }

    public boolean remove(Object o) {
        for(int i = entries.length-1; i>entries.length-pop; --i) if(entries[i]==o){ entries[i] = null; shift(); return true; }
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        for(Object o : c)
            for(int i = entries.length-1; i>entries.length-pop; --i)
                if(entries[i]==o) break;
                else if(i==entries.length-pop+1) return false;
        return true;
    }

    public boolean addAll(Collection<? extends E> c) {
        preparePopulate(c.size());
        return false;
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        //TODO: Implement
        return false;
    }

    public boolean removeAll(Collection<?> c) {
        //TODO: Implement
        return false;
    }

    public boolean retainAll(Collection<?> c) {
        //TODO: Implement
        return false;
    }

    public void clear() {
        //TODO: Implement

    }

    public E get(int index) {
        if(index>=pop || index<0) throw new IndexOutOfBoundsException();                                                // Ensure that user has defined a valid index
        return (E) entries[entries.length-pop+index];
    }

    public E set(int index, E element) {
        if(index>=pop || index<0) throw new IndexOutOfBoundsException();                                                // Ensure that user has defined a valid index
        E e = (E) entries[entries.length-pop+index];
        entries[entries.length-pop+index] = element;
        if(element==null) adaptLoad(0);                                                                      // Handle load adaptation
        return e;
    }

    public void add(int index, E element) {
        //TODO: Implement
    }

    public E remove(int index) {
        //TODO: Implement
        return null;
    }

    public int indexOf(Object o) {
        //TODO: Implement
        return 0;
    }

    public int lastIndexOf(Object o) {
        //TODO: Implement
        return 0;
    }

    public ListIterator<E> listIterator() {
        //TODO: Implement
        return null;
    }

    public ListIterator<E> listIterator(int index) {
        //TODO: Implement
        return null;
    }

    public List<E> subList(int fromIndex, int toIndex) {
        //TODO: Implement
        return null;
    }

    /**
     * Shift populated to replace unpopulated (removed) entries
     */
    protected void shift(){
        /*
        Reads three data points:
        1: starting position of a block of populated entries,
        2: ending position,
        3: starting position of next block

                     1        2           3
                     ▼       ▼          ▼
        [U, U, U, U, P, P, P, U, U, U, U, P, P, U, U, P]         Pass 1
                     4        7           11


                                 1              2     3
                                 ▼             ▼    ▼
        [U, U, U, U, U, U, U, U, P, P, P, P, P, U, U, P]         Pass 2
                                 8              13    15


                                       1
                                       ▼
        [U, U, U, U, U, U, U, U, U, U, P, P, P, P, P, P]         Pass 3 ("2" can't be placed because end was found)

         */
        for(int i = 0; i<entries.length-1; ++i)
            if(entries[i]!=null)
                for(int j = i; j<entries.length; ++j)
                    if(entries[j]==null) {
                        for (int k = j; k < entries.length + 1; ++k)
                            if (k < entries.length && entries[k] != null) {
                                System.arraycopy(entries, i, entries, k - j + i, j - i);
                                break;
                            }else if (k == entries.length) System.arraycopy(entries, i, entries, k - j + i, j - i);
                        break;
                    }else if(j==entries.length-1) return;                                                               // No more unpopulated entries found
    }

    /**
     * Array load adaptation.
     */
    protected void adaptLoad(int accountFor){
        if(!checkShifted()) shift(); // Ensure that array is properly shifted before adapting load.
        adaptLoad0(accountFor);
    }

    /**
     * Internal array load adaptation if it is known for a fact that array already has been shifted.
     */
    private void adaptLoad0(int accountFor){
        if(pop+accountFor<=0){
            entries = new Object[1];
            return;
        }
        Object[] o = new Object[(int) Math.max(1, Math.min((pop+accountFor)/load, maxSize))];                           // Load adaptation algorithm capping at maxSize or 0
        System.arraycopy(entries, entries.length-pop, o, o.length-pop, pop);
        entries = o;
    }

    /**
     * Check if populated space contain unpopulated entries.
     * If there are unpopulated entries, it means that array isn't propperly shifted.
     * @return True if values are shifted, false if there exist unpopulated entries in the populated space.
     */
    protected boolean checkShifted(){
        for(int i = entries.length-1; i>entries.length-pop; --i) if(entries[i]==null) return false; // Check if any unpopulated values exist in the populated range
        return true;
    }

    /**
     * Prepares entry array for population of new values.
     * @param accountFor New values to account for (in pop variable hasn't been updated).
     */
    protected void preparePopulate(int accountFor){
        if(accountFor+pop>entries.length) adaptLoad(accountFor);                                                        // If new elements exceed limit, adapt load
        if(accountFor>entries.length) return;                                                                           // No need to delete elements if new values exceed limit
        if(accountFor+pop>entries.length) System.arraycopy(entries, entries.length-pop, entries,
                entries.length-pop+accountFor, pop+accountFor-entries.length);                          // Shift old values (possible deletion)
    }
}
