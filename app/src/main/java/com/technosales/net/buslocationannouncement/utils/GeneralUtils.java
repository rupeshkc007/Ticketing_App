package com.technosales.net.buslocationannouncement.utils;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class GeneralUtils {
    public static Boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityMgr.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected())
            return true;

        return false;
    }

    public static float calculateDistance(Double startLat, Double startLng, Double endLat, Double endLng) {
        float distance;
        Location startingLocation = new Location("starting point");
        startingLocation.setLatitude(startLat);
        startingLocation.setLongitude(startLng);

        //Get the target location
        Location endingLocation = new Location("ending point");
        endingLocation.setLatitude(endLat);
        endingLocation.setLongitude(endLng);

        distance = startingLocation.distanceTo(endingLocation);

        return distance;
    }
}
