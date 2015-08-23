package com.integreight.onesheeld.sdk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class KnownShields implements List<KnownShield> {
    private static KnownShields instance = null;
    private List<KnownShield> knownShields;

    private KnownShields() {
        knownShields = new ArrayList<>();
    }

    static KnownShields getInstance() {
        if (instance == null) {
            synchronized (KnownShields.class) {
                if (instance == null) {
                    instance = new KnownShields();
                }
            }
        }
        return instance;
    }

    @Override
    public void add(int location, KnownShield object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(KnownShield object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int location, Collection<? extends KnownShield> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends KnownShield> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object object) {
        return knownShields.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return knownShields.containsAll(collection);
    }

    @Override
    public KnownShield get(int location) {
        return knownShields.get(location);
    }

    @Override
    public int indexOf(Object object) {
        return knownShields.indexOf(object);
    }

    @Override
    public boolean isEmpty() {
        return knownShields.isEmpty();
    }

    @Override
    public Iterator<KnownShield> iterator() {
        return knownShields.iterator();
    }

    @Override
    public int lastIndexOf(Object object) {
        return knownShields.lastIndexOf(object);
    }

    @Override
    public ListIterator<KnownShield> listIterator() {
        return knownShields.listIterator();
    }

    @Override
    public ListIterator<KnownShield> listIterator(int location) {
        return knownShields.listIterator(location);
    }

    @Override
    public KnownShield remove(int location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public KnownShield set(int location, KnownShield object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return knownShields.size();
    }

    @Override
    public List<KnownShield> subList(int start, int end) {
        return knownShields.subList(start, end);
    }

    @Override
    public Object[] toArray() {
        return knownShields.toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        return knownShields.toArray(array);
    }
}
