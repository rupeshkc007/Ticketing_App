/*
 * Copyright 2015 Anton Tananaev (anton.tananaev@gmail.com)
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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.technosales.net.buslocationannouncement.R;
import com.technosales.net.buslocationannouncement.helper.DatabaseHelper;
import com.technosales.net.buslocationannouncement.pojo.RouteStationList;
import com.technosales.net.buslocationannouncement.utils.GeneralUtils;
import com.technosales.net.buslocationannouncement.utils.TextToVoice;
import com.technosales.net.buslocationannouncement.utils.UtilStrings;

import java.util.ArrayList;
import java.util.List;

public class TrackingController implements PositionProvider.PositionListener, NetworkManager.NetworkHandler {

    private static final String TAG = TrackingController.class.getSimpleName();
    private static final int RETRY_DELAY = 30 * 1000;
    private static final int WAKE_LOCK_TIMEOUT = 120 * 1000;

    private boolean isOnline;
    private boolean isWaiting;

    private Context context;
    private Handler handler;
    private SharedPreferences preferences;
    private SharedPreferences taxiPreferences;

    private String url;

    private PositionProvider positionProvider;
    private DatabaseHelper databaseHelper;
    private NetworkManager networkManager;

    private PowerManager.WakeLock wakeLock;
    private List<RouteStationList> routeStationLists = new ArrayList<>();
    private TextToVoice textToVoice;
    private int preOrder = 0;
    private String preOrderId = "";
    private String nextStation;

    private void lock() {
        wakeLock.acquire(WAKE_LOCK_TIMEOUT);
    }

    private void unlock() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    public TrackingController(Context context) {
        this.context = context;
        handler = new Handler();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        taxiPreferences = context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0);


        positionProvider = new PositionProvider(context, this);
        databaseHelper = new DatabaseHelper(context);
        textToVoice = new TextToVoice(context);
        textToVoice.initTTs();
        networkManager = new NetworkManager(context, this);
        isOnline = networkManager.isOnline();

        url = preferences.getString(MainFragment.KEY_URL, context.getString(R.string.settings_url_default_value));

        routeStationLists = databaseHelper.routeStationLists();

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
    }

    public void start() {
        if (isOnline) {
            read();
        }
        try {
            positionProvider.startUpdates();
        } catch (SecurityException e) {
            Log.w(TAG, e);
        }
        networkManager.start();
    }

    public void stop() {
        networkManager.stop();
        try {
            positionProvider.stopUpdates();
        } catch (SecurityException e) {
            Log.w(TAG, e);
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onPositionUpdate(Position position) {
        StatusActivity.addMessage(context.getString(R.string.status_location_update));
        if (position != null) {
            write(position);
            for (int i = 0; i < routeStationLists.size(); i++) {
                RouteStationList routeStationList = routeStationLists.get(i);
                double stationLat = Double.parseDouble(routeStationList.station_lat);
                double stationLng = Double.parseDouble(routeStationList.station_lng);

                float distance = GeneralUtils.calculateDistance(stationLat, stationLng, position.getLatitude(), position.getLongitude());
                int currentOrder = routeStationList.station_order;
                String currentOrderId = routeStationList.station_id;
                if (distance <= 50) {
                    if (!currentOrderId.equals(preOrderId)) {
                        if (preOrder < currentOrder) {
                            if (currentOrder == routeStationLists.size()) {
                                nextStation = databaseHelper.nextStation(currentOrder - 1);
                            } else {
                                nextStation = databaseHelper.nextStation(currentOrder + 1);
                            }
                        } else {
                            if (databaseHelper.getDouble(routeStationList.station_id) > 1) {
                                currentOrder = databaseHelper.nextStationId(routeStationList.station_id);
                                nextStation = databaseHelper.nextStation(currentOrder + 1);
                            } else {
                                if (currentOrder != 1) {
                                    nextStation = databaseHelper.nextStation(currentOrder - 1);
                                } else {
                                    nextStation = databaseHelper.nextStation(currentOrder + 1);
                                }
                            }

                        }
                        preOrder = currentOrder;
                        preOrderId = currentOrderId;

                        textToVoice.speakStation(routeStationList.station_name, nextStation);
                    }
                }

            }


            context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putString(UtilStrings.LATITUDE, String.valueOf(position.getLatitude())).apply();
            context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putString(UtilStrings.LONGITUDE, String.valueOf(position.getLongitude())).apply();

            Log.v("Location", "bat :" + position.getBattery());
        }
    }

    @Override
    public void onNetworkUpdate(boolean isOnline) {
        int message = isOnline ? R.string.status_network_online : R.string.status_network_offline;
        StatusActivity.addMessage(context.getString(message));
        if (!this.isOnline && isOnline) {
            read();
        }
        this.isOnline = isOnline;
    }

    //
    // State transition examples:
    //
    // write -> read -> send -> delete -> read
    //
    // read -> send -> retry -> read -> send
    //

    private void log(String action, Position position) {
        if (position != null) {
            action += " (" +
                    "id:" + position.getId() +
                    " time:" + position.getTime().getTime() / 1000 +
                    " lat:" + position.getLatitude() +
                    " lon:" + position.getLongitude() + ")";
        }
        Log.d(TAG, action);
    }

    private void write(Position position) {
        log("write", position);
        lock();
        databaseHelper.insertPositionAsync(position, new DatabaseHelper.DatabaseHandler<Void>() {
            @Override
            public void onComplete(boolean success, Void result) {
                if (success) {
                    if (isOnline && isWaiting) {
                        read();
                        isWaiting = false;
                    }
                }
                unlock();
            }
        });
    }

    private void read() {
        log("read", null);
        lock();
        databaseHelper.selectPositionAsync(new DatabaseHelper.DatabaseHandler<Position>() {
            @Override
            public void onComplete(boolean success, Position result) {
                if (success) {
                    if (result != null) {
                        if (result.getDeviceId().equals(preferences.getString(MainFragment.KEY_DEVICE, null))) {
                            send(result);
                        } else {
                            delete(result);
                        }
                    } else {
                        isWaiting = true;
                    }
                } else {
                    retry();
                }
                unlock();
            }
        });
    }

    private void delete(Position position) {
        log("delete", position);
        lock();
        databaseHelper.deletePositionAsync(position.getId(), new DatabaseHelper.DatabaseHandler<Void>() {
            @Override
            public void onComplete(boolean success, Void result) {
                if (success) {
                    read();
                } else {
                    retry();
                }
                unlock();
            }
        });
    }

    private void send(final Position position) {
        log("send", position);
        lock();
        String request = ProtocolFormatter.formatRequest(url, position);
        RequestManager.sendRequestAsync(request, new RequestManager.RequestHandler() {
            @Override
            public void onComplete(boolean success) {
                if (success) {
                    delete(position);
                } else {
                    StatusActivity.addMessage(context.getString(R.string.status_send_fail));
                    retry();
                }
                unlock();
            }
        });
    }

    private void retry() {
        log("retry", null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isOnline) {
                    read();
                }
            }
        }, RETRY_DELAY);
    }

}
