package uk.ac.warwick.cs126.stores;

import uk.ac.warwick.cs126.interfaces.IReviewStore;
import uk.ac.warwick.cs126.models.Review;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Comparator;
import java.util.Calendar;

import org.apache.commons.io.IOUtils;

import uk.ac.warwick.cs126.structures.MyAVLTree;
import uk.ac.warwick.cs126.structures.MyArrayList;
import uk.ac.warwick.cs126.structures.MyHashMap;
import uk.ac.warwick.cs126.util.DataChecker;
import uk.ac.warwick.cs126.util.KeywordChecker;
import uk.ac.warwick.cs126.util.StringFormatter;

public class ReviewStore implements IReviewStore {

    private MyAVLTree<Long, Review> reviewTreeByID;
    private MyAVLTree<DateIDKey, Review> reviewTreeByDate;
    private MyAVLTree<RatingLatestDateIDKey, Review> reviewTreeByRating;
    
    //I call these Forests to distinguish them from Regular Trees.
    private MyAVLTree<CustomerRestaurantKey, MyAVLTree<LatestDateKey, Review>> customerRestaurantForest;
    private MyAVLTree<Long, MyAVLTree<DateIDKey, Review>> reviewForestByCustomerID;
    private MyAVLTree<Long, MyAVLTree<DateIDKey, Review>> reviewForestByRestaurantID;
    private MyHashMap<Long, Integer> blackListedID;
    private DataChecker dataChecker;

    public ReviewStore() {
        // Initialise variables here
        reviewTreeByID = new MyAVLTree<>();
        reviewTreeByDate = new MyAVLTree<>();
        customerRestaurantForest = new MyAVLTree<>();
        reviewTreeByRating = new MyAVLTree<>();
        reviewForestByCustomerID = new MyAVLTree<>();
        reviewForestByRestaurantID = new MyAVLTree<>();
        blackListedID = new MyHashMap<>();
        dataChecker = new DataChecker();
    }

    public Review[] loadReviewDataToArray(InputStream resource) {
        Review[] reviewArray = new Review[0];

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

            Review[] loadedReviews = new Review[lineCount - 1];

            BufferedReader tsvReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            int reviewCount = 0;
            String row;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            tsvReader.readLine();
            while ((row = tsvReader.readLine()) != null) {
                if (!("".equals(row))) {
                    String[] data = row.split("\t");
                    Review review = new Review(
                            Long.parseLong(data[0]),
                            Long.parseLong(data[1]),
                            Long.parseLong(data[2]),
                            formatter.parse(data[3]),
                            data[4],
                            Integer.parseInt(data[5]));
                    loadedReviews[reviewCount++] = review;
                }
            }
            tsvReader.close();

            reviewArray = loadedReviews;

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return reviewArray;
    }

    public boolean addReview(Review review) {
        if(!dataChecker.isValid(review)){
            return false;
        }
        if(isDuplicateReviewID(review)){
            //Remove existing review in Tree with the ID reivew.getID()
            Review removedReview = reviewTreeByID.get(review.getID());
            reviewTreeByID.remove(review.getID());
            reviewTreeByDate.remove(new DateIDKey(review.getDateReviewed(), review.getID()));
            reviewTreeByRating.remove(new RatingLatestDateIDKey(review.getRating(), review.getDateReviewed(), review.getID()));

            customerRestaurantForest.get(
            new CustomerRestaurantKey(removedReview.getCustomerID(), removedReview.getRestaurantID())
            ).remove(new LatestDateKey(removedReview.getDateReviewed()));
            

            Review replacementReview = customerRestaurantForest.get(
                new CustomerRestaurantKey(removedReview.getCustomerID(), removedReview.getRestaurantID())
                ).getMinimum();
            
            if(replacementReview != null){
                reviewTreeByID.add(replacementReview.getID(), replacementReview);
                reviewTreeByDate.add(new DateIDKey(replacementReview.getDateReviewed(), replacementReview.getID()), replacementReview);
                reviewTreeByRating.add(new RatingLatestDateIDKey(replacementReview.getRating(), replacementReview.getDateReviewed(), replacementReview.getID()), replacementReview);

                blackListedID.remove(replacementReview.getID());

                //Adding the replacement into reviewForestByCustomerID:
                if(reviewForestByCustomerID.contains(replacementReview.getCustomerID())){
                    //Tree exists
                    reviewForestByCustomerID.get(replacementReview.getCustomerID()).add(new DateIDKey(replacementReview.getDateReviewed(), replacementReview.getID()), replacementReview);
                }
                else{
                    //Make a new Tree inside the Forest:
                    reviewForestByCustomerID.add(replacementReview.getCustomerID(), new MyAVLTree<DateIDKey, Review>(new DateIDKey(replacementReview.getDateReviewed(), replacementReview.getID()), replacementReview));
                }

                //Adding the replacement into reviewForestByRestaurantID:
                if(reviewForestByRestaurantID.contains(replacementReview.getRestaurantID())){
                    //Tree exists
                    reviewForestByRestaurantID.get(replacementReview.getRestaurantID()).add(new DateIDKey(replacementReview.getDateReviewed(), replacementReview.getID()), replacementReview);
                }
                else{
                    //Make a new Tree inside the Forest:
                    reviewForestByRestaurantID.add(replacementReview.getRestaurantID(), new MyAVLTree<DateIDKey, Review>(new DateIDKey(replacementReview.getDateReviewed(), replacementReview.getID()), replacementReview));
                }
            }
            else{ // if replacementReview == null
                customerRestaurantForest.remove(new CustomerRestaurantKey(removedReview.getCustomerID(), removedReview.getRestaurantID()));
            }

            blackListedID.add(review.getID(), 0);
            return false;
        }
        if(blackListedID.contains(review.getID())){
            return false;
        }

        //REPLACEMENT OF EXISTING CUSTOMER-RESTAURANT review 
        if(isDuplicateCustomerRestaurantID(review)){
            Review prevNewestReview = customerRestaurantForest.get(
                new CustomerRestaurantKey(review.getCustomerID(), review.getRestaurantID())).getMinimum();

            //Add Duplicate CustomerRestaurant Review to Forest.
            customerRestaurantForest.get(
                new CustomerRestaurantKey(review.getCustomerID(), review.getRestaurantID())).add(new LatestDateKey(review.getDateReviewed()), review);

            //Determine the new Latest Review
            Review newNewestReview = customerRestaurantForest.get(
                new CustomerRestaurantKey(review.getCustomerID(), review.getRestaurantID())).getMinimum();
            
            if(newNewestReview.getDateReviewed().compareTo(prevNewestReview.getDateReviewed()) > 0)
            {
                //Replacement in reviewTreeByID:
                reviewTreeByID.remove(prevNewestReview.getID());
                reviewTreeByID.add(newNewestReview.getID(), newNewestReview);
                //Replacement in reviewTreeByDate:
                reviewTreeByDate.remove(new DateIDKey(prevNewestReview.getDateReviewed(), prevNewestReview.getID()));
                reviewTreeByDate.add(new DateIDKey(newNewestReview.getDateReviewed(), newNewestReview.getID()), newNewestReview);
                //Replacement in reviewTreeByRating:
                reviewTreeByRating.remove(new RatingLatestDateIDKey(prevNewestReview.getRating(), prevNewestReview.getDateReviewed(), prevNewestReview.getID()));
                reviewTreeByRating.add(new RatingLatestDateIDKey(newNewestReview.getRating(), newNewestReview.getDateReviewed(), newNewestReview.getID()), review);
                
                blackListedID.add(prevNewestReview.getID(), 0);

                //REPLACEMENT INSIDE reviewForestByCustomerID:
                    //First Remove existing Review:
                    reviewForestByCustomerID.get(prevNewestReview.getCustomerID()).remove(new DateIDKey(prevNewestReview.getDateReviewed(), prevNewestReview.getID()));
                    //Second Add Replacement Review:
                if(reviewForestByCustomerID.contains(newNewestReview.getCustomerID())){
                    //Tree exists
                    reviewForestByCustomerID.get(newNewestReview.getCustomerID()).add(new DateIDKey(newNewestReview.getDateReviewed(), newNewestReview.getID()), newNewestReview);
                }
                else{
                    //Make a new Tree inside the Forest:
                    reviewForestByCustomerID.add(newNewestReview.getCustomerID(), new MyAVLTree<DateIDKey, Review>(new DateIDKey(newNewestReview.getDateReviewed(), newNewestReview.getID()), newNewestReview));
                }

                //REPLACEMENT INSIDE reviewForestByRestaurantID:
                    //First Remove existing Review:
                    reviewForestByRestaurantID.get(prevNewestReview.getRestaurantID()).remove(new DateIDKey(prevNewestReview.getDateReviewed(), prevNewestReview.getID()));
                    //Second Add Replacement Review:
                if(reviewForestByRestaurantID.contains(newNewestReview.getRestaurantID())){
                    //Tree exists
                    reviewForestByRestaurantID.get(newNewestReview.getRestaurantID()).add(new DateIDKey(newNewestReview.getDateReviewed(), newNewestReview.getID()), newNewestReview);
                }
                else{
                    //Make a new Tree inside the Forest:
                    reviewForestByRestaurantID.add(newNewestReview.getRestaurantID(), new MyAVLTree<DateIDKey, Review>(new DateIDKey(newNewestReview.getDateReviewed(), newNewestReview.getID()), newNewestReview));
                }


                return true;
            }
            return false;
        }

        // The fabulous case with Valid Non-duplicate Review:
        reviewTreeByID.add(review.getID(), review);
        reviewTreeByDate.add(new DateIDKey(review.getDateReviewed(), review.getID()), review);
        reviewTreeByRating.add(new RatingLatestDateIDKey(review.getRating(), review.getDateReviewed(), review.getID()), review);
        customerRestaurantForest.add(new CustomerRestaurantKey(review.getCustomerID(), review.getRestaurantID()), new MyAVLTree<LatestDateKey, Review>(new LatestDateKey(review.getDateReviewed()), review));
        //Add into reviewForestByCustomerID:
        if(reviewForestByCustomerID.contains(review.getCustomerID())){
            reviewForestByCustomerID.get(review.getCustomerID()).add(new DateIDKey(review.getDateReviewed(), review.getID()), review);
        }
        else{
            reviewForestByCustomerID.add(review.getCustomerID(), new MyAVLTree<DateIDKey, Review>(new DateIDKey(review.getDateReviewed(), review.getID()), review));
        }
        //Add into reviewForestByRestaurantID:
        if(reviewForestByRestaurantID.contains(review.getRestaurantID())){
            reviewForestByRestaurantID.get(review.getRestaurantID()).add(new DateIDKey(review.getDateReviewed(), review.getID()), review);
        }
        else{
            reviewForestByRestaurantID.add(review.getRestaurantID(), new MyAVLTree<DateIDKey, Review>(new DateIDKey(review.getDateReviewed(), review.getID()), review));
        }
        
        
        return true;
    }

    public boolean addReview(Review[] reviews) {
        boolean successAddingReviews = true;
        for(int i = 0; i < reviews.length; i++){
            if(!addReview(reviews[i])){
                successAddingReviews = false;
            }
        }
        return successAddingReviews;
    }

    public Review getReview(Long id) {
        return reviewTreeByID.get(id);
        
    }

    public Review[] getReviews() {
        if(!reviewTreeByID.isEmpty()){
            Object[] arr = reviewTreeByID.toArray();
            Review[] reviewArray = new Review[arr.length];
            for(int i = 0; i < arr.length; i++){
                reviewArray[i] = (Review)arr[i];
            }
            return reviewArray;
        }
        return new Review[0];
    }

    public Review[] getReviewsByDate() {
        if(!reviewTreeByDate.isEmpty()){
            Object[] arr = reviewTreeByDate.toArray();
            Review[] reviewArrayByDate = new Review[arr.length];
            for(int i = 0; i < arr.length; i++){
                reviewArrayByDate[i] = (Review)arr[i];
            }
            return reviewArrayByDate;
        }
        return new Review[0];
    }

    public Review[] getReviewsByRating() {
        if(!reviewTreeByRating.isEmpty()){
            Object[] arr = reviewTreeByRating.toArray();
            Review[] reviewArrayByRating = new Review[arr.length];
            for(int i = 0; i < arr.length; i++){
                reviewArrayByRating[i] = (Review)arr[i];
            }
            return reviewArrayByRating;
        }
        return new Review[0];
    }

    public Review[] getReviewsByCustomerID(Long id) {
        if(reviewForestByCustomerID.contains(id)){
            Object[] arr = reviewForestByCustomerID.get(id).toArray();
            Review[] reviewsByCustomerIDArray = new Review[arr.length];
            for(int i = 0; i < arr.length; i++){
                reviewsByCustomerIDArray[i] = (Review)arr[i];
            }
            return reviewsByCustomerIDArray;
        }
        return new Review[0];
    }

    public Review[] getReviewsByRestaurantID(Long id) {
        if(reviewForestByRestaurantID.contains(id)){
            Object[] arr = reviewForestByRestaurantID.get(id).toArray();
            Review[] reviewsByRestaurantIDArray = new Review[arr.length];
            for(int i = 0; i < arr.length; i++){
                reviewsByRestaurantIDArray[i] = (Review)arr[i];
            }
            return reviewsByRestaurantIDArray;
        }
        return new Review[0];
    }

    public float getAverageCustomerReviewRating(Long id) {
        if(reviewForestByCustomerID.contains(id)){
            Review[] reviewsByCustomer = getReviewsByCustomerID(id);
            float averageRating = 0;
            for(int i = 0; i < reviewsByCustomer.length; i++){
                averageRating += reviewsByCustomer[i].getRating();
            }
            return Math.round((averageRating)/(reviewsByCustomer.length) * 10.0f) / 10.0f;
        }
        return 0.0f;
    }

    public float getAverageRestaurantReviewRating(Long id) {
        if(reviewForestByRestaurantID.contains(id)){
            Review[] reviewsByRestaurant = getReviewsByRestaurantID(id);
            float averageRating = 0;
            for(int i = 0; i < reviewsByRestaurant.length; i++){
                averageRating += reviewsByRestaurant[i].getRating();
            }
            return Math.round((averageRating)/(reviewsByRestaurant.length) * 10.0f) / 10.0f;
        }
        return 0.0f;
    }

    public int[] getCustomerReviewHistogramCount(Long id) {
        Review[] reviewsOfCustomer = getReviewsByCustomerID(id);
        if(reviewsOfCustomer.length != 0){
            int[] histogram = new int[5];
            for(int i = 0; i < reviewsOfCustomer.length; i++){
                if(reviewsOfCustomer[i].getRating() == 1){
                    histogram[0]++;
                }   
                else if(reviewsOfCustomer[i].getRating() == 2){
                    histogram[1]++;
                } 
                else if(reviewsOfCustomer[i].getRating() == 3){
                    histogram[2]++;
                }
                else if(reviewsOfCustomer[i].getRating() == 4){
                    histogram[3]++;
                }
                else{
                    histogram[4]++;
                }
            }
            return histogram;
        }
        return new int[5];
    }

    public int[] getRestaurantReviewHistogramCount(Long id) {
        Review[] reviewsOfRestaurant = getReviewsByRestaurantID(id);
        if(reviewsOfRestaurant.length != 0){
            int[] histogram = new int[5];
            for(int i = 0; i < reviewsOfRestaurant.length; i++){
                if(reviewsOfRestaurant[i].getRating() == 1){
                    histogram[0]++;
                }   
                else if(reviewsOfRestaurant[i].getRating() == 2){
                    histogram[1]++;
                } 
                else if(reviewsOfRestaurant[i].getRating() == 3){
                    histogram[2]++;
                }
                else if(reviewsOfRestaurant[i].getRating() == 4){
                    histogram[3]++;
                }
                else{
                    histogram[4]++;
                }
            }
            return histogram;
        }
        return new int[5];
    }

    public Long[] getTopCustomersByReviewCount() {
        if(!reviewForestByCustomerID.isEmpty()){
            Object[] arr = reviewForestByCustomerID.toArray();
            MyAVLTree<DateIDKey, Review>[] arrayOfCustomerIDTrees = new MyAVLTree[arr.length];
            for(int i = 0; i < arr.length; i++){
                arrayOfCustomerIDTrees[i] = (MyAVLTree<DateIDKey, Review>)arr[i];
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
                        if((new ReviewCountDate(arrayOfCustomerIDTrees[topIndex].size(), arrayOfCustomerIDTrees[topIndex].getMinimum().getDateReviewed()).compareTo(
                            new ReviewCountDate(arrayOfCustomerIDTrees[j].size(), arrayOfCustomerIDTrees[j].getMinimum().getDateReviewed())
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

    public Long[] getTopRestaurantsByReviewCount() {
        if(!reviewForestByRestaurantID.isEmpty()){
            Object[] arr = reviewForestByRestaurantID.toArray();
            MyAVLTree<DateIDKey, Review>[] arrayOfRestaurantIDTrees = new MyAVLTree[arr.length];
            for(int i = 0; i < arr.length; i++){
                arrayOfRestaurantIDTrees[i] = (MyAVLTree<DateIDKey, Review>)arr[i];
            }


            int topRestaurantIDsize = (arrayOfRestaurantIDTrees.length > 20)? 20:arrayOfRestaurantIDTrees.length;
            Long[] topRestaurantIDs = new Long[20];
            for(int i = 0; i < topRestaurantIDsize; i++){
                int topIndex = 0;
                for(int j = 1; j < arrayOfRestaurantIDTrees.length; j++){
                    while(arrayOfRestaurantIDTrees[topIndex] == null){
                        topIndex++;
                    }
                    if(arrayOfRestaurantIDTrees[j] != null){
                        if((new ReviewCountDate(arrayOfRestaurantIDTrees[topIndex].size(), arrayOfRestaurantIDTrees[topIndex].getMinimum().getDateReviewed()).compareTo(
                            new ReviewCountDate(arrayOfRestaurantIDTrees[j].size(), arrayOfRestaurantIDTrees[j].getMinimum().getDateReviewed())
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

    public Long[] getTopRatedRestaurants() {
        
        if(!reviewForestByRestaurantID.isEmpty()){
            //Obtain the Array whose elements are AVL Trees
            Object[] arr = reviewForestByRestaurantID.toArray();
            MyAVLTree<DateIDKey, Review>[] arrayOfRestaurantIDTrees = new MyAVLTree[arr.length];
            for(int i = 0; i < arr.length; i++){
                arrayOfRestaurantIDTrees[i] = (MyAVLTree<DateIDKey, Review>)arr[i];
            }

            //Obtain Average Rating of Every Restaurant in an Array:
            float[] averageRatings = new float[arrayOfRestaurantIDTrees.length];
            
            for(int i = 0; i < averageRatings.length; i++){
                Object[] arrayOfReviews = arrayOfRestaurantIDTrees[i].toArray();
                for(int k = 0; k < arrayOfReviews.length; k++){
                    averageRatings[i] += ((Review)arrayOfReviews[k]).getRating();
                }
                averageRatings[i] /= arrayOfReviews.length;
            }

            //Determine the top 20 (or less) Restaurants with Best Ratings sorted by Rating, OldestDate then RestaurantID.
            int topRestaurantIDsize = (arrayOfRestaurantIDTrees.length > 20)? 20:arrayOfRestaurantIDTrees.length;
            Long[] topRestaurantIDs = new Long[20];
            for(int i = 0; i < topRestaurantIDsize; i++){
                int topIndex = 0;
                for(int j = 1; j < arrayOfRestaurantIDTrees.length; j++){
                    while(arrayOfRestaurantIDTrees[topIndex] == null){
                        topIndex++;
                    }
                    if((arrayOfRestaurantIDTrees[j] != null)){
                        RatingOldestDateIDKey topRatingOldestDateIDKey = new RatingOldestDateIDKey(averageRatings[topIndex], arrayOfRestaurantIDTrees[topIndex].getMinimum().getDateReviewed(), arrayOfRestaurantIDTrees[topIndex].getRootValue().getRestaurantID());
                        RatingOldestDateIDKey jRatingOldestDateIDKey = new RatingOldestDateIDKey(averageRatings[j], arrayOfRestaurantIDTrees[j].getMinimum().getDateReviewed(), arrayOfRestaurantIDTrees[j].getRootValue().getRestaurantID());
                        if(topRatingOldestDateIDKey.compareTo(jRatingOldestDateIDKey) > 0){
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

    public String[] getTopKeywordsForRestaurant(Long id) {
        //This implementation utilises the fact that there exists a bijection between keywords, and the indices of those keywords in KeywordChecker.keywords[]
        KeywordChecker keywordChecker = new KeywordChecker();
        Review[] reviewsOfRestaurant = getReviewsByRestaurantID(id);
        int[] keywordFrequencies = new int[82];
        for(int i = 0; i < reviewsOfRestaurant.length; i++){
            String[] wordsInReview = reviewsOfRestaurant[i].getReview().split(" ");
            for(int j = 0; j < wordsInReview.length; j++){
                wordsInReview[j] = wordsInReview[j].replaceAll("\\W", ""); //remove punctuation.
                if(keywordChecker.isAKeyword(wordsInReview[j])) {
                    keywordFrequencies[keywordChecker.getIndexOfKeyword(wordsInReview[j])]++;
                }
            }
        }

        int nonZeroFrequenciesCount = 0;
        for(int i = 0; i < keywordFrequencies.length; i++){
            if(keywordFrequencies[i] != 0){
                nonZeroFrequenciesCount++;
            }
        }

        nonZeroFrequenciesCount = (nonZeroFrequenciesCount > 5)? 5 : nonZeroFrequenciesCount;

        //This nested Loop has at most 5*(82 - 1) = 405 iterations 
        String[] topKeywords = new String[5];
        int k = 0;
        for(int i = 0; i < nonZeroFrequenciesCount; i++){
            int topIndex = 0;
            for(int j = 1; j < 82; j++){
                while(keywordFrequencies[topIndex] == -1){
                    topIndex++;
                }
                if(keywordFrequencies[j] != -1){
                    if(keywordFrequencies[topIndex] < keywordFrequencies[j] || ((keywordFrequencies[topIndex] == keywordFrequencies[j] && keywordChecker.getKeyword(topIndex).compareTo(keywordChecker.getKeyword(j)) > 0))){
                        topIndex = j;
                    }
                }
            }
            topKeywords[k++] = keywordChecker.getKeyword(topIndex);
            keywordFrequencies[topIndex] = -1;
        }

        return topKeywords;

    }

    public Review[] getReviewsContaining(String searchTerm) {
        if((!reviewTreeByDate.isEmpty()) && (searchTerm != null) && (searchTerm != "")){
            searchTerm = StringFormatter.convertAccentsFaster(searchTerm);
            searchTerm = searchTerm.toUpperCase();
            Object[] reviewTreeArray = reviewTreeByDate.toArray();

            int sizeOfReviewsContaining = 0;
            for(int i = 0; i < reviewTreeArray.length; i++){
                if(searchTermMatches(searchTerm, (Review)reviewTreeArray[i])){
                    sizeOfReviewsContaining++;
                }
            }

            Review[] reviewsContaining = new Review[sizeOfReviewsContaining];
            int k = 0;
            for(int i = 0; i < reviewTreeArray.length; i++){
                if(searchTermMatches(searchTerm, (Review)reviewTreeArray[i])){
                    reviewsContaining[k++] = (Review)reviewTreeArray[i];
                }
            }

            return reviewsContaining;
        }

        return new Review[0];
    }

    public static boolean searchTermMatches(String searchTerm, Review review){
        String reviewString = review.getReview();
        reviewString = StringFormatter.convertAccentsFaster(reviewString);
        reviewString = reviewString.toUpperCase();
        return reviewString.contains(searchTerm);
    }

    //This is to check if there exists a Duplicate reviewID already in the store
    public boolean isDuplicateReviewID(Review review){
        return reviewTreeByID.contains(review.getID());
    }
    //This is to check if there exists a Duplicate customerRestaurantID pair already in the store
    private boolean isDuplicateCustomerRestaurantID(Review review){
        return customerRestaurantForest.contains(new CustomerRestaurantKey(review.getCustomerID(), review.getRestaurantID()));
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            
            if(sdf.format(this.date).equals(sdf.format(anotherKey.getDate()))){
                if(this.id < anotherKey.getID()){
                    return -1;
                }
                if(this.id == anotherKey.getID()){
                    return 0;
                }
                return 1;
            }
            if(this.date.after(anotherKey.getDate())){
                return -1;
            }
            return 1;
           
        }
    }

    class LatestDateKey implements Comparable<LatestDateKey>{
        private Date date;

        public LatestDateKey(Date date){
            this.date = date;
        }
        public Date getDate(){
            return date;
        }

        @Override
        public int compareTo(LatestDateKey anotherKey){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            if(sdf.format(this.date).equals(sdf.format(anotherKey.getDate()))){
                return 0;
            }
            if(this.date.after(anotherKey.getDate())){
                return  -1;
            }
            return 1;
        }
    }

    class RatingLatestDateIDKey implements Comparable<RatingLatestDateIDKey>{
        private float rating;
        private Date date;
        private Long id;

        public RatingLatestDateIDKey(float rating, Date date, Long id){
            this.rating = rating;
            this.date = date;
            this.id = id;
        }

        public float getRating(){
            return rating;
        }
        public Date getDate(){
            return date;
        }
        
        public Long getID(){
            return id;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        public int compareTo(RatingLatestDateIDKey anotherKey){
            if(this.rating > anotherKey.getRating()){
                return -1;
            }
            if(this.rating == anotherKey.getRating() && (sdf.format(this.date).equals(sdf.format(anotherKey.getDate())))){
                if(this.id < anotherKey.getID()){
                    return -1;
                }
                if(this.id.equals(anotherKey.getID())){
                    return 0;
                }
                return 1;
            }
            if(this.rating == anotherKey.getRating() && (this.date.after(anotherKey.getDate()))){
                return -1;
            }
            return 1;
        }
    }

    class RatingOldestDateIDKey implements Comparable<RatingOldestDateIDKey>{
        private float rating;
        private Date date;
        private Long id;

        public RatingOldestDateIDKey(float rating, Date date, Long id){
            this.rating = rating;
            this.date = date;
            this.id = id;
        }

        public float getRating(){
            return rating;
        }
        public Date getDate(){
            return date;
        }
        
        public Long getID(){
            return id;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        public int compareTo(RatingOldestDateIDKey anotherKey){
            if(this.rating > anotherKey.getRating()){
                return -1;
            }
            if(this.rating == anotherKey.getRating() && (sdf.format(this.date).equals(sdf.format(anotherKey.getDate())))){
                if(this.id < anotherKey.getID()){
                    return -1;
                }
                if(this.id.equals(anotherKey.getID())){
                    return 0;
                }
                return 1;
            }
            if(this.rating == anotherKey.getRating() && (this.date.before(anotherKey.getDate()))){
                return -1;
            }
            return 1;
        }
    }

    class ReviewCountDate implements Comparable<ReviewCountDate>{
        private int count;
        private Date date;
    
        public ReviewCountDate(int count, Date date){
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
        public int compareTo(ReviewCountDate anotherKey){
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
}
