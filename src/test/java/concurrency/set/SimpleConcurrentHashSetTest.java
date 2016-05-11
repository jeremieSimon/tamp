package concurrency.set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import tamp.set.SimpleConcurrentHashSet;

public class SimpleConcurrentHashSetTest {

    SimpleConcurrentHashSet<Integer> set;

    @Test
    public void testAdd() {
        set = new SimpleConcurrentHashSet<Integer>(4);
        set.add(10);
        assertTrue(set.contains(10));
    }

    @Test
    public void testAddNegtive() {
        set = new SimpleConcurrentHashSet<Integer>(4);
        set.add(-10);
        assertTrue(set.contains(-10));
    }

    @Test
    public void testContains() {
        set = new SimpleConcurrentHashSet<Integer>(4);
        set.add(10);
        assertTrue(set.contains(10));
    }

    @Test
    public void testAddRemoveAndContains() {
        set = new SimpleConcurrentHashSet<Integer>(4);
        set.add(10);
        set.remove(10);
        assertFalse(set.contains(10));
    }

    @Test
    public void testAddDuplicateRemoveAndContains() {
        set = new SimpleConcurrentHashSet<Integer>(4);
        set.add(10);
        set.add(10);
        set.remove(10);
        assertFalse(set.contains(10));
    }

    @Test
    public void testRemoveWhenNotExists() {
        set = new SimpleConcurrentHashSet<Integer>(4);
        set.remove(10);
        assertFalse(set.contains(10));
    }

    @Test
    public void testForceResizeThenCheckContains() {

    }

}
