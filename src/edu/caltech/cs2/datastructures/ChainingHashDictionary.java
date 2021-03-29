package edu.caltech.cs2.datastructures;

import edu.caltech.cs2.interfaces.ICollection;
import edu.caltech.cs2.interfaces.IDictionary;

import java.util.Iterator;
import java.util.function.Supplier;

public class ChainingHashDictionary<K, V> implements IDictionary<K, V> {
    private Supplier<IDictionary<K, V>> chain;
    private int size;
    private IDictionary<K, V>[] data;

    public ChainingHashDictionary(Supplier<IDictionary<K, V>> chain) {

        this.data = new IDictionary[17]; //implementing IDictionary of length 16

        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = chain.get();//implementing hashtable
        }
        this.chain = chain;
        this.size = 0;

    }

    /**
     * @param key
     * @return value corresponding to key
     */
    @Override
    public V get(K key) {
        int index = key.hashCode() % this.data.length;
        if (index < 0) {
            index += this.data.length;
        }
        if (this.data[index].containsKey(key)) {
            return this.data[index].get(key);
        }
        return null;

    }

    @Override
    public V remove(K key) {
        int index = key.hashCode() % this.data.length;
        if (index < 0) {
            index += this.data.length;
        }
        if (this.data[index].containsKey(key)) {
            V old = this.data[index].get(key);
            this.data[index].remove(key);
            size--;
            return old;
        }
        return null;
    }

    private IDictionary<K, V>[] resize() {
        IDictionary<K, V>[] newData = new IDictionary[this.data.length * 2];

        for (int i = 0; i < newData.length; i++) {
            newData[i] = this.chain.get();//implementing hashtable
        }

        for (int i = 0; i < data.length; i++) {
            for (K k : this.data[i].keys()) {
                int index = k.hashCode() % newData.length;
                if (index < 0) {
                    index += newData.length;
                }
                newData[index].put(k, this.data[i].get(k));
            }
        }
        this.data = newData;

        return this.data;

    }

    @Override
    public V put(K key, V value) {

        int index = key.hashCode() % this.data.length;
        if (index < 0) {
            index += this.data.length;
        }
        if (this.data[index].containsKey(key)) {
            V old = this.data[index].get(key);
            this.data[index].put(key, value);
            return old;
        }

        if (this.size > this.data.length) {//usually size/length = load factor (avg # const), size = ele, length = bucket
            //load factor is 1
            this.data = resize();
        }

        int index2 = key.hashCode() % this.data.length;
        if (index2 < 0) {
            index2 += this.data.length;
        }
        this.data[index2].put(key, value);
        size++;
        return null;

    }

    @Override
    public boolean containsKey(K key) {

        int index = key.hashCode() % data.length;
        if (index < 0) {
            index += data.length;
        }
        return this.data[index].keys().contains(key);
    }

    /**
     * @param value
     * @return true if the HashDictionary contains a key-value pair with
     * this value, and false otherwise
     */
    @Override
    public boolean containsValue(V value) {

        for (int i = 0; i < this.data.length; i++) {
            if (this.data[i].values().contains(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return number of key-value pairs in the HashDictionary
     */
    @Override
    public int size() {
        return this.size;
    }

    @Override
    public ICollection<K> keys() {
        ICollection<K> keyList = new LinkedDeque<K>();
        for (int i = 0; i < data.length; i++) {
            for (K k : this.data[i].keys()) {
                keyList.add(k);
            }
        }
        return keyList;
    }

    @Override
    public ICollection<V> values() {
        ICollection<V> valList = new LinkedDeque<V>();
        for (int i = 0; i < data.length; i++) {
            for (V v : this.data[i].values()) {
                valList.add(v);
            }
        }
        return valList;
    }

    /**
     * @return An iterator for all entries in the HashDictionary
     */
    @Override
    public Iterator<K> iterator() {
        return new ChainingHashDictionaryIterator();
    }

    public class ChainingHashDictionaryIterator implements Iterator<K> {

        private ArrayDeque<Object> store = new ArrayDeque<Object>();

        public ChainingHashDictionaryIterator() {

            for (IDictionary d : ChainingHashDictionary.this.data) {
                for (Object k : d.keys()) {
                    this.store.add(k);
                }
            }
        }

        public boolean hasNext() {
            if (this.store.peek() != null) {
                return true;
            }
            return false;
        }

        public K next() {
            return (K) this.store.removeFront();
        }

    }
}
