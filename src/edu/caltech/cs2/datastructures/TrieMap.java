package edu.caltech.cs2.datastructures;

import edu.caltech.cs2.interfaces.ICollection;
import edu.caltech.cs2.interfaces.IDeque;
import edu.caltech.cs2.interfaces.ITrieMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

public class TrieMap<A, K extends Iterable<A>, V> implements ITrieMap<A, K, V> {
    private TrieNode<A, V> root;
    private Function<IDeque<A>, K> collector;
    private int size;

    public TrieMap(Function<IDeque<A>, K> collector) {
        this.root = null;
        this.collector = collector;
        this.size = 0;
    }

    @Override
    public boolean isPrefix(K key) {
        TrieNode<A, V> current = this.root;
        if (current == null){
            return false;
        }

        for (A character: key){
            if (current.pointers.get(character) != null){
                current = current.pointers.get(character);
            }
            else{
                return false;
            }
        }
        return true;
    }

    @Override
    public IDeque<V> getCompletions(K prefix) {
        ArrayDeque<K> keys = new ArrayDeque<>();
        ArrayDeque<V> values = new ArrayDeque<>();
        TrieNode<A, V> current = this.root;
        if (current == null){
            return values;
        }

        for (A character: prefix){
            if (current.pointers.get(character) != null){
                current = current.pointers.get(character);
            }
            else{
                return values;
            }
        }
        this.valueHelper(current, keys, values, new ArrayDeque<>());
        return values;
    }

    @Override
    public void clear() {
        this.root = null;
        this.size = 0;
    }

    @Override
    public V get(K key) {
        TrieNode<A, V> current = this.root;
        if (current == null){
            return null;
        }

        for (A character: key){
            if (current.pointers.get(character) != null){
                current = current.pointers.get(character);
            }
            else{
                return null;
            }
        }
        return current.value;
    }

    private boolean helper_remove(Iterator<A> iter, TrieNode<A, V> current_node) {
        if (current_node == null) {
            return false;
        }
        if (!iter.hasNext()) {
            if (current_node.value != null) {
                this.size--;
            }
            current_node.value = null;
            return current_node.pointers.isEmpty();
        }
        else {
            A letter = iter.next();
            boolean b = helper_remove(iter, current_node.pointers.get(letter));
            if (b) {
                current_node.pointers.remove(letter);
            }
            return current_node.pointers.isEmpty() && current_node.value == null;
        }
    }

    @Override
    public V remove(K key) {
        if (this.root == null){
            return null;
        }
        V key_value = this.get(key);
        boolean value = helper_remove(key.iterator(), this.root);
        if (value) {
            this.root = null;
        }
        return key_value;
    }

    @Override
    public V put(K key, V value) {
        if (this.root == null){
            this.root = new TrieNode<>();
        }
        TrieNode<A, V> current = this.root;
        for (A character: key){
            if (current.pointers.get(character) == null){
                current.pointers.put(character, new TrieNode<>());
            }
            current = current.pointers.get(character);

        }
        V returnValue = current.value;
        if (current.value == null){size++;}
        current.value = value;
        return returnValue;
    }

    @Override
    public boolean containsKey(K key) {
        return this.get(key) != null;
    }

    @Override
    public boolean containsValue(V value) {
        ICollection<V> vals = this.values();
        return vals.contains(value);
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public ICollection<K> keys() {
        ArrayDeque<K> keys = new ArrayDeque<>();
        if (this.root == null){
            return keys;
        }
        keyHelper(this.root, keys, new ArrayDeque<>());
        return keys;
    }
    private void keyHelper(TrieNode<A, V> node, ICollection<K> keys, ArrayDeque<A> accumulator){
        if (node.value != null){
            keys.add(collector.apply(accumulator));
        }
        for (A letter: node.pointers.keySet()){
            TrieNode<A,V> childNode = node.pointers.get(letter);
            accumulator.addBack(letter);
            keyHelper(childNode, keys, accumulator);
            accumulator.removeBack();
        }
    }

    @Override
    public ICollection<V> values() {
        ArrayDeque<K> keys = new ArrayDeque<>();
        ArrayDeque<V> vals = new ArrayDeque<>();
        if (this.root == null){
            return vals;
        }
        valueHelper(this.root, keys, vals, new ArrayDeque<>());
        return vals;
    }
    private void valueHelper(TrieNode<A, V> node, ICollection<K> keys,ICollection<V> values, ArrayDeque<A> accumulator){
        if (node.value != null){
            keys.add(collector.apply(accumulator));
            values.add(node.value);
        }
        for (A letter: node.pointers.keySet()){
            TrieNode<A,V> childNode = node.pointers.get(letter);
            accumulator.addBack(letter);
            valueHelper(childNode, keys, values , accumulator);
            accumulator.removeBack();
        }
    }


    @Override
    public Iterator<K> iterator() {
        return this.keys().iterator();
    }


    private static class TrieNode<A, V> {
        public final Map<A, TrieNode<A, V>> pointers;
        public V value;

        public TrieNode() {
            this(null);
        }

        public TrieNode(V value) {
            this.pointers = new HashMap<>();
            this.value = value;
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            if (this.value != null) {
                b.append("[").append(this.value).append("]-> {\n");
                this.toString(b, 1);
                b.append("}");
            }
            else {
                this.toString(b, 0);
            }
            return b.toString();
        }

        private String spaces(int i) {
            return " ".repeat(Math.max(0, i));
        }

        protected boolean toString(StringBuilder s, int indent) {
            boolean isSmall = this.pointers.entrySet().size() == 0;

            for (Map.Entry<A, TrieNode<A, V>> entry : this.pointers.entrySet()) {
                A idx = entry.getKey();
                TrieNode<A, V> node = entry.getValue();

                if (node == null) {
                    continue;
                }

                V value = node.value;
                s.append(spaces(indent)).append(idx).append(value != null ? "[" + value + "]" : "");
                s.append("-> {\n");
                boolean bc = node.toString(s, indent + 2);
                if (!bc) {
                    s.append(spaces(indent)).append("},\n");
                }
                else if (s.charAt(s.length() - 5) == '-') {
                    s.delete(s.length() - 5, s.length());
                    s.append(",\n");
                }
            }
            if (!isSmall) {
                s.deleteCharAt(s.length() - 2);
            }
            return isSmall;
        }
    }
}
