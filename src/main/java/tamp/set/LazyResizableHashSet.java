package tamp.set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * On resize start using 2 tables an old one and a new one.
 * all add operations happen on the new one
 * contains and remove happen on both.
 * In the back a thread will move element bucket by bucket from the old table to the new table.
 * The idea here is not to have some kind of stop the world when resizing.
 */
public class LazyResizableHashSet<T> implements SimpleSet<T> {

    final Lock[] locks;
    volatile List<T>[] elements;
    volatile List<T>[] newElements;
    AtomicBoolean resizing;
    AtomicInteger size; // current number of elements
    final ExecutorService service;

    public LazyResizableHashSet(int capacity) {
        this.size = new AtomicInteger();
        this.resizing = new AtomicBoolean(false);
        this.elements = new ArrayList[capacity];
        this.locks = new Lock[capacity];
        this.service = Executors.newFixedThreadPool(5);
        for (int i = 0; i < capacity; i++) {
            elements[i] = new ArrayList<T>();
            locks[i] = new ReentrantLock();
        }
    }

    /**
     * On a resize we need to check both, the old table and the new table
     * So we need to lock both the old table and the new table.
     */
    @Override
    public boolean contains(final T element) {
        int hash = Math.abs(element.hashCode() % elements.length);
        int newHash = Math.abs(element.hashCode() % (elements.length * 2));

        boolean lockBothTable = acquire(element);
        try {
            if (!resizing.get())
                return elements[hash].contains(element);
            return elements[hash].contains(element)
                || newElements[newHash].contains(element);
        } finally {
            release(element, lockBothTable);
        }
    }

    /**
     * On a resize only need to add the element in the new table
     * so on a resize we only need to lock the new table.
     */
    @Override
    public void add(T element) {
        if (contains(element)) {
            return;
        }

        int hash = Math.abs(element.hashCode() % elements.length);
        int newHash = Math.abs(element.hashCode() % (elements.length * 2));

        boolean lockBothTable = acquire(element);
        try {
            if (!resizing.get()) {
                elements[hash].add(element);
            } else {
                newElements[newHash].add(element);
            }
        } finally {
            release(element, lockBothTable);
        }

        if (size.addAndGet(1) >= elements.length / 2) {
            resize(elements.length);
        }
    }

    public int capacity() {
        return elements.length;
    }

    public int size() {
        return size.get();
    }

    /**
     * On a resize needs to potentially remove from both tables
     * the old one and the new one
     */
    @Override
    public boolean remove(T element) {
        int hash = Math.abs(element.hashCode() % elements.length);
        boolean lockBothTable = acquire(element);
        try {
            boolean isRemoved = false;
            if (lockBothTable) {
                isRemoved = newElements[hash].remove(element);
            }
             isRemoved |= elements[hash].remove(element);
            if (isRemoved)
                size.getAndDecrement();
            return isRemoved;
        } finally {
            release(element, lockBothTable);
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(elements);
    }

    /**
     * move all elements from old table to new table asynchronously.
     */
    private void resize(final int currentCapacity) {
        if (resizing.get()) // someone is already resizing
            return;
        service.submit(resizer(currentCapacity));
    }

    @Override
    protected void finalize() {
        service.shutdown();
    }

    private Runnable resizer(final int currentCapacity) {
        return new Runnable() {
            @Override
            public void run() {

                //someone is already resizing or a resize was initiated but another finished in the meantime
                if (resizing.get()
                    || currentCapacity != elements.length) {
                    return;
                }

                //create the new elements table
                int newCapacity = elements.length * 2;
                newElements = new ArrayList[elements.length * 2];
                for (int i = 0; i < newCapacity; i++)
                    newElements[i] = new ArrayList<T>();

                if (!resizing.compareAndSet(false, true))
                    return;

                // add elements from old table to new table
                for (int i = 0; i < elements.length; i++) {
                    List<T> bucket = elements[i];
                    for (T element: bucket) {
                        int hash = Math.abs(element.hashCode()) % newElements.length;
                        acquire(element);
                        try {
                            newElements[hash].add(element);
                        } finally {
                            release(element, true);
                        }
                    }
                }

                //replace old table by new table
                //replace old size by new size
                elements = newElements;
                resizing.set(false);
                return;
            }
        };
    }

    /**
     * if no resize on going then only need a lock one lock because the element can only be in one place.
     * Else, need both need a lock on the old table and the new table.
     * @param element
     */
    private boolean acquire(T element) {
        int hash = Math.abs(element.hashCode() % elements.length);
        int newHash = Math.abs(element.hashCode() % (elements.length * 2));
        boolean lockBothTable = false;
        if (resizing.get()) {
            locks[newHash % locks.length].lock();
            lockBothTable = true;
        }
        locks[hash % locks.length].lock();
        return lockBothTable;
    }

    /**
     * if no resize on going then only need a lock one lock because the element can only be in one place.
     * Else, need both need a lock on the old table and the new table.
     * @param element
     */
    private void release(T element, boolean unlockBothTable) {
        int hash = Math.abs(element.hashCode() % elements.length);
        int newHash = Math.abs(element.hashCode() % (elements.length * 2));

        if (unlockBothTable) {
            locks[newHash % locks.length].unlock();
        }
        locks[hash % locks.length].unlock();
    }
}
