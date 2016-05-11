package concurrency.set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import concurrent.set.CuckooHashSet;

public class CuckooHashSetTest {

    CuckooHashSet<Integer> set;

    @Test
    public void testAdd() {
        set = new CuckooHashSet<Integer>(4);
        set.add(10);
        assertTrue(set.contains(10));
    }

    @Test
    public void testAddNegtive() {
        set = new CuckooHashSet<Integer>(4);
        set.add(-10);
        assertTrue(set.contains(-10));
    }

    @Test
    public void testContains() {
        set = new CuckooHashSet<Integer>(4);
        set.add(10);
        assertTrue(set.contains(10));
    }

    @Test
    public void testAddRemoveAndContains() {
        set = new CuckooHashSet<Integer>(4);
        set.add(10);
        set.remove(10);
        assertFalse(set.contains(10));
    }

    @Test
    public void testAddDuplicateRemoveAndContains() {
        set = new CuckooHashSet<Integer>(4);
        set.add(10);
        set.add(10);
        set.remove(10);
        assertFalse(set.contains(10));
    }

    @Test
    public void testRemoveWhenNotExists() {
        set = new CuckooHashSet<Integer>(4);
        set.remove(10);
        assertFalse(set.contains(10));
    }

    @Test
    public void testForceResizeThenCheckContains() {

    }


}
