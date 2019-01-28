package com.technosales.net.buslocationannouncement.network;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.technosales.net.buslocationannouncement.R;
import com.technosales.net.buslocationannouncement.utils.GeneralUtils;
import com.technosales.net.buslocationannouncement.utils.UtilStrings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class RegisterDevice {
    private static ProgressDialog progressDialog;

    public static void RegisterDevice(final Context context, String device_no) {
        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", device_no);
        AQuery aQuery = new AQuery(context);
        if (GeneralUtils.isNetworkAvailable(context)) {
            aQuery.ajax(UtilStrings.REGISTER_URL, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject object, AjaxStatus status) {
                    super.callback(url, object, status);
                    Log.i("response", "register" + object);
                    if (object != null) {

                        String error = object.optString("error");
                        if (error.equalsIgnoreCase("false")) {
                            JSONArray data = object.optJSONArray("data");
                            ArrayList<String> routeList = new ArrayList<>();

                            if (data.length() > 1) {

                                for (int i = 0; i < data.length(); i++) {
                                    try {
                                        JSONObject route = data.getJSONObject(i);
                                        routeList.add(route.getString("route_id") + "(" + route.optString("route_nepali") + ")");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                final Dialog dialog = new Dialog(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
                                View view = LayoutInflater.from(context).inflate(R.layout.choose_route, null);

                                ListView listView = (ListView) view.findViewById(R.id.routeList);
                                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                                        R.layout.route_list_item, routeList);

                                listView.setAdapter(adapter);

                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                        StringTokenizer stringTokenizer = new StringTokenizer(adapter.getItem(i), "(");

                                        dialog.dismiss();
                                        progressDialog = new ProgressDialog(context);
                                        progressDialog.setMessage("Please Wait");
                                        progressDialog.setCancelable(false);
                                        progressDialog.show();
                                        RouteStation.getRouteStation(context, stringTokenizer.nextToken(), progressDialog);
                                        context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putString(UtilStrings.ROUTE_NAME, stringTokenizer.nextToken().replace(")", "")).apply();
                                    }
                                });


                                dialog.setContentView(view);
                                dialog.setCancelable(true);
                                dialog.show();
                            } else {
                                progressDialog = new ProgressDialog(context);
                                progressDialog.setMessage("Please Wait");
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                                RouteStation.getRouteStation(context, data.optJSONObject(0).optString("route_id"), progressDialog);
                                context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putString(UtilStrings.ROUTE_NAME, data.optJSONObject(0).optString("route_nepali")).apply();

                            }
                        }
                    }
                }
            });
        }
    }
}
