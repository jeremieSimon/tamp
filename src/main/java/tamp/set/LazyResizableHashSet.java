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
 */
public class LazyResizableHashSet<T> implements SimpleSet<T> {

    final Lock[] locks;
    volatile List<T>[] elements;
    volatile List<T>[] newElements;
    AtomicBoolean resizing;
    AtomicInteger size; // current number of elements

    public LazyResizableHashSet(int capacity) {
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
        int hash = Math.abs(element.hashCode() % elements.length);
        int newHash = Math.abs(element.hashCode() % (elements.length * 2));

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
        int hash = Math.abs(element.hashCode() % elements.length);
        int newHash = Math.abs(element.hashCode() % (elements.length * 2));

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

        if (size.getAndIncrement() > elements.length / 2) {
            resize(elements.length);
        }
    }

    /**
     * On a resize needs to lock both elements and newElements
     */
    @Override
    public boolean remove(T element) {
        int hash = Math.abs(element.hashCode() % elements.length);
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

    /**
     * move all elements from old table to new table asynchronously.
     */
    private void resize(final int currentCapacity) {
        if (resizing.get()) // someone is already resizing
            return;

        ExecutorService service = Executors.newFixedThreadPool(1);
        try {
            service.submit(resizer(currentCapacity));
        } finally {
            service.shutdown();
        }
    }

    private Runnable resizer(final int currentCapacity) {
        return new Runnable() {
            @Override
            public void run() {
                System.out.println("starting the resize");

                //someone is already resizing or someone already just resized
                if (!resizing.compareAndSet(false, true)
                    || currentCapacity != elements.length) {
                    return;
                }
                //create the new elements table
                int newCapacity = elements.length * 2;
                List<T>[] newElements = new ArrayList[elements.length * 2];
                for (int i = 0; i < newCapacity; i++)
                    newElements[i] = new ArrayList<T>();

                // add elements from old table to new table
                for (int i = 0; i < elements.length; i++) {
                    List<T> bucket = elements[i];
                    for (T element: bucket) {
                        int hash = Math.abs(element.hashCode()) % newElements.length;
                        acquire(element);
                        try {
                            newElements[hash].add(element);
                        } finally {
                            release(element);
                        }
                    }
                }

                //replace old table by new table
                //replace old size by new size
                System.out.println("done resizing");
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
    private void acquire(T element) {
        int hash = Math.abs(element.hashCode() % elements.length);
        int newHash = Math.abs(element.hashCode() % (elements.length * 2));

        if (resizing.get()) {
            locks[newHash % locks.length].lock();
        }
        locks[hash % locks.length].lock();
    }

    /**
     * if no resize on going then only need a lock one lock because the element can only be in one place.
     * Else, need both need a lock on the old table and the new table.
     * @param element
     */
    private void release(T element) {
        int hash = Math.abs(element.hashCode() % elements.length);
        int newHash = Math.abs(element.hashCode() % (elements.length * 2));

        if (resizing.get()) {
            locks[newHash % locks.length].unlock();
        }
        locks[hash % locks.length].unlock();
    }
}
