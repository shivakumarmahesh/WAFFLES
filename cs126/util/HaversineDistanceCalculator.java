package uk.ac.warwick.cs126.util;
import java.math.*;
public class HaversineDistanceCalculator {

    private final static float R = 6372.8f;
    private final static float kilometresInAMile = 1.609344f;

    public static float inKilometres(float lat1, float lon1, float lat2, float lon2) {
        double d = inPreciseKilometres(lat1, lon1, lat2, lon2);
        return (Math.round(d * 10f) / 10f);
    }

    public static float inMiles(float lat1, float lon1, float lat2, float lon2) {
        double d = inPreciseKilometres(lat1, lon1, lat2, lon2);
        d /= kilometresInAMile; // convertion to Miles
        return (Math.round(d * 10f) / 10f);
    }

    public static double inPreciseKilometres(float lat1, float lon1, float lat2, float lon2) {
        float PI = 3.141592f;
        float lat1Rad = lat1 * PI / 180f;
        float lat2Rad = lat2 * PI / 180f;
        float lon1Rad = lon1 * PI / 180f;
        float lon2Rad = lon2 * PI / 180f;
        double a = Math.sin((lat2Rad-lat1Rad) / 2) * Math.sin((lat2Rad-lat1Rad) / 2) + 
        Math.cos(lat1Rad) * Math.cos(lat2Rad) * 
        Math.sin((lon2Rad-lon1Rad) / 2) * Math.sin((lon2Rad-lon1Rad) / 2);
        double d = R*2*Math.asin(Math.sqrt(a));
        return d;
    }
}