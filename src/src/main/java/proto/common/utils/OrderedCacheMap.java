package proto.common.utils;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Generic Thread Safe Ordered Cache with Optional Size Limit Eviction Policy
 * O(log n) insert
 * O(1) head/tail lookup
 * @param <T>
 */
public class OrderedCacheMap<T, U> {

    private final ConcurrentSkipListMap<T, U> cache;
    private Integer maxSize;

    public OrderedCacheMap(Comparator<T> comparator) {
        this(comparator, null);
    }

    public OrderedCacheMap(Comparator<T> comparator, Integer maxSize) {
        this.cache = new ConcurrentSkipListMap<>(comparator);
        if(maxSize != null) {
            this.maxSize = maxSize;
        }
    }

    public void put(T t, U u) {
        if(!this.cache.containsKey(t)) {
            this.cache.put(t, u);
            if(this.maxSize != null) {
                if (this.cache.size() >= maxSize) {
                    this.cache.pollLastEntry();
                }
            }
        }
    }

    public ConcurrentSkipListMap<T, U> getCache() {
        return cache;
    }

    public Map.Entry<T, U> getFirst() {
        return cache.firstEntry();
    }

    public Map.Entry<T, U> getLast() {
        return cache.lastEntry();
    }

    public Map.Entry<T, U> pollFirst() {
        return cache.pollFirstEntry();
    }

    public Map.Entry<T, U> pollLast() {
        return cache.pollLastEntry();
    }

    public U get(T t) {
        return cache.get(t);
    }

    public boolean containsKey(T t) {
        return cache.containsKey(t);
    }

    public void remove(T t) {
        cache.remove(t);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderedCacheMap)) return false;
        OrderedCacheMap<?, ?> that = (OrderedCacheMap<?, ?>) o;
        return Objects.equals(cache, that.cache);
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public int currentSize() {
        return cache.size();
    }

    @Override
    public int hashCode() {
        return Objects.hash(cache);
    }

    @Override
    public String toString() {
        return cache.toString();
    }

}
