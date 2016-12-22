package com.tofvesson.collections;

final class Empty {
    /**
     * Internal reference to reserved, unpopulated entries in collections.
     */
    static final Empty empty = new Empty();
}