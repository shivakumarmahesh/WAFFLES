package uk.ac.warwick.cs126.structures;

public class Node<K extends Comparable<K>,V>{
	
	private K key;
	private V value;	
	private int height; 
	private Node leftChild, rightChild; 

	Node(K key, V value) { 
		this.key = key;
		this.value = value;
		
		height = 0; 
		
		leftChild = null;
		rightChild = null;
	} 

	public K getKey(){
		return key;
	}

	public void setKey(K key){
		this.key = key;
	}

	public V getValue(){
		return value;
	}

	public void setValue(V value){
		this.value = value;
	}

	public int height(){
		return this.height;
	}

	public void setHeight(int height){
		this.height = height;
	}

	public Node getLeft(){
		return (leftChild);
	}

	public void setLeft(Node<K,V> left){
		leftChild = left;
	}

	public Node getRight(){
		return (rightChild);
	}

	public void setRight(Node<K,V> right){
		rightChild = right;
	}

    public int compareTo(K otherKey){
        return this.getKey().compareTo(otherKey);
    }

}