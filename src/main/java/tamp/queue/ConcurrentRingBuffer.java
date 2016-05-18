package tamp.queue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple bounded queue where each entry can be composed of a value and a lock.
 */
public class ConcurrentRingBuffer<T> implements SimpleQueue<T> {

    private final ReentrantLock[] locks;
    private final T[] elements;
    private final int capacity;
    private AtomicInteger getPos;
    private AtomicInteger insertPos;
    private AtomicInteger numberOfElements;

    public ConcurrentRingBuffer(final int capacity) {
        this.capacity = capacity;
        insertPos = new AtomicInteger(-1);
        getPos = new AtomicInteger(0);
        numberOfElements = new AtomicInteger(0);

        locks = new ReentrantLock[capacity];
        elements = (T[]) new Object[capacity];

        for (int i = 0; i < capacity; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    @Override
    public void add(final T element) {
        int currInsertPos = insertPos.get();
        if (numberOfElements.get() == capacity) {
            throw new RuntimeException(); // full
        }

        int insertIndex = (currInsertPos + 1) % capacity;
        if (!insertPos.compareAndSet(currInsertPos, insertIndex))
            add(element);

        locks[insertIndex].lock();
        try {
            elements[insertIndex] = element;
            numberOfElements.getAndIncrement();
        } finally {
            locks[insertIndex].unlock();
        }
    }

    @Override
    public T get() {
        if (numberOfElements.get() == 0) // either non-init of empty
            return null;

        int currGetPos = getPos.getAndIncrement() % capacity;
        locks[currGetPos].lock();
        try {
            T element = elements[currGetPos];
            numberOfElements.getAndDecrement();
            return element;
        } finally {
            locks[currGetPos].unlock();
        }
    }

    @Override
    public String toString() {
        int currNumElements = numberOfElements.get();
        if  (currNumElements == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        int currGetPos = getPos.get();
        int end = currGetPos + currNumElements;
        while (currGetPos < end) {
            sb.append(elements[currGetPos % capacity] + ", ");
            currGetPos++;
        }
        return sb.substring(0, sb.length() - 2).toString();
    }

}
