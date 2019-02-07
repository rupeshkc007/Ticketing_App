package com.technosales.net.buslocationannouncement.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.github.angads25.toggle.LabeledSwitch;
import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.rt.printerlibrary.bean.BluetoothEdrConfigBean;
import com.rt.printerlibrary.bean.UsbConfigBean;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.connect.PrinterInterface;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.enumerate.ESCFontTypeEnum;
import com.rt.printerlibrary.enumerate.SettingEnum;
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
import com.rt.printerlibrary.setting.TextSetting;
import com.technosales.net.buslocationannouncement.R;
import com.technosales.net.buslocationannouncement.adapter.PriceAdapter;
import com.technosales.net.buslocationannouncement.helper.DatabaseHelper;
import com.technosales.net.buslocationannouncement.pojo.PriceList;
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.technosales.net.buslocationannouncement.trackcar.MainFragment.KEY_DEVICE;
import static com.technosales.net.buslocationannouncement.trackcar.MainFragment.KEY_URL;

public class TicketAndTracking extends BaseActivity implements PrinterObserver {

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
    private TextView route_name;


    /////
    private int checkedConType = BaseEnum.CON_USB;
    private PrinterFactory printerFactory;
    private RTPrinter rtPrinter;
    private PrinterInterface curPrinterInterface = null;
    public Object configObj;
    public UsbDeviceChooseDialog usbDeviceChooseDialog;
    public BluetoothDeviceChooseDialog bluetoothDeviceChooseDialog;


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
        /*trackCarPrefs.edit().putString(KEY_DEVICE, "12345678").apply();*/
        databaseHelper = new DatabaseHelper(this);
        new TrackingController(this);
        startTrackingService(true, false);

        /**/
        priceListView = findViewById(R.id.priceListView);
        normalDiscountToggle = findViewById(R.id.normalDiscountToggle);
        totalCollectionTickets = findViewById(R.id.totalCollectionTickets);
        route_name = findViewById(R.id.route_name);
        route_name.setText(getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getString(UtilStrings.ROUTE_NAME, ""));

        normalDiscountToggle.setLabelOn(getString(R.string.discount_rate));
        normalDiscountToggle.setLabelOff(getString(R.string.normal_rate));
        normalDiscountToggle.setOn(false);
        /*normalDiscountToggle.setColorOff(getResources().getColor(android.R.color.black));
        normalDiscountToggle.setColorOn(getResources().getColor(R.color.colorAccent));*/

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
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
        String isToday = getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getString(UtilStrings.DATE_TIME, "");
        if (!isToday.equals(GeneralUtils.getDate())) {
            getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().putString(UtilStrings.DATE_TIME, GeneralUtils.getDate()).apply();
            getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().remove(UtilStrings.TOTAL_TICKETS).apply();
            getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().remove(UtilStrings.TOTAL_COLLECTIONS).apply();
            setTotal();
        }

        totalCollectionTickets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().remove(UtilStrings.TOTAL_TICKETS).apply();
                getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).edit().remove(UtilStrings.TOTAL_COLLECTIONS).apply();
                setTotal();
                databaseHelper.clearAllFromData();
            }
        });

        setTotal();

        setEscPrint();
        /*showUSBDeviceChooseDialog();*/
        showBluetoothDeviceChooseDialog();
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
        PrinterInterface printerInterface  = piFactory.create();
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

    @Override
    public void initView() {

    }

    @Override
    public void addListener() {

    }

    @Override
    public void init() {
        BaseApplication.instance.setCurrentCmdType(BaseEnum.CMD_ESC);
        printerFactory = new UniversalPrinterFactory();
        rtPrinter = printerFactory.create();

        /*tv_ver.setText("PrinterExample Ver: v" + TonyUtils.getVersionName(this));*/
        PrinterObserverManager.getInstance().add(this);//添加连接状态监听

    }

    public void setPriceLists(int min) {
        priceLists = databaseHelper.priceLists(min);
        priceListView.setAdapter(new PriceAdapter(priceLists, TicketAndTracking.this));
        setTotal();

    }

    public void setTotal() {
        int totalTickets = getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getInt(UtilStrings.TOTAL_TICKETS, 0);
        int totalCollections = getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0).getInt(UtilStrings.TOTAL_COLLECTIONS, 0);
//        totalCollectionTickets.setText("Total Tickets :" + String.valueOf(totalTickets) + "\n Total Colletions :" + String.valueOf(totalCollections));
        totalCollectionTickets.setText(getString(R.string.total_tickets) + GeneralUtils.getUnicodeNumber(totalTickets) + "\n" + getString(R.string.total_collections) + GeneralUtils.getUnicodeNumber(totalCollections));


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
}
