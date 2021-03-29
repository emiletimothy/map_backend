package edu.caltech.cs2.datastructures;

import edu.caltech.cs2.interfaces.IDictionary;
import edu.caltech.cs2.interfaces.IPriorityQueue;

import java.util.Iterator;

public class MinFourHeap<E> implements IPriorityQueue<E> {

    private static final int DEFAULT_CAPACITY = 10;

    private int size;
    private PQElement<E>[] data;
    private IDictionary<E, Integer> keyToIndexMap;

    /**
     * Creates a new empty heap with DEFAULT_CAPACITY.
     */
    public MinFourHeap() {
        this.size = 0;
        this.data = new PQElement[DEFAULT_CAPACITY];
        this.keyToIndexMap = new ChainingHashDictionary<>(MoveToFrontDictionary::new);
    }

    private void percolateUp(PQElement<E> key, int index) {
        while (key.priority < this.data[(index - 1)/4].priority) {
            int parent_index = (index - 1) / 4;
            this.keyToIndexMap.put(key.data, parent_index);
            this.keyToIndexMap.put(this.data[parent_index].data, index);
            PQElement<E> parent_data = this.data[parent_index];
            this.data[parent_index] = this.data[index];
            this.data[index] = parent_data;
            index = this.keyToIndexMap.get(key.data);
        }
    }

    private int get_smallest_index(int index) {
        double min_counter = this.data[index].priority;
        int val = index;
        for (int i = 1; i <= 4; i++) {
            if (4 * index + i >= this.size) {
                break;
            }
            double child = this.data[4 * index + i].priority;
            if (min_counter > child) {
                min_counter = child;
                val = 4 * index + i;
            }
        }
        return val;
    }

    private void percolateDown(PQElement<E> key, int index) {
        while (key.priority > this.data[get_smallest_index(index)].priority) {
            int small = get_smallest_index(index);
            this.keyToIndexMap.put(key.data, small);
            this.keyToIndexMap.put(this.data[small].data, index);
            PQElement<E> temp = this.data[index];
            this.data[index] = this.data[small];
            this.data[small] = temp;
            index = this.keyToIndexMap.get(key.data);
        }
    }

    @Override
    public void increaseKey(PQElement<E> key) {
        if (!this.keyToIndexMap.containsKey(key.data)) {
            throw new IllegalArgumentException("Key is not in loop.");
        }
        this.data[this.keyToIndexMap.get(key.data)] = key;
        percolateDown(key, this.keyToIndexMap.get(key.data));
    }

    @Override
    public void decreaseKey(PQElement<E> key) {
        if (!this.keyToIndexMap.containsKey(key.data)) {
            throw new IllegalArgumentException("Key is not in loop.");
        }
        this.data[this.keyToIndexMap.get(key.data)] = key;
        this.percolateUp(key, this.keyToIndexMap.get(key.data));
    }

    private void resize() {
        PQElement<E>[] new_array = new PQElement[this.data.length * 2];
        for (int i = 0; i < this.data.length; i++) {
            new_array[i] = this.data[i];
        }
        this.data = new_array;
    }


    @Override
    public boolean enqueue(PQElement<E> pqElement) {
        if (pqElement.data == null) {
            return false;
        }
        if (this.keyToIndexMap.containsKey(pqElement.data)) {
            throw new IllegalArgumentException("Key is already present. ");
        }
        if (this.size >= this.data.length) {
            resize();
        }
        this.data[this.size] = pqElement;
        this.keyToIndexMap.put(pqElement.data, this.size);
        this.size++;
        percolateUp(this.data[this.size - 1], this.size - 1);
        return true;
    }

    @Override
    public PQElement<E> dequeue() {
        if (this.size == 0) {
            return null;
        }
        PQElement element = this.data[0];
        this.keyToIndexMap.remove(this.data[0].data);
        this.data[0] = this.data[this.size - 1];
        this.data[this.size - 1] = null;
        this.size--;
        if (this.size != 0) {
            this.keyToIndexMap.put(this.data[0].data, 0);
            percolateDown(this.data[0], 0);
        }
        return element;
    }

    @Override
    public PQElement<E> peek() {
        return this.data[0];
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public Iterator<PQElement<E>> iterator() {
        return null;
    }
}