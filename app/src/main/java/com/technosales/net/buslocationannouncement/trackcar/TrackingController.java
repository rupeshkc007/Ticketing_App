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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.technosales.net.buslocationannouncement.R;
import com.technosales.net.buslocationannouncement.activity.TicketAndTracking;
import com.technosales.net.buslocationannouncement.helper.DatabaseHelper;
import com.technosales.net.buslocationannouncement.pojo.AdvertiseList;
import com.technosales.net.buslocationannouncement.pojo.RouteStationList;
import com.technosales.net.buslocationannouncement.utils.GeneralUtils;
import com.technosales.net.buslocationannouncement.utils.TextToVoice;
import com.technosales.net.buslocationannouncement.utils.UtilStrings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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
    private TextToSpeech textToSpeech;
    private int preOrder = 0;
    private String preOrderId = "";
    private String nextStation;
    private MediaPlayer mediaPlayer;
    private int length = 0;
    private boolean isPaused;

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
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(new Locale("nep"));
                }
            }
        });
//        textToSpeech.setPitch(0.99f);

        positionProvider = new PositionProvider(context, this);
        databaseHelper = new DatabaseHelper(context);
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

//            Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
            context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putString(UtilStrings.LATITUDE, String.valueOf(position.getLatitude())).apply();
            context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putString(UtilStrings.LONGITUDE, String.valueOf(position.getLongitude())).apply();

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
                                context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putBoolean(UtilStrings.FORWARD, false).apply();
                            } else {
                                nextStation = databaseHelper.nextStation(currentOrder + 1);
                                context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putBoolean(UtilStrings.FORWARD, true).apply();

                            }
                        } else if (preOrder > currentOrder && databaseHelper.lastStation(currentOrderId) == routeStationLists.size()) {
                            nextStation = databaseHelper.nextStation(currentOrder - 1);
                            context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putBoolean(UtilStrings.FORWARD, false).apply();

                        } else {
                            if (databaseHelper.getDouble(routeStationList.station_id) > 1) {

                                currentOrder = databaseHelper.nextStationId(routeStationList.station_id);
                                nextStation = databaseHelper.nextStation(currentOrder + 1);
                                context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putBoolean(UtilStrings.FORWARD, true).apply();


                            } else {
                                if (currentOrder != 1) {
                                    nextStation = databaseHelper.nextStation(currentOrder - 1);
                                    context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putBoolean(UtilStrings.FORWARD, false).apply();

                                } else {
                                    nextStation = databaseHelper.nextStation(currentOrder + 1);
                                    context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putBoolean(UtilStrings.FORWARD, true).apply();

                                }
                            }

                        }
                        preOrder = currentOrder;
                        preOrderId = currentOrderId;

                        speakStation(routeStationList.station_name, nextStation, currentOrderId);


                    }
                    break;
                }

            }
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

    private void speakStation(String current, String next, String stationId) {
        final ArrayList<AdvertiseList> adList = databaseHelper.adFilename(stationId);
        if (adList.size() == 0) {
            try {
                if (mediaPlayer.isPlaying()) {
                    isPaused = true;
                    mediaPlayer.pause();
                    length = mediaPlayer.getCurrentPosition();
                } else {
                    isPaused = false;
                }
            } catch (Exception ex) {

            }

        } else {
            isPaused = false;
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;

            } catch (Exception ex) {
                Log.e("PlayerEx", "" + ex.toString());
            }
        }
        String speakVoice = context.getString(R.string.hamiahile) + current + ", " + context.getString(R.string.aaipugeu) + next + ", " + context.getString(R.string.pugnechau);
        textToSpeech.speak(speakVoice, TextToSpeech.QUEUE_FLUSH, null);
        Log.i("speakVoice", "" + speakVoice);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (adList.size() > 0) {

                    File file0 = adList.get(0).adFile;
                    if (file0.exists()) {
                        mediaPlayer = MediaPlayer.create(context, Uri.fromFile(file0));
                        mediaPlayer.start();
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                databaseHelper.updateAdCount(adList.get(0).adId);
                                if (adList.size() > 1) {
                                    File file1 = adList.get(1).adFile;
                                    if (file1.exists()) {
                                        mediaPlayer = mp;
                                        mediaPlayer = MediaPlayer.create(context, Uri.fromFile(file1));
                                        mediaPlayer.start();
                                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                            @Override
                                            public void onCompletion(MediaPlayer mp) {
                                                databaseHelper.updateAdCount(adList.get(1).adId);
                                                if (adList.size() > 2) {
                                                    File file2 = adList.get(2).adFile;
                                                    if (file2.exists()) {
                                                        mediaPlayer = mp;
                                                        mediaPlayer = MediaPlayer.create(context, Uri.fromFile(file2));
                                                        mediaPlayer.start();
                                                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                            @Override
                                                            public void onCompletion(MediaPlayer mp) {
                                                                databaseHelper.updateAdCount(adList.get(2).adId);
                                                            }
                                                        });
                                                    }

                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }

                } else {
                    if (isPaused) {
                        mediaPlayer.seekTo(length);
                        mediaPlayer.start();
                    } else {


                        // play info & guidance here

                        databaseHelper.noticeList();
                        if (databaseHelper.noticeList().size() > 0) {
                            Random r = new Random();
                            int randomN = r.nextInt((databaseHelper.noticeList().size()));

                            Log.i("randomSize", "" + databaseHelper.noticeList().size() + "::" + randomN);
                            File noticeFile = databaseHelper.noticeList().get(randomN).adFile;
                            if (noticeFile.exists()) {
                                mediaPlayer = MediaPlayer.create(context, Uri.fromFile(noticeFile));
                                mediaPlayer.start();
                            }

                        }

                    }

                }
            }
        }, GeneralUtils.getDelayTime(speakVoice.length()));

    }

}
