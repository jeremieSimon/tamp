package tamp.set;

public interface SimpleSet<T> {

    public void add(T element);

    public boolean remove(T element);

    public boolean contains(T element);
}
