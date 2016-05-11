package concurrent.set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * On resize start using 2 tables an old one and a new one.
 * all add operations happen on the new one
 * contains and remove happen on both.
 * In the back a thread will move element bucket by bucket from the old table to the new table.
 */
public class LazyResizableHashSet<T> implements SimpleSet<T> {

    final Lock[] locks;
    List<T>[] elements;
    List<T>[] newElements;
    AtomicBoolean resizing;
    AtomicInteger size; // current number of elements
    int capacity; // max number of elements that can be contained

    public LazyResizableHashSet(int capacity) {
        this.capacity = capacity;
        this.size = new AtomicInteger();
        this.resizing = new AtomicBoolean(false);
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
        int newHash = Math.abs(element.hashCode() % (capacity * 2));

        try {
            acquire(element);
            if (!resizing.get())
                return elements[hash].contains(element);
            return elements[hash].contains(element)
                || newElements[newHash].contains(element);
        } finally {
            release(element);
        }
    }

    /**
     * On a resize only need to lock the newElements
     */
    @Override
    public void add(T element) {
        int hash = Math.abs(element.hashCode() % capacity);
        int newHash = Math.abs(element.hashCode() % (capacity * 2));

        if (contains(element)) {
            return;
        }

        acquire(element);
        try {
            if (!resizing.get()) {
                elements[hash].add(element);
            } else {
                newElements[newHash].add(element);
            }
        } finally {
            release(element);
        }

        if (size.getAndIncrement() > capacity / 2) {
            resize();
        }
    }

    /**
     * On a resize needs to lock both elements and newElements
     */
    @Override
    public boolean remove(T element) {
        int hash = Math.abs(element.hashCode() % capacity);
        acquire(element);
        try {
            boolean isRemoved = elements[hash].remove(element);
            if (isRemoved)
                size.getAndDecrement();
            return isRemoved;
        } finally {
            release(element);
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(elements);
    }

    private void resize() {
        if (resizing.get()) // someone is already resizing
            return;

        //TODO
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

    /**
     * if no resize on going then only need a lock one lock because the element can only be in one place
     * @param element
     */
    private void acquire(T element) {
        int hash = Math.abs(element.hashCode() % capacity);
        int newHash = Math.abs(element.hashCode() % (capacity * 2));

        if (resizing.get())
            locks[newHash % locks.length].lock();
        else {
            locks[hash % locks.length].lock();
        }
    }

    private void release(T element) {
        int hash = Math.abs(element.hashCode() % capacity);
        int newHash = Math.abs(element.hashCode() % (capacity * 2));

        if (resizing.get())
            locks[newHash % locks.length].unlock();
        else
            locks[hash % locks.length].unlock();
    }
}
