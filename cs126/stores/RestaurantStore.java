package uk.ac.warwick.cs126.stores;

import uk.ac.warwick.cs126.interfaces.IRestaurantStore;
import uk.ac.warwick.cs126.models.Cuisine;
import uk.ac.warwick.cs126.models.EstablishmentType;
import uk.ac.warwick.cs126.models.Place;
import uk.ac.warwick.cs126.models.PriceRange;
import uk.ac.warwick.cs126.models.Restaurant;
import uk.ac.warwick.cs126.models.RestaurantDistance;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.IOUtils;

import uk.ac.warwick.cs126.structures.MyAVLTree;
import uk.ac.warwick.cs126.structures.MyHashMap;
import uk.ac.warwick.cs126.structures.MyArrayList;

import uk.ac.warwick.cs126.util.ConvertToPlace;
import uk.ac.warwick.cs126.util.HaversineDistanceCalculator;
import uk.ac.warwick.cs126.util.DataChecker;
import uk.ac.warwick.cs126.util.StringFormatter;


public class RestaurantStore implements IRestaurantStore {

    private MyAVLTree<Long,Restaurant> restaurantTreeByID;
    private MyAVLTree<RestaurantNameKey, Restaurant> restaurantTreeByName;
    private MyAVLTree<RestaurantDateKey, Restaurant> restaurantTreeByDate;
    private MyAVLTree<RestaurantWarwickStarsKey, Restaurant> restaurantTreeByWarwickStars;
    private MyAVLTree<RestaurantDistanceKey, RestaurantDistance> restaurantTreeByDistance;
    private MyHashMap<Long, Integer> blackListedID;
    private DataChecker dataChecker;

    public RestaurantStore() {
        // Initialise variables here
        restaurantTreeByID = new MyAVLTree<>();
        restaurantTreeByName = new MyAVLTree<>();
        restaurantTreeByDate = new MyAVLTree<>();
        restaurantTreeByWarwickStars = new MyAVLTree<>();
        blackListedID = new MyHashMap<>();
        dataChecker = new DataChecker();
    }

    public Restaurant[] loadRestaurantDataToArray(InputStream resource) {
        Restaurant[] restaurantArray = new Restaurant[0];

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

            Restaurant[] loadedRestaurants = new Restaurant[lineCount - 1];

            BufferedReader csvReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            String row;
            int restaurantCount = 0;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            csvReader.readLine();
            while ((row = csvReader.readLine()) != null) {
                if (!("".equals(row))) {
                    String[] data = row.split(",");

                    Restaurant restaurant = new Restaurant(
                            data[0],
                            data[1],
                            data[2],
                            data[3],
                            Cuisine.valueOf(data[4]),
                            EstablishmentType.valueOf(data[5]),
                            PriceRange.valueOf(data[6]),
                            formatter.parse(data[7]),
                            Float.parseFloat(data[8]),
                            Float.parseFloat(data[9]),
                            Boolean.parseBoolean(data[10]),
                            Boolean.parseBoolean(data[11]),
                            Boolean.parseBoolean(data[12]),
                            Boolean.parseBoolean(data[13]),
                            Boolean.parseBoolean(data[14]),
                            Boolean.parseBoolean(data[15]),
                            formatter.parse(data[16]),
                            Integer.parseInt(data[17]),
                            Integer.parseInt(data[18]));

                    loadedRestaurants[restaurantCount++] = restaurant;
                }
            }
            csvReader.close();

            restaurantArray = loadedRestaurants;

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return restaurantArray;
    }

    public boolean addRestaurant(Restaurant restaurant) {
        if(!dataChecker.isValid(restaurant)){
            return false;
        }

        //I set the true ID before checking if it is a Duplicate ID
        restaurant.setID(dataChecker.extractTrueID(restaurant.getRepeatedID()));
        if(isDuplicateID(restaurant)){
            restaurantTreeByID.remove(restaurant.getID());
            restaurantTreeByName.remove(new RestaurantNameKey(restaurant.getName(), restaurant.getID()));
            restaurantTreeByDate.remove(new RestaurantDateKey(restaurant.getDateEstablished(), restaurant.getName(), restaurant.getID()));
            restaurantTreeByWarwickStars.remove(new RestaurantWarwickStarsKey(restaurant.getWarwickStars(), restaurant.getName(), restaurant.getID()));
            blackListedID.add(restaurant.getID(), 0);
            return false;
        }

        if(blackListedID.contains(restaurant.getID())){
            return false;
        }

        restaurantTreeByID.add(restaurant.getID(), restaurant);
        restaurantTreeByName.add(new RestaurantNameKey(restaurant.getName(), restaurant.getID()), restaurant);
        restaurantTreeByDate.add(new RestaurantDateKey(restaurant.getDateEstablished(), restaurant.getName(), restaurant.getID()), restaurant);
        if(restaurant.getWarwickStars() >= 1){
            restaurantTreeByWarwickStars.add(new RestaurantWarwickStarsKey(restaurant.getWarwickStars(), restaurant.getName(), restaurant.getID()), restaurant);
        }
        return true;
    }

    public boolean addRestaurant(Restaurant[] restaurants) {
        boolean successAddingRestraunts = true;
        for(int i = 0; i < restaurants.length; i++){
            if(!addRestaurant(restaurants[i])){
                successAddingRestraunts = false;
            }
        }
        return successAddingRestraunts;
    }

    public Restaurant getRestaurant(Long id) {
        return restaurantTreeByID.get(id);
    }

    public Restaurant[] getRestaurants() {
        //inOrder Traversal
        if(!restaurantTreeByID.isEmpty()){
            Object[] arr = restaurantTreeByID.toArray();
            Restaurant[] restaurantArray = new Restaurant[arr.length];
            for(int i = 0; i < arr.length; i++){
                restaurantArray[i] = (Restaurant)arr[i];
            }
            return restaurantArray;
        }
        return new Restaurant[0];
    }

    public Restaurant[] getRestaurants(Restaurant[] restaurants) {
        //AVLTreeSort:
        if(restaurants.length != 0){
            MyAVLTree<Long, Restaurant> restaurantsByID = new MyAVLTree<>();
            for(int i = 0; i < restaurants.length; i++){
                restaurantsByID.add(dataChecker.extractTrueID(restaurants[i].getRepeatedID()), restaurants[i]);
            }
            Object[] arr = restaurantsByID.toArray();
            for(int i = 0; i < restaurants.length; i++){
                restaurants[i] = (Restaurant)arr[i];
            }
            return restaurants;
        }
        return new Restaurant[0];
    }

    public Restaurant[] getRestaurantsByName() {
        if(!restaurantTreeByName.isEmpty()){
            Object[] arr = restaurantTreeByName.toArray();
            Restaurant[] restaurantArray = new Restaurant[arr.length];
            for(int i = 0; i < arr.length; i++){
                restaurantArray[i] = (Restaurant)arr[i];
            }
            return restaurantArray;
        }
        return new Restaurant[0];
    }

    public Restaurant[] getRestaurantsByDateEstablished() {
        if(!restaurantTreeByDate.isEmpty()){
            Object[] arr = restaurantTreeByDate.toArray();
            Restaurant[] restaurantArray = new Restaurant[arr.length];
            for(int i = 0; i < arr.length; i++){
                restaurantArray[i] = (Restaurant)arr[i];
            }
            return restaurantArray;
        }
        return new Restaurant[0];
    }

    public Restaurant[] getRestaurantsByDateEstablished(Restaurant[] restaurants) {
        //TreeSort:
        if(restaurants.length != 0){
            MyAVLTree<RestaurantDateKey, Restaurant> restaurantsByDate = new MyAVLTree<>();
            for(int i = 0; i < restaurants.length; i++){
                restaurantsByDate.add(new RestaurantDateKey(restaurants[i].getDateEstablished(), restaurants[i].getName(), dataChecker.extractTrueID(restaurants[i].getRepeatedID())), restaurants[i]);
            }
            Object[] arr = restaurantsByDate.toArray();
            for(int i = 0; i < restaurants.length; i++){
                restaurants[i] = (Restaurant)arr[i];
            }
            return restaurants;
        }
        return new Restaurant[0];
    }

    public Restaurant[] getRestaurantsByWarwickStars() {
        if(!restaurantTreeByWarwickStars.isEmpty()){
            Object[] arr = restaurantTreeByWarwickStars.toArray();
            Restaurant[] restaurantArray = new Restaurant[arr.length];
            for(int i = 0; i < arr.length; i++){
                restaurantArray[i] = (Restaurant)arr[i];
            }
            return restaurantArray;
        }
        return new Restaurant[0];
    }

    public Restaurant[] getRestaurantsByRating(Restaurant[] restaurants) {
        if(restaurants.length != 0){
            MyAVLTree<RestaurantRatingKey, Restaurant> restaurantsByRating = new MyAVLTree<>();
            for(int i = 0; i < restaurants.length; i++){
                restaurantsByRating.add(new RestaurantRatingKey(restaurants[i].getCustomerRating(), restaurants[i].getName(), dataChecker.extractTrueID(restaurants[i].getRepeatedID())), restaurants[i]);
            }
            Object[] arr = restaurantsByRating.toArray();
            for(int i = 0; i < restaurants.length; i++){
                restaurants[i] = (Restaurant)arr[i];
            }
            return restaurants;
        }
        return new Restaurant[0];
    }

    public RestaurantDistance[] getRestaurantsByDistanceFrom(float latitude, float longitude) {
        if(!restaurantTreeByID.isEmpty()){
            Object[] arr = restaurantTreeByID.toArray();
            Restaurant[] restaurantArray = new Restaurant[arr.length];
            for(int i = 0; i < arr.length; i++){
                restaurantArray[i] = (Restaurant)arr[i];
            }
            MyAVLTree<RestaurantDistanceKey, RestaurantDistance> restaurantTreeByDistance = new MyAVLTree<>();
            for(int i = 0; i < restaurantArray.length; i++){
                float distance = HaversineDistanceCalculator.inKilometres(latitude, longitude, restaurantArray[i].getLatitude(), restaurantArray[i].getLongitude());
                restaurantTreeByDistance.add(new RestaurantDistanceKey(distance, restaurantArray[i].getName()), new RestaurantDistance(restaurantArray[i], distance));
            }
            arr = restaurantTreeByDistance.toArray();
            RestaurantDistance[] restaurantDistanceArray = new RestaurantDistance[arr.length];
            for(int i = 0; i < arr.length; i++){
                restaurantDistanceArray[i] = (RestaurantDistance)arr[i];
            }
            return restaurantDistanceArray;
        }
        return new RestaurantDistance[0];
    }

    public RestaurantDistance[] getRestaurantsByDistanceFrom(Restaurant[] restaurants, float latitude, float longitude) {
        if(restaurants.length != 0){
            MyAVLTree<RestaurantDistanceKey, RestaurantDistance> restaurantTreeByDistance = new MyAVLTree<>();
            for(int i = 0; i < restaurants.length; i++){
                float distance = HaversineDistanceCalculator.inKilometres(latitude, longitude, restaurants[i].getLatitude(), restaurants[i].getLongitude());
                restaurantTreeByDistance.add(new RestaurantDistanceKey(distance, restaurants[i].getName()), new RestaurantDistance(restaurants[i], distance));
            }
            Object[] arr = restaurantTreeByDistance.toArray();
            RestaurantDistance[] restaurantDistanceArray = new RestaurantDistance[arr.length];
            for(int i = 0;i < arr.length; i++){
                restaurantDistanceArray[i] = (RestaurantDistance)arr[i];
            }
            return restaurantDistanceArray;
        }
        return new RestaurantDistance[0];
    }

    public Restaurant[] getRestaurantsContaining(String searchTerm) {
        if(!restaurantTreeByID.isEmpty() && (searchTerm != null) && (searchTerm != "")){
            ConvertToPlace converter = new ConvertToPlace();
            Object[] arr = restaurantTreeByName.toArray();
            Restaurant[] restaurantArray = new Restaurant[arr.length];
            String[] placeNameArray = new String[arr.length];
            
            for(int i = 0; i < arr.length; i++){
               restaurantArray[i] = (Restaurant)arr[i];
               placeNameArray[i] = converter.convert(restaurantArray[i].getLatitude(), restaurantArray[i].getLongitude()).getName();
            }

            searchTerm = searchTerm.trim();
            searchTerm = StringFormatter.convertAccentsFaster(searchTerm);
		    searchTerm = searchTerm.toUpperCase();
            int sizeOfRestaurantsContaining = 0;
		    for(int i = 0; i < restaurantArray.length; i++){
		    	if(searchTermMatches(searchTerm, restaurantArray[i], placeNameArray[i])){
		    		sizeOfRestaurantsContaining++;
		    	}
		    }

		    Restaurant[] restaurantsContaining = new Restaurant[sizeOfRestaurantsContaining];
		    int j = 0;
	    	for(int i = 0; i < restaurantArray.length; i++){
		    	if(searchTermMatches(searchTerm, restaurantArray[i], placeNameArray[i])){
		    		restaurantsContaining[j++] = restaurantArray[i];
		    	}
		    }

		    return (restaurantsContaining); 
        }
        return new Restaurant[0];
    }

    public static boolean searchTermMatches(String str, Restaurant restaurant, String placeName){
		String restaurantName = restaurant.getName();
        String restaurantCuisine = restaurant.getCuisine().toString();
        String restaurantPlace = placeName;
        restaurantName = restaurantName.trim();
        restaurantName = StringFormatter.convertAccentsFaster(restaurantName);
		restaurantName = restaurantName.toUpperCase();
        restaurantCuisine = restaurantCuisine.trim();
        restaurantCuisine = StringFormatter.convertAccentsFaster(restaurantCuisine);
		restaurantCuisine = restaurantCuisine.toUpperCase();
        restaurantPlace = restaurantPlace.trim();
        restaurantPlace = StringFormatter.convertAccentsFaster(restaurantPlace);
		restaurantPlace = restaurantPlace.toUpperCase();
		if(str.contains(" ")){
			String[] parts = str.split(" ", 2);// "Only consider the one whitespace" ~ from the guide
			String stringLeftofSpace = parts[0];
			String stringRightofSpace = parts[1]; 

			return ((restaurantName.contains(stringLeftofSpace) && restaurantName.contains(stringRightofSpace)) || (restaurantCuisine.contains(stringLeftofSpace) && restaurantCuisine.contains(stringRightofSpace)) || (restaurantPlace.contains(stringLeftofSpace) && restaurantPlace.contains(stringRightofSpace)));
		}

		else{
			return (restaurantName.contains(str) || restaurantCuisine.contains(str) || restaurantPlace.contains(str));
		}
    }

    private boolean isDuplicateID(Restaurant restaurant){
        return restaurantTreeByID.contains(restaurant.getID());
    }

    class RestaurantNameKey implements Comparable<RestaurantNameKey>{

        private String name;
        private Long id;

        public RestaurantNameKey(String name, Long id){
            this.name = name;
            this.id = id;
        }
        public String getName() {
            return name;
        }
        public Long getID(){
            return id;
        }

        @Override
        public int compareTo(RestaurantNameKey anotherKey) {
            if(this.name.toUpperCase().compareTo(anotherKey.getName().toUpperCase()) < 0){
                return -1;
            }
            if(this.name.toUpperCase().compareTo(anotherKey.getName().toUpperCase()) == 0 && this.id < anotherKey.getID()){
                return -1;
            }
            if(this.name.toUpperCase().compareTo(anotherKey.getName().toUpperCase()) == 0 && this.id == anotherKey.getID()){
                return 0;
            }
            return 1;
        }
    }

    class RestaurantDateKey implements Comparable<RestaurantDateKey>{
        private Date date;
        private String name;
        private Long id;

        public RestaurantDateKey(Date date, String name, Long id){
            this.date = date;
            this.name = name;
            this.id = id;
        }

        public Date getDate(){
            return date;
        }
        public String getName(){
            return name;
        }
        public Long getID(){
            return id;
        }
        @Override
        public int compareTo(RestaurantDateKey anotherKey) {
            if(this.date.before(anotherKey.getDate())){
                return -1;
            }
            if(this.date.compareTo(anotherKey.getDate()) == 0 && this.name.toUpperCase().compareTo(anotherKey.getName().toUpperCase()) < 0){
                return -1;
            }
            if(this.date.compareTo(anotherKey.getDate()) == 0 && this.name.toUpperCase().compareTo(anotherKey.getName().toUpperCase()) == 0 && this.id < anotherKey.getID()){
                return -1;
            }
            if(this.date.compareTo(anotherKey.getDate()) == 0 && this.name.toUpperCase().compareTo(anotherKey.getName().toUpperCase()) == 0 && this.id == anotherKey.getID()){
                return 0;
            }
            return 1;
        }


    }

    class RestaurantWarwickStarsKey implements Comparable<RestaurantWarwickStarsKey>{
        private int stars;
        private String name;
        private long id;

        RestaurantWarwickStarsKey(int stars, String name, long id){
            this.stars = stars;
            this.name = name;
            this.id = id;
        }
        public int getStars(){
            return stars;
        }
        public String getName(){
            return name;
        }
        public long getID(){
            return id;
        }
        @Override
        public int compareTo(RestaurantWarwickStarsKey anotherKey) {
            if(this.stars > anotherKey.getStars()){
                return -1;
            }
            if(this.stars == anotherKey.getStars() && this.name.toUpperCase().compareTo(anotherKey.getName().toUpperCase()) < 0){
                return -1;
            }
            if(this.stars == anotherKey.getStars() && this.name.toUpperCase().compareTo(anotherKey.getName().toUpperCase()) == 0 && this.id < anotherKey.getID()){
                return -1;
            }
            if(this.stars == anotherKey.getStars() && this.name.toUpperCase().compareTo(anotherKey.getName().toUpperCase()) == 0 && this.id == anotherKey.getID()){
                return 0;
            }
            return 1;
        }

    }

    class RestaurantRatingKey implements Comparable<RestaurantRatingKey>{
        private float rating;
        private String name;
        private long id;

        public RestaurantRatingKey(float rating, String name, long id){
            this.rating = rating;
            this.name = name;
            this.id = id;
        }
        public float getRating(){
            return rating;
        }
        public String getName(){
            return name;
        }
        public long getID(){
            return id;
        }

        @Override
        public int compareTo(RestaurantRatingKey anotherKey) {
            if(this.rating > anotherKey.getRating()){
                return -1;
            }
            if(this.rating == anotherKey.getRating() && this.name.toUpperCase().compareTo(anotherKey.getName().toUpperCase()) < 0){
                return -1;
            }
            if(this.rating == anotherKey.getRating() && this.name.toUpperCase().compareTo(anotherKey.getName().toUpperCase()) == 0 && this.id < anotherKey.getID()){
                return -1;
            }
            if(this.rating == anotherKey.getRating() && this.name.toUpperCase().compareTo(anotherKey.getName().toUpperCase()) == 0 && this.id == anotherKey.getID()){
                return 0;
            }
            return 1;
        }


    }

    class RestaurantDistanceKey implements Comparable<RestaurantDistanceKey>{
        private float distance;
        private String name;

        public RestaurantDistanceKey(float distance, String name){
            this.distance = distance;
            this.name = name;
        }

        public float getDistance(){
            return distance;
        }

        public String getName(){
            return name;
        }

        @Override
        public int compareTo(RestaurantDistanceKey anotherKey) {
            if(this.distance < anotherKey.getDistance()){
                return -1;
            }
            if(this.distance == anotherKey.getDistance() && this.name.toUpperCase().compareTo(anotherKey.getName().toUpperCase()) < 0){
                return -1;
            }
            if(this.distance == anotherKey.getDistance() && this.name.toUpperCase().compareTo(anotherKey.getName().toUpperCase()) == 0){
                return 0;
            }
            return 1;
        }

    }
}

