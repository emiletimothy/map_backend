package edu.caltech.cs2.datastructures;

import edu.caltech.cs2.interfaces.ICollection;
import edu.caltech.cs2.interfaces.IDictionary;

import java.util.Iterator;

public class MoveToFrontDictionary<K, V> implements IDictionary<K, V> {

    private Node<K, V> head; // head
    private int size; // size

    public MoveToFrontDictionary(){
        head = null;
        size = 0;
    }

    private static class Node<K, V> {
        public final K key;
        public final V value;
        public Node<K, V> next;

        public Node(K key, V value) {
            this(key, value, null);
        }

        public Node(K key, V value, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    public V remove(K key) {
        if (this.size == 0) {
            return null;
        }
        if (this.head.key.equals(key)) {
            V value = this.head.value;
            this.head = this.head.next;
            this.size--;
            return value;
        }
        Node<K, V> previous_node = this.head;
        Node<K, V> current_node = this.head;
        for (int i = 1; i < this.size(); i++) {
            current_node = current_node.next;
            if (current_node.key.equals(key)) {
                V value = current_node.value;
                previous_node.next = current_node;
                this.size--;
                return value;
            }
            previous_node = previous_node.next;
        }
        return null;
    }

    public V put(K key, V value) {
        if (this.head == null) {
            this.head = new Node<>(key, value);
        }
        else {
            Node<K, V> new_node = new Node<>(key, value);
            new_node.next = this.head;
            this.head = new_node;
        }
        this.size++;
        Node<K, V> current_node = this.head;
        Node<K, V> previous_node = this.head;
        for (int i = 1; i < this.size(); i++) {
            current_node = current_node.next;
            if (current_node.key.equals(key)) {
                previous_node.next = current_node.next;
                this.size--;
                return current_node.value;
            }
            previous_node = previous_node.next;
        }
        return null;
    }

    public boolean containsKey(K key) {
        return this.get(key) != null;
    }

    public boolean containsValue(V value) {

        return this.values().contains(value);
    }

    public int size() {
        return this.size;
    }

    public ICollection<K> keys() {
        Node<K, V> current_node = this.head;
        ICollection<K> key_set = new LinkedDeque<>();
        for (int i = 0; i < this.size(); i++) {
            key_set.add(current_node.key);
            current_node = current_node.next;
        }
        return key_set;
    }

    public ICollection<V> values() {
        Node<K, V> current_node = this.head;
        ICollection<V> value_set = new LinkedDeque<>();
        for (int i = 0; i < this.size(); i++) {
            value_set.add(current_node.value);
            current_node = current_node.next;
        }
        return value_set;
    }

    public V get(K key) {
        if (this.head == null) {
            return null;
        }
        if (this.head.key.equals(key)) {
            return this.head.value;
        }
        Node<K, V> current_node = this.head;
        Node<K, V> previous_node = this.head;
        for (int i = 1; i < this.size(); i++) {
            current_node = current_node.next;
            if (current_node.key.equals(key)) {
                V value = current_node.value;
                previous_node.next = current_node.next;
                Node<K, V> new_node = new Node<>(key, value);
                new_node.next = this.head;
                this.head = new_node;
                return value;
            }
            previous_node = previous_node.next;
        }
        return null;
    }

    public Iterator<K> iterator() {
        return this.keys().iterator();
    }
}
