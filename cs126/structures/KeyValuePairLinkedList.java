package uk.ac.warwick.cs126.structures;

public class KeyValuePairLinkedList<K extends Comparable<K>,V> {

    protected ListElement<KeyValuePair<K,V>> head;
    protected int size;
    
    public KeyValuePairLinkedList() {
        head = null;
        size = 0;
    }
    
    public void add(K key, V value) {
        this.add(new KeyValuePair<K,V>(key,value));
    }

    public void add(KeyValuePair<K,V> kvp) {
        ListElement<KeyValuePair<K,V>> new_element = 
                new ListElement<>(kvp);
        new_element.setNext(head);
        if(head != null){
            head.setPrev(new_element);
        }
        head = new_element;
        size++;
    }

    public boolean remove(K key){
        ListElement<KeyValuePair<K,V>> ptr = head;
        
        while(ptr != null) {
            if(ptr.getValue().getKey().equals(key)) {
                size--;
                if(ptr == head){
                   head = head.getNext();
                   ptr = null;//garbage collection
                   return true;
                }
                if(ptr.getNext() != null){
                    ptr.getNext().setPrev(ptr.getPrev());
                }
                if(ptr.getPrev() != null){
                    ptr.getPrev().setNext(ptr.getNext());
                }
                ptr = null; //garbage collection
                return true; 
            }
            
            ptr = ptr.getNext();
        }
        return false;
    }
    
    public int size() {
        return size;
    }
    
    public ListElement<KeyValuePair<K,V>> getHead() {
        return head;
    }
    
    public KeyValuePair<K,V> get(K key) {
        ListElement<KeyValuePair<K,V>> temp = head;
        
        while(temp != null) {
            if(temp.getValue().getKey().equals(key)) {
                return temp.getValue();
            }
            
            temp = temp.getNext();
        }
        
        return null;
    }

    public String toString(){
        ListElement<KeyValuePair<K,V>> ptr = head;
        StringBuilder ret = new StringBuilder("");
        while(ptr != null){
            ret.append(ptr.getValue().getValue() + "\n");
            ptr = ptr.getNext();
        }
        if((ret.length() - 1) >= 0){
            ret.deleteCharAt(ret.length() - 1);
        }
        return ret.toString();
    }
}