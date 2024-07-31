package tech.xavi.soulsync.entity;

import java.util.LinkedHashMap;
import java.util.Map;

public class FixedSizeMap<K, V> extends LinkedHashMap<K, V> {

    private final int MAX_SIZE;

    public FixedSizeMap(int maxSize) {
        super(maxSize, 0.75f, true);
        this.MAX_SIZE = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > MAX_SIZE;
    }

}