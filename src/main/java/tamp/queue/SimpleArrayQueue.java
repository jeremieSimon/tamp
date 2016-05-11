package tamp.queue;

public class SimpleArrayQueue<T> implements SimpleQueue<T> {

    public final int size;
    int tail;
    int head;
    final T[] elements;

    public SimpleArrayQueue(int size) {
        this.size = size;
        elements = (T[]) new Object[size];
        tail = 0;
        head = -1;
    }

    @Override
    public void add(T element) {
        if (head != -1 && ((head + 1) % size) == tail) {
            throw new RuntimeException();
        }
        head = (head + 1) % size;
        elements[head] = element;
    }

    @Override
    public T get() {
        T element = elements[tail];
        elements[tail] = null;
        if (element == null)
            throw new RuntimeException();
        tail = (tail + 1) % size;
        return element;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (head == -1)
            return "()";

        for (int i = tail; i % size < head; i++) {
            sb.append(i + " ");
        }
        sb.append(head);
        return sb.toString();
    }
}
