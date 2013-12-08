package com.romainpiel.lib.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class BoundedTreeSet<E> extends TreeSet<E> {

    private int maxSize = Integer.MAX_VALUE;

    public BoundedTreeSet(int maxSize) {
        super();
    }

    public BoundedTreeSet(Collection<? extends E> c) {
        super(c);
    }

    public BoundedTreeSet(Comparator<? super E> c) {
        super(c);
    }

    public BoundedTreeSet(SortedSet<E> s) {
        super(s);
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int max) {
        maxSize = max;
        adjust();
    }

    private void adjust() {
        while (maxSize < size()) {
            remove(first());
        }
    }

    public boolean add(E item) {
        boolean out = super.add(item);
        adjust();
        return out;
    }

    public boolean addAll(Collection<? extends E> c) {
        boolean out = super.addAll(c);
        adjust();
        return out;
    }
}