package edu.neu.ccs.prl.zeugma.internal.runtime.struct;

import java.util.NoSuchElementException;

public class Queue<E> {
    private Node<E> head, tail = null;
    private int size = 0;

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public E dequeue() {
        if (head == null) {
            throw new NoSuchElementException();
        }
        E result = head.value;
        head = head.next;
        size--;
        if (head == null) {
            // Removed the last element
            tail = null;
        }
        return result;
    }

    public E peek() {
        if (head == null) {
            throw new NoSuchElementException();
        }
        return head.value;
    }

    public void enqueue(E element) {
        Node<E> node = new Node<>(element, null);
        size++;
        if (tail == null) {
            // Adding first element
            head = tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
    }

    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private Node<E> current = head;

            @Override
            public E next() {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                E result = current.value;
                current = current.next;
                return result;
            }

            @Override
            public boolean hasNext() {
                return current != null;
            }
        };
    }

    private static final class Node<E> {
        private final E value;
        private Node<E> next;

        private Node(E value, Node<E> next) {
            this.value = value;
            this.next = next;
        }
    }
}
