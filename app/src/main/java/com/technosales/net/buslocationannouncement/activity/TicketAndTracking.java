package com.technosales.net.buslocationannouncement.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.github.angads25.toggle.LabeledSwitch;
import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean;
import com.rt.printerlibrary.bean.UsbConfigBean;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.connect.PrinterInterface;
import com.rt.printerlibrary.enumerate.BmpPrintMode;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.enumerate.ESCFontTypeEnum;
import com.rt.printerlibrary.enumerate.SettingEnum;
import com.rt.printerlibrary.exception.SdkException;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.factory.connect.BluetoothFactory;
import com.rt.printerlibrary.factory.connect.PIFactory;
import com.rt.printerlibrary.factory.connect.UsbFactory;
import com.rt.printerlibrary.factory.printer.PrinterFactory;
import com.rt.printerlibrary.factory.printer.ThermalPrinterFactory;
import com.rt.printerlibrary.factory.printer.UniversalPrinterFactory;
import com.rt.printerlibrary.observer.PrinterObserver;
import com.rt.printerlibrary.observer.PrinterObserverManager;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.setting.BitmapSetting;
import com.rt.printerlibrary.setting.CommonSetting;
import com.rt.printerlibrary.setting.TextSetting;
import com.technosales.net.buslocationannouncement.R;
import com.technosales.net.buslocationannouncement.adapter.PriceAdapter;
import com.technosales.net.buslocationannouncement.adapter.PriceAdapterPlaces;
import com.technosales.net.buslocationannouncement.adapter.PriceAdapterPrices;
import com.technosales.net.buslocationannouncement.helper.DatabaseHelper;
import com.technosales.net.buslocationannouncement.network.GetAdvertisements;
import com.technosales.net.buslocationannouncement.network.GetPricesFares;
import com.technosales.net.buslocationannouncement.network.TicketInfoDataPush;
import com.technosales.net.buslocationannouncement.pojo.HelperList;
import com.technosales.net.buslocationannouncement.pojo.PriceList;
import com.technosales.net.buslocationannouncement.pojo.RouteStationList;
import com.technosales.net.buslocationannouncement.printer.AidlUtil;
import com.technosales.net.buslocationannouncement.trackcar.AutostartReceiver;
import com.technosales.net.buslocationannouncement.trackcar.TrackingController;
import com.technosales.net.buslocationannouncement.trackcar.TrackingService;
import com.technosales.net.buslocationannouncement.utils.GeneralUtils;
import com.technosales.net.buslocationannouncement.utils.UtilStrings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.technosales.net.buslocationannouncement.trackcar.MainFragment.KEY_ACCURACY;
import static com.technosales.net.buslocationannouncement.trackcar.MainFragment.KEY_DEVICE;
import static com.technosales.net.buslocationannouncement.trackcar.MainFragment.KEY_DISTANCE;
import static com.technosales.net.buslocationannouncement.trackcar.MainFragment.KEY_INTERVAL;
import static com.technosales.net.buslocationannouncement.trackcar.MainFragment.KEY_URL;

public class TicketAndTracking extends AppCompatActivity implements GetPricesFares.OnPriceUpdate {

    private static final int PERMISSIONS_REQUEST_LOCATION = 2;
    private static final int ALARM_MANAGER_INTERVAL = 15000;
    private static final int STORAGE_PERMISSION_CODE = 111;

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    private SharedPreferences trackCarPrefs;
    private List<PriceList> priceLists = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private RecyclerView priceListView;
    public LabeledSwitch normalDiscountToggle;
    private TextView totalCollectionTickets;
    public TextView totalRemainingTickets;
    private TextView route_name;
    public TextView helperName;
    private TextView mode_selector;
    private ImageView settingMenu;


    /////

    private Toolbar mainToolBar;
    private int totalTickets;
    private int totalCollections;
    private boolean reset = true;
    private int mode;
    private GridLayoutManager gridLayoutManager;

    int pastVisiblesItems, visibleItemCount, totalItemCount;
    private PriceAdapterPrices priceAdapterPrices;
    private List<RouteStationList> routeStationListsForInfinite;
    int listVisiblePosition;
    private SharedPreferences preferences;

    private boolean isFirstRun;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_and_tracking);

        AidlUtil.getInstance().connectPrinterService(this);

        AidlUtil.getInstance().initPrinter();


        /**/
        preferences = getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0);
        alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, AutostartReceiver.class), 0);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        trackCarPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        trackCarPrefs.edit().putString(KEY_URL, getResources().getString(R.string.settings_url_default_value)).apply();
        trackCarPrefs.edit().putString(KEY_DEVICE, preferences.getString(UtilStrings.DEVICE_ID, "")).apply();
        trackCarPrefs.edit().putString(KEY_ACCURACY, "high").apply();
        trackCarPrefs.edit().putString(KEY_INTERVAL, "0").apply();
        trackCarPrefs.edit().putString(KEY_DISTANCE, "0").apply();
        /*trackCarPrefs.edit().putString(KEY_DEVICE, "12345678").apply();*/
        databaseHelper = new DatabaseHelper(this);
        new TrackingController(this);
        startTrackingService(true, false);

        /**/
        priceListView = findViewById(R.id.priceListView);
        normalDiscountToggle = findViewById(R.id.normalDiscountToggle);
        totalCollectionTickets = findViewById(R.id.totalCollectionTickets);
        totalRemainingTickets = findViewById(R.id.remainingTickets);
        route_name = findViewById(R.id.route_name);
        helperName = findViewById(R.id.helperName);
        mainToolBar = findViewById(R.id.mainToolBar);
        mode_selector = findViewById(R.id.mode_selector);
        settingMenu = findViewById(R.id.settingMenu);

        setSupportActionBar(mainToolBar);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.mipmap.helper_choose);
        mainToolBar.setOverflowIcon(drawable);

        helperName.setText(preferences.getString(UtilStrings.NAME_HELPER, ""));
        mode = preferences.getInt(UtilStrings.MODE, UtilStrings.MODE_3);
        preferences.edit().putInt(UtilStrings.ROUTE_LIST_SIZE, databaseHelper.routeStationLists().size()).apply();


        route_name.setSelected(true);
        route_name.setText(preferences.getString(UtilStrings.DEVICE_NAME, "") + "-" + preferences.getString(UtilStrings.ROUTE_NAME, ""));

        normalDiscountToggle.setLabelOn(getString(R.string.discount_rate));
        normalDiscountToggle.setLabelOff(getString(R.string.normal_rate));
        normalDiscountToggle.setOn(false);
        /*normalDiscountToggle.setColorOff(getResources().getColor(android.R.color.black));
        normalDiscountToggle.setColorOn(getResources().getColor(R.color.colorAccent));*/

        int spanCount = 4;

        if (mode == UtilStrings.MODE_3) {
            spanCount = 1;
            new LinearSnapHelper().attachToRecyclerView(priceListView);
        }
        routeStationListsForInfinite = databaseHelper.routeStationLists();
        priceAdapterPrices = new PriceAdapterPrices(routeStationListsForInfinite, this, databaseHelper);


        gridLayoutManager = new GridLayoutManager(this, spanCount);
        priceListView.setLayoutManager(gridLayoutManager);
        priceListView.setHasFixedSize(true);

//        databaseHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.PRICE_TABLE);

        priceLists = databaseHelper.priceLists(normalDiscountToggle.isOn());
        /*if (priceLists.size() == 0) {
            priceLists = GeneralUtils.priceCsv(this);
        }*/
        if (mode == UtilStrings.MODE_1) {
            priceListView.setAdapter(new PriceAdapter(priceLists, this));
            mode_selector.setText(getString(R.string.normal_mode));
        } else if (mode == UtilStrings.MODE_2) {
            priceListView.setAdapter(new PriceAdapterPlaces(priceLists, this));
            mode_selector.setText(getString(R.string.places_mode));
        } else {
//            priceListView.setAdapter(new PriceAdapterPrices(databaseHelper.routeStationLists(),TicketAndTracking.this));
            priceListView.setAdapter(priceAdapterPrices);
            mode_selector.setText(getString(R.string.price_mode));
        }

        mode_selector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(TicketAndTracking.this, mode_selector);
                //inflating menu from xml resource
                popup.inflate(R.menu.mode_select_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mode_1:
                                setMode(UtilStrings.MODE_1, 4, getString(R.string.normal_mode));
                                return true;
                            case R.id.mode_2:
                                setMode(UtilStrings.MODE_2, 4, getString(R.string.places_mode));
                                return true;
                            case R.id.mode_3:
                                setMode(UtilStrings.MODE_3, 1, getString(R.string.price_mode));
                                return true;
                        }
                        return true;

                    }
                });
                //displaying the popup
                popup.show();
            }
        });
        settingMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(TicketAndTracking.this, settingMenu);
                //inflating menu from xml resource
                popup.inflate(R.menu.pop_up_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.updateFare) {
                            new GetPricesFares(TicketAndTracking.this, TicketAndTracking.this).getFares(preferences.getString(UtilStrings.DEVICE_ID, ""), true);
                            return true;
                        }
                        return true;

                    }
                });
                //displaying the popup
                popup.show();
            }
        });


        normalDiscountToggle.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(LabeledSwitch labeledSwitch, boolean isOn) {
                mode = preferences.getInt(UtilStrings.MODE, UtilStrings.MODE_3);
                /*totalRemainingTickets.setText(GeneralUtils.getUnicodeNumber(String.valueOf(databaseHelper.listTickets().size())) + "\n" + GeneralUtils.getUnicodeNumber(String.valueOf(databaseHelper.remainingAmount())));
                if (databaseHelper.listTickets().size() > 0) {
                    boolean datasending = preferences.getBoolean(UtilStrings.DATA_SENDING, false);
                    if (!datasending) {
                        databaseHelper.ticketInfoLists();
                    }


                }*/

                if (isOn) {
                    if (mode != UtilStrings.MODE_3) {
                        setPriceLists(isOn);
                    } else {
//                        priceListView.setAdapter(new PriceAdapterPrices(databaseHelper.routeStationLists(),TicketAndTracking.this));
                        priceListView.setAdapter(priceAdapterPrices);
                        priceListView.getLayoutManager().scrollToPosition(listVisiblePosition);
                    }
                } else {
                    if (mode != UtilStrings.MODE_3) {
                        setPriceLists(isOn);
                    } else {
                        priceListView.setAdapter(priceAdapterPrices);
//                        priceListView.setAdapter(new PriceAdapterPrices(databaseHelper.routeStationLists(),TicketAndTracking.this));
                        priceListView.getLayoutManager().scrollToPosition(listVisiblePosition);
                    }
                }


            }
        });
        isToday();
        GeneralUtils.createTicketFolder();


        totalCollectionTickets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

             /*   AlertDialog alertDialog = new AlertDialog.Builder(TicketAndTracking.this).create();
                alertDialog.setTitle("Clear Data");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                preferences.edit().remove(UtilStrings.TOTAL_TICKETS).apply();
                                preferences.edit().remove(UtilStrings.TOTAL_COLLECTIONS).apply();
                                setTotal();
                                databaseHelper.clearAllFromData();
                                databaseHelper.clearTxtTable();
                            }
                        });
                alertDialog.show();
*/

            }
        });

        totalCollectionTickets.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(TicketAndTracking.this).create();
                alertDialog.setTitle("Write To Text");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                databaseHelper.writeToFile();

                            }
                        });
                alertDialog.show();
                return false;
            }
        });

        setTotal();
        try {
            rHandler.removeCallbacks(rTicker);
        } catch (Exception ex) {

        }
        interValDataPush();

        priceListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {


                listVisiblePosition = gridLayoutManager.findFirstVisibleItemPosition();
                if (dy > 0) //check for scroll down
                {
                    visibleItemCount = gridLayoutManager.getChildCount();
                    totalItemCount = gridLayoutManager.getItemCount();
                    pastVisiblesItems = gridLayoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount - 9) {
                        mode = preferences.getInt(UtilStrings.MODE, UtilStrings.MODE_3);
                        if (mode == UtilStrings.MODE_3 && preferences.getInt(UtilStrings.ROUTE_TYPE, UtilStrings.NON_RING_ROAD) == UtilStrings.RING_ROAD) {
                            routeStationListsForInfinite.addAll(databaseHelper.routeStationLists());
                            priceAdapterPrices.notifyDataChange(routeStationListsForInfinite);
                        }
                        //Do pagination.. i.e. fetch new data
                    }
                }
            }
        });




    }


    private void setMode(int modeType, int spanCount, String modeStr) {
        preferences.edit().putInt(UtilStrings.MODE, modeType).apply();
        gridLayoutManager = new GridLayoutManager(TicketAndTracking.this, spanCount);
        priceListView.setLayoutManager(gridLayoutManager);
        mode_selector.setText(modeStr);
        switch (modeType) {
            case UtilStrings.MODE_1:
                priceListView.setAdapter(new PriceAdapter(priceLists, this));
                break;
            case UtilStrings.MODE_2:
                priceListView.setAdapter(new PriceAdapterPlaces(priceLists, this));
                break;
            case UtilStrings.MODE_3:
//                priceListView.setAdapter(new PriceAdapterPrices(databaseHelper.routeStationLists(),TicketAndTracking.this));
                priceListView.setAdapter(priceAdapterPrices);
                break;
        }
    }

    private void isToday() {
        String isToday = preferences.getString(UtilStrings.DATE_TIME, "");
        if (!isToday.equals(GeneralUtils.getDate())) {
            preferences.edit().putString(UtilStrings.DATE_TIME, GeneralUtils.getDate()).apply();
            preferences.edit().remove(UtilStrings.TOTAL_TICKETS).apply();
            preferences.edit().remove(UtilStrings.TOTAL_COLLECTIONS).apply();
            new DatabaseHelper(this).clearTxtTable();
            setTotal();
            isFirstRun = preferences.getBoolean(UtilStrings.FIRST_RUN, true);
            if (isFirstRun) {
                preferences.edit().putBoolean(UtilStrings.RESET, false).apply();
            } else {
                preferences.edit().putBoolean(UtilStrings.RESET, true).apply();
            }
            preferences.edit().putBoolean(UtilStrings.FIRST_RUN, false).apply();

            /*TicketInfoDataPush.resetData(TicketAndTracking.this);*/

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (isReadStorageAllowed()) {
                    new GetAdvertisements(this).getAdv();
                }
            } else {
                new GetAdvertisements(this).getAdv();
            }

        } else {
            if (databaseHelper.noticeAdSize() == 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (isReadStorageAllowed()) {
                        new GetAdvertisements(this).getAdv();
                    }
                } else {
                    new GetAdvertisements(this).getAdv();
                }
            }
        }
    }



    public void setPriceLists(boolean discountToogle) {
        priceLists = databaseHelper.priceLists(discountToogle);
        if (mode == UtilStrings.MODE_1) {

            priceListView.setAdapter(new PriceAdapter(priceLists, TicketAndTracking.this));
        } else {
            priceListView.setAdapter(new PriceAdapterPlaces(priceLists, TicketAndTracking.this));
        }
        /*saveBitmap(getBitmapFromView(priceListView));*/
        setTotal();

    }

    private void saveBitmap(Bitmap bitmap) {
        File deviceScreenShotPath = new File("/storage/sdcard0/DCIM/Camera/file.jpg");


        FileOutputStream fos;
        try {
            fos = new FileOutputStream(deviceScreenShotPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e("GREC", e.getMessage(), e);
        } catch (IOException e) {
            Log.e("GREC", e.getMessage(), e);

        }

    }

    public void setTotal() {
        totalTickets = preferences.getInt(UtilStrings.TOTAL_TICKETS, 0);
        totalCollections = preferences.getInt(UtilStrings.TOTAL_COLLECTIONS, 0);
//        totalCollectionTickets.setText("Total Tickets :" + String.valueOf(totalTickets) + "\n Total Colletions :" + String.valueOf(totalCollections));
        totalCollectionTickets.setText(getString(R.string.total_tickets) + GeneralUtils.getUnicodeNumber(String.valueOf(totalTickets)) + "\n" + getString(R.string.total_collections) + GeneralUtils.getUnicodeNumber(String.valueOf(totalCollections)));


    }
    //////
    //////

    public static Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }
    //////
    /////


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
                } else {

                    if (!isReadStorageAllowed()) {
                        requestStoragePermission();
                    }
                }
            }

            startTrackingService(false, granted);
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                new GetAdvertisements(this).getAdv();
            } else {
                //Displaying another toast if permission is not granted
                requestStoragePermission();

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        for (int i = 0; i < databaseHelper.helperLists().size(); i++) {
            HelperList helperList = databaseHelper.helperLists().get(i);

            menu.add(0, i, 0, helperList.helper_id + "-" + helperList.helper_name);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId(); //to get the selected menu id
        String name = (String) item.getTitle(); //to get the selected menu name
        Log.i("menuItem", name + "");
        String helperNameId[] = name.split("-");

        /*helperName = helperNameId[1];*/
        preferences.edit().putString(UtilStrings.ID_HELPER, helperNameId[0]).apply();
        preferences.edit().putString(UtilStrings.NAME_HELPER, helperNameId[1]).apply();

        helperName.setText(preferences.getString(UtilStrings.NAME_HELPER, ""));
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
        isToday();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isToday();
    }

    Handler rHandler;
    Runnable rTicker;
    int i = 0;

    public void interValDataPush() {
        rHandler = new Handler();
        rTicker = new Runnable() {
            public void run() {
                long now = SystemClock.uptimeMillis();
                long next = now + 15000;
                i++;
                if (i >= 10) {
                    i = 0;
                }
                isToday();

                reset = preferences.getBoolean(UtilStrings.RESET, false);
                if (!reset) {
                    totalTickets = preferences.getInt(UtilStrings.TOTAL_TICKETS, 0);
                    if (totalTickets != preferences.getInt(UtilStrings.SENT_TICKET, 0))
                        TicketInfoDataPush.pushBusData(TicketAndTracking.this, totalTickets, totalCollections);

                    if (databaseHelper.listTickets().size() > 0) {
                        databaseHelper.ticketInfoLists();
                    }
                } else {
                    TicketInfoDataPush.resetData(TicketAndTracking.this);
                }


                totalRemainingTickets.setText(GeneralUtils.getUnicodeNumber(String.valueOf(databaseHelper.listTickets().size())) + "\n" + GeneralUtils.getUnicodeNumber(String.valueOf(databaseHelper.remainingAmount())));

                rHandler.postAtTime(rTicker, next);
            }
        }

        ;
        rTicker.run();

    }


    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);

    }

    private boolean isReadStorageAllowed() {

        //Getting the permission status
        int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //If permission is granted returning true
        if (read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }


    @Override
    public void onPriceUpdate() {
        startActivity(new Intent(this, TicketAndTracking.class));
        finish();
    }
}
