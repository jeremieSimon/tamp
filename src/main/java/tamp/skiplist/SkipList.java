package tamp.skiplist;

public interface SkipList<E> {

    public void add(E element);
    public void remove(E element);
    public boolean contains(E element);
}
