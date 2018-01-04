package net.tofvesson.collections;

import net.tofvesson.reflection.SafeReflection;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unchecked")
public final class Collections {

    private static final Field arrayListElements;
    private static final Field arrayListSize;

    static{
        arrayListElements = SafeReflection.getField(ArrayList.class, "elementData");
        arrayListSize = SafeReflection.getField(ArrayList.class, "size");
    }


    /**
     * Flip a collection and return the given collection with it's contents flipped.
     * @param c Collection to flip/reverse.
     * @param <T> Type of elements in collection.
     * @param <V> Type of collection to flip
     * @return The given collection.
     */
    public static <T, V extends Collection<T>> V flip(V c){
        ArrayList<T> a = flipNew(c);
        c.clear();
        c.addAll(a);
        return c;
    }

    /**
     * Reverse a collection and store the result in a new {@link ArrayList}.
     * @param c Collection to reverse.
     * @param <T> Type of the elements contained in the collection
     * @return ArrayList containing a reversed set of the collection.
     */
    public static <T> ArrayList<T> flipNew(Collection<T> c){
        ArrayList<T> a = new ArrayList<T>();
        T[] t = (T[]) c.toArray();
        for(int i = c.size(); i>0; --i) a.add(t[i-1]);
        return a;
    }

    /**
     * Check if a given array contains a value that fulfills the predicate.
     * @param o Values to check
     * @param p Predicate to use for checks.
     * @return True of any of the values matched.
     */
    public static <T> boolean arrayContains(T[] o, PredicateCompat<T> p){ for(T o1 : o) if(p.apply(o1)) return true; return false; }

    /**
     * Create an {@link ArrayList} from a given set of values.
     * @param t Values to create ArrayList from.
     * @param <T> Type of the values to insert.
     * @return An ArrayList containing the given set of values.
     */
    public static <T> ArrayList<T> fromArray(T[] t){ return setValues(new ArrayList<T>(), t); }

    /**
     * Overwrite values in {@link ArrayList} with the given value set.
     * @param a ArrayList to replace values in
     * @param t Values to insert
     * @param <T> Type of the values being inserted
     * @return The given ArrayList.
     */
    public static <T> ArrayList<T> setValues(ArrayList<T> a, T[] t){
        try{
            arrayListElements.set(a, t);
            arrayListSize.setInt(a, t.length);
        }
        catch(NoSuchFieldError e){}
        catch (IllegalAccessException e){}
        return a;
    }

    /**
     * Add an array to an {@link ArrayList} without having to loop.
     * @param a ArrayList to add elements to
     * @param values Elements to add
     * @param <T> Type of the elements to add
     * @return The given ArrayList
     */
    public static <T> ArrayList<T> addAll(ArrayList<T> a, T[] values){
        try{
            int size = arrayListSize.getInt(a);
            T[] t = (T[]) arrayListElements.get(a);
            T[] t1 = (T[]) Array.newInstance(values.getClass().getComponentType(), size+values.length);
            System.arraycopy(t, 0, t1, 0, size);
            System.arraycopy(values, 0, t1, t.length, values.length);
            arrayListElements.set(a, t1);
            arrayListSize.setInt(a, t1.length);
        }
        catch(NoSuchFieldError e){}
        catch (IllegalAccessException e){}
        return a;
    }

    /**
     * Predicate interface to add compatibility with older versions of Java that don't include it
     * @param <T> The type to evaluate
     */
    public interface PredicateCompat<T>{ boolean apply(T t); }
}
