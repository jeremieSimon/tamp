package tamp.queue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentQueueWSignal<T> {

    final ReentrantLock getLock;
    final ReentrantLock addLock;
    final Condition nonEmptyQueue;
    final Condition nonFullQueue;
    volatile SimpleNode<T> head;
    volatile SimpleNode<T> tail;
    final int capacity;
    AtomicInteger size;

    public ConcurrentQueueWSignal(int capacity) {
        this.capacity = capacity;
        head = new SimpleNode<T>(null); // head here is just as a dummy marker
        head.next = tail;
        size = new AtomicInteger();

        addLock = new ReentrantLock();
        nonFullQueue = addLock.newCondition();

        getLock = new ReentrantLock();
        nonEmptyQueue = getLock.newCondition();
    }

    public void add(T value) throws InterruptedException {

        while (size.get() == capacity) {
            addLock.lock();
            try {
                nonFullQueue.await(1, TimeUnit.SECONDS);
            } finally {
                addLock.unlock();
            }
        }

        SimpleNode<T> newNode = new SimpleNode<>(value);
        if (size.getAndIncrement() == 0) {
            tail = newNode;
            head.next = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }

        getLock.lock();
        try {
            nonEmptyQueue.signalAll();
        } finally {
            getLock.unlock();
        }
    }

    public T get() throws InterruptedException {
        while (size.get() == 0) {
            getLock.lock();
            try {
                nonEmptyQueue.await();
            } finally {
                getLock.unlock();
            }
        }

        addLock.lock();
        try {
            SimpleNode<T> getNode = head.next;
            head.next = getNode.next;
            size.getAndDecrement();
            nonFullQueue.signalAll();
            return getNode.value;
        } finally {
            addLock.unlock();
        }
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

