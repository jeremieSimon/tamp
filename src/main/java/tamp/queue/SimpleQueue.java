package tamp.queue;

public interface SimpleQueue<T> {

    void add(T element);
    T get();
}
