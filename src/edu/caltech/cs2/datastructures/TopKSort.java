package edu.caltech.cs2.datastructures;
import edu.caltech.cs2.datastructures.MinFourHeap;
import edu.caltech.cs2.interfaces.IPriorityQueue;

public class TopKSort {
    /**
     * Sorts the largest K elements in the array in descending order. Modifies the array in place.
     * @param array - the array to be sorted; will be manipulated.
     * @param K - the number of values to sort
     * @param <E> - the type of values in the array
     * @throws IllegalArgumentException if K < 0
     */
    public static <E> void sort(IPriorityQueue.PQElement<E>[] array, int K) {
        if (K < 0) {
            throw new IllegalArgumentException("K cannot be negative!");
        }
        MinFourHeap heap = new MinFourHeap();
        if (array.length > 0) {
            if (K > 0 && K <= array.length) {
                for (int i = 0; i < K; i++) {
                    heap.enqueue(array[i]);
                }
                for (int j = K; j < array.length; j++) {
                    IPriorityQueue.PQElement element = heap.peek();
                    if (element.priority < array[j].priority) {
                        heap.dequeue();
                        heap.enqueue(array[j]);
                    }
                }
                for (int i = K - 1; i >= 0; i--) {
                    array[i] = heap.dequeue();
                }
                for (int j = K; j < array.length; j++) {
                    array[j] = null;
                }
            } else {
                for (int i = 0; i < array.length; i++) {
                    array[i] = null;
                }
            }
        }
    }
}
