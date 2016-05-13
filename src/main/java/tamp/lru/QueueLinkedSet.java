package tamp.lru;

import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

/**
 * Set with remove, add, and popHead of the queue operations performing in O(1)
 */
public class QueueLinkedSet<T> {

    private Node<T> head;
    private Node<T> tail;
    private Map<T, Node<T>> elements;
    private final int capacity;
    private int size;

    public QueueLinkedSet(final int capacity) {
        this.capacity = capacity;

        size = 0;
        elements = Maps.newHashMap();

        head = new Node<>(null);
        tail = new Node<>(null);
        tail.prev = head;
        head.next = tail;
    }

    public boolean contains(final T element) {
        return elements.containsKey(element);
    }

    public void add(final T element) {
        elements.put(element, addToList(element));
        size++;
    }

    public Optional<T> addAndPopIfFull(final T element) {
        add(element);
        if (size > capacity) {
            return pop();
        }
        return Optional.absent();
    }

    public Optional<T> pop() {
        if (size == 0)
            return Optional.absent();
        Node<T> victim = head.next;
        head.next = victim.next;
        victim.prev = head;
        size--;
        return Optional.of(victim.element);
    }

    public boolean remove(final T element) {
        if (!elements.containsKey(element)) {
            return false;
        }

        removeFromList(elements.get(element));
        size--;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Node<T> curr = head.next;
        while (curr != null && curr != tail) {
            sb.append(curr.element + " ");
            curr = curr.next;
        }
        return sb.toString();
    }

    private void removeFromList(Node<T> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private Node<T> addToList(T element) {
        Node<T> newNode = new Node<T>(element);
        Node<T> last = tail.prev;
        last.next = newNode;
        tail.prev = newNode;
        newNode.prev = last;
        return newNode;
    }

    static class Node<T> {
        final T element;
        Node<T> next;
        Node<T> prev;

        Node(T element) {
            this.element = element;
        }
    }

}
