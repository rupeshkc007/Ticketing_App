package com.technosales.net.buslocationannouncement.network;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.technosales.net.buslocationannouncement.activity.TicketAndTracking;
import com.technosales.net.buslocationannouncement.helper.DatabaseHelper;
import com.technosales.net.buslocationannouncement.pojo.RouteStationList;
import com.technosales.net.buslocationannouncement.utils.GeneralUtils;
import com.technosales.net.buslocationannouncement.utils.UtilStrings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RouteStation {
    public static void getRouteStation(final Context context, final String routeId, final ProgressDialog progressDialog) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("routeId", routeId);
        AQuery aQuery = new AQuery(context);
        final DatabaseHelper databaseHelper = new DatabaseHelper(context);
        if (GeneralUtils.isNetworkAvailable(context)) {
            aQuery.ajax(UtilStrings.ROUTE_STATION, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject object, AjaxStatus status) {
                    super.callback(url, object, status);
                    Log.i("getObject", "" + object);
                    if (object != null) {

                        String error = object.optString("error");
                        JSONArray data = object.optJSONArray("data");
                        int order = 0;
                        databaseHelper.clearStations();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject dataobj = data.optJSONObject(i);
                            String sts = dataobj.optString("status");
                            if (sts.equals("0")) {
                                order++;
                                RouteStationList routeStationList = new RouteStationList();
                                routeStationList.station_id = dataobj.optString("station_id");
                                routeStationList.station_order = order;
                                routeStationList.station_name = dataobj.optString("name_nepali");
                                routeStationList.station_lat = dataobj.optString("latitude");
                                routeStationList.station_lng = dataobj.optString("longitude");

                                databaseHelper.insertStations(routeStationList);


                            }
                        }
                        progressDialog.dismiss();
                        context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putString(UtilStrings.ROUTE_ID, routeId).apply();
                        /*context.startActivity(new Intent(context, AnnounceActivity.class));*/
                        context.startActivity(new Intent(context, TicketAndTracking.class));
                    } else {
                        getRouteStation(context, routeId, progressDialog);
                    }
                }
            });
        }

    }
}
