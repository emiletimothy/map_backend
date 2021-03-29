package edu.caltech.cs2.datastructures;

import edu.caltech.cs2.interfaces.IDeque;
import edu.caltech.cs2.interfaces.IQueue;
import edu.caltech.cs2.interfaces.IStack;

import java.util.Iterator;

public class ArrayDeque<E> implements IDeque<E>, IQueue<E>, IStack<E> {
    private E[] data;
    private static final int default_capacity = 10;
    private int size;
    private static final int grow_factor = 2;

    public ArrayDeque() {
        this(default_capacity);
    }

    public ArrayDeque(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException();
        }
        this.data = (E[]) new Object[initialCapacity];
        this.size = 0;
    }

    private void resize() {
        if (this.size == data.length) {
            E[] newData = (E[]) new Object[data.length * grow_factor];
            if (this.size > 0) {
                System.arraycopy(this.data, 0, newData, 0, this.size);
            }
            this.data = newData;
        }
    }

    public String toString() {
        if (this.size == 0) {
            return "[]";
        }
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < this.size; i++) {
            result.append(this.data[i]).append(", ");
        }
        result = new StringBuilder(result.substring(0, result.length() - 2));
        return (result + "]");
    }

    public void addFront(E e) {
        resize();
        if (this.size > 0) {
            System.arraycopy(this.data, 0, this.data, 1, this.size);
        }
        this.data[0] = e;
        this.size++;
    }

    public void addBack(E e) {
        resize();
        this.data[this.size] = e;
        this.size++;
    }

    public E removeFront() {
        if (this.size == 0) {
            return null;
        }
        E value = this.data[0];
        if (this.size > 0) {
            System.arraycopy(this.data, 1, this.data, 0, this.size - 1);
        }
        this.size--;
        return value;
    }

    public E removeBack() {
        if (this.size == 0) {
            return null;
        }
        E value = this.data[this.size - 1];
        this.size--;
        return value;
    }

    public boolean enqueue(E e) {
        int old_size = this.size;
        addFront(e);
        return old_size < this.size;
    }

    public E dequeue() {
        return removeBack();
    }

    public boolean push(E e) {
        int old_size = this.size;
        addBack(e);
        return old_size < this.size;
    }

    public E pop() {
        if (this.size == 0) {
            return null;
        }
        return removeBack();
    }

    public E peek() {
        if (this.size == 0) {
            return null;
        }
        return this.data[this.size - 1];
    }

    public E peekFront() {
        if (this.size == 0) {
            return null;
        }
        return this.data[0];
    }


    public E peekBack() {
        return peek();
    }

    public int size() {
        return this.size;
    }

    public Iterator<E> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<E> {
        private int currentIndex;

        public ArrayDequeIterator() {
            this.currentIndex = 0;
        }

        public boolean hasNext() {
            return this.currentIndex < (ArrayDeque.this).size();
        }

        public E next() {
            E element = (ArrayDeque.this.data[this.currentIndex]);
            this.currentIndex++;
            return element;
        }
    }
}
