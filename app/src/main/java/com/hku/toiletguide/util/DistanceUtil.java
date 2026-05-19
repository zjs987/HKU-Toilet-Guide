package com.hku.toiletguide.util;

import android.location.Location;

public class DistanceUtil {
    private static final double HKU_LAT = 22.2836;
    private static final double HKU_LNG = 114.1370;

    private DistanceUtil() {
    }

    public static float distanceFromHkuCenter(double latitude, double longitude) {
        return distanceBetween(HKU_LAT, HKU_LNG, latitude, longitude);
    }

    public static float distanceBetween(double fromLatitude, double fromLongitude, double toLatitude, double toLongitude) {
        float[] result = new float[1];
        Location.distanceBetween(fromLatitude, fromLongitude, toLatitude, toLongitude, result);
        return result[0];
    }

    public static String metersLabel(float meters) {
        if (meters >= 1000) {
            return String.format("%.1f km", meters / 1000f);
        }
        return Math.round(meters) + " m";
    }
}
