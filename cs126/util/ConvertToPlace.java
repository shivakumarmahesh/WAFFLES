package uk.ac.warwick.cs126.util;

import uk.ac.warwick.cs126.interfaces.IConvertToPlace;
import uk.ac.warwick.cs126.models.Place;
import uk.ac.warwick.cs126.structures.MyAVLTree;
import uk.ac.warwick.cs126.structures.MyHashMap;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;

public class ConvertToPlace implements IConvertToPlace {
    
    private MyHashMap<String, Place> placesMap;
    public ConvertToPlace() {
        // Initialise things here
        Place[] placesArray = getPlacesArray();
        
        placesMap = new MyHashMap<>(16000);
        for(int i = 0; i < placesArray.length; i++){
            placesMap.add(String.valueOf(placesArray[i].getLatitude()) + String.valueOf(placesArray[i].getLongitude()), placesArray[i]);
        }
    }

    public Place convert(float latitude, float longitude) {
        return placesMap.get(String.valueOf(latitude) + String.valueOf(longitude));
    }

    public Place[] getPlacesArray() {
        Place[] placeArray = new Place[0];

        try {
            InputStream resource = ConvertToPlace.class.getResourceAsStream("/data/placeData.tsv");
            if (resource == null) {
                String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
                String resourcePath = Paths.get(currentPath, "data", "placeData.tsv").toString();
                File resourceFile = new File(resourcePath);
                resource = new FileInputStream(resourceFile);
            }

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

            Place[] loadedPlaces = new Place[lineCount - 1];

            BufferedReader tsvReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            int placeCount = 0;
            String row;

            tsvReader.readLine();
            while ((row = tsvReader.readLine()) != null) {
                if (!("".equals(row))) {
                    String[] data = row.split("\t");
                    Place place = new Place(
                            data[0],
                            data[1],
                            Float.parseFloat(data[2]),
                            Float.parseFloat(data[3]));
                    loadedPlaces[placeCount++] = place;
                }
            }
            tsvReader.close();

            placeArray = loadedPlaces;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return placeArray;
    }

    class LatitudeLongitude implements Comparable<LatitudeLongitude>{
        private float latitude;
        private float longitude;

        public LatitudeLongitude(float latitude, float longitude){
            this.latitude = latitude;
            this.longitude = longitude;
        }
        public float getLatitude(){
            return latitude;
        }
        public float getLongitude(){
            return longitude;
        }

        @Override
        public int compareTo(LatitudeLongitude anotherKey){
            if(this.latitude < anotherKey.getLatitude()){
                return -1;
            }
            if(this.latitude == anotherKey.getLatitude() && this.longitude < anotherKey.getLongitude()){
                return  -1;
            }
            if(this.latitude == anotherKey.getLatitude() && this.longitude == anotherKey.getLongitude()){
                return 0;
            }
            return 1;
        }
    }
}

