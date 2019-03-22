package com.technosales.net.buslocationannouncement.network;

import android.content.Context;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.technosales.net.buslocationannouncement.activity.TicketAndTracking;
import com.technosales.net.buslocationannouncement.helper.DatabaseHelper;
import com.technosales.net.buslocationannouncement.pojo.TicketInfoList;
import com.technosales.net.buslocationannouncement.utils.GeneralUtils;
import com.technosales.net.buslocationannouncement.utils.UtilStrings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketInfoDataPush {
   /* public static void pushBusData(final Context context, final List<TicketInfoList> ticketInfoLists) {
        context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putBoolean(UtilStrings.DATA_SENDING, true).apply();
        if (GeneralUtils.isNetworkAvailable(context)) {
            for (int i = 0; i < ticketInfoLists.size(); i++) {
                final TicketInfoList ticketInfoList = ticketInfoLists.get(i);
                Map<String, Object> params = new HashMap<>();
                params.put("helper_id", ticketInfoList.helper_id);
                params.put("ticket_number", ticketInfoList.ticketNumber);
                params.put("price", ticketInfoList.ticketPrice);
                params.put("device_time", ticketInfoList.ticketDate + " " + ticketInfoList.ticketTime);
                params.put("ticket_type", ticketInfoList.ticketType);
                params.put("latitude", ticketInfoList.ticketLat);
                params.put("longitude", ticketInfoList.ticketLng);
                params.put("trip", "1");
                params.put("device_id", context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getString(UtilStrings.DEVICE_ID, ""));
                AQuery aQuery = new AQuery(context);
                Log.i("getParams", "" + params);
                aQuery.ajax(UtilStrings.TICKET_POST, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                    @Override
                    public void callback(String url, JSONObject object, AjaxStatus status) {
                        super.callback(url, object, status);
                        Log.i("getParams", object + ticketInfoList.ticketNumber);
                        if (object != null) {
                            if (object.optString("error").equals("false") || object.optString("error").equals("true")) {
                                new DatabaseHelper(context).deleteFromLocalId(ticketInfoList.ticketNumber);
                            }


                        } else {
                        }
                    }
                }.timeout(1000 * 60 * 15));
            }
            context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putBoolean(UtilStrings.DATA_SENDING, false).apply();
        } else {
            context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putBoolean(UtilStrings.DATA_SENDING, false).apply();

        }

    }*/


    public static void pushBusData(final Context context, final int totalTicks, final int totalCollection) {
        if (GeneralUtils.isNetworkAvailable(context)) {
            final Map<String, Object> params = new HashMap<>();
            params.put("current_helper", context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getString(UtilStrings.ID_HELPER, ""));
            params.put("ticket", totalTicks);
            params.put("income", totalCollection);
            params.put("device_id", context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getString(UtilStrings.DEVICE_ID, ""));
            AQuery aQuery = new AQuery(context);
            aQuery.ajax(UtilStrings.UPDATE_TICKET, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject object, AjaxStatus status) {
                    super.callback(url, object, status);
                    Log.i("getParams", object + ":" + params + "");
                    if (object != null) {
                        if (object.optString("error").equals("false")) {
                            /*new DatabaseHelper(context).deleteFromLocalId(ticketInfoList.ticketNumber);*/

                            context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putInt(UtilStrings.SENT_TICKET, totalTicks).apply();

                        }


                    } else {

                        pushBusData(context, totalTicks, totalCollection);
                    }
                }
            });
        } else {

        }

    }

    public static void resetData(final Context context) {
        if (GeneralUtils.isNetworkAvailable(context)) {
            final Map<String, Object> params = new HashMap<>();
            params.put("device_id", context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getString(UtilStrings.DEVICE_ID, ""));
            AQuery aQuery = new AQuery(context);
            aQuery.ajax(UtilStrings.RESET_DEVICE, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject object, AjaxStatus status) {
                    super.callback(url, object, status);
                    Log.i("getParams", object + ":" + params + "");
                    if (object != null) {
                        if (object.optString("error").equals("false")) {
                            context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putBoolean(UtilStrings.RESET, false).apply();
                        }


                    } else {

                        resetData(context);
                    }
                }
            });
        } else {

        }

    }
}
