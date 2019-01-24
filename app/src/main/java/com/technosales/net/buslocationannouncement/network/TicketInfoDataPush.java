package com.technosales.net.buslocationannouncement.network;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.technosales.net.buslocationannouncement.helper.DatabaseHelper;
import com.technosales.net.buslocationannouncement.pojo.TicketInfoList;
import com.technosales.net.buslocationannouncement.utils.GeneralUtils;
import com.technosales.net.buslocationannouncement.utils.UtilStrings;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TicketInfoDataPush {

    public static void pushBusData(final Context context, JSONObject ticketInfoObject) {
        if (GeneralUtils.isNetworkAvailable(context)) {
            Map<String, Object> params = new HashMap<>();
            params.put("data", ticketInfoObject);
            Log.i("getParams", "" + params);

            AQuery aQuery = new AQuery(context);
            aQuery.ajax(UtilStrings.TICKET_POST, params, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject object, AjaxStatus status) {
                    super.callback(url, object, status);
                    Log.i("getParams", "" + object);
                    if (object != null) {
                        if (object.optString("error").equals("false")) {
                            new DatabaseHelper(context).deleteFromLocal();
                        }

                    } else {
                    }
                }
            });
        } else {
        }

    }
}
