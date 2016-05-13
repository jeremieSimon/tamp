package concurrency.set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import tamp.set.LazyResizableHashSet;
import tamp.set.SimpleHashSet;
import tamp.set.SimpleSet;

public class LazyResizableHashSetTest {

    SimpleSet<Integer> set;

    @Test
    public void testAdd() {
        set = new LazyResizableHashSet<Integer>(4);
        set.add(10);
        assertTrue(set.contains(10));
    }

    @Test
    public void testAddNegtive() {
        set = new LazyResizableHashSet<Integer>(4);
        set.add(-10);
        assertTrue(set.contains(-10));
    }

    @Test
    public void testContains() {
        set = new SimpleHashSet<Integer>(4);
        set.add(10);
        assertTrue(set.contains(10));
    }

    @Test
    public void testAddRemoveAndContains() {
        set = new LazyResizableHashSet<Integer>(4);
        set.add(10);
        set.remove(10);
        assertFalse(set.contains(10));
    }

    @Test
    public void testAddDuplicateRemoveAndContains() {
        set = new LazyResizableHashSet<Integer>(4);
        set.add(10);
        set.add(10);
        set.remove(10);
        assertFalse(set.contains(10));
    }

    @Test
    public void testRemoveWhenNotExists() {
        set = new LazyResizableHashSet<Integer>(4);
        set.remove(10);
        assertFalse(set.contains(10));
    }

    @Test
    public void testResize() throws InterruptedException {
        int capacity = 4;
        LazyResizableHashSet<Integer> set = new LazyResizableHashSet<>(capacity);
        set.add(0);
        set.add(1); //trigger resize

        Thread.sleep(100);
        assertEquals(8, set.capacity());
    }

    @Test
    public void testSeveralResize() throws InterruptedException {
        int capacity = 4;
        LazyResizableHashSet<Integer> set = new LazyResizableHashSet<>(capacity);
        set.add(0);
        set.add(1); //trigger resize

        set.add(2);
        set.add(3); // trigger another resize

        set.add(4);
        set.add(5);
        set.add(6);
        set.add(7); // trigger another resize

        Thread.sleep(1000); // sleep to make sure the async resize is done
        assertEquals(32, set.capacity());
    }
}
