package com.technosales.net.buslocationannouncement.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.github.angads25.toggle.LabeledSwitch;
import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.technosales.net.buslocationannouncement.R;
import com.technosales.net.buslocationannouncement.adapter.PriceAdapter;
import com.technosales.net.buslocationannouncement.helper.DatabaseHelper;
import com.technosales.net.buslocationannouncement.pojo.PriceList;
import com.technosales.net.buslocationannouncement.trackcar.AutostartReceiver;
import com.technosales.net.buslocationannouncement.trackcar.TrackingController;
import com.technosales.net.buslocationannouncement.trackcar.TrackingService;
import com.technosales.net.buslocationannouncement.utils.GeneralUtils;
import com.technosales.net.buslocationannouncement.utils.UtilStrings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.technosales.net.buslocationannouncement.trackcar.MainFragment.KEY_DEVICE;
import static com.technosales.net.buslocationannouncement.trackcar.MainFragment.KEY_URL;

public class TicketAndTracking extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_LOCATION = 2;
    private static final int ALARM_MANAGER_INTERVAL = 15000;

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    private SharedPreferences trackCarPrefs;
    private List<PriceList> priceLists = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private RecyclerView priceListView;
    public LabeledSwitch normalDiscountToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_and_tracking);

        /**/
        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, AutostartReceiver.class), 0);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        trackCarPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        trackCarPrefs.edit().putString(KEY_URL, getResources().getString(R.string.settings_url_default_value)).apply();
        trackCarPrefs.edit().putString(KEY_DEVICE, getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getString(UtilStrings.DEVICE_ID, "")).apply();
        databaseHelper = new DatabaseHelper(this);
        new TrackingController(this);
        startTrackingService(true, false);

        /**/
        priceListView = findViewById(R.id.priceListView);
        normalDiscountToggle = findViewById(R.id.normalDiscountToggle);
        normalDiscountToggle.setLabelOn(getString(R.string.discount_rate));
        normalDiscountToggle.setLabelOff(getString(R.string.normal_rate));
        normalDiscountToggle.setOn(false);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        priceListView.setLayoutManager(gridLayoutManager);
        priceListView.setHasFixedSize(true);


        priceLists = databaseHelper.priceLists(4);
        if (priceLists.size() == 0) {
            priceLists = GeneralUtils.priceCsv(this);
        }
        priceListView.setAdapter(new PriceAdapter(priceLists, this));


        normalDiscountToggle.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(LabeledSwitch labeledSwitch, boolean isOn) {
                if (isOn) {

                    setPriceLists(0);
                } else {
                    setPriceLists(4);
                }


            }
        });
    }

    public void setPriceLists(int min) {
        priceLists = databaseHelper.priceLists(min);
        priceListView.setAdapter(new PriceAdapter(priceLists, TicketAndTracking.this));
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
}
