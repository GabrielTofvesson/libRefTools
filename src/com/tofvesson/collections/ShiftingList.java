package com.tofvesson.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings({"unchecked", "ReturnOfInnerClass", "unused"})
public class ShiftingList<E> implements List<E> {
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

    /**
     * Internal reference to reserved, unpopulated entries.
     */
    static final Empty empty = new Empty();

    public ShiftingList(int maxSize, double load){
        this.maxSize = maxSize<1?20:maxSize;
        this.load = load>0.99||load<0.1?0.75:load;
        entries = new Object[1];
    }

    public ShiftingList(int maxSize){
        this(maxSize, 0.75);
    }

    public int size() {
        return pop;
    }

    public boolean isEmpty() {
        return pop==0;
    }

    public boolean contains(Object o) {
        if(o==empty) return false;
        for(int i = 0; i<pop; ++i) if((o!=null && o.equals(entries[i])) || (o==null && entries[i]==empty)) return true;
        return false;
    }

    public Object[] toArray() {
        Object[] o = new Object[pop];
        System.arraycopy(entries, 0, o, 0, pop);
        for(int i = 0; i<o.length; ++i) if(o[i]==empty) o[i] = null;
        return o;
    }

    public <T> T[] toArray(T[] a) {
        for(int i = 0; i<Math.min(pop, a.length); ++i) a[i] = (T) entries[i];
        return a;
    }

    public boolean add(E e) {
        if(contains(e)) return false;
        preparePopulate(1);
        entries[0] = e!=null?e:empty;
        pop=pop!=maxSize?pop+1:pop;
        return true;
    }

    public boolean remove(Object o) {
        for(int i = 0; i<pop; ++i)
            if(entries[i]==o){
                entries[i]=null;
                --pop;
                shift();
                if(pop<entries.length*load) adaptLoad();
                return true;
            }
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        boolean b = true;
        for(Object o : c) b &= contains(o);
        return b;
    }

    public boolean addAll(Collection<? extends E> c) {
        ArrayList<? super E> l = new ArrayList<E>();
        for(E e : c) if(!contains(e)) l.add(e);
        if(l.size()>maxSize) for(int i = maxSize; i<l.size(); ++i) l.remove(i);
        preparePopulate(l.size());
        for(int i = 0; i<l.size(); ++i) entries[i] = l.get(i);
        pop=Math.min(pop+l.size(), maxSize);
        return true;
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        if(index>=maxSize) return false;
        if(index>=entries.length || index<0 || c.size()==0) return false;
        ArrayList<? super E> l = new ArrayList<E>();
        for(E e : c) if(!contains(e)) l.add(e);
        if(index+l.size()>maxSize) for(int i = maxSize-index; i<l.size(); ++i) l.remove(i);
        adaptLoad(l.size());
        pop = pop+l.size() >= maxSize ? pop : pop+l.size();
        if(l.size()+index<entries.length) System.arraycopy(entries, index, entries, l.size()+1, entries.length-l.size()-1);
        for(int i = 0; i<l.size(); ++i) entries[i+index] = l.get(i);
        shift();                                                                                                        // Ensure no misalignment happens
        return true;
    }

    public boolean retainAll(Collection<?> c) {
        int removed = 0;
        for(int i = 0; i<pop; ++i)
            if(!c.contains(entries[i])) {
                entries[i] = null;
                ++removed;
            }
        if(removed==0) return false;
        shift();
        if((pop-=removed)<entries.length*load) adaptLoad();
        return true;
    }

    public boolean removeAll(Collection<?> c) {
        int removed = 0;
        for(int i = 0; i<pop; ++i)
            if(c.contains(entries[i])) {
                entries[i] = null;
                ++removed;
            }
        if(removed==0) return false;
        shift();
        if((pop-=removed)<entries.length*load) adaptLoad();
        return true;
    }

    public void clear() {
        pop = 0;
        entries = new Object[1];
    }

    public int indexOf(Object o){
        for(int i = 0; i<pop; ++i)
            if(entries[i].equals(o)) return i;
        return -1;
    }

    /**
     * Adapts array size according to currently populated entries.
     * Meant to be used when populated entries variable has already been updated.
     * Same as invoking {@link #adaptLoad(int)} with parameter 0.
     */
    protected void adaptLoad(){
        adaptLoad(0);
    }

    /**
     * Changes size of array to account for new entries.
     * @param accountFor The amount new elements to be accounted for.
     */
    protected void adaptLoad(int accountFor){
        if(pop+accountFor<=0){
            entries = new Object[1];
            return;
        }
        Object[] o = new Object[(int) Math.max(1, Math.min((pop+accountFor)/load, maxSize))];                           // Load adaptation algorithm capping at maxSize or 0
        System.arraycopy(entries, 0, o, 0, Math.min(o.length, entries.length));                                         // Move as many entries as possible
        entries = o;
    }

    /**
     * Shift all elements towards the start of the arrays.
     */
    protected void shift(){
        for(int i = 0; i<entries.length; ++i)
            if(entries[i]==null && i!=pop)
                for(int j = i; j<entries.length; ++j)
                    if(entries[j]!=null){
                        entries[i] = entries[j];
                        entries[j] = null;
                        break;
                    }else if(j+1==entries.length) return;                                                               // Found all populated entries
    }

    /**
     * Prepares entry array for population of new values.
     * @param accountFor New values to account for (in pop variable hasn't been updated).
     */
    protected void preparePopulate(int accountFor){
        if(accountFor+pop>entries.length) adaptLoad(accountFor);                                                        // If new elements exceed limit, adapt load
        if(accountFor>entries.length) return;                                                                           // No need to delete elements if new values exceed limit
        System.arraycopy(entries, 0, entries, accountFor, entries.length-accountFor);                                   // Shift array elements to account for new elements
    }

    public E get(int i){
        if(i>pop) return null;
        return entries[i]==empty?null:(E)entries[i];
    }

    public E set(int index, E element) {
        if(index > pop) return null;
        E e = get(index);
        entries[index] = element==null?empty:element;
        return e;
    }

    public void add(int index, E element) {
        if(index<0 || contains(element)) return;
        Object o = element==null?empty:element;
        pop = pop==maxSize?pop:pop+1;
        adaptLoad();
        if((index>=entries.length?index=Math.min(entries.length-1, pop):index)==entries.length-1){
            entries[index] = o;
            return;
        }
        System.arraycopy(entries, index, entries, index+1, entries.length-(index+1));
        entries[index] = o;
    }

    public E remove(int index) {
        if(index>pop || index<0) return null;
        E e = entries[index]==empty?null:(E)entries[index];
        entries[index] = null;
        shift();
        if(--pop<entries.length*load) adaptLoad();
        return e;
    }

    public int lastIndexOf(Object o) {
        for(int i = pop; i>0; --i)
            if(o.equals(entries[i]))
                return i;
        return -1;
    }

    public Iterator<E> iterator(){ return new Iterator<E>(this); }

    public ListIterator<E> listIterator(){ return new ListIterator<E>(this); }

    public ListIterator<E> listIterator(int index) {
        if(index<0) throw new RuntimeException("Invalid starting point for iterator defined: "+index);
        return new ListIterator<E>(this, index);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        if(fromIndex<0 || fromIndex>=toIndex || fromIndex>pop || toIndex>pop) return new ArrayList<E>();
        ShiftingList<E> l = new ShiftingList<E>(maxSize);
        for(int i = toIndex-1; i>fromIndex; --i) l.add(entries[i]==empty?null:(E)entries[i]);
        return l;
    }

    /**
     * Standard iterator. For observing list.
     * @param <V> Type of object stored in list.
     */
    public static class Iterator<V> implements java.util.Iterator<V>{
        protected int counter = 0;
        private final ShiftingList ref;
        private Object previous;
        public Iterator(ShiftingList ref){ this.ref = ref; }
        public boolean hasNext() { return counter<ref.pop; }
        public V next() { return (V)(previous=ref.entries[counter++]==empty?null:ref.entries[counter-1]); }
        public void remove(){ if(counter!=0){ ref.remove(previous); --counter; } }
    }

    /**
     * List iterator. For making modifications on-the-go while going through list.
     * @param <V> Type of object stored in list.
     */
    public static class ListIterator<V> implements java.util.ListIterator<V>{
        protected int counter = 0;
        protected boolean opNxt = false;
        private Object pEl = null;
        private final int pop;
        private final Object[] entries;
        private final ShiftingList list;
        public ListIterator(ShiftingList list){ this.pop = list.pop; this.entries = list.entries; this.list = list;}
        public ListIterator(ShiftingList list, int start){ this(list); counter = start; }
        public boolean hasNext() { return counter<pop; }
        public V next() { opNxt = true; return (V)(pEl=entries[counter++]==empty?null:entries[counter-1]); }
        public boolean hasPrevious() { return counter>0&&pop!=0; }
        public V previous() { opNxt = false; return (V)(pEl=entries[--counter]==empty?null:entries[counter]); }
        public int nextIndex() { return counter+1<pop?counter+1:pop; }
        public int previousIndex() { return counter!=0?counter-1:0; }
        public void remove() { list.remove(counter-(opNxt?0:1)); }
        public void set(V v) { if(pEl==entries[counter-(opNxt?0:1)]) entries[counter-(opNxt?0:1)] = v==null?empty:v; }
        public void add(V v) { list.add(counter, v); }
    }
}
