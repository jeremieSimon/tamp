package tamp.skiplist;

public class SimpleSkipList<E extends Comparable<E>> implements SkipList<E> {

    private final int numberOfLevel;
    private int numberOfElements;
    private final SkipListNode<E> head;
    private final SkipListNode<E> tail;

    public SimpleSkipList(final int numberOfLevel) {
        this.numberOfLevel = numberOfLevel;
        numberOfElements = 0;
        head = new SkipListNode<E>(null, new SkipListNode[numberOfLevel]);
        tail = new SkipListNode<E>(null, new SkipListNode[numberOfLevel]);
        for (int i = 0; i < numberOfLevel; i++) {
            head.succs[i] = tail;
        }
    }

    @Override
    public void add(final E element) {
        int numberOfLevels = generateNumberOfLevels();

        // case 1
        if (numberOfElements == 0) {
            SkipListNode<E>[] succs = new SkipListNode[numberOfLevel];
            for (int i = 0; i < numberOfLevel; i++) {
                succs[i] = tail;
            }
            SkipListNode<E> node = new SkipListNode<>(element, succs);
            for (int i = 0; i < numberOfLevels + 1; i++) {
                head.succs[i] = node;
            }
            numberOfElements++;
            return;
        }

        // case 2
        SkipListNode<E>[] preds = new SkipListNode[numberOfLevel];
        SkipListNode<E>[] succs = new SkipListNode[numberOfLevel];
        SkipListNode<E> currNode = head;
        for (int i = numberOfLevel - 1; i >= 0; i--) {
            while (true) {
                if (currNode.succs[i] == tail || currNode.succs[i].value.compareTo(element) > 0) {
                    preds[i] = currNode;
                    succs[i] = currNode.succs[i];
                    break;
                } else if (currNode.succs[i].value.compareTo(element) == 0) {
                    return; //already exists
                }
                currNode = currNode.succs[i];
            }
        }
        SkipListNode<E> node = new SkipListNode<>(element, succs);
        for (int i = 0; i < numberOfLevels + 1; i++) {
            preds[i].succs[i] = node;
        }
        numberOfElements++;
    }

    @Override
    public boolean remove(final E element) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean contains(final E element) {
        SkipListNode<E> currNode = head;
        for (int i = numberOfLevel - 1; i >= 0; i--) {
            while (true) {
                if (currNode.succs[i] == tail || currNode.succs[i].value.compareTo(element) > 0) {
                    break;
                } else if (currNode.succs[i].value.compareTo(element) == 0) {
                    return true;
                }
                currNode = currNode.succs[i];
            }
        }
        return false;
    }

    public int size() {
        return numberOfElements;
    }

    @Override
    public String toString() {
        if (numberOfElements == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        SkipListNode<E> node = head.succs[0];
        while (node.value != null) {
            sb.append(node.value + ", ");
            node = node.succs[0];
        }
        return sb.toString();
    }

    private int generateNumberOfLevels() {
        int level = 0;
        while (level < (numberOfLevel - 1)) {
            if (Math.random() < 0.5) break;
            level++;
        }
        return level;
    }

    static class SkipListNode<E> {
        final E value;
        final SkipListNode<E>[] succs;

        SkipListNode(final E value) {
            this(value, null);
        }

        SkipListNode(final E value,
            final SkipListNode<E>[] succs) {
            this.value = value;
            this.succs = succs;
        }

        @Override
        public String toString() {
            return value == null ? "null" : value.toString();
        }
    }
}
