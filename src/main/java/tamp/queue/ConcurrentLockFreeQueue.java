package tamp.queue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentLockFreeQueue<T> {

    volatile SimpleNode<T> head;
    volatile AtomicReference<SimpleNode<T>> tail;
    final int capacity;
    AtomicInteger size;

    public ConcurrentLockFreeQueue(int capacity) {
        this.capacity = capacity;
        head = new SimpleNode<T>(null); // head here is just as a dummy marker
        head.next = new AtomicReference<SimpleNode<T>>();
        tail = new AtomicReference<ConcurrentLockFreeQueue.SimpleNode<T>>(head);
        size = new AtomicInteger();
    }

    public void add(T value) {
        if (size.get() == capacity) {
            throw new RuntimeException();
        }

        SimpleNode<T> newNode = new SimpleNode<>(value);

        while (true) {
            SimpleNode<T> tailRef = tail.get();
            SimpleNode<T> afterTail = tailRef.next.get();
            if (tailRef == tail.get()) {
                if (afterTail == null) {
                    if (tailRef.next.compareAndSet(afterTail, newNode)) {
                        tail.compareAndSet(tailRef, newNode);
                        size.getAndIncrement();
                        return;
                    }
                } else {
                    tail.compareAndSet(tailRef, afterTail);
                }
            }
        }

    }

    public T get() {
        /*if (size.getAndDecrement() == 0) {
            throw new RuntimeException();
        }
        SimpleNode<T> getNode = head.next;
        head.next = getNode.next;

        return getNode.value;
        */
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        SimpleNode<T> curr = head.next.get();
        while (curr != null) {
            sb.append(curr.value + " ");
            curr = curr.next.get();
        }
        return sb.toString();
    }

    public static class SimpleNode<T> {
        final T value;
        AtomicReference<SimpleNode<T>> next;

        public SimpleNode(T value) {
            this.value = value;
            this.next = new AtomicReference<ConcurrentLockFreeQueue.SimpleNode<T>>(null);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SimpleNode other = (SimpleNode) obj;
            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }

    }

    public static void main(String[] args) throws InterruptedException {
        ConcurrentLockFreeQueue<Integer> q = new ConcurrentLockFreeQueue<Integer>(4000);
        Filler f1 = new Filler(q, 1);
        Filler f2 = new Filler(q, 2);
        Filler f3 = new Filler(q, 3);
        Filler f4 = new Filler(q, 4);
        Filler f5 = new Filler(q, 5);
        Filler f6 = new Filler(q, 6);
        Filler f7 = new Filler(q, 7);
        Filler f8 = new Filler(q, 8);
        Filler f9 = new Filler(q, 9);
        Filler f10 = new Filler(q, 10);
        Filler f11 = new Filler(q, 11);
        Filler f12 = new Filler(q, 12);

        f1.start();
        f2.start();
        f3.start();
        f4.start();
        f5.start();
        f6.start();
        f7.start();
        f8.start();
        f9.start();
        f10.start();
        f11.start();
        f12.start();

        Thread.sleep(500);
        System.out.println(q.size.get());
        System.out.println(q);
    }

    public static class Filler extends Thread {

        final int n;
        final ConcurrentLockFreeQueue<Integer> q;
        final int maxEnq = 50;
        public Filler(ConcurrentLockFreeQueue<Integer> q,
            int n) {
            this.q = q;
            this.n = n;
        }

        @Override
        public void run() {
            int i = 0;
            while (i < maxEnq) {
                i++;
                q.add(n);
            }
        }
    }

    public static class Getter extends Thread {

        final ConcurrentLockFreeQueue<Integer> q;

        public Getter(ConcurrentLockFreeQueue<Integer> q) {
            this.q = q;
        }

        @Override
        public void run() {
                q.get();
                System.out.println("Thread " + Thread.currentThread().getName()
                + "getting i");

        }
    }
}
