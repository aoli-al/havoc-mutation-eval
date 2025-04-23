package edu.neu.ccs.prl.zeugma.internal.runtime.struct;

public class SimpleSet<E> {
    private final ObjectIntMap<E> backingMap = new ObjectIntMap<>();

    public boolean add(E element) {
        if (backingMap.containsKey(element)) {
            return false;
        } else {
            backingMap.put(element, 0);
            return true;
        }
    }

    public ObjectIntMap<E> getBackingMap() {
        return backingMap;
    }

    public boolean contains(Object object) {
        return backingMap.containsKey(object);
    }

    public boolean isEmpty() {
        return backingMap.isEmpty();
    }

    public int size() {
        return backingMap.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof SimpleSet)) {
            return false;
        }
        SimpleSet<?> other = (SimpleSet<?>) o;
        return backingMap.equals(other.backingMap);
    }

    @Override
    public int hashCode() {
        return backingMap.hashCode();
    }
}
