package concurrent.set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple example of a concurrent hashset where you would have one lock
 * per bucket.
 * In case of resizing
 *  1. the locks are not growing
 *  2. the whole set is locked
 */
public class SimpleConcurrentHashSet<T> implements SimpleSet<T> {

    AtomicInteger size;
    int capacity;
    final Lock[] locks;
    List<T>[] elements;

    @SuppressWarnings("unchecked")
    public SimpleConcurrentHashSet(int capacity) {
        this.capacity = capacity;
        this.size = new AtomicInteger();
        this.elements = new ArrayList[capacity];
        this.locks = new Lock[capacity];

        for (int i = 0; i < capacity; i++) {
            elements[i] = new ArrayList<T>();
            locks[i] = new ReentrantLock();
        }
    }

    @Override
    public boolean contains(T element) {
        int hash = Math.abs(element.hashCode() % capacity);
        try {
            locks[hash % locks.length].lock();
            return elements[hash].contains(element);
        } finally {
            locks[hash % locks.length].unlock();
        }
    }

    @Override
    public void add(T element) {
        int hash = Math.abs(element.hashCode() % capacity);

        if (elements[hash].contains(element)) {
            return;
        }

        locks[hash % locks.length].lock();
        try {
            elements[hash].add(element);
        } finally {
            locks[hash % locks.length].unlock();
        }
        if (size.getAndIncrement() > capacity / 2) {
            resize();
        }
    }

    @Override
    public boolean remove(T element) {
        int hash = Math.abs(element.hashCode() % capacity);
        locks[hash % locks.length].lock();
        try {
            boolean isRemoved = elements[hash].remove(element);
            if (isRemoved)
                size.getAndDecrement();
            return isRemoved;
        } finally {
            locks[hash % locks.length].unlock();
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(elements);
    }

    private void resize() {
        for (int i = 0; i < locks.length; i++) {
            locks[i].lock();
        }
        try {
            int newCapacity = capacity * 2;
            List<T>[] newElements = new ArrayList[capacity * 2];
            for (int i = 0; i < capacity; i++) {
                newElements[i] = elements[i];
            }
            for (int i = capacity; i < newCapacity; i++) {
                newElements[i] = new ArrayList<T>();
            }
            elements = newElements;
            capacity = newCapacity;
        } finally {
            for (int i = 0; i < locks.length; i++) {
                locks[i].lock();
            }
        }
    }
}
