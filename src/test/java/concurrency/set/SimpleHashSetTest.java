package concurrency.set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import concurrent.set.SimpleHashSet;

public class SimpleHashSetTest {

    SimpleHashSet<Integer> set;

    @Test
    public void testAdd() {
        set = new SimpleHashSet<Integer>(4);
        set.add(10);
        assertTrue(set.contains(10));
    }

    @Test
    public void testAddNegtive() {
        set = new SimpleHashSet<Integer>(4);
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
        set = new SimpleHashSet<Integer>(4);
        set.add(10);
        set.remove(10);
        assertFalse(set.contains(10));
    }

    @Test
    public void testAddDuplicateRemoveAndContains() {
        set = new SimpleHashSet<Integer>(4);
        set.add(10);
        set.add(10);
        set.remove(10);
        assertFalse(set.contains(10));
    }

    @Test
    public void testRemoveWhenNotExists() {
        set = new SimpleHashSet<Integer>(4);
        set.remove(10);
        assertFalse(set.contains(10));
    }

    @Test
    public void testForceResizeThenCheckContains() {

    }

}
