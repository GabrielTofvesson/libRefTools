package net.tofvesson.collections;

/**
 * Compat version of java.util.Optional
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class Optional<T> {

    private final T value;
    private final boolean hasValue;

    private Optional(T t){
        value = nonNull(t);
        hasValue = true;
    }
    private Optional(){
        value = null;
        hasValue = false;
    }

    protected T getValue(){ return value; }
    public boolean isPresent(){ return hasValue; }
    public T get(){ return valueOrThrow(getValue(), isPresent()); }

    public T or(Supplier<? extends Optional<? extends T>> s){
        nonNull(s);
        if(isPresent()) return getValue();
        Optional<? extends T> o = s.get();
        return valueOrThrow(o.getValue(), o.isPresent());
    }
    public Optional<T> filter(Collections.PredicateCompat<T> p){
        nonNull(p);
        if(!isPresent()) return this;
        return p.apply(getValue())?this:(Optional<T>) empty();
    }


    public static <T> Optional<T> empty(){ return new Optional<T>(); }
    public static <T> Optional<T> of(T value){ return new Optional<T>(value); }
    public static <T> Optional<T> ofNullable(T value){ return value==null?(Optional<T>) empty() : of(value); }

    private static <T> T nonNull(T o){
        if(o==null) throw new NullPointerException();
        return o;
    }

    private static <T> T valueOrThrow(T t, boolean thr){
        if(thr) throw new NullPointerException();
        return nonNull(t);
    }
}
