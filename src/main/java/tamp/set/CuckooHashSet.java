package tamp.set;

import java.util.Arrays;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Example of a simple hashSet using the open addressing technique of the Cuckoo hasing.
 */
public class CuckooHashSet<T> implements SimpleSet<T> {

    private static final int MAX_TRIES = 4;
    private static final HashFunction H0 = Hashing.md5();
    private static final HashFunction H1 = Hashing.adler32();

    private T table0[];
    private T table1[];
    private int size;

    public CuckooHashSet(int size) {
        this.size = size;
        table0 = (T[]) new Object[size];
        table1 = (T[]) new Object[size];
    }

    @Override
    public void add(T element) {
        if (contains(element))
            return;
        T swapped = null;
        for (int i = 0; i < MAX_TRIES; i++) {
            swapped = swap((swapped == null ? element: swapped));
            if (swapped == null) {
                return;
            }
        }
        resize();
        add(element);
    }

    @Override
    public boolean contains(T element) {
        int hash0 = Math.abs(H0.newHasher()
            .putInt(element.hashCode())
            .hash()
            .asInt()) % size;
        int hash1 = Math.abs(H1.newHasher()
            .putInt(element.hashCode())
            .hash()
            .asInt()) % size;
        return element.equals(table0[hash0])|| element.equals(table1[hash1]);
    }

    @Override
    public boolean remove(T element) {

        int hash0 = Math.abs(H0.newHasher()
        .putInt(element.hashCode())
        .hash()
        .asInt()) % size;
        int hash1 = Math.abs(H1.newHasher()
            .putInt(element.hashCode())
            .hash()
            .asInt()) % size;
        if (element.equals(table0[hash0])) {
                table0[hash0] = null;
                return true;
        } else if (element.equals(table1[hash1])) {
                table1[hash1] = null;
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return Arrays.toString(table0)
            + "\n" + Arrays.toString(table1);
    }

    private void resize() {
        int newSize = size * 2;
        T newTable0[] = (T[]) new Object[newSize];
        T newTable1[] = (T[]) new Object[newSize];
        for (int i = 0; i < size; i++) {
            newTable0[i] = table0[i];
            newTable1[i] = table1[i];
        }
        table0 = newTable0;
        table1 = newTable1;
        size = newSize;
    }

    private T swap(T element) {

        int hash = Math.abs(H0.newHasher()
            .putInt(element.hashCode())
            .hash()
            .asInt()) % size;

        T moved = table0[hash];
        table0[hash] = element;

        if (moved == null) {
            return null;
        } else {
            int movedHash = Math.abs(H1.newHasher()
                .putInt(moved.hashCode())
                .hash()
                .asInt()) % size;
            T evicted = table1[movedHash];
            table1[movedHash] = moved;
            return evicted;
        }
    }
}
