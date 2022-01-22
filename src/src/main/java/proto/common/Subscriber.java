package proto.common;

import com.lmax.disruptor.EventHandler;

/**
 * Generic subscriber that utilizes disruptor event handler
 * @param <T>
 */
public interface Subscriber<T> extends EventHandler<T> {
}
