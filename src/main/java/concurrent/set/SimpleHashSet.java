package concurrent.set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Most Simple example of a closed addressing HashSet.
 */
public class SimpleHashSet<T> implements SimpleSet<T> {

    int size;
    int capacity;
    List<T>[] elements;

    @SuppressWarnings("unchecked")
    public SimpleHashSet(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        this.elements = new ArrayList[capacity];
    }

    @Override
    public boolean contains(T element) {
        int hash = Math.abs(element.hashCode() % capacity);
        if (elements[hash] == null)
            return false;
        return elements[hash].contains(element);
    }

    @Override
    public void add(T element) {
        int hash = Math.abs(element.hashCode() % capacity);
        if (elements[hash] == null)
            elements[hash] = new ArrayList<T>();
        if (!elements[hash].contains(element)) {
            elements[hash].add(element);
            size++;
        }

        if (size > capacity / 2) {
            resize();
        }
    }

    @Override
    public boolean remove(T element) {
        int hash = Math.abs(element.hashCode() % capacity);
        if (elements[hash] == null)
            return false;

        boolean isRemoved = elements[hash].remove(element);
        if (isRemoved) size--;
        return isRemoved;

    }

    @Override
    public String toString() {
        return Arrays.toString(elements);
    }

    private void resize() {
        int newCapacity = capacity * 2;
        List<T>[] newElements = new ArrayList[capacity * 2];
        for (int i = 0; i < capacity; i++) {
            newElements[i] = elements[i];
        }
        elements = newElements;
        capacity = newCapacity;
    }
}
