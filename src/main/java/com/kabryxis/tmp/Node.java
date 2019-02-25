package com.kabryxis.tmp;

public class Node<T> {
	
	private T obj;
	private Node<T> previous;
	
	public Node(T obj) {
		this.obj = obj;
	}
	
	public Node(T obj, Node<T> previous) {
		this.obj = obj;
		this.previous = previous;
	}
	
	public T get() {
		return obj;
	}
	
	public boolean hasPrevious() {
		return previous != null;
	}
	
	public Node<T> getPrevious() {
		return previous;
	}
	
	public Node<T> after(T obj) {
		return new Node<>(obj, this);
	}
	
}
