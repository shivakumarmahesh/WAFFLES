package uk.ac.warwick.cs126.util;

import java.lang.annotation.Repeatable;
import java.math.BigDecimal;
import java.math.BigInteger;
import uk.ac.warwick.cs126.interfaces.IDataChecker;

import uk.ac.warwick.cs126.models.Customer;
import uk.ac.warwick.cs126.models.Restaurant;
import uk.ac.warwick.cs126.models.Favourite;
import uk.ac.warwick.cs126.models.Review;

import java.util.Date;

public class DataChecker implements IDataChecker {

    public DataChecker() {
        // Initialise things here
    }

    public Long extractTrueID(String[] repeatedID) {
        if(repeatedID.length != 3){
            return null;
        }
        if(repeatedID[0].length() != 16 || repeatedID[1].length() != 16 || repeatedID[2].length() != 16){
            return null;
        }
        if(repeatedID[0].equals(repeatedID[1])){
            return Long.parseLong(repeatedID[0]);
        }
        if(repeatedID[1].equals(repeatedID[2])){
            return Long.parseLong(repeatedID[1]);
        }
        if(repeatedID[2].equals(repeatedID[0])){
            return Long.parseLong(repeatedID[2]);
        }
        return null;
    }

    public boolean isValid(Long inputID) {
        if(inputID % Math.pow(10,16) != inputID){
            return false;
        }

        int[] digitCounts = new int[9];
        for(int i = 1; i <= 16; i++){
            //double digit = ((inputID % Math.pow(10,i)) - (inputID % Math.pow(10,i-1))) / (Math.pow(10,i-1)); //BRACKETING
            BigDecimal bigDigit;
            double tenToThei = Math.pow(10,i);
            double tenToTheiMinusOne = Math.pow(10, i-1);
            BigInteger exp1 = BigInteger.valueOf(inputID).remainder(BigInteger.valueOf((long)tenToThei));
            BigInteger exp2 = BigInteger.valueOf(inputID).remainder(BigInteger.valueOf((long)tenToTheiMinusOne));
            BigDecimal exp3 = new BigDecimal(exp1.subtract(exp2));
            BigDecimal exp4 = new BigDecimal(tenToTheiMinusOne);
            bigDigit = exp3.divide(exp4);
            int digit = bigDigit.intValue();

            if(digit == 0){
                return false;

            }
            digitCounts[digit - 1]++;
            if(digitCounts[digit - 1] > 3){
                return false;
            }
        }
        return true;

    }

    public boolean isValid(Customer customer) {
        if(customer == null){
            return false;
        }
        if((customer.getID() == null) || (customer.getFirstName() == null) || (customer.getLastName() == null) || (customer.getDateJoined() == null)){
            return false;
        }
        if(!isValid(customer.getID())){
            return false;
        }
        return true;

    }

    public boolean isValid(Restaurant restaurant) {
        if(restaurant == null){
            return false;
        }
        if((extractTrueID(restaurant.getRepeatedID()) == null) || (restaurant.getName() == null) || (restaurant.getOwnerFirstName() == null) || (restaurant.getOwnerLastName() == null) || (restaurant.getCuisine() == null) || (restaurant.getEstablishmentType() == null) || (restaurant.getPriceRange() == null) || (restaurant.getDateEstablished() == null) || (restaurant.getLastInspectedDate() == null)){
            return false;
        }
        if(!isValid(extractTrueID(restaurant.getRepeatedID()))){
            return false;
        }
        if(restaurant.getLastInspectedDate().before(restaurant.getDateEstablished())){
            return false;
        }
        if(!(restaurant.getFoodInspectionRating() == 0 || restaurant.getFoodInspectionRating() == 1 || restaurant.getFoodInspectionRating() == 2 || restaurant.getFoodInspectionRating() == 3 || restaurant.getFoodInspectionRating() == 4 || restaurant.getFoodInspectionRating() == 5)){
            return false;
        }
        if(!(restaurant.getWarwickStars() == 0 || restaurant.getWarwickStars() == 1 || restaurant.getWarwickStars() == 2 || restaurant.getWarwickStars() == 3)){
            return false;
        }
        if(!(restaurant.getCustomerRating() == 0.0f || (restaurant.getCustomerRating() >= 1 && restaurant.getCustomerRating() <= 5))){
            return false;
        }

        return true;
    }

    public boolean isValid(Favourite favourite) {
        if(favourite == null){
            return false;
        }
        if(favourite.getID() == null || favourite.getCustomerID() == null || favourite.getRestaurantID() == null || favourite.getDateFavourited() == null){
            return false;
        }
        if(!isValid(favourite.getID()) || !isValid(favourite.getCustomerID()) || !isValid(favourite.getRestaurantID())){
            return false;
        }
        return true;
    }

    public boolean isValid(Review review) {
        if(review == null){
            return false;
        }
        if(review.getID() == null || review.getCustomerID() == null || review.getRestaurantID() == null || review.getDateReviewed() == null || review.getReview() == null){
            return false;
        } 
        if(!isValid(review.getID()) || !isValid(review.getCustomerID()) || !isValid(review.getRestaurantID())){
            return false;
        }
        return true;
    }
}