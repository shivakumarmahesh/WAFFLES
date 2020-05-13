package uk.ac.warwick.cs126.stores;

import java.util.Comparator;
import java.util.Random;

import uk.ac.warwick.cs126.interfaces.ICustomerStore;
import uk.ac.warwick.cs126.models.Customer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.io.IOUtils;

import uk.ac.warwick.cs126.structures.MyAVLTree;
import uk.ac.warwick.cs126.structures.MyHashMap;

import uk.ac.warwick.cs126.util.DataChecker;
import uk.ac.warwick.cs126.util.StringFormatter;

public class CustomerStore implements ICustomerStore {

    private MyAVLTree<Long, Customer> customerTreeByID;
    private MyAVLTree<LastNameFirstNameID, Customer> customerTreeByName;
    private DataChecker dataChecker;
    private MyHashMap<Long, Integer> blackListedID;

    public CustomerStore() {
        // Initialise variables here
        customerTreeByID = new MyAVLTree<>();
        customerTreeByName = new MyAVLTree<>();
        dataChecker = new DataChecker();
        blackListedID = new MyHashMap<>();
    }

    public Customer[] loadCustomerDataToArray(InputStream resource) {
        Customer[] customerArray = new Customer[0];

        try {
            byte[] inputStreamBytes = IOUtils.toByteArray(resource);
            BufferedReader lineReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            int lineCount = 0;
            String line;
            while ((line=lineReader.readLine()) != null) {
                if (!("".equals(line))) {
                    lineCount++;
                }
            }
            lineReader.close();

            Customer[] loadedCustomers = new Customer[lineCount - 1];

            BufferedReader csvReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            int customerCount = 0;
            String row;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            csvReader.readLine();
            while ((row = csvReader.readLine()) != null) {
                if (!("".equals(row))) {
                    String[] data = row.split(",");

                    Customer customer = (new Customer(
                            Long.parseLong(data[0]),
                            data[1],
                            data[2],
                            formatter.parse(data[3]),
                            Float.parseFloat(data[4]),
                            Float.parseFloat(data[5])));

                    loadedCustomers[customerCount++] = customer;
                }
            }
            csvReader.close();

            customerArray = loadedCustomers;

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return customerArray;
    }

    public boolean addCustomer(Customer customer) {
        if(!dataChecker.isValid(customer)){
            return false;
        }   

        //If the customer to be added has a customerID that exists in the store:
        if(isDuplicateID(customer)){
            customerTreeByID.remove(customer.getID());
            customerTreeByName.remove(new LastNameFirstNameID(customer.getLastName(), customer.getFirstName(), customer.getID()));

            blackListedID.add(customer.getID(), 0);
            return false;
        }

        if(blackListedID.contains(customer.getID())){
            return false;
        }

        customerTreeByID.add(customer.getID(), customer);
        customerTreeByName.add(new LastNameFirstNameID(customer.getLastName(), customer.getFirstName(), customer.getID()), customer);
        return true;
    }

    public boolean addCustomer(Customer[] customers) { 
        boolean successAddingCustomers = true;
        for(int i = 0; i < customers.length; i++){
            if(!addCustomer(customers[i])){
                successAddingCustomers = false;
            }
        }
        return successAddingCustomers;
    }

    public Customer getCustomer(Long id) { 
        return customerTreeByID.get(id);
    }

    public Customer[] getCustomers() {
        if(!customerTreeByID.isEmpty()){
            //Java generics and Arrays don't play well, there is a lot of casting away from Objects in this CourseWork.
            //inOrder Traversal:
            Object[] arr = customerTreeByID.toArray();
            Customer[] customerArray = new Customer[arr.length];
            for(int i = 0; i < arr.length; i++){ 
                customerArray[i] = (Customer)arr[i];
            }
            return customerArray;
        }
        return new Customer[0];
    }

    public Customer[] getCustomers(Customer[] customers) {
        if(customers.length > 0){
            randomizedQuickSort(customers, 0, customers.length - 1, new SortByID()); 
            return customers;
        }
        else{
            return new Customer[0];
        }
    }

    public Customer[] getCustomersByName() {
        if(!customerTreeByName.isEmpty()){
            Object[] arr = customerTreeByName.toArray();
            Customer[] customerArray = new Customer[arr.length];
            for(int i = 0; i < arr.length; i++){
                customerArray[i] = (Customer)arr[i];
            }
            return customerArray;
        }
        return new Customer[0];
    }

    public Customer[] getCustomersByName(Customer[] customers) {
        if(customers.length > 0){
            randomizedQuickSort(customers, 0, customers.length - 1, new SortByName());
            return customers;
        }
        else{
            return new Customer[0];
        }
    }

    public Customer[] getCustomersContaining(String searchTerm) {
        if(!customerTreeByID.isEmpty() && (searchTerm != null) && (searchTerm != "")){
            Object[] arr = customerTreeByName.toArray();
            Customer[] customerArray = new Customer[arr.length];
            for(int i = 0; i < arr.length; i++){
               customerArray[i] = (Customer)arr[i];
            }

            searchTerm = searchTerm.trim();
            searchTerm = StringFormatter.convertAccentsFaster(searchTerm);
            searchTerm = searchTerm.toUpperCase();

            int sizeOfCustomersContaining = 0;
		    for(int i = 0; i < customerArray.length; i++){
		    	if(searchTermMatches(searchTerm, customerArray[i])){
		    		sizeOfCustomersContaining++;
		    	}
		    }

		    Customer[] customersContaining = new Customer[sizeOfCustomersContaining];
		    int j = 0;
	    	for(int i = 0; i < customerArray.length; i++){
		    	if(searchTermMatches(searchTerm, customerArray[i])){
		    		customersContaining[j++] = customerArray[i];
		    	}
		    }

            //returns customersContaining in sorted order since it was sorted already in the AVLTree.
		    return customersContaining; 
        }
        return new Customer[0];
        
    }

    private boolean isDuplicateID(Customer customer){
       return (getCustomer(customer.getID()) != null);
    }

    //Random pivot QuickSort
    public void randomizedQuickSort(Customer[] customers, int left, int right, Comparator<Customer> c){
        if (right - left <= 0){
            return;
        }

        else{
            Random rand = new Random();
            int pivotIndex = left + rand.nextInt(right - left + 1);
            
            Customer temp = customers[pivotIndex];
            customers[pivotIndex] = customers[right];
            customers[right] = temp;

            Customer pivot = customers[right];
            int partition = partition(customers, left, right, pivot, c);
            randomizedQuickSort(customers, left, partition - 1, c);
            randomizedQuickSort(customers, partition + 1, right, c);
        }
    }

    public int partition(Customer[] customers, int left, int right, Customer pivot, Comparator<Customer> c){
        int leftPtr = left - 1;
        int rightPtr = right;
        while (true){
            while (c.compare(customers[++leftPtr], pivot) < 0)
                ;
            while (rightPtr > 0 && c.compare(customers[--rightPtr] ,pivot) > 0)
                ;
            if (leftPtr >= rightPtr){
                break;
            }
            else{
                Customer temp = customers[leftPtr];
                customers[leftPtr] = customers[rightPtr];
                customers[rightPtr] = temp;
            }
                
        }
        Customer temp = customers[leftPtr];
        customers[leftPtr] = customers[right];
        customers[right] = temp;
        return leftPtr;
    }

    public static boolean searchTermMatches(String str, Customer customer){
		String customerFirstName = customer.getFirstName();
		String customerLastName = customer.getLastName();
        customerFirstName = customerFirstName.trim();
        customerFirstName = StringFormatter.convertAccentsFaster(customerFirstName);
		customerFirstName = customerFirstName.toUpperCase();
        customerLastName = customerLastName.trim();
        customerLastName = StringFormatter.convertAccentsFaster(customerLastName);
		customerLastName = customerLastName.toUpperCase();

		if(str.contains(" ")){
			String[] parts = str.split(" ", 2);
			String strFirstName = parts[0];
			String strLastName = parts[1]; 

			return (customerFirstName.contains(strFirstName) && customerLastName.contains(strLastName));
		}

		else{
			return (customerFirstName.contains(str) || customerLastName.contains(str));
		}
    }
    
    //The Comparators are used for RandomizedQuickSort
    class SortByID implements Comparator<Customer>{
        public int compare(Customer customer1, Customer customer2){
            if(customer1.getID() > customer2.getID()){
                return 1;
            }
            else if(customer1.getID() == customer2.getID()){
                return 0;
            }
            else{
                return -1;
            }
        }
    }

    class SortByName implements Comparator<Customer>{
        public int compare(Customer customer1, Customer customer2){
            boolean lastNameCompare = (customer1.getLastName().toUpperCase().compareTo(customer2.getLastName().toUpperCase()) < 0);
            boolean lastNameEqual = (customer1.getLastName().toUpperCase().compareTo(customer2.getLastName().toUpperCase()) == 0);
            boolean firstNameCompare = (customer1.getFirstName().toUpperCase().compareTo(customer2.getFirstName().toUpperCase()) < 0);
            boolean firstNameEqual = (customer1.getFirstName().toUpperCase().compareTo(customer2.getFirstName().toUpperCase()) == 0);
            boolean idCompare = (customer1.getID() < customer2.getID());
            
            if ((lastNameCompare) || (lastNameEqual && firstNameCompare) || (lastNameEqual && firstNameEqual && idCompare)){
                return -1;
            }
            else{
                return 1;
            }
        }
    }    

    //The Comparable is used for the AVLTree
    class LastNameFirstNameID implements Comparable<LastNameFirstNameID>{
        private String lastName;
        private String firstName;
        private Long id;

        public LastNameFirstNameID(String lastName, String firstName, Long id){
            this.lastName = lastName;
            this.firstName = firstName;
            this.id = id;
        }
        public String getLastName() {
            return lastName;
        }
        public String getFirstName(){
            return firstName;
        }
        public Long getID(){
            return id;
        }

        @Override
        public int compareTo(LastNameFirstNameID anotherKey) {
            if(this.lastName.toUpperCase().compareTo(anotherKey.getLastName().toUpperCase()) < 0){
                return -1;
            }
            if(this.lastName.toUpperCase().compareTo(anotherKey.getLastName().toUpperCase()) == 0 && this.firstName.toUpperCase().compareTo(anotherKey.getFirstName().toUpperCase()) < 0){
                return -1;
            }
            if(this.lastName.toUpperCase().compareTo(anotherKey.getLastName().toUpperCase()) == 0 && this.firstName.toUpperCase().compareTo(anotherKey.getFirstName().toUpperCase()) == 0 && this.id < anotherKey.getID()){
                return -1;
            }
            if(this.lastName.toUpperCase().compareTo(anotherKey.getLastName().toUpperCase()) == 0 && this.firstName.toUpperCase().compareTo(anotherKey.getFirstName().toUpperCase()) == 0 && this.id == anotherKey.getID()){
                return 0;
            }
            return 1;
        }
    }
    

}
