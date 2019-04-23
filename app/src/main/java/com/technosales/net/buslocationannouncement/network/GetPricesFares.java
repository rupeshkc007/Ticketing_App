package com.technosales.net.buslocationannouncement.network;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.technosales.net.buslocationannouncement.helper.DatabaseHelper;
import com.technosales.net.buslocationannouncement.utils.GeneralUtils;
import com.technosales.net.buslocationannouncement.utils.TextToVoice;
import com.technosales.net.buslocationannouncement.utils.UtilStrings;

import org.json.JSONArray;
import org.json.JSONObject;

public class GetPricesFares {
    Context context;

    public GetPricesFares(Context context) {
        this.context = context;
    }

    public void getFares(final String number) {
        AQuery aQuery = new AQuery(context);
        aQuery.ajax(UtilStrings.TICKET_PRICE_LIST, JSONArray.class, new AjaxCallback<JSONArray>() {
            @Override
            public void callback(String url, JSONArray object, AjaxStatus status) {
                super.callback(url, object, status);
                if (object != null) {
                    Log.i("jsdfsdgfjsdf", "hgjfgshdf" + object);
                    DatabaseHelper databaseHelper = new DatabaseHelper(context);

                    databaseHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.PRICE_TABLE);
                    for (int i = 0; i < object.length(); i++) {
                        JSONObject jsonObject = object.optJSONObject(i);
                        String normal_ticket_rate = jsonObject.optString("normal_ticket_rate");
                        String discounted_ticket_rate = jsonObject.optString("discounted_ticket_rate");
                        String min_distance = jsonObject.optString("min_distance");
                        String distance_up_to = jsonObject.optString("distance_up_to");

                        ContentValues contentValues = new ContentValues();
                        contentValues.put(DatabaseHelper.PRICE_VALUE, GeneralUtils.getUnicodeNumber(normal_ticket_rate));
                        contentValues.put(DatabaseHelper.PRICE_DISCOUNT_VALUE, GeneralUtils.getUnicodeNumber(discounted_ticket_rate));
                        contentValues.put(DatabaseHelper.PRICE_MIN_DISTANCE, min_distance);
                        contentValues.put(DatabaseHelper.PRICE_DISTANCE, distance_up_to);
                        databaseHelper.insertPrice(contentValues);

                    }
                    RegisterDevice.RegisterDevice(context, number);

                }
            }
        });
    }
}
