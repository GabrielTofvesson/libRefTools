package com.tofvesson.reflection;

import java.util.HashMap;

/**
 * A definition for custom enum creation.
 */
public class EnumDefinition {
    HashMap<Object, Class> params = new HashMap<>(); // Assign a specific type to each parameter

    /**
     * Put an object in the parameter list.
     * @param value The parameter to supply. (Type is derived automatically)
     * @return A reference to the EnumDefinition object this method was called on (for chaining).
     */
    public EnumDefinition putObject(Object value){
        params.put(value, value.getClass());
        return this;
    }

    /**
     * Put a primitive value in the parameter list.
     * @param autoBoxed An autoboxed version of the parameter. (For example putPrimitive(5) will automatically become Integer)
     * @return A reference to the EnumDefinition object this method was called on (for chaining).
     * @throws NoSuchFieldException
     */
    public EnumDefinition putPrimitive(Object autoBoxed) throws NotAutoBoxedException{
        // All autoboxed versions of primitives have a reference to their boxed primitive
        try {
            params.put(autoBoxed, autoBoxed.getClass().getDeclaredField("value").getType());
        }catch(Exception e){ throw new NotAutoBoxedException(); }
        return this;
    }

    /**
     * Safe wrapper of {@link #putPrimitive(Object)} for boolean.
     * @param b Boolean parameter.
     * @return A reference to the EnumDefinition object this method was called on (for chaining).
     */
    public EnumDefinition putBoolean(boolean b){
        try { return putPrimitive(b);  } catch (NotAutoBoxedException e) { }
        return this;
    }

    /**
     * Safe wrapper of {@link #putPrimitive(Object)} for byte.
     * @param b Byte parameter.
     * @return A reference to the EnumDefinition object this method was called on (for chaining).
     */
    public EnumDefinition putByte(byte b){
        try { return putPrimitive(b); } catch (NotAutoBoxedException e) { }
        return this;
    }

    /**
     * Safe wrapper of {@link #putPrimitive(Object)} for char.
     * @param c Character parameter.
     * @return A reference to the EnumDefinition object this method was called on (for chaining).
     */
    public EnumDefinition putChar(char c){
        try { return putPrimitive(c); } catch (NotAutoBoxedException e) { }
        return this;
    }

    /**
     * Safe wrapper of {@link #putPrimitive(Object)} for short.
     * @param s Short parameter.
     * @return A reference to the EnumDefinition object this method was called on (for chaining).
     */
    public EnumDefinition putShort(short s){
        try { return putPrimitive(s); } catch (NotAutoBoxedException e) { }
        return this;
    }

    /**
     * Safe wrapper of {@link #putPrimitive(Object)} for int.
     * @param i Integer parameter.
     * @return A reference to the EnumDefinition object this method was called on (for chaining).
     */
    public EnumDefinition putInt(int i){
        try { return putPrimitive(i); } catch (NotAutoBoxedException e) { }
        return this;
    }

    /**
     * Safe wrapper of {@link #putPrimitive(Object)} for long.
     * @param l Long parameter.
     * @return A reference to the EnumDefinition object this method was called on (for chaining).
     */
    public EnumDefinition putLong(long l){
        try { return putPrimitive(l); } catch (NotAutoBoxedException e) { }
        return this;
    }

    /**
     * Safe wrapper of {@link #putPrimitive(Object)} for float.
     * @param f Floating point parameter.
     * @return A reference to the EnumDefinition object this method was called on (for chaining).
     */
    public EnumDefinition putFloat(float f){
        try { return putPrimitive(f); } catch (NotAutoBoxedException e) { }
        return this;
    }

    /**
     * Safe wrapper of {@link #putPrimitive(Object)} for double.
     * @param d Double parameter.
     * @return A reference to the EnumDefinition object this method was called on (for chaining).
     */
    public EnumDefinition putDouble(double d){
        try { return putPrimitive(d); } catch (NotAutoBoxedException e) { }
        return this;
    }
}
