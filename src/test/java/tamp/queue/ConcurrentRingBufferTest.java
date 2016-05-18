package tamp.queue;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ConcurrentRingBufferTest {

    ConcurrentRingBuffer<Integer> ringBuffer;

    @Before
    public void setup() {
        ringBuffer = new ConcurrentRingBuffer<Integer>(4);
    }

    @Test
    public void testGetOnEmpty() {
        Integer e = ringBuffer.get();
        assertEquals(null, e);
    }

    @Test
    public void testGet() {
        ringBuffer.add(1);
        assertEquals(new Integer(1), ringBuffer.get());
    }

    @Test
    public void testAdd() {
        ringBuffer.add(1);
    }

    @Test
    public void testAddToFullCapacity() {
        ringBuffer.add(1);
        ringBuffer.add(2);
        ringBuffer.add(3);
        ringBuffer.add(4);
    }

    @Test (expected = RuntimeException.class)
    public void testAddWhenFull() {
        ringBuffer.add(1);
        ringBuffer.add(2);
        ringBuffer.add(3);
        ringBuffer.add(4);
        ringBuffer.add(5);
    }

    @Test
    public void testWhenMoreAddThanCapacity() {
        ringBuffer.add(1);
        ringBuffer.add(2);
        ringBuffer.add(3);
        ringBuffer.add(4);

        ringBuffer.get();
        ringBuffer.get();
        ringBuffer.add(5);
        ringBuffer.add(6);
        System.out.println(ringBuffer);
    }
}
