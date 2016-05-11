package concurrent.queue;

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

    public static void main(String[] args) throws InterruptedException {
        ConcurrentQueueWSignal<Integer> q = new ConcurrentQueueWSignal<Integer>(10);
        Filler f = new Filler(q, 1);
        Filler f2 = new Filler(q, 2);
        Getter g1 = new Getter(q);
        Getter g2 = new Getter(q);
        g2.start();
        f.start();
        f2.start();
        g1.start();
        Thread.sleep(10000);
        System.out.println(q);
        System.out.println("size " + q.size.get());

    }

    public static class Filler extends Thread {

        final ConcurrentQueueWSignal<Integer> q;
        final int i;

        public Filler(ConcurrentQueueWSignal<Integer> q,
            int i) {
            this.q = q;
            this.i = i;
        }

        @Override
        public void run() {
            try {
                int n = 0;
                while (n < 10) {
                    n++;
                    q.add(i);
                    System.out.println(Thread.currentThread().getName() + " "
                        + q + " size " + q.size.get());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Getter extends Thread {

        final ConcurrentQueueWSignal<Integer> q;

        public Getter(ConcurrentQueueWSignal<Integer> q) {
            this.q = q;
        }

        @Override
        public void run() {
            try {
                int n = 0;
                while (n < 10) {
                    int i = q.get();
                    System.out.println("Thread " + Thread.currentThread().getName()
                    + "getting " + i);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

