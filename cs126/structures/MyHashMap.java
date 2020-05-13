package uk.ac.warwick.cs126.structures;


// This line allows us to cast our object to type (E) without any warnings.
// For further detais, please see: http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/SuppressWarnings.html
@SuppressWarnings("unchecked") 
public class MyHashMap<K extends Comparable<K>,V> implements IMap<K,V> {

    protected KeyValuePairLinkedList[] table;
    int size; 
    
    public MyHashMap() {
        /* for very simple hashing, primes reduce collisions */
        this(97);
    }
    
    public MyHashMap(int tablesize) {
        size = 0;
        table = new KeyValuePairLinkedList[tablesize];
        initTable();
    }
    
    protected void initTable() {
        for(int i = 0; i < table.length; i++) {
            table[i] = new KeyValuePairLinkedList<>();
        }
    }
    
    protected int hash(K key) {
        int code = key.hashCode();
        return code;    
    }
    
    public void add(K key, V value) {
        int hash_code = hash(key);
        int location = hash_code % table.length;
        if(location < 0){
            location+= table.length;
        }
        //System.out.println("Adding " + value + " under key " + key + " at location " + location);
        table[location].add(key,value);
        this.size++;
    }

    public boolean contains(K key){
        return (get(key) != null);
    }

    public void clear(int tablesize){
        size = 0;
        table = new KeyValuePairLinkedList[tablesize];
        initTable();
    }

    public boolean isEmpty(){
        return (this.size() == 0);
    }

    public int size(){
        return this.size;
    }

    public V get(K key) {
        int hash_code = hash(key);
        int location = hash_code % table.length;
        if(location < 0){
            location+= table.length;
        }
        // ListElement<KeyValuePair> ptr = table[location].head;
        
        if((V)table[location].get(key) != null){
            return (V)table[location].get(key).getValue();
        }
        else{
            return null;
        }
    }

    public boolean remove(K key){
        int hash_code = hash(key);
        int location = hash_code % table.length;
        if(location < 0){
            location+= table.length;
        }
        //System.out.println("Attemping to remove value under key "+ key);
        if(table[location].remove(key)){
            this.size--;
            return true;
        }
        else{
            return false;
        }
    }
    
    public String toString() {
        // Returns a String representation of the elements inside the array.
        if (this.isEmpty()) {
            return "Empty";
        }

        StringBuilder ret = new StringBuilder("");
        for (int i = 0; i < table.length; i++) {
            if(table[i] != null){
                ret.append(table[i].toString() + "\n");
            }
        }
        ret.deleteCharAt(ret.length() - 1);
        return ret.toString();
    }

    public V[] toArray(){
        V[] arr = (V[])new Object[this.size()];
        int arrIndex = 0;
        for(int i = 0; i < table.length; i++){
            if(table[i] != null){
                ListElement<KeyValuePair> ptr = table[i].head;
                while(ptr != null){
                    arr[arrIndex++] = (V)ptr.getValue().getValue();
                    ptr = ptr.getNext();
                }
            }
        }
        return arr;
    }   
    
}
