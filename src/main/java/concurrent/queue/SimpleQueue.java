package concurrent.queue;

public class SimpleQueue<T> {

    SimpleNode<T> head;
    SimpleNode<T> tail;

    public SimpleQueue() {
        head = new SimpleNode<T>(null); // head here is just as a dummy marker
        head.next = tail;
    }

    public void add(T value) {
        SimpleNode<T> newNode = new SimpleNode<>(value);
        if (tail == null) {
            tail = newNode;
            head.next = newNode;
            return;
        }
        tail.next = newNode;
        tail = newNode;
    }

    public T get() {
        if (head.next == null) {
            throw new RuntimeException();
        }
        SimpleNode<T> getNode = head.next;
        head.next = getNode.next;
        return getNode.value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        SimpleNode<T> curr = head.next;
        while (curr != null) {
            sb.append(curr.value + " ");
            curr = curr.next;
        }
        return sb.toString();
    }

    public static class SimpleNode<T> {
        final T value;
        SimpleNode<T> next;

        public SimpleNode(T value) {
            this.value = value;
        }
    }
}
