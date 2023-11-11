package org.sinou.pydia.sdk.utils;

import java.util.HashMap;
import java.util.Map;

import org.sinou.pydia.sdk.api.Store;

public class MemoryStore<T> implements Store<T> {

    private final Map<String, T> objects = new HashMap<>();

    @Override
    public void put(String id, T obj) {
        objects.put(id, obj);
    }

    @Override
    public T get(String id) {
        return objects.get(id);
    }

    @Override
    public Map<String, T> getAll() {
        return objects;
    }

    @Override
    public void remove(String id) {
        objects.remove(id);
    }

    @Override
    public void clear() {
        objects.clear();
    }
}
