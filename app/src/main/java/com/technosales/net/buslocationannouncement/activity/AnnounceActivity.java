package com.technosales.net.buslocationannouncement.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.technosales.net.buslocationannouncement.R;
import com.technosales.net.buslocationannouncement.helper.DatabaseHelper;
import com.technosales.net.buslocationannouncement.pojo.RouteStationList;
import com.technosales.net.buslocationannouncement.trackcar.AboutActivity;
import com.technosales.net.buslocationannouncement.trackcar.AutostartReceiver;
import com.technosales.net.buslocationannouncement.trackcar.StatusActivity;
import com.technosales.net.buslocationannouncement.trackcar.TrackingController;
import com.technosales.net.buslocationannouncement.trackcar.TrackingService;
import com.technosales.net.buslocationannouncement.utils.TextToVoice;
import com.technosales.net.buslocationannouncement.utils.UtilStrings;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static com.technosales.net.buslocationannouncement.trackcar.MainFragment.KEY_DEVICE;
import static com.technosales.net.buslocationannouncement.trackcar.MainFragment.KEY_URL;

public class AnnounceActivity extends AppCompatActivity implements MapboxMap.OnMarkerClickListener {
    private static final int PERMISSIONS_REQUEST_LOCATION = 2;
    private static final int ALARM_MANAGER_INTERVAL = 15000;

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    private SharedPreferences trackCarPrefs;
    private MapView mapView;
    private List<RouteStationList> routeStationLists = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private TextToVoice textToVoice;
    private Marker marker;
    private int preOrder = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, "pk.eyJ1IjoiY2hldHRyaXIxIiwiYSI6ImNqZ3l6NnViNTAxYTIycHA4cGZveno0YmoifQ.n0loTxgpheqz4ttVe_gDrA");
        setContentView(R.layout.activity_announce);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        databaseHelper = new DatabaseHelper(this);
        textToVoice = new TextToVoice(this);
        textToVoice.initTTs();

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        routeStationLists = databaseHelper.routeStationLists();
                        mapboxMap.setOnMarkerClickListener(AnnounceActivity.this);
                        for (int i = 0; i < routeStationLists.size(); i++) {
                            RouteStationList routeStationList = routeStationLists.get(i);
                            marker = mapboxMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(Double.parseDouble(routeStationList.station_lat), Double.parseDouble(routeStationList.station_lng)))
                                    .title(routeStationList.station_name)
                                    .snippet(String.valueOf(routeStationList.station_order) + "." + routeStationList.station_id));


                            if (i == 0) {
                                CameraPosition position = new CameraPosition.Builder()
                                        .target(new LatLng(Double.parseDouble(routeStationList.station_lat), Double.parseDouble(routeStationList.station_lng))) // Sets the new camera position
                                        .zoom(17) // Sets the zoom
                                        .bearing(180) // Rotate the camera
                                        .tilt(30) // Set the camera tilt
                                        .build(); // Creates a CameraPosition from the builder

                                mapboxMap.animateCamera(CameraUpdateFactory
                                        .newCameraPosition(position), 7000);
                            }


                        }


                    }
                });
            }
        });


        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, AutostartReceiver.class), 0);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        trackCarPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        trackCarPrefs.edit().putString(KEY_URL, getResources().getString(R.string.settings_url_default_value)).apply();
        trackCarPrefs.edit().putString(KEY_DEVICE, getSharedPreferences(UtilStrings.SHARED_PREFERENCES,0).getString(UtilStrings.DEVICE_ID,"")).apply();

        new TrackingController(this);
        startTrackingService(true, false);

    }


    private void startTrackingService(boolean checkPermission, boolean permission) {
        if (checkPermission) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                permission = true;
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
                }
                return;
            }
        }

        if (permission) {
            ContextCompat.startForegroundService(this, new Intent(this, TrackingService.class));
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    ALARM_MANAGER_INTERVAL, ALARM_MANAGER_INTERVAL, alarmIntent);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

            }
        } else {

        }
    }

    private void stopTrackingService() {
        alarmManager.cancel(alarmIntent);
        this.stopService(new Intent(this, TrackingService.class));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            startTrackingService(false, granted);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        // return true so that the menu pop up is opened
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.status) {
            startActivity(new Intent(this, StatusActivity.class));
            return true;
        } else if (item.getItemId() == R.id.about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        /*if (textToVoice.initTTs() != null) {
            textToVoice.initTTs().stop();
            textToVoice.initTTs().shutdown();
        }*/
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        String nextStation = "";
        String[] tokens = marker.getSnippet().toString().split(".");
        StringTokenizer stringTokenizer = new StringTokenizer(marker.getSnippet(), ".");
        int currentOrder = Integer.parseInt(stringTokenizer.nextToken());
        String current_id = stringTokenizer.nextToken();
        if (preOrder != currentOrder) {
            if (preOrder < currentOrder) {
                if (currentOrder == routeStationLists.size()) {
                    nextStation = databaseHelper.nextStation(currentOrder - 1);
                } else {
                    nextStation = databaseHelper.nextStation(currentOrder + 1);
                }
            } else {
                if (databaseHelper.getDouble(current_id) > 1) {
                    currentOrder = databaseHelper.nextStationId(current_id);
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

            textToVoice.speakStation(marker.getTitle().toString(), nextStation);
        }
        return false;
    }
}
