package com.technosales.net.buslocationannouncement.utils;

import android.content.ContentValues;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import com.technosales.net.buslocationannouncement.R;
import com.technosales.net.buslocationannouncement.helper.DatabaseHelper;
import com.technosales.net.buslocationannouncement.pojo.PriceList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

                    String[] priceDistance = line.split("-");
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DatabaseHelper.PRICE_VALUE, priceDistance[0]);
                    contentValues.put(DatabaseHelper.PRICE_MIN_DISTANCE, priceDistance[1]);
                    contentValues.put(DatabaseHelper.PRICE_DISTANCE, priceDistance[2]);
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

    public static String getDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyMMdd");
        return df.format(c.getTime());
    }

    public static String getFullDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-M-dd");
        return df.format(c.getTime());
    }

    public static String getTime() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        return df.format(c.getTime());
    }

    public static String getUnicodeNumber(String number) {
        String unicodeChar = "";
        for (int i = 0; i < number.length(); i++) {
            char character = number.charAt(i);
            String valueOfchar = String.valueOf(character);
            if (valueOfchar.equals("1")) {
                valueOfchar = "१";
            } else if (valueOfchar.equals("2")) {
                valueOfchar = "२";
            } else if (valueOfchar.equals("3")) {
                valueOfchar = "३";
            } else if (valueOfchar.equals("4")) {
                valueOfchar = "४";
            } else if (valueOfchar.equals("5")) {
                valueOfchar = "५";
            } else if (valueOfchar.equals("6")) {
                valueOfchar = "६";
            } else if (valueOfchar.equals("7")) {
                valueOfchar = "७";
            } else if (valueOfchar.equals("8")) {
                valueOfchar = "८";
            } else if (valueOfchar.equals("9")) {
                valueOfchar = "९";
            } else if (valueOfchar.equals("0")) {
                valueOfchar = "०";
            }

            unicodeChar = unicodeChar + valueOfchar;

        }

        return unicodeChar;
    }

    public static String getNepaliMonth(String valueOfchar) {
        String nepali_month = "";
        if (valueOfchar.equals("1")) {
            nepali_month = "बैशाख";
        } else if (valueOfchar.equals("2")) {
            nepali_month = "जेठ";
        } else if (valueOfchar.equals("3")) {
            nepali_month = "आषाढ";
        } else if (valueOfchar.equals("4")) {
            nepali_month = "साउन";
        } else if (valueOfchar.equals("5")) {
            nepali_month = "भाद्र";
        } else if (valueOfchar.equals("6")) {
            nepali_month = "आश्विन";
        } else if (valueOfchar.equals("7")) {
            nepali_month = "कार्तिक";
        } else if (valueOfchar.equals("8")) {
            nepali_month = "मंसिर";
        } else if (valueOfchar.equals("9")) {
            nepali_month = "पौष";
        } else if (valueOfchar.equals("10")) {
            nepali_month = "माघ";
        } else if (valueOfchar.equals("11")) {
            nepali_month = "फाल्गुण";
        } else if (valueOfchar.equals("12")) {
            nepali_month = "चैत्र";
        }

        Log.i("getNepaliDate", "" + nepali_month);
        return nepali_month;
    }

    public static void createTicketFolder() {
        File ticketFolder = new File(Environment.getExternalStorageDirectory() + "/TicketData");
        if (!ticketFolder.isDirectory())
            ticketFolder.mkdirs();
    }

    public static void writeInTxt(File txtFile, String data) {
        try {
            FileOutputStream fOut = new FileOutputStream(txtFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
