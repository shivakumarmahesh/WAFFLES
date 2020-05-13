# WAFFLES
Backend of a website devoloped using AVLTree's for the CS126 coursework at University of Warwick
# CS126 WAFFLES Coursework Report [1942271]

## CustomerStore
### Overview

* I have used an `AVLTree` structure to store and process customers because customers in the store needed to be in **sorted order** and AVL trees have the best **Worst Case gaurantees** in terms of Search, Insertion and Deletion. 
* I have used a `HashMap` structure to blacklist duplicate CustomerIDs since the have the best  **Average Case gaurantees** in terms of Search, Insertion and Deletion.
* I have used `inOrderTraversal` to return array of customers from the Store sorted by name and ID since Tree traversal is **linear** in time.
* I used `RandomizedQuickSort` to sort customers by name and ID if they are in an array, because it is the fastest comparison-based sorting algorithm, it is **inplace**, and with **high probability** is never O(n^2) in time. 

### Space Complexity

Store         | Worst Case | Description
------------- | ---------- | -----------
CustomerStore | O(n)       | I have used two `AVLTrees` to store customers, and a single `HashMap` to store blackListed IDs. <br>Where `n` is total customers added.

### Time Complexity

Method                           | Average Case     | Description
-------------------------------- | ---------------- | -----------
addCustomer(Customer c)          | O(logn)          | AVLTree insertion and deletion is logarithmic<br>`n` is total customers in the store
addCustomer(Customer[] c)        | O(nlogn)         | Add all customers <br>`n` is the length of the input array 
getCustomer(Long id)             | O(logn)          | Binary search <br>`n` is total customers in the store
getCustomers()                   | O(n)             | inOrder Traversal <br>`n` is total customers in the store
getCustomers(Customer[] c)       | O(nlogn)         | RandomizedQuickSort <br>`n` is the length of the input array
getCustomersByName()             | O(n)             | inOrder Traversal <br>`n` is total customers in the store
getCustomersByName(Customer[] c) | O(nlogn)         | RandomizedQuickSort <br>`n` is the length of the input array
getCustomersContaining(String s) | O(a + n*(a + b)) | Searches all customers <br>`a` is the average time it takes to convert accents <br>`n` is total customers <br>`b` is average string search time

<div style="page-break-after: always;"></div>

## FavouriteStore
### Overview
* I have used a single `AVLTree` to store favourites sorted by ID.
* I have used one `AVLForest` which is an *AVLTree who's Nodes are AVLTrees*, for blacklisted ID and duplicate ID replacement.
* I have used one `HashMap` to store blacklisted Favourite IDs. 
* To get favourites by customerID I have used one `AVLForest` to access a customers favourites in **time logarithmic in the number of Customers** and an identical structure for restaurants. This time is logarithmic provided The number of customers in Store dominates the number of favourites he has.
* I used `inOrderTraversal` to return an array of favourites from the store sorted by ID in **linear time**
* To get common, missing and notcommon favourite Restaurants I have used a `2-way Merge` algorithm.
* To get top Customers by Favourite count I use the existing `AVLForest` sorted by customerIDs and similarly for top Restaurants by Favourite count as well.

### Space Complexity
Store          | Worst Case | Description
-------------- | ---------- | -----------
FavouriteStore | O(n)       | I have used four `AVLTrees` <br>Where `n` is the number of favourites to be added (Invalid or Valid)

### Time Complexity
Method                                                          | Average Case     | Description
--------------------------------------------------------------- | ---------------- | -----------
addFavourite(Favourite f)                                       | O(logn)          |  AVLTree insertion and deletion is logarithmic<br>`n` is total favourites in the store.
addFavourite(Favourite[] f)                                     | O(nlogn)         |  Add all customers <br>`n` is the size of the input array
getFavourite(Long id)                                           | O(logn)          |  BinarySearch<br>`n` is total favourites in the store.
getFavourites()                                                 | O(n)             |  inOrder Traversal<br>`n` is total favourites in the store.
getFavourites(Favourite[] f)                                    | O(nlogn)         |  Tree Sort<br>`n` is the size of the input array
getFavouritesByCustomerID(Long id)                              | O(logn + m)      |  BinarySearch<br>`n` is the total unique customerIDs in the store, and `m` is the number of favourites of Customer with ID : id.
getFavouritesByRestaurantID(Long id)                            | O(logn + m)      |  BinarySearch<br>`n` is the total unique restaurantIDs in the store, and `m` is the number of favourites of Restaurant with ID : id.
getCommonFavouriteRestaurants(<br>&emsp; Long id1, Long id2)    | O(nlogn + mlogm) |  RandomizedQuickSort dominates 2-way-Merge <br>`n` is the total number of favourites of Customer with ID : id1 and `m` is the total number of favourites of Customer with ID : id2.
getMissingFavouriteRestaurants(<br>&emsp; Long id1, Long id2)   | O(nlogn + mlogm) |  RandomizedQuickSort dominates 2-way-Merge <br>`n` is the total number of favourites of Customer with ID : id1 and `m` is the total number of favourites of Customer with ID : id2.
getNotCommonFavouriteRestaurants(<br>&emsp; Long id1, Long id2) | O(nlogn + mlogm) |  RandomizedQuickSort dominates 2-way-Merge <br>`n` is the total number of favourites of Customer with ID : id1 and `m` is the total number of favourites of Customer with ID : id2.
getTopCustomersByFavouriteCount()                               | O(n)             |  <br>`n` is total favourites in the store
getTopRestaurantsByFavouriteCount()                             | O(n)             |  <br>`n` is total favourites in the store

<div style="page-break-after: always;"></div>

## RestaurantStore
### Overview
* I have used an `AVLTree` structure to store and process restaurants because restaurants in the store needed to be in **sorted order** and AVL trees have the best **Worst Case gaurantees** in terms of Search, Insertion and Deletion. 
* I have used a `HashMap` structure to blacklist duplicate RestaurantIDs since the have the best  **Average Case gaurantees** in terms of Search, Insertion and Deletion. 
* I have used `inOrderTraversal` to return array of restaurants from the Store sorted by ID, Name, Date, WarwickStars, Rating and Distance since traversal is **linear** in time.
* I used `TreeSort` to sort restaurants by  ID, Name, Date, WarwickStars, Rating and Distance since it has the best **Worst Case gaurantees** for comparison based Sorting alogirthms.

### Space Complexity
Store           | Worst Case | Description
--------------- | ---------- | -----------
RestaurantStore | O(n)     | I have used five `AVLTrees` to store restaurants each one sorted by a different Restaurant attribute, and a single `HashMap` to store blacklisted Restaurant IDs. <br>Where `n` is the total restaurants added.

### Time Complexity
Method                                                                        | Average Case     | Description
----------------------------------------------------------------------------- | ---------------- | -----------
addRestaurant(Restaurant r)                                                   | O(logn)          | AVLTree insertion  and deletion is logarithmic<br>`n` is number of restaurants in the store
addRestaurant(Restaurant[] r)                                                 | O(nlogn)         | Add all restaurants <br>`n` is the size of the input array
getRestaurant(Long id)                                                        | O(logn)          | Binarysearch<br>`n` is number of restaurants in the store
getRestaurants()                                                              | O(n)             | inOrderTraversal in linear<br>`n` is the number of restaurants in the store.
getRestaurants(Restaurant[] r)                                                | O(nlogn)         | TreeSort is linearithmic<br>`n` is the size of the input array
getRestaurantsByName()                                                        | O(n)             | inOrderTraversal in linear<br>`n` is the number of restaurants in the store.
getRestaurantsByDateEstablished()                                             | O(n)             | inOrderTraversal in linear <br>`n` is the number of restaurants in the store.
getRestaurantsByDateEstablished(<br>&emsp; Restaurant[] r)                    | O(nlogn)         | TreeSort is linearithmic<br>`n` is the size of the input array
getRestaurantsByWarwickStars()                                                | O(n)             | inOrderTraversal in linear <br>`n` is the number of restaurants in the store.
getRestaurantsByRating(Restaurant[] r)                                        | O(nlogn)         | TreeSort is linearithmic<br>`n` is the size of the input array
getRestaurantsByDistanceFrom(<br>&emsp; float lat, float lon)                 | O(nlogn)         | TreeSort is linearithmic<br>`n` is the number of restaurants in the store.
getRestaurantsByDistanceFrom(<br>&emsp; Restaurant[] r, float lat, float lon) | O(nlogn)         | TreeSort is linearithmic<br>`n` is the size of the input array
getRestaurantsContaining(String s)                                            | O(a + n*(a + b)) | Searches all restaurants <br>`a` is the average time it takes to convert accents <br>`n` is total customers <br>`b` is average string search time

<div style="page-break-after: always;"></div>

## ReviewStore
### Overview
* I have used a three `AVLTrees` to store reviews sorted by ID, Date and Rating.
* I have used one `AVLForest` which is an *AVLTree who's Nodes are AVLTrees*, for blacklisted ID and duplicate ID replacement.
* I have used one `HashMap` to store blacklisted Review IDs. 
* I used `inOrderTraversal` to return an array of reviews from the store sorted by ID in **linear time**
* To get reviews by customerID I have used one `AVLForest` to access a customers reviews in **time logarithmic in the number of Customers** and an identical structure for restaurants. This time is logarithmic provided The number of customers in Store dominates the number of reviews he has written.
* To get common, missing and notcommon review Restaurants I have used a `2-way Merge` algorithm.
* To get top Customers by Review count I use the existing `AVLForest` sorted by customerIDs and similarly for top Restaurants by Review count as well.

### Space Complexity
Store           | Worst Case | Description
--------------- | ---------- | -----------
ReviewStore     | O(...)     | I have used `...` ... <br>Where `...` is ...

### Time Complexity
Method                                     | Average Case     | Description
------------------------------------------ | ---------------- | -----------
addReview(Review r)                        | O(logn)          |  AVLTree insertion and deletion is logarithmic<br>`n` is total reviews in the store. 
addReview(Review[] r)                      | O(nlogn)         |  Add all reviews<br>`n` is the size of the input array
getReview(Long id)                         | O(logn)          |  BinarySearch<br>`n` is total reviews in the store.
getReviews()                               | O(n)             |  inOrder Traversal<br>`n` is total reviews in the store. 
getReviews(Review[] r)                     | O(nlogn)         |  TreeSort<br>`n` is the size of the input array 
getReviewsByDate()                         | O(n)             |  inOrder Traversal<br>`n` is total reviews in the store. 
getReviewsByRating()                       | O(n)             |  inOrder Traversal<br>`n` is total reviews in the store. 
getReviewsByRestaurantID(Long id)          | O(logn + m)      |  BinarySearch and array traversal<br>`n` is the total unique restaurantIDs in the store, and `m` is the number of favourites of the Restaurant with ID : id. 
getReviewsByCustomerID(Long id)            | O(logn + m)      |  BinarySearch and array traversal<br>`n` is the total unique customerIDs in the store, and `m` is the number of favourites of the Customer with ID : id.
getAverageCustomerReviewRating(Long id)    | O(logn + m)      |  BinarySearch and array traversal<br>`n` is the  total unique customerIDs in the store, and `m` is the number of favourites of the Customer with ID : id. 
getAverageRestaurantReviewRating(Long id)  | O(logn + m)      | BinarySearch and array traversal<br>`n` is the  total unique restaurantIDs in the store, and `m` is the number of favourites of the Restaurant with ID : id.
getCustomerReviewHistogramCount(Long id)   | O(logn + m)      |  BinarySearch and array traversal<br>`n` is the total unique customerIDs in the store, and `m` is the number of favourites of the Customer with ID : id.
getRestaurantReviewHistogramCount(Long id) | O(logn + m)      |  BinarySearch and array traversal<br>`n` is the total unique restaurantIDs in the store, and `m` is the number of favourites of the Restaurant with ID : id. 
getTopCustomersByReviewCount()             | O(n)             |  Array traversal<br>`n` is the total unique customerIDs in the store  
getTopRestaurantsByReviewCount()           | O(n)             |  Array traversal<br>`n` is the total unique restaurantIDs in the store  
getTopRatedRestaurants()                   | O(n)             |  Array traversal<br>`n` is the total unique restaurantIDs in the store
getTopKeywordsForRestaurant(Long id)       | O(n)             |  Array traversal<br>`n` is total number of words across all reviews written for Restaurant with ID : id. 
getReviewsContaining(String s)             | O(a + n*(a + b)) | Searches all reviews <br>`a` is the average time it takes to convert accents <br>`n` is total customers <br>`b` is average string search time

<div style="page-break-after: always;"></div>

## Util
### Overview
* **ConvertToPlace** 
    * Stores latitude longitude to Place conversions in a HashMap for efficient lookup
* **DataChecker**
    * Data checking utility
* **HaversineDistanceCalculator (HaversineDC)**
    * Utility of calculate distance between two latitude longitude co-ordinates 
* **KeywordChecker**
    * Stores keyword to index (in keyword[ ] array) conversions inside a HashMap effectively establishing a bijection between them
* **StringFormatter**
    * Stores accent to converted accent in a HashMap for efficient lookup

### Space Complexity
Util               | Worst Case | Description
-------------------| ---------- | -----------
ConvertToPlace     | O(n)       | `n` is the total number of places in the dataset
DataChecker        | O(1)       | No additional datastructure was required for this implementation
HaversineDC        | O(1)       | No additional datastructure was required for this implementation 
KeywordChecker     | O(n)       | `n` is the total number of keywords
StringFormatter    | O(n)       | `n` is the total number of accents that can be converted 

### Time Complexity
Util              | Method                                                                             | Average Case     | Description
----------------- | ---------------------------------------------------------------------------------- | ---------------- | -----------
ConvertToPlace    | convert(float lat, float lon)                                                      | O(1)             | Search and retrieval from a HashMap
DataChecker       | extractTrueID(String[] repeatedID)                                                 | O(1)             | No expensive operations or Iteration was required
DataChecker       | isValid(Long id)                                                                   | O(1)             | No expensive operations or Iteration was required
DataChecker       | isValid(Customer customer)                                                         | O(1)             | No expensive operations or Iteration was required
DataChecker       | isValid(Favourite favourite)                                                       | O(1)             | No expensive operations or Iteration was required
DataChecker       | isValid(Restaurant restaurant)                                                     | O(1)             | No expensive operations or Iteration was required
DataChecker       | isValid(Review review)                                                             | O(1)             | No expensive operations or Iteration was required
HaversineDC       | inKilometres(<br>&emsp; float lat1, float lon1, <br>&emsp; float lat2, float lon2) | O(1)             | No expensive opeations or Iteration was required
HaversineDC       | inMiles(<br>&emsp; float lat1, float lon1, <br>&emsp; float lat2, float lon2)      | O(1)             | No expensive opeations or Iteration was required
KeywordChecker    | isAKeyword(String s)                                                               | O(1)             | Search in a HashMap
StringFormatter   | convertAccentsFaster(String s)                                                     | O(k)             | `k` is the length of the string to strip accents from
