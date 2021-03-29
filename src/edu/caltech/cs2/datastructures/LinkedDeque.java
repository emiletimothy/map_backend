package edu.caltech.cs2.datastructures;

import edu.caltech.cs2.interfaces.IDeque;
import edu.caltech.cs2.interfaces.IQueue;
import edu.caltech.cs2.interfaces.IStack;

import java.util.Iterator;

public class LinkedDeque<E> implements IDeque<E>, IQueue<E>, IStack<E> {
    private Node<E> head;
    private int size;
    private Node<E> tail;
    public LinkedDeque(){
        head = null;
        size = 0;
    }
    private static class Node<E> {
        public final E data;
        public Node<E> next;
        public Node<E> prev;

        public Node(E data) {
            this(data, null);
        }

        public Node(E data, Node<E> next) {
            this.data = data;
            this.next = next;
            this.prev = null;
        }
    }
    public void addFront(E e) {
        if (this.head == null){
            this.head = new Node<>(e);
            this.tail = head;
        }
        else {
            Node<E> newHead = new Node<>(e);
            newHead.next = this.head;
            this.head.prev = newHead;
            this.head = newHead;
        }
        this.size++;
    }

    @Override
    public void addBack(E e) {
        if (this.head == null){
            this.head = new Node<>(e);
            this.tail = head;
        }
        else{
            Node<E> newLast = new Node<>(e);
            newLast.prev = this.tail;
            this.tail.next = newLast;
            this.tail = newLast;
        }
        size++;
    }

    @Override
    public E removeFront() {
        if (size == 1){
            E returnValue = this.head.data;
            this.head = null;
            this.tail = null;
            size = 0;
            return returnValue;
        }
        else if (size == 0){
            return null;
        }
        else {
            E returnValue = this.head.data;
            this.head = this.head.next;
            this.head.prev = null;
            size--;
            return returnValue;
        }
    }

    @Override
    public E removeBack() {
        if (size == 1){
            E returnValue = this.head.data;
            this.head = null;
            this.tail = null;
            size = 0;
            return returnValue;
        }
        else if (size == 0){
            return null;
        }
        else{
            E returnValue = this.tail.data;
            this.tail = this.tail.prev;
            this.tail.next = null;
            size--;
            return returnValue;
        }
    }
    public String toString(){
        StringBuilder result = new StringBuilder();
        Node<E> currentNode = head;
        for (int i = 0; i<this.size(); i++){
            result.append(currentNode.data).append(", ");
            currentNode = currentNode.next;
        }
        if (result.toString().equals("")) {return "[]";}
        return "["+result.substring(0,result.length()-2)+"]";
    }

    @Override
    public boolean enqueue(E e) {
        this.addFront(e);
        return true;
    }

    @Override
    public E dequeue() {
        return this.removeBack();
    }

    @Override
    public boolean push(E e) {
        this.addBack(e);
        return true;
    }

    @Override
    public E pop() {
        return this.removeBack();
    }

    @Override
    public E peek() {
        return this.peekBack();
    }

    @Override
    public E peekFront() {
        if (this.head == null){
            return null;
        }
        return this.head.data;
    }

    @Override
    public E peekBack() {
        if (this.tail == null){
            return null;
        }
        return this.tail.data;
    }

    @Override
    public Iterator<E> iterator() {
        return new LinkedDequeIterator();
    }

    @Override
    public int size() {
        return this.size;
    }
    private class LinkedDequeIterator implements Iterator<E>{
        private Node<E> currentNode;
        private int currentIndex = 0;
        public LinkedDequeIterator(){
            this.currentNode = LinkedDeque.this.head;
        }
        public boolean hasNext(){
            return this.currentIndex<LinkedDeque.this.size();
        }
        public E next(){
            E element = this.currentNode.data;
            this.currentNode = this.currentNode.next;
            currentIndex += 1;
            return element;
        }

    }
}
