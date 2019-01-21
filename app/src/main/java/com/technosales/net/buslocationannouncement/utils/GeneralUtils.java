package com.technosales.net.buslocationannouncement.utils;

import android.content.ContentValues;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.technosales.net.buslocationannouncement.R;
import com.technosales.net.buslocationannouncement.helper.DatabaseHelper;
import com.technosales.net.buslocationannouncement.pojo.PriceList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

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

    public static List<PriceList> priceCsv(Context context) {
        InputStream is = context.getResources().openRawResource(R.raw.price);
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        String line = "";

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-16"), 100);
            try {
                while ((line = br.readLine()) != null) {

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DatabaseHelper.PRICE_VALUE, line);
                    databaseHelper.insertPrice(contentValues);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return databaseHelper.priceLists(4);
    }
}
