package proto.common;

/**
 * Publisher
 * @param <T>
 */
public interface Publisher<T> {
    /**
     * start publisher
     */
    public void start();

    /**
     * shutdown publisher
     */
    public void shutdown();

    /**
     * publish data
     * @param t
     */
    public void publish(T t);
}
