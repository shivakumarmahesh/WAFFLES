package uk.ac.warwick.cs126.structures;

public class SinglyLinkedList<E>{

	private class Item<E> {
		private E element;
		private Item<E> next;

		public Item(E e, Item<E> n){
			element = e;
			next = n;
		}

		public E getElement(){
			return element;
		}

		public Item<E> getNext(){
			return next;
		}

		public void setNext(Item<E> n){
			next = n;
		}

	}

	private Item<E> head = null;
	private Item<E> tail = null;
	private int size = 0;

	public SinglyLinkedList(){}

	public int size(){
		return size;
	}

	public boolean isEmpty(){
		return (size == 0);
	}

	public E first(){
		if(isEmpty()){
			return null;
		}
		else{
			return head.getElement();
		}
	}

	public E last(){
		if(isEmpty()){
			return null;
		}
		else{
			return tail.getElement();
		}
	}

	public void addFirst(E e){
		head = new Item<>(e, head);
		if(size == 0){
			tail = head;
		}
		size++;
	}

	public void addLast(E e){
		Item last = new Item<>(e,null);

		if(isEmpty()){
			head = last;
		}
		else{
			tail.setNext(last);
		}
		tail = last;
		size++;
	}

	public E removeFirst(){
		if(isEmpty()){
			return null;
		}
		E answer = head.getElement();
		head = head.getNext();
		size--;

		if(size == 0){
			tail = null;
		}
		return answer;
	}

}

