package com.technosales.net.buslocationannouncement.network;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.technosales.net.buslocationannouncement.helper.DatabaseHelper;
import com.technosales.net.buslocationannouncement.utils.UtilStrings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class GetAdvertisements {
    Context context;

    public GetAdvertisements(Context context) {
        this.context = context;
    }

    public void getAdv() {
        AQuery aQuery = new AQuery(context);
        final DatabaseHelper databaseHelper = new DatabaseHelper(context);
        aQuery.ajax(UtilStrings.ADVERTISEMENTS_URL + "9842721343", JSONArray.class, new AjaxCallback<JSONArray>() {
            //        aQuery.ajax(UtilStrings.ADVERTISEMENTS_URL + context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getString(UtilStrings.DEVICE_ID, ""), JSONArray.class, new AjaxCallback<JSONArray>() {
            @Override
            public void callback(String url, JSONArray object, AjaxStatus status) {
                super.callback(url, object, status);
                Log.i("insertAd", "" + object);
                if (object != null) {
                    databaseHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.ADVERTISEMENT_TABLE);
                    for (int i = 0; i < object.length(); i++) {
                        JSONObject adObj = object.optJSONObject(i);
                        String ad_id = adObj.optString("file_id");
                        int ad_count = adObj.optInt("count");


                        String ad_file[] = adObj.optJSONObject("file").optString("file").split("/");
                        JSONArray id_array = adObj.optJSONArray("station_id");
                        String[] station_id = new String[id_array.length()];
                        for (int j = 0; j < id_array.length(); j++) {
                            station_id[j] = id_array.optString(j);
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(DatabaseHelper.ADVERTISEMENT_ID, ad_id);
                            contentValues.put(DatabaseHelper.ADVERTISEMENT_COUNT, ad_count);
                            contentValues.put(DatabaseHelper.ADVERTISEMENT_STATIONS, station_id[j]);
                            contentValues.put(DatabaseHelper.ADVERTISEMENT_FILE, ad_file[1]);
                            databaseHelper.insertAdv(contentValues);
                        }
                        downloadFile(ad_file[1]);
                    }


                }
            }
        });
    }

    public void downloadFile(String fileName) {
        File ext = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File target = new File(ext, fileName);
        AQuery aq = new AQuery(context);
        aq.download("http://202.52.240.149:85/routemanagement/public/storage/adv_files/" + fileName, target, new AjaxCallback<File>() {
            public void callback(String url, File file, AjaxStatus status) {
                if (file != null) {

                } else {

                }
            }

        });
    }

}
