package uk.ac.warwick.cs126.structures;
// This line allows us to cast our object to type (E) without any warnings.
// For further detais, please see: http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/SuppressWarnings.html
// import java.util.Stack;
import uk.ac.warwick.cs126.structures.MyStack;
@SuppressWarnings("unchecked") 
public class MyAVLTree<K extends Comparable<K>, V>{
	private Node root;
	private int size;

	public MyAVLTree(){
		size = 0;	
	}
	public MyAVLTree(Comparable<K> key, V value){
		root = insert(root, key, value);
	}
	//AVL TREE METHODS:
	private Node rightRotate(Node x){
		Node y = x.getLeft();
		Node temp = y.getRight();

		y.setRight(x);
		x.setLeft(temp);

		x.setHeight(max(height(x.getLeft()), height(x.getRight())) + 1);
		y.setHeight(max(height(y.getLeft()), height(y.getRight())) + 1);
		return y;
	}

	private Node leftRotate(Node x){
		Node y = x.getRight();
		Node temp = y.getLeft();

		y.setLeft(x);
		x.setRight(temp);

		x.setHeight(max(height(x.getLeft()), height(x.getRight())) + 1);
		y.setHeight(max(height(y.getLeft()), height(y.getRight())) + 1);

		return y;
	}

	private Node insert(Node node, Comparable<K> key, V value){

		//BST Insertion:
		if(node == null){
			size++;
			return (new Node(key, value));
		}
		if(node.compareTo(key) > 0){
			node.setLeft(insert(node.getLeft(), key, value));
		}
		else if(node.compareTo(key) < 0){
			node.setRight(insert(node.getRight(), key, value));
		}
		else{
			return node;
		}

		//Update Heights:
		node.setHeight(max(height(node.getLeft()), height(node.getRight())) + 1);
		

		//Get balance factor:
		int balanceFactor = balanceFactor(node);

		//LL Rotation
		if(balanceFactor > 1 && node.getLeft().compareTo(key) > 0){
			return rightRotate(node);
		}
		//LR Rotation
		if(balanceFactor > 1 && node.getLeft().compareTo(key) < 0) { 
			node.setLeft(leftRotate(node.getLeft())); 
			return rightRotate(node); 
		}
		//RL Rotation
		if(balanceFactor < -1 && node.getRight().compareTo(key) > 0){
			node.setRight(rightRotate(node.getRight()));
			return leftRotate(node);
		}
		//RR Rotation
		if(balanceFactor < -1 && node.getRight().compareTo(key) < 0){
			return leftRotate(node);
		}

		//unchanged NODE pointer:
		return node;
	}

	private Node delete(Node root, Comparable<K> key){
		if(root == null){
			return root;
		}
		if(root.compareTo(key) > 0){
			root.setLeft(delete(root.getLeft(), key));
		}
		else if(root.compareTo(key) < 0){
			root.setRight(delete(root.getRight(), key));
		}

		else{
			//Node with two children REMOVAL:
			if(root.getLeft() != null && root.getRight() != null){
				Node temp = minimumNode(root.getRight());
				root.setKey(temp.getKey());
				root.setValue(temp.getValue());
				root.setRight(delete(root.getRight(), temp.getKey()));
			}
			else{
				Node temp = null;
				if(temp != root.getLeft()){
					temp = root.getLeft();
				}
				else{
					temp = root.getRight();
				}

				//If there is 1 child.
				if(temp != null){
					root = temp;
				}
				else{
					temp = root;
					root = null;
				}
			}
		}
		//If tree had just one element then return.
		if(root == null){
			return root;
		}

		//Update height of current Node.
		root.setHeight(max(height(root.getLeft()), height(root.getRight())) + 1);

		//Determine BalanceFactor of this Node in order to fix imbalances.
		int balanceFactor = balanceFactor(root);

		//If there is imbalance:
		//LL Rotation
		if(balanceFactor > 1 && balanceFactor(root.getLeft()) >= 0){
			return rightRotate(root);
		}
		//LR Rotation
		if(balanceFactor > 1 && balanceFactor(root.getLeft()) < 0){
			root.setLeft(leftRotate(root.getLeft()));
			return rightRotate(root);
		}
		//RL Rotation
		if(balanceFactor < -1 && balanceFactor(root.getRight()) > 0){
			root.setRight(rightRotate(root.getRight()));
			return leftRotate(root);
		}
		//RR Rotation
		if(balanceFactor < -1 && balanceFactor(root.getRight()) <= 0){
			return leftRotate(root);
		}

		return root;
	}

	private Node search(Node walk, Comparable<K> key){
		//BST Insertion:
		if(walk == null){
			return (null);
		}
		else if(walk.compareTo(key) == 0){
			return walk;
		}
		else if(walk.compareTo(key) < 0){
			return search(walk.getRight(), key);
		}
		else{
			return search(walk.getLeft(), key);
		}
	}

	//AUXILLARY METHODS:
	private Node minimumNode(Node node){
		Node walk = node;
		//Find leftmost External point
		while(walk.getLeft() != null){
			walk = walk.getLeft();
		}
		return walk;
	}

	private int height(Node N){
		if(N != null){
			return N.height();
		}

		return -1;
	}

	private int balanceFactor(Node N){
		if(N != null){
			return height(N.getLeft()) - height(N.getRight());
		}
		return 0;
	}

	private int max(int height1, int height2){
		return (height1 > height2) ? height1 : height2;
	}

	//PUBLIC METHODS:

	public void add(Comparable<K> key, V value){
		root = insert(root, key, value);
	}

	public boolean contains(Comparable<K> key){
		return (get(key) != null);
	}

	public boolean isEmpty(){
		return (size == 0);
	}

	public int size(){
		return (size);
	}

	public V get(Comparable<K> key){
		Node found = search(root, key);
		if(found != null){
			return (V)found.getValue();
		}
		else{
			return null;
		}
	}

	public void remove(Comparable<K> key){
		if(contains(key)){
			size--;
		}
		root = delete(root, key);
	}

	public V getMinimum(){
		if(!isEmpty()){
			return (V)minimumNode(root).getValue();
		}
		else{
			return null;
		}
	}

	public V getRootValue(){
		return (V)root.getValue();
	}

	public V[] toArray(){
		V[] arr = (V[])new Object[this.size()];
		int arrIndex = 0;
		if(isEmpty()){
			return arr;
		}

		MyStack<Node> stack = new MyStack<Node>();
		Node walk = root;

		while((walk != null || stack.size() > 0) && (arrIndex < size())){
			while(walk != null){
				stack.push(walk);
				walk = walk.getLeft();
			}
			walk = stack.pop();
			arr[arrIndex++] = (V)walk.getValue();
			walk = walk.getRight();
		} 

		return arr;
	}

	public V[] toArrayReversed(){
		V[] arr = (V[])new Object[this.size()];
		int arrIndex = 0;
		if(isEmpty()){
			return arr;
		}

		MyStack<Node> stack = new MyStack<Node>();
		Node walk = root;

		while(walk != null || stack.size() > 0){
			while(walk != null){
				stack.push(walk);
				walk = walk.getRight();
			}
			walk = stack.pop();
			arr[arrIndex++] = (V)walk.getValue();
			walk = walk.getLeft();
		} 

		return arr;
	}
	
}