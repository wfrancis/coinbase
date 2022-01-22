package proto.common.utils;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generic Thread Safe Ordered Cache with Optional Size Limit Eviction Policy
 * O(log n) insert
 * O(1) head/tail lookup
 * @param <T>
 */
public class OrderedCacheSet<T> {

    private final ConcurrentSkipListSet<T> cache;
    private AtomicInteger size;
    private AtomicInteger maxSize;

    public OrderedCacheSet(Comparator<T> comparator) {
        this(comparator, null);
    }

    public OrderedCacheSet(Comparator<T> comparator, Integer maxSize) {
        this.cache = new ConcurrentSkipListSet<>(comparator);
        if(maxSize != null) {
            this.size    = new AtomicInteger(0);
            this.maxSize = new AtomicInteger(maxSize);
        }
    }

    public void add(T t) {
        if(!this.cache.contains(t)) {
            this.cache.add(t);
            if(this.maxSize != null) {
                if (this.size.get() < maxSize.get()) {
                    this.size.incrementAndGet();
                } else {
                    this.cache.pollLast();
                }
            }
        }
    }

    public T getFirst() {
        return cache.first();
    }

    public T getLast() {
        return cache.last();
    }

    public T pollFirst() {
        return cache.pollFirst();
    }

    public T pollLast() {
        return cache.pollLast();
    }

    public void remove(T t) {
        cache.remove(t);
    }

    public T[] cacheElements() {
        return (T[]) cache.stream().toArray();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderedCacheSet)) return false;
        OrderedCacheSet<?> that = (OrderedCacheSet<?>) o;
        return cache.equals(that.cache);
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
