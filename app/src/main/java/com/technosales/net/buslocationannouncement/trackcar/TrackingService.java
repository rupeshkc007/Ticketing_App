/*
 * Copyright 2012 - 2017 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.technosales.net.buslocationannouncement.trackcar;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.technosales.net.buslocationannouncement.R;
import com.technosales.net.buslocationannouncement.activity.TicketAndTracking;
import com.technosales.net.buslocationannouncement.helper.DatabaseHelper;
import com.technosales.net.buslocationannouncement.utils.GeneralUtils;
import com.technosales.net.buslocationannouncement.utils.UtilStrings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TrackingService extends Service {

    private static final String TAG = TrackingService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;

    private TrackingController trackingController;

    private static Notification createNotification(Context context) {
        Notification notify;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainApplication.PRIMARY_CHANNEL)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setCategory(Notification.CATEGORY_SERVICE);
            Intent intent;
            intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
            builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, 0));
            notify = builder.build();
        } else {
            String NOTIFICATION_CHANNEL_ID = "buslocation";
            String channelName = "Buslocation background service";
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Getting Your Location")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            notify = notification;
        }

        return notify;


    }


    public static class HideNotificationService extends Service {
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            startForeground(NOTIFICATION_ID, createNotification(this));
            stopForeground(true);

        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            stopSelfResult(startId);
            return START_NOT_STICKY;
        }
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "service create");
        StatusActivity.addMessage(getString(R.string.status_service_create));


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            trackingController = new TrackingController(this);
            trackingController.start();
        }
        startForeground(NOTIFICATION_ID, createNotification(this));
        ContextCompat.startForegroundService(this, new Intent(this, HideNotificationService.class));

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {

            try {


                AutostartReceiver.completeWakefulIntent(intent);
            } catch (Exception ex) {

            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "service destroy");

        try {
            StatusActivity.addMessage(getString(R.string.status_service_destroy));
            if (trackingController != null) {
                trackingController.stop();
            }
        } catch (Exception e) {

        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            stopForeground(true);

        }


    }



}
