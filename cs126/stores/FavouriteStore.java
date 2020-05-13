package uk.ac.warwick.cs126.stores;

import uk.ac.warwick.cs126.interfaces.IFavouriteStore;
import uk.ac.warwick.cs126.models.Favourite;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Comparator;
import org.apache.commons.io.IOUtils;

import uk.ac.warwick.cs126.structures.MyAVLTree;
import uk.ac.warwick.cs126.structures.MyArrayList;
import uk.ac.warwick.cs126.structures.MyHashMap;

import uk.ac.warwick.cs126.util.DataChecker;

public class FavouriteStore implements IFavouriteStore {

    
    private MyAVLTree<Long, Favourite> favouriteTreeByID;

    //I call these Forests to distinguish them from Regular Trees.
    private MyAVLTree<CustomerRestaurantKey, MyAVLTree<Date, Favourite>> customerRestaurantForest;
    private MyAVLTree<Long, MyAVLTree<DateIDKey, Favourite>> favouriteForestByCustomerID;
    private MyAVLTree<Long, MyAVLTree<DateIDKey, Favourite>> favouriteForestByRestaurantID;
    private MyHashMap<Long, Integer> blackListedID;
    private DataChecker dataChecker;

    public FavouriteStore() {
        // Initialise variables here
        favouriteTreeByID = new MyAVLTree<>();
        customerRestaurantForest = new MyAVLTree<>();
        favouriteForestByCustomerID = new MyAVLTree<>();
        favouriteForestByRestaurantID = new MyAVLTree<>();
                 
        blackListedID = new MyHashMap<>();
        dataChecker = new DataChecker();
    }

    public Favourite[] loadFavouriteDataToArray(InputStream resource) {
        Favourite[] favouriteArray = new Favourite[0];

        try {
            byte[] inputStreamBytes = IOUtils.toByteArray(resource);
            BufferedReader lineReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            int lineCount = 0;
            String line;
            while ((line = lineReader.readLine()) != null) {
                if (!("".equals(line))) {
                    lineCount++;
                }
            }
            lineReader.close();

            Favourite[] loadedFavourites = new Favourite[lineCount - 1];

            BufferedReader csvReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            int favouriteCount = 0;
            String row;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            csvReader.readLine();
            while ((row = csvReader.readLine()) != null) {
                if (!("".equals(row))) {
                    String[] data = row.split(",");
                    Favourite favourite = new Favourite(
                            Long.parseLong(data[0]),
                            Long.parseLong(data[1]),
                            Long.parseLong(data[2]),
                            formatter.parse(data[3]));
                    loadedFavourites[favouriteCount++] = favourite;
                }
            }
            csvReader.close();

            favouriteArray = loadedFavourites;

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return favouriteArray;
    }

    public boolean addFavourite(Favourite favourite) {
        if(!dataChecker.isValid(favourite)){
            return false;
        }
        if(isDuplicateFavouriteID(favourite)){

            Favourite removedFavourite = favouriteTreeByID.get(favourite.getID());
            favouriteTreeByID.remove(favourite.getID());

            customerRestaurantForest.get(
            new CustomerRestaurantKey(removedFavourite.getCustomerID(), removedFavourite.getRestaurantID())
            ).remove(removedFavourite.getDateFavourited());
            

            Favourite replacementFavourite = customerRestaurantForest.get(
                new CustomerRestaurantKey(removedFavourite.getCustomerID(), removedFavourite.getRestaurantID())
                ).getMinimum();
            
            
            if(replacementFavourite != null){
                favouriteTreeByID.add(replacementFavourite.getID(), replacementFavourite);
                blackListedID.remove(replacementFavourite.getID());

                //Adding the replacement into favouriteForestByCustomerID:
                if(favouriteForestByCustomerID.contains(replacementFavourite.getCustomerID())){
                    //Tree exists
                    favouriteForestByCustomerID.get(replacementFavourite.getCustomerID()).add(new DateIDKey(replacementFavourite.getDateFavourited(), replacementFavourite.getID()), replacementFavourite);
                }
                else{
                    //Make a new Tree inside the Forest:
                    favouriteForestByCustomerID.add(replacementFavourite.getCustomerID(), new MyAVLTree<DateIDKey, Favourite>(new DateIDKey(replacementFavourite.getDateFavourited(), replacementFavourite.getID()), replacementFavourite));
                }

                //Adding the replacement into favouriteForestByRestaurantID:
                if(favouriteForestByRestaurantID.contains(replacementFavourite.getRestaurantID())){
                    //Tree exists
                    favouriteForestByRestaurantID.get(replacementFavourite.getRestaurantID()).add(new DateIDKey(replacementFavourite.getDateFavourited(), replacementFavourite.getID()), replacementFavourite);
                }
                else{
                    //Make a new Tree inside the Forest:
                    favouriteForestByRestaurantID.add(replacementFavourite.getRestaurantID(), new MyAVLTree<DateIDKey, Favourite>(new DateIDKey(replacementFavourite.getDateFavourited(), replacementFavourite.getID()), replacementFavourite));
                }


            }
            else{ // if replacementFavourite == null
                customerRestaurantForest.remove(new CustomerRestaurantKey(removedFavourite.getCustomerID(), removedFavourite.getRestaurantID()));
            }

            blackListedID.add(favourite.getID(), 0);
            return false;
        }
        if(blackListedID.contains(favourite.getID())){
            return false;
        }

        //REPLACEMENT OF EXISTING CUSTOMER RESTAURANT 
        if(isDuplicateCustomerRestaurantID(favourite)){
            Favourite prevOldestFavourite = customerRestaurantForest.get(
                new CustomerRestaurantKey(favourite.getCustomerID(), favourite.getRestaurantID())).getMinimum();


            customerRestaurantForest.get(
                new CustomerRestaurantKey(favourite.getCustomerID(), favourite.getRestaurantID())).add(favourite.getDateFavourited(), favourite);

            Favourite newOldestFavourite = customerRestaurantForest.get(
                new CustomerRestaurantKey(favourite.getCustomerID(), favourite.getRestaurantID())).getMinimum();
            
            if(newOldestFavourite.getDateFavourited().compareTo(prevOldestFavourite.getDateFavourited()) < 0)
            {
                favouriteTreeByID.remove(prevOldestFavourite.getID());
                favouriteTreeByID.add(newOldestFavourite.getID(), newOldestFavourite);
                blackListedID.add(prevOldestFavourite.getID(), 0);

                //REPLACEMENT INSIDE favouriteForestByCustomerID:
                    //First Remove existing Favourite:
                favouriteForestByCustomerID.get(prevOldestFavourite.getCustomerID()).remove(new DateIDKey(prevOldestFavourite.getDateFavourited(), prevOldestFavourite.getID()));
                    //Second Add Replacement Favourite:
                if(favouriteForestByCustomerID.contains(newOldestFavourite.getCustomerID())){
                    //Tree exists
                    favouriteForestByCustomerID.get(newOldestFavourite.getCustomerID()).add(new DateIDKey(newOldestFavourite.getDateFavourited(), newOldestFavourite.getID()), newOldestFavourite);
                }
                else{
                    //Make a new Tree inside the Forest:
                    favouriteForestByCustomerID.add(newOldestFavourite.getCustomerID(), new MyAVLTree<DateIDKey, Favourite>(new DateIDKey(newOldestFavourite.getDateFavourited(), newOldestFavourite.getID()), newOldestFavourite));
                }

                //REPLACEMENT INSIDE favouriteForestByRestaurantID:
                    //First Remove existing Favourite:
                    favouriteForestByRestaurantID.get(prevOldestFavourite.getRestaurantID()).remove(new DateIDKey(prevOldestFavourite.getDateFavourited(), prevOldestFavourite.getID()));
                    //Second Add Replacement Favourite:
                if(favouriteForestByRestaurantID.contains(newOldestFavourite.getRestaurantID())){
                    //Tree exists
                    favouriteForestByRestaurantID.get(newOldestFavourite.getRestaurantID()).add(new DateIDKey(newOldestFavourite.getDateFavourited(), newOldestFavourite.getID()), newOldestFavourite);
                }
                else{
                    //Make a new Tree inside the Forest:
                    favouriteForestByRestaurantID.add(newOldestFavourite.getRestaurantID(), new MyAVLTree<DateIDKey, Favourite>(new DateIDKey(newOldestFavourite.getDateFavourited(), newOldestFavourite.getID()), newOldestFavourite));
                }
                

                return true;
            }
            return false;
        }

        // The fabulous case with Valid Non-duplicate Favourite:
        favouriteTreeByID.add(favourite.getID(), favourite);
        customerRestaurantForest.add(new CustomerRestaurantKey(favourite.getCustomerID(), favourite.getRestaurantID()), new MyAVLTree<Date, Favourite>(favourite.getDateFavourited(), favourite));
        
        //Add into favouriteForestByCustomerID:
        if(favouriteForestByCustomerID.contains(favourite.getCustomerID())){
            favouriteForestByCustomerID.get(favourite.getCustomerID()).add(new DateIDKey(favourite.getDateFavourited(), favourite.getID()), favourite);
        }
        else{
            favouriteForestByCustomerID.add(favourite.getCustomerID(), new MyAVLTree<DateIDKey, Favourite>(new DateIDKey(favourite.getDateFavourited(), favourite.getID()), favourite));
        }

        //Add into favouriteForestByRestaurantID:
        if(favouriteForestByRestaurantID.contains(favourite.getRestaurantID())){
            favouriteForestByRestaurantID.get(favourite.getRestaurantID()).add(new DateIDKey(favourite.getDateFavourited(), favourite.getID()), favourite);
        }
        else{
            favouriteForestByRestaurantID.add(favourite.getRestaurantID(), new MyAVLTree<DateIDKey, Favourite>(new DateIDKey(favourite.getDateFavourited(), favourite.getID()), favourite));
        }
        
        return true;
        
    }

    public boolean addFavourite(Favourite[] favourites) {
        boolean successAddingFavourites = true;
        for(int i = 0; i < favourites.length; i++){
            if(!addFavourite(favourites[i])){
                successAddingFavourites = false;
            }
        }
        return successAddingFavourites;
    }

    public Favourite getFavourite(Long id) {
        return favouriteTreeByID.get(id);
    }

    public Favourite[] getFavourites() {
        if(!favouriteTreeByID.isEmpty()){
            Object[] arr = favouriteTreeByID.toArray();
            Favourite[] favouriteArray = new Favourite[arr.length];
            for(int i = 0; i < arr.length; i++){
                favouriteArray[i] = (Favourite)arr[i];
            }
            return favouriteArray;
        }
        return new Favourite[0];
    }

    public Favourite[] getFavouritesByCustomerID(Long id) {
        if(favouriteForestByCustomerID.contains(id)){
            Object[] arr = favouriteForestByCustomerID.get(id).toArray();
            Favourite[] favouritesByCustomerIDArray = new Favourite[arr.length];
            for(int i = 0; i < arr.length; i++){
                favouritesByCustomerIDArray[i] = (Favourite)arr[i];
            }
            return favouritesByCustomerIDArray;
        }
        return new Favourite[0];
    }

    public Favourite[] getFavouritesByRestaurantID(Long id) {
        if(favouriteForestByRestaurantID.contains(id)){
            Object[] arr = favouriteForestByRestaurantID.get(id).toArray();
            Favourite[] favouritesByRestaurantIDArray = new Favourite[arr.length];
            for(int i = 0; i < arr.length; i++){
                favouritesByRestaurantIDArray[i] = (Favourite)arr[i];
            }
            return favouritesByRestaurantIDArray;
        }
        return new Favourite[0];
    }

    public Long[] getCommonFavouriteRestaurants(Long customer1ID, Long customer2ID) {
        Favourite[] favouritesCustomer1 = getFavouritesByCustomerID(customer1ID);
        Favourite[] favouritesCustomer2 = getFavouritesByCustomerID(customer2ID);
        if(favouritesCustomer1.length > 0 && favouritesCustomer2.length > 0){
            
            //Sort By RestaurantID
            randomizedQuickSort(favouritesCustomer1, 0, favouritesCustomer1.length - 1, new SortByRestaurantID());
            randomizedQuickSort(favouritesCustomer2, 0, favouritesCustomer2.length - 1, new SortByRestaurantID());
            int commonFavouritesCount = 0;
            int i = 0;
            int j = 0;
            //Determine commonFavouritesCount 
            while(i < favouritesCustomer1.length && j < favouritesCustomer2.length){
                if(favouritesCustomer1[i].getRestaurantID().equals(favouritesCustomer2[j].getRestaurantID())){
                    commonFavouritesCount++;
                    i++;
                    j++;
                }
                else if(favouritesCustomer1[i].getRestaurantID().compareTo(favouritesCustomer2[j].getRestaurantID()) < 0){
                    
                    i++;
                }
                else{
                    j++;
                }
            }
            //Add common Favourites into an Array
            Favourite[] commonFavourites = new Favourite[commonFavouritesCount];
            int k = 0;
            i = 0;
            j = 0;
            while(i < favouritesCustomer1.length && j < favouritesCustomer2.length){
                if(favouritesCustomer1[i].getRestaurantID().equals(favouritesCustomer2[j].getRestaurantID())){
                    commonFavourites[k++] = (favouritesCustomer1[i].getDateFavourited().after(favouritesCustomer2[j].getDateFavourited()))? favouritesCustomer1[i]:favouritesCustomer2[j];
                    i++;
                    j++;
                }
                else if(favouritesCustomer1[i].getRestaurantID().compareTo(favouritesCustomer2[j].getRestaurantID()) < 0){
                    i++;
                }
                else{
                    j++;
                }
            }
            randomizedQuickSort(commonFavourites, 0, commonFavourites.length - 1, new SortByDateRestaurantID());
            Long[] commonRestaurantIDs = new Long[commonFavouritesCount];
            for(i = 0; i < commonFavouritesCount; i++){
                commonRestaurantIDs[i] = commonFavourites[i].getRestaurantID();
            }
            return commonRestaurantIDs;
            
        }
        return new Long[0];
    }

    public Long[] getMissingFavouriteRestaurants(Long customer1ID, Long customer2ID) {
        Favourite[] favouritesCustomer1 = getFavouritesByCustomerID(customer1ID);
        Favourite[] favouritesCustomer2 = getFavouritesByCustomerID(customer2ID);

        //If one of the Customers had no Favourites:
        if(favouritesCustomer1.length != 0 && favouritesCustomer2.length == 0){
            randomizedQuickSort(favouritesCustomer1, 0, favouritesCustomer1.length, new SortByDateRestaurantID());
            Long[] missingRestaurantIDs = new Long[favouritesCustomer1.length];
            for(int i = 0; i < favouritesCustomer1.length; i++){
                missingRestaurantIDs[i] = favouritesCustomer1[i].getRestaurantID();
            }
            return missingRestaurantIDs;
        }
        if(favouritesCustomer1.length == 0 && favouritesCustomer2.length != 0){
            randomizedQuickSort(favouritesCustomer2, 0, favouritesCustomer2.length, new SortByDateRestaurantID());
            Long[] missingRestaurantIDs = new Long[favouritesCustomer2.length];
            for(int i = 0; i < favouritesCustomer2.length; i++){
                missingRestaurantIDs[i] = favouritesCustomer2[i].getRestaurantID();
            }
            return missingRestaurantIDs;
        }
        //If both customers have at least one favourite
        if(favouritesCustomer1.length != 0 && favouritesCustomer2.length != 0){
            randomizedQuickSort(favouritesCustomer1, 0, favouritesCustomer1.length - 1, new SortByRestaurantID());
            randomizedQuickSort(favouritesCustomer2, 0, favouritesCustomer2.length - 1, new SortByRestaurantID());
            int missingFavouritesCount = 0;
            int i = 0;
            int j = 0;
            //Determine missingFavouritesCount 
            while(i < favouritesCustomer1.length && j < favouritesCustomer2.length){
                if(favouritesCustomer1[i].getRestaurantID().compareTo(favouritesCustomer2[j].getRestaurantID()) < 0){
                    missingFavouritesCount++;
                    i++;
                }
                else if(favouritesCustomer1[i].getRestaurantID().compareTo(favouritesCustomer2[j].getRestaurantID()) > 0){
                    j++;
                }
                else{
                    i++;
                    j++;
                }
            }
            Favourite[] missingFavourites = new Favourite[missingFavouritesCount];
            int k = i = j = 0;//reset i and j
            while(i < favouritesCustomer1.length && j < favouritesCustomer2.length){
                if(favouritesCustomer1[i].getRestaurantID().compareTo(favouritesCustomer2[j].getRestaurantID()) < 0){
                    missingFavourites[k++] = favouritesCustomer1[i];
                    i++;
                }
                else if(favouritesCustomer1[i].getRestaurantID().compareTo(favouritesCustomer2[j].getRestaurantID()) > 0){

                    j++;
                }
                else{
                    i++;
                    j++;
                }
            }
            randomizedQuickSort(missingFavourites, 0, missingFavourites.length - 1, new SortByDateRestaurantID());
            Long[] missingRestaurantIDs = new Long[missingFavouritesCount];
            for(i = 0; i < missingFavouritesCount; i++){
                missingRestaurantIDs[i] = missingFavourites[i].getRestaurantID();
            }
            return missingRestaurantIDs;
        }

        return new Long[0];
    }

    public Long[] getNotCommonFavouriteRestaurants(Long customer1ID, Long customer2ID) {
        Favourite[] favouritesCustomer1 = getFavouritesByCustomerID(customer1ID);
        Favourite[] favouritesCustomer2 = getFavouritesByCustomerID(customer2ID);
        //If one of the customers has no favourites
        if(favouritesCustomer1.length != 0 && favouritesCustomer2.length == 0){
            randomizedQuickSort(favouritesCustomer1, 0, favouritesCustomer1.length, new SortByDateRestaurantID());
            Long[] notcommonRestaurantIDs = new Long[favouritesCustomer1.length];
            for(int i = 0; i < favouritesCustomer1.length; i++){
                notcommonRestaurantIDs[i] = favouritesCustomer1[i].getRestaurantID();
            }
            return notcommonRestaurantIDs;
        }
        if(favouritesCustomer1.length == 0 && favouritesCustomer2.length != 0){
            randomizedQuickSort(favouritesCustomer2, 0, favouritesCustomer2.length, new SortByDateRestaurantID());
            Long[] notcommonRestaurantIDs = new Long[favouritesCustomer2.length];
            for(int i = 0; i < favouritesCustomer2.length; i++){
                notcommonRestaurantIDs[i] = favouritesCustomer2[i].getRestaurantID();
            }
            return notcommonRestaurantIDs;
        }
        //If both have at least one favourite
        if(favouritesCustomer1.length != 0 && favouritesCustomer2.length != 0){
            randomizedQuickSort(favouritesCustomer1, 0, favouritesCustomer1.length - 1, new SortByRestaurantID());
            randomizedQuickSort(favouritesCustomer2, 0, favouritesCustomer2.length - 1, new SortByRestaurantID());
            int notcommonFavouritesCount = 0;
            int i = 0;
            int j = 0;
            //Determine notcommonFavouritesCount 
            while(i < favouritesCustomer1.length && j < favouritesCustomer2.length){
                if(favouritesCustomer1[i].getRestaurantID().compareTo(favouritesCustomer2[j].getRestaurantID()) < 0){
                    notcommonFavouritesCount++;
                    i++;
                }
                else if(favouritesCustomer1[i].getRestaurantID().compareTo(favouritesCustomer2[j].getRestaurantID()) > 0){
                    notcommonFavouritesCount++;
                    j++;
                }
                else{
                    i++;
                    j++;
                }
            }
            Favourite[] notcommonFavourites = new Favourite[notcommonFavouritesCount];
            int k = i = j = 0;//reset i and j
            while(i < favouritesCustomer1.length && j < favouritesCustomer2.length){
                if(favouritesCustomer1[i].getRestaurantID().compareTo(favouritesCustomer2[j].getRestaurantID()) < 0){
                    notcommonFavourites[k++] = favouritesCustomer1[i];
                    i++;
                }
                else if(favouritesCustomer1[i].getRestaurantID().compareTo(favouritesCustomer2[j].getRestaurantID()) > 0){
                    notcommonFavourites[k++] = favouritesCustomer2[j];
                    j++;
                }
                else{
                    i++;
                    j++;
                }
            }
            randomizedQuickSort(notcommonFavourites, 0, notcommonFavourites.length - 1, new SortByDateRestaurantID());
            Long[] notcommonRestaurantIDs = new Long[notcommonFavouritesCount];
            for(i = 0; i < notcommonFavouritesCount; i++){
                notcommonRestaurantIDs[i] = notcommonFavourites[i].getRestaurantID();
            }
            return notcommonRestaurantIDs;
        }

        return new Long[0];
    }

    public Long[] getTopCustomersByFavouriteCount() {
        if(!favouriteForestByCustomerID.isEmpty()){
            Object[] arr = favouriteForestByCustomerID.toArray();
            MyAVLTree<DateIDKey, Favourite>[] arrayOfCustomerIDTrees = new MyAVLTree[arr.length];
            for(int i = 0; i < arr.length; i++){
                arrayOfCustomerIDTrees[i] = (MyAVLTree<DateIDKey, Favourite>)arr[i];
            }


            int topCustomerIDsize = (arrayOfCustomerIDTrees.length > 20)? 20:arrayOfCustomerIDTrees.length;
            Long[] topCustomerIDs = new Long[20];
            for(int i = 0; i < topCustomerIDsize; i++){
                int topIndex = 0;
                for(int j = 1; j < arrayOfCustomerIDTrees.length; j++){
                    while(arrayOfCustomerIDTrees[topIndex] == null){
                        topIndex++;
                    }
                    if((arrayOfCustomerIDTrees[j] != null)){
                        //Minimums of arrayOfCustomerIDTress[index] are favourites that have the latest Date for a particular Customer.
                        if((new FavouriteCountDate(arrayOfCustomerIDTrees[topIndex].size(), arrayOfCustomerIDTrees[topIndex].getMinimum().getDateFavourited()).compareTo(
                            new FavouriteCountDate(arrayOfCustomerIDTrees[j].size(), arrayOfCustomerIDTrees[j].getMinimum().getDateFavourited())
                        ) >= 0)){
                            topIndex = j;
                        }
                    }
                }
                topCustomerIDs[i] = arrayOfCustomerIDTrees[topIndex].getRootValue().getCustomerID();
                arrayOfCustomerIDTrees[topIndex] = null;
            }
            return topCustomerIDs;
        }
        
        return new Long[20];
    }

    public Long[] getTopRestaurantsByFavouriteCount() {
        if(!favouriteForestByRestaurantID.isEmpty()){
            Object[] arr = favouriteForestByRestaurantID.toArray();
            MyAVLTree<DateIDKey, Favourite>[] arrayOfRestaurantIDTrees = new MyAVLTree[arr.length];
            for(int i = 0; i < arr.length; i++){
                arrayOfRestaurantIDTrees[i] = (MyAVLTree<DateIDKey, Favourite>)arr[i];
            }


            int topRestaurantIDsize = (arrayOfRestaurantIDTrees.length > 20)? 20:arrayOfRestaurantIDTrees.length;
            Long[] topRestaurantIDs = new Long[20];
            for(int i = 0; i < topRestaurantIDsize; i++){
                int topIndex = 0;
                for(int j = 1; j < arrayOfRestaurantIDTrees.length; j++){
                    while(arrayOfRestaurantIDTrees[topIndex] == null){
                        topIndex++;
                    }
                    if((arrayOfRestaurantIDTrees[j] != null)){
                        //Minimums of arrayOfRestaurantIDTress[index] are favourites that have the latest Date for a particular Restaurant.
                        if((new FavouriteCountDate(arrayOfRestaurantIDTrees[topIndex].size(), arrayOfRestaurantIDTrees[topIndex].getMinimum().getDateFavourited()).compareTo(
                            new FavouriteCountDate(arrayOfRestaurantIDTrees[j].size(), arrayOfRestaurantIDTrees[j].getMinimum().getDateFavourited())
                        ) >= 0)){
                            topIndex = j;
                        }
                    }
                }
                topRestaurantIDs[i] = arrayOfRestaurantIDTrees[topIndex].getRootValue().getRestaurantID();
                arrayOfRestaurantIDTrees[topIndex] = null;
            }
            return topRestaurantIDs;
        }
        
        return new Long[20];
    }

    //This is to check if there exists a Duplicate favouriteID already in the store.
    private boolean isDuplicateFavouriteID(Favourite favourite){
        return favouriteTreeByID.contains(favourite.getID());
    }

    //This is to check if there exists a Duplicate customerRestaurantID pair already in the store.
    private boolean isDuplicateCustomerRestaurantID(Favourite favourite){
        return customerRestaurantForest.contains(new CustomerRestaurantKey(favourite.getCustomerID(), favourite.getRestaurantID()));
    }

    //Random pivot QuickSort
    public void randomizedQuickSort(Favourite[] favourites, int left, int right, Comparator<Favourite> c){
        if (right - left <= 0){
            return;
        }

        else{
            Random rand = new Random();
            int pivotIndex = left + rand.nextInt(right - left + 1);
            
            Favourite temp = favourites[pivotIndex];
            favourites[pivotIndex] = favourites[right];
            favourites[right] = temp;

            Favourite pivot = favourites[right];
            int partition = partition(favourites, left, right, pivot, c);
            randomizedQuickSort(favourites, left, partition - 1, c);
            randomizedQuickSort(favourites, partition + 1, right, c);
        }
    }

    public int partition(Favourite[] favourites, int left, int right, Favourite pivot, Comparator<Favourite> c){
        int leftPtr = left - 1;
        int rightPtr = right;
        while (true){
            while (c.compare(favourites[++leftPtr], pivot) < 0)
                ;
            while (rightPtr > 0 && c.compare(favourites[--rightPtr] ,pivot) > 0)
                ;
            if (leftPtr >= rightPtr){
                break;
            }
            else{
                Favourite temp = favourites[leftPtr];
                favourites[leftPtr] = favourites[rightPtr];
                favourites[rightPtr] = temp;
            }
                
        }
        Favourite temp = favourites[leftPtr];
        favourites[leftPtr] = favourites[right];
        favourites[right] = temp;
        return leftPtr;
    }

    class CustomerRestaurantKey implements Comparable<CustomerRestaurantKey>{
        private long customerID;
        private long restaurantID;
    
        public CustomerRestaurantKey(Long customerID, Long restaurantID){
            this.customerID = customerID;
            this.restaurantID = restaurantID;
        }
        public float getCustomerID(){
            return customerID;
        }
        public float getRestaurantID(){
            return restaurantID;
        }
    
        @Override
        public int compareTo(CustomerRestaurantKey anotherKey){
            if(this.customerID < anotherKey.getCustomerID()){
                return -1;
            }
            if(this.customerID == anotherKey.getCustomerID() && this.restaurantID < anotherKey.getRestaurantID()){
                return  -1;
            }
            if(this.customerID == anotherKey.getCustomerID() && this.restaurantID == anotherKey.getRestaurantID()){
                return 0;
            }
            return 1;
        }
    }

    class DateIDKey implements Comparable<DateIDKey>{
        private Date date;
        private Long id;

        public DateIDKey(Date date, Long id){
            this.id = id;
            this.date = date;
        }

        public Date getDate(){
            return date;
        }

        public Long getID(){
            return id;
        }

        @Override
        public int compareTo(DateIDKey anotherKey){
            if(this.date.after(anotherKey.getDate())){
                return -1;
            }
            if(this.date.equals(anotherKey.getDate()) && this.id < anotherKey.getID()){
                return  -1;
            }
            if(this.date.equals(anotherKey.getDate()) && this.id == anotherKey.getID()){
                return 0;
            }
            return 1;
        }
    }

    class FavouriteCountDate implements Comparable<FavouriteCountDate>{
        private int count;
        private Date date;
    
        public FavouriteCountDate(int count, Date date){
            this.count = count;
            this.date = date;
        }

        public int getCount(){
            return count;
        }

        public Date getDate(){
            return date;
        }

        @Override
        public int compareTo(FavouriteCountDate anotherKey){
            if(this.count > anotherKey.getCount()){
                return -1;
            }
            if(this.count == anotherKey.getCount() && this.date.before(anotherKey.getDate())){
                return -1;
            }
            if(this.count == anotherKey.getCount() && this.date.equals(anotherKey.getDate())){
                return 0;
            }
            return 1;
        }
    }

    class SortByRestaurantID implements Comparator<Favourite>{
        public int compare(Favourite favourite1, Favourite favourite2){
            if(favourite1.getRestaurantID() < favourite2.getRestaurantID()){
                return -1;
            }
            if(favourite1.getRestaurantID() == favourite2.getRestaurantID()){
                return 0;
            }
            return 1;
        }
    }
    
    class SortByDateRestaurantID implements Comparator<Favourite>{
        public int compare(Favourite favourite1, Favourite favourite2){
            if(favourite1.getDateFavourited().after(favourite2.getDateFavourited())){
                return -1;
            }
            if(favourite1.getDateFavourited().equals(favourite2.getDateFavourited()) && favourite1.getRestaurantID() < favourite2.getRestaurantID()){
                return -1;
            }
            if(favourite1.getDateFavourited().equals(favourite2.getDateFavourited()) && favourite1.getRestaurantID() == favourite2.getRestaurantID()){
                return 0;
            }
            return 1;
        }
    }
}
