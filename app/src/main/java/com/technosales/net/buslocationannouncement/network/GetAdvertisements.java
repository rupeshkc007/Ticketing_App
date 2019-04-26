package com.technosales.net.buslocationannouncement.network;

import android.content.ContentValues;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

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
        aQuery.ajax(UtilStrings.ADVERTISEMENTS_URL + "9842721343", JSONObject.class, new AjaxCallback<JSONObject>() {
            //        aQuery.ajax(UtilStrings.ADVERTISEMENTS_URL + context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getString(UtilStrings.DEVICE_ID, ""), JSONArray.class, new AjaxCallback<JSONArray>() {
            @Override
            public void callback(String url, JSONObject object, AjaxStatus status) {
                super.callback(url, object, status);
                Log.i("insertAd", "" + object);
                if (object != null) {
                    databaseHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.ADVERTISEMENT_TABLE);
                    JSONArray advArray = object.optJSONArray("adv");
                    JSONArray noticeArray = object.optJSONArray("notice");
                    for (int i = 0; i < advArray.length(); i++) {
                        JSONObject adObj = advArray.optJSONObject(i);
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
                            contentValues.put(DatabaseHelper.ADVERTISEMENT_TYPE, UtilStrings.TYPE_ADV);
//                            contentValues.put(DatabaseHelper.ADVERTISEMENT_STATIONS,"1003");
                            contentValues.put(DatabaseHelper.ADVERTISEMENT_STATIONS, station_id[j]);
                            contentValues.put(DatabaseHelper.ADVERTISEMENT_FILE, ad_file[1]);
                            databaseHelper.insertAdv(contentValues);
                        }
                        downloadFile(ad_file[1]);
                    }
                    for (int i = 0; i < noticeArray.length(); i++) {
                        JSONObject noticeObj = noticeArray.optJSONObject(i);
                        String notice_file[] = noticeObj.optString("file").split("/");
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(DatabaseHelper.ADVERTISEMENT_FILE, notice_file[1]);
                        contentValues.put(DatabaseHelper.ADVERTISEMENT_TYPE, UtilStrings.TYPE_NOTICE);
                        databaseHelper.insertAdv(contentValues);
                        downloadFile(notice_file[1]);
                    }


                }
            }
        });
    }

    private void downloadFile(final String fileName) {
        File ext = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File target = new File(ext, fileName);
        AQuery aq = new AQuery(context);
        aq.download("http://202.52.240.149:85/routemanagement/public/storage/adv_files/" + fileName, target, new AjaxCallback<File>() {
            public void callback(String url, File file, AjaxStatus status) {
                if (file != null) {
                    Toast.makeText(context, fileName + " Download", Toast.LENGTH_SHORT).show();
                } else {

                }
            }

        });
    }

}
