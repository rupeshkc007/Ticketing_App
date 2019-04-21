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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.angads25.toggle.LabeledSwitch;
import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean;
import com.rt.printerlibrary.bean.LableSizeBean;
import com.rt.printerlibrary.bean.Position;
import com.rt.printerlibrary.bean.UsbConfigBean;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.cmd.TscFactory;
import com.rt.printerlibrary.connect.PrinterInterface;
import com.rt.printerlibrary.enumerate.BmpPrintMode;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.enumerate.ESCFontTypeEnum;
import com.rt.printerlibrary.enumerate.PrintDirection;
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
import com.technosales.net.buslocationannouncement.network.TicketInfoDataPush;
import com.technosales.net.buslocationannouncement.pojo.HelperList;
import com.technosales.net.buslocationannouncement.pojo.PriceList;
import com.technosales.net.buslocationannouncement.pojo.RouteStationList;
import com.technosales.net.buslocationannouncement.printer.BluetoothDeviceChooseDialog;
import com.technosales.net.buslocationannouncement.printer.UsbDeviceChooseDialog;
import com.technosales.net.buslocationannouncement.printer.apps.BaseActivity;
import com.technosales.net.buslocationannouncement.printer.apps.BaseApplication;
import com.technosales.net.buslocationannouncement.printer.utils.BaseEnum;
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
import static com.technosales.net.buslocationannouncement.trackcar.MainFragment.KEY_URL;

public class TicketAndTracking extends AppCompatActivity implements PrinterObserver {

    private static final int PERMISSIONS_REQUEST_LOCATION = 2;
    private static final int ALARM_MANAGER_INTERVAL = 15000;

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


    /////
    private int checkedConType = BaseEnum.CON_USB;
    private PrinterFactory printerFactory;
    private RTPrinter rtPrinter;
    private PrinterInterface curPrinterInterface = null;
    public Object configObj;
    public UsbDeviceChooseDialog usbDeviceChooseDialog;
    public BluetoothDeviceChooseDialog bluetoothDeviceChooseDialog;

    private int bmpPrintWidth = 40;
    public Bitmap mBitmap;
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
        trackCarPrefs.edit().putString(KEY_ACCURACY, "high").apply();
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

        setSupportActionBar(mainToolBar);
        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.mipmap.helper_choose);
        mainToolBar.setOverflowIcon(drawable);

        helperName.setText(getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getString(UtilStrings.NAME_HELPER, ""));
        mode = getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getInt(UtilStrings.MODE, UtilStrings.MODE_3);
        getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putInt(UtilStrings.ROUTE_LIST_SIZE, databaseHelper.routeStationLists().size()).apply();


        route_name.setSelected(true);
        route_name.setText(getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getString(UtilStrings.DEVICE_NAME, "") + "-" + getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getString(UtilStrings.ROUTE_NAME, ""));

        normalDiscountToggle.setLabelOn(getString(R.string.discount_rate));
        normalDiscountToggle.setLabelOff(getString(R.string.normal_rate));
        normalDiscountToggle.setOn(false);
        /*normalDiscountToggle.setColorOff(getResources().getColor(android.R.color.black));
        normalDiscountToggle.setColorOn(getResources().getColor(R.color.colorAccent));*/

        int spanCount = 4;

        if (mode == UtilStrings.MODE_3) {
            spanCount = 1;
        }
        routeStationListsForInfinite = databaseHelper.routeStationLists();
        priceAdapterPrices = new PriceAdapterPrices(routeStationListsForInfinite, this);


        gridLayoutManager = new GridLayoutManager(this, spanCount);
        priceListView.setLayoutManager(gridLayoutManager);
        priceListView.setHasFixedSize(true);

//        databaseHelper.getWritableDatabase().execSQL("DELETE FROM " + DatabaseHelper.PRICE_TABLE);

        priceLists = databaseHelper.priceLists();
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


        normalDiscountToggle.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(LabeledSwitch labeledSwitch, boolean isOn) {
                mode = getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getInt(UtilStrings.MODE, UtilStrings.MODE_3);
                /*totalRemainingTickets.setText(GeneralUtils.getUnicodeNumber(String.valueOf(databaseHelper.listTickets().size())) + "\n" + GeneralUtils.getUnicodeNumber(String.valueOf(databaseHelper.remainingAmount())));
                if (databaseHelper.listTickets().size() > 0) {
                    boolean datasending = getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getBoolean(UtilStrings.DATA_SENDING, false);
                    if (!datasending) {
                        databaseHelper.ticketInfoLists();
                    }


                }*/

                if (isOn) {
                    if (mode != UtilStrings.MODE_3) {
                        setPriceLists();
                    } else {
//                        priceListView.setAdapter(new PriceAdapterPrices(databaseHelper.routeStationLists(),TicketAndTracking.this));
                        priceListView.setAdapter(priceAdapterPrices);
                        priceListView.getLayoutManager().scrollToPosition(listVisiblePosition);
                    }
                } else {
                    if (mode != UtilStrings.MODE_3) {
                        setPriceLists();
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

                AlertDialog alertDialog = new AlertDialog.Builder(TicketAndTracking.this).create();
                alertDialog.setTitle("CONFIRM");
                alertDialog.setMessage("Clear Data");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().remove(UtilStrings.TOTAL_TICKETS).apply();
                                getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().remove(UtilStrings.TOTAL_COLLECTIONS).apply();
                                setTotal();
                                databaseHelper.clearAllFromData();
                                databaseHelper.clearTxtTable();
                            }
                        });
                alertDialog.show();


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
                        mode = getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getInt(UtilStrings.MODE, UtilStrings.MODE_3);
                        if (mode == UtilStrings.MODE_3) {
                            routeStationListsForInfinite.addAll(databaseHelper.routeStationLists());
                            priceAdapterPrices.notifyDataChange(routeStationListsForInfinite);
                        }
                        //Do pagination.. i.e. fetch new data
                    }
                }
            }
        });


        BaseApplication.instance.setCurrentCmdType(BaseEnum.CMD_ESC);
        printerFactory = new UniversalPrinterFactory();
        rtPrinter = printerFactory.create();

        /*tv_ver.setText("PrinterExample Ver: v" + TonyUtils.getVersionName(this));*/
        PrinterObserverManager.getInstance().add(this);//添加连接状态监听

        setEscPrint();
        /*showUSBDeviceChooseDialog();    //use for voting*/


        showBluetoothDeviceChooseDialog();
    }

    private void setMode(int modeType, int spanCount, String modeStr) {
        getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putInt(UtilStrings.MODE, modeType).apply();
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
        String isToday = getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getString(UtilStrings.DATE_TIME, "");
        if (!isToday.equals(GeneralUtils.getDate())) {
            getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putString(UtilStrings.DATE_TIME, GeneralUtils.getDate()).apply();
            getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().remove(UtilStrings.TOTAL_TICKETS).apply();
            getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().remove(UtilStrings.TOTAL_COLLECTIONS).apply();
            new DatabaseHelper(this).clearTxtTable();
            setTotal();

            getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putBoolean(UtilStrings.RESET, true).apply();
            TicketInfoDataPush.resetData(TicketAndTracking.this);
        }
    }


    private void showBluetoothDeviceChooseDialog() {
        bluetoothDeviceChooseDialog = new BluetoothDeviceChooseDialog();
        bluetoothDeviceChooseDialog.setOnDeviceItemClickListener(new BluetoothDeviceChooseDialog.onDeviceItemClickListener() {
            @Override
            public void onDeviceItemClick(BluetoothDevice device) {

                /*configObj = new BluetoothEdrConfigBean(device);
                connectBlueTh();*/
            }
        });
        bluetoothDeviceChooseDialog.show(TicketAndTracking.this.getFragmentManager(), null);
    }


    private void showUSBDeviceChooseDialog() {
        usbDeviceChooseDialog = new UsbDeviceChooseDialog();
        usbDeviceChooseDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UsbDevice mUsbDevice = (UsbDevice) parent.getAdapter().getItem(position);
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(
                        TicketAndTracking.this,
                        0,
                        new Intent(TicketAndTracking.this.getApplicationInfo().packageName),
                        0);
/*
                tv_device_selected.setText(getString(R.string.adapter_usbdevice) + mUsbDevice.getDeviceId()); //+ (position + 1));
*/
                configObj = new UsbConfigBean(BaseApplication.getInstance(), mUsbDevice, mPermissionIntent);
                /*tv_device_selected.setTag(BaseEnum.HAS_DEVICE);
                isConfigPrintEnable(configObj);*/
                usbDeviceChooseDialog.dismiss();

                doConnect();
            }
        });
        usbDeviceChooseDialog.show(getFragmentManager(), null);
    }

    public void connectBlueTh() {
        BluetoothEdrConfigBean bluetoothEdrConfigBean = (BluetoothEdrConfigBean) configObj;
        connectBluetooth(bluetoothEdrConfigBean);
    }

    private void connectBluetooth(BluetoothEdrConfigBean bluetoothEdrConfigBean) {
        PIFactory piFactory = new BluetoothFactory();
        PrinterInterface printerInterface = piFactory.create();
        printerInterface.setConfigObject(bluetoothEdrConfigBean);
        rtPrinter.setPrinterInterface(printerInterface);
        try {
            rtPrinter.connect(bluetoothEdrConfigBean);
            BaseApplication.instance.setRtPrinter(rtPrinter);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

    }

    public void doConnect() {
        UsbConfigBean usbConfigBean = (UsbConfigBean) configObj;
        connectUSB(usbConfigBean);
    }

    private void connectUSB(UsbConfigBean usbConfigBean) {
        UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        PIFactory piFactory = new UsbFactory();
        PrinterInterface printerInterface = piFactory.create();
        printerInterface.setConfigObject(usbConfigBean);
        rtPrinter.setPrinterInterface(printerInterface);

        if (mUsbManager.hasPermission(usbConfigBean.usbDevice)) {
            try {
                rtPrinter.connect(usbConfigBean);
                BaseApplication.instance.setRtPrinter(rtPrinter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mUsbManager.requestPermission(usbConfigBean.usbDevice, usbConfigBean.pendingIntent);
        }


    }


    public void setPriceLists() {
        priceLists = databaseHelper.priceLists();
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
        totalTickets = getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getInt(UtilStrings.TOTAL_TICKETS, 0);
        totalCollections = getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getInt(UtilStrings.TOTAL_COLLECTIONS, 0);
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
                }
            }
            startTrackingService(false, granted);
        }
    }


    @Override
    public void printerObserverCallback(PrinterInterface printerInterface, int i) {

    }

    @Override
    public void printerReadMsgCallback(PrinterInterface printerInterface, byte[] bytes) {

    }

    private void setEscPrint() {
        /*BaseApplication.instance.setCurrentCmdType(BaseEnum.CMD_ESC);*/
        printerFactory = new ThermalPrinterFactory();
        rtPrinter = printerFactory.create();
        rtPrinter.setPrinterInterface(curPrinterInterface);
    }

    public void escPrint(String ticketNumber) throws UnsupportedEncodingException {
        rtPrinter = BaseApplication.getInstance().getRtPrinter();
        if (rtPrinter != null) {
            CmdFactory escFac = new EscFactory();
            Cmd escCmd = escFac.create();
            escCmd.append(escCmd.getHeaderCmd());//初始化, Initial

            escCmd.setChartsetName("UTF-8");

            TextSetting textSetting = new TextSetting();

            textSetting.setAlign(CommonEnum.ALIGN_MIDDLE);//对齐方式-左对齐，居中，右对齐
            textSetting.setBold(SettingEnum.Disable);
            textSetting.setUnderline(SettingEnum.Disable);
            textSetting.setIsAntiWhite(SettingEnum.Disable);
            textSetting.setDoubleHeight(SettingEnum.Disable);
            textSetting.setDoubleWidth(SettingEnum.Disable);

            textSetting.setEscFontType(ESCFontTypeEnum.FONT_A_12x24);

            escCmd.append(escCmd.getTextCmd(textSetting, ticketNumber, "UTF-8"));

            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getHeaderCmd());//初始化, Initial
            escCmd.append(escCmd.getLFCRCmd());

            rtPrinter.writeMsgAsync(escCmd.getAppendCmds());
        }
    }


    public void escImgPrint() throws SdkException {

        new Thread(new Runnable() {
            @Override
            public void run() {


                CmdFactory cmdFactory = new EscFactory();
                Cmd cmd = cmdFactory.create();
                cmd.append(cmd.getHeaderCmd());

                CommonSetting commonSetting = new CommonSetting();
                commonSetting.setAlign(CommonEnum.ALIGN_MIDDLE);
                commonSetting.setEscLineSpacing(12);
                cmd.append(cmd.getCommonSettingCmd(commonSetting));

                BitmapSetting bitmapSetting = new BitmapSetting();
                bitmapSetting.setBmpPrintMode(BmpPrintMode.MODE_SINGLE_COLOR);

                bitmapSetting.setBimtapLimitWidth(bmpPrintWidth * 8);
                try {
                    /*Bitmap bb= Bitmap.createScaledBitmap(mBitmap, 300, 100, false);*/
                    cmd.append(cmd.getBitmapCmd(bitmapSetting, mBitmap));
                } catch (SdkException e) {
                    e.printStackTrace();
                }
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                cmd.append(cmd.getLFCRCmd());
                if (rtPrinter != null) {
                    rtPrinter.writeMsg(cmd.getAppendCmds());//Sync Write
                }

            }
        }).start();

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
        getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putString(UtilStrings.ID_HELPER, helperNameId[0]).apply();
        getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putString(UtilStrings.NAME_HELPER, helperNameId[1]).apply();

        helperName.setText(getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getString(UtilStrings.NAME_HELPER, ""));
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

                reset = getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getBoolean(UtilStrings.RESET, true);
                if (!reset) {
                    totalTickets = getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getInt(UtilStrings.TOTAL_TICKETS, 0);
                    if (totalTickets != getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getInt(UtilStrings.SENT_TICKET, 0))
                        TicketInfoDataPush.pushBusData(TicketAndTracking.this, totalTickets, totalCollections);

                    if (databaseHelper.listTickets().size() > 0) {
                        databaseHelper.ticketInfoLists();
                    }
                } else {
                    TicketInfoDataPush.resetData(TicketAndTracking.this);
                }


                totalRemainingTickets.setText(GeneralUtils.getUnicodeNumber(String.valueOf(databaseHelper.listTickets().size())) + "\n" + GeneralUtils.getUnicodeNumber(String.valueOf(databaseHelper.remainingAmount())));
                /*if (databaseHelper.listTickets().size() > 0) {
                    boolean datasending = getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getBoolean(UtilStrings.DATA_SENDING, false);
                    if (!datasending) {
                        databaseHelper.ticketInfoLists();
                    }


                }*/
                rHandler.postAtTime(rTicker, next);
            }
        }

        ;
        rTicker.run();

    }


}
