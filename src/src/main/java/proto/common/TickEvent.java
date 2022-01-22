package proto.common;

import com.lmax.disruptor.EventFactory;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Generic TickEvent
 * @param <T>
 */
public class TickEvent<T> {
    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public final static EventFactory EVENT_FACTORY = TickEvent::new;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
