package com.tofvesson.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unchecked")
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
    public Object[] toArray() {
        Object[] o = new Object[pop];
        System.arraycopy(entries, 0, o, 0, pop);
        for(int i = 0; i<o.length; ++i) if(o[i]==empty) o[i] = null;
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
                if(pop<entries.length*load) {
                    shift();
                    adaptLoad(-1);
                }
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
    public boolean addAll(int index, Collection<? extends E> c) {
        if(index>=entries.length || index<0 || c.size()==0) return false;
        ArrayList<Object> l = new ArrayList<>(c);
        for(int i = 0; i<l.size(); ++i) if(l.get(i)==null) l.set(i, empty);
        if(pop+l.size()>maxSize) for(int i = pop+l.size()-maxSize; i<l.size(); ++i) l.remove(l.size());
        pop = pop==maxSize?pop:pop+l.size();
        if(pop==entries.length) adaptLoad();
        if(index==entries.length-1){
            entries[index] = l.get(0);
            return true;
        }
        if(l.size()+index<entries.length)
            System.arraycopy(entries, index, entries, index+1, entries.length-(index+l.size()));
        for(int i = index; i<Math.min(index+l.size(), entries.length); ++i) entries[i] = l.get(i);
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
        if((pop-=removed)<entries.length*load) adaptLoad();
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
        if((pop-=removed)<entries.length*load) adaptLoad();
        return true;
    }

    @Override
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
    }

    /**
     * Shift all elements towards the start of the arrays.
     */
    protected void shift(){
        for(int i = 0; i<entries.length; ++i)
            if(entries[i]==null && i!=pop-1)
                for(int j = i; j<entries.length; ++j)
                    if(entries[j]!=null){
                        entries[i] = entries[j];
                        entries[i] = null;
                        break;
                    }else if(j+1==entries.length) return;                                                               // Found all populated entries
    }

    protected void preparePopulate(int accountFor){
        if(accountFor>entries.length) adaptLoad(accountFor);                                                            // If new elements exceed limit, adapt load
        if(accountFor>entries.length) return;                                                                           // No need to delete elements if new values exceed limit
        System.arraycopy(entries, 0, entries, accountFor, entries.length-accountFor);                                   // Shift array elements to account for new elements
    }

    @Override
    public E get(int i){
        if(i>pop) return null;
        return entries[i]==empty?null:(E)entries[i];
    }

    @Override
    public E set(int index, E element) {
        if(index > pop) return null;
        E e = get(index);
        entries[index] = element;
        return e;
    }

    @Override
    public void add(int index, E element) {
        if(index>=entries.length || index<0) return;
        Object o = element==null?empty:element;
        pop = pop==maxSize?pop:pop+1;
        if(pop==entries.length) adaptLoad();
        if(index==entries.length-1){
            entries[index] = o;
            return;
        }
        System.arraycopy(entries, index, entries, index+1, entries.length-(index+1));
        entries[index] = o;
    }

    @Override
    public E remove(int index) {
        if(index>pop || index<0) return null;
        E e = entries[index]==empty?null:(E)entries[index];
        entries[index] = null;
        shift();
        if(--pop<entries.length*load) adaptLoad();
        return e;
    }

    @Override
    public int lastIndexOf(Object o) {
        for(int i = pop; i>0; --i)
            if(o.equals(entries[i]))
                return i;
        return -1;
    }

    @Override
    public Iterator<E> iterator(){
        return new Iterator<>();
    }

    @Override
    public ListIterator<E> listIterator() {
        return new ListIterator<>();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        if(index<0) throw new RuntimeException("Invalid starting point for iterator defined: "+index);
        return new ListIterator<>(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        if(fromIndex<0 || fromIndex>=toIndex || fromIndex>pop || toIndex>pop) return new ArrayList<>();
        ShiftingList<E> l = new ShiftingList<>(maxSize);
        for(int i = toIndex-1; i>fromIndex; --i) l.add(entries[i]==empty?null:(E)entries[i]);
        return l;
    }

    public class Iterator<V> implements java.util.Iterator<V>{

        int counter = 0;

        @Override
        public boolean hasNext() {
            return counter<ShiftingList.this.pop;
        }

        @Override
        public V next() {
            return entries[counter++]==empty?null:(V)entries[counter];
        }
    }

    public class ListIterator<V> implements java.util.ListIterator<V>{

        protected int counter = 0;
        protected boolean opNxt = false;
        private Object pEl = null;

        public ListIterator(){}
        public ListIterator(int start){ counter = start; }

        @Override
        public boolean hasNext() {
            return counter<ShiftingList.this.pop;
        }

        @Override
        public V next() {
            opNxt = true;
            return (V)(pEl=entries[counter++]==empty?null:entries[counter-1]);
        }

        @Override
        public boolean hasPrevious() {
            return counter>0&&ShiftingList.this.pop!=0;
        }

        @Override
        public V previous() {
            opNxt = false;
            return (V)(pEl=entries[--counter]==empty?null:entries[counter]);
        }

        @Override
        public int nextIndex() {
            return counter+1<pop?counter+1:pop;
        }

        @Override
        public int previousIndex() {
            return counter!=0?counter-1:0;
        }

        @Override
        public void remove() {
            ShiftingList.this.remove(counter-(opNxt?0:1));
        }

        @Override
        public void set(V v) {
            if(pEl==entries[counter-(opNxt?0:1)]) entries[counter-(opNxt?0:1)] = v==null?empty:v;
        }

        @Override
        public void add(V v) {
            ShiftingList.this.add(counter, (E)v);
        }
    }
}
