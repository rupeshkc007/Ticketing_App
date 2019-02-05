package com.technosales.net.buslocationannouncement.printer;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.rt.printerlibrary.bean.UsbConfigBean;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.connect.PrinterInterface;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.enumerate.ConnectStateEnum;
import com.rt.printerlibrary.enumerate.ESCFontTypeEnum;
import com.rt.printerlibrary.enumerate.SettingEnum;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.factory.connect.PIFactory;
import com.rt.printerlibrary.factory.connect.UsbFactory;
import com.rt.printerlibrary.factory.printer.PrinterFactory;
import com.rt.printerlibrary.factory.printer.ThermalPrinterFactory;
import com.rt.printerlibrary.factory.printer.UniversalPrinterFactory;
import com.rt.printerlibrary.observer.PrinterObserver;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.setting.TextSetting;
import com.technosales.net.buslocationannouncement.printer.app.BaseActivity;
import com.technosales.net.buslocationannouncement.printer.app.BaseApplication;
import com.technosales.net.buslocationannouncement.printer.app.BaseEnum;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ConnectUsbPrinter extends BaseActivity implements PrinterObserver {
    private Context context;
    private Object configObj;
    private ArrayList<PrinterInterface> printerInterfaceArrayList = new ArrayList<>();
    private UsbDeviceReceiver mUsbReceiver;
    private List<UsbDevice> mList;
    private RTPrinter rtPrinter;
    @BaseEnum.ConnectType
    private int checkedConType = BaseEnum.CON_USB;
    private PrinterFactory printerFactory;
    private PrinterInterface curPrinterInterface = null;
    private String printStr = "RUPESH KC";
    private String mChartsetName = "GBK";

    public ConnectUsbPrinter(Context context) {
        this.context = context;
        init();
    }

    public void connectUSB(UsbConfigBean usbConfigBean) {
        UsbManager mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
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

    private boolean isInConnectList(Object configObj) {
        boolean isInList = false;
        for (int i = 0; i < printerInterfaceArrayList.size(); i++) {
            PrinterInterface printerInterface = printerInterfaceArrayList.get(i);
            if (configObj.toString().equals(printerInterface.getConfigObject().toString())) {
                if (printerInterface.getConnectState() == ConnectStateEnum.Connected) {
                    isInList = true;
                    break;
                }
            }
        }
        return isInList;
    }

    private void registerUsbReceiver() {
        checkedConType = BaseEnum.CON_USB;
        /*BaseApplication.instance.setCurrentCmdType(BaseEnum.CMD_ESC);*/
        printerFactory = new ThermalPrinterFactory();
        rtPrinter = printerFactory.create();
        rtPrinter.setPrinterInterface(curPrinterInterface);
        mUsbReceiver = new UsbDeviceReceiver(new UsbDeviceReceiver.CallBack() {
            @Override
            public void onPermissionGranted(UsbDevice usbDevice) {

            }

            @Override
            public void onDeviceAttached(UsbDevice usbDevice) {
                mList.add(usbDevice);
                UsbDevice usbDev = mList.get(0);
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        new Intent(context.getApplicationInfo().packageName),
                        0);
                configObj = new UsbConfigBean(BaseApplication.getInstance(), usbDev, mPermissionIntent);
                UsbConfigBean usbConfigBean = (UsbConfigBean) configObj;
                connectUSB(usbConfigBean);
                /*doConnect();*/
            }

            @Override
            public void onDeviceDetached(UsbDevice usbDevice) {
                mList.remove(usbDevice);
                if (mList.size() == 0) {
                }
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(mUsbReceiver, intentFilter);

        try {
            new TextPrinter(context, "abc").escPrint();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void doConnect() {
        UsbConfigBean usbConfigBean = (UsbConfigBean) configObj;
        connectUSB(usbConfigBean);
    }

    @Override
    public void printerObserverCallback(PrinterInterface printerInterface, int state) {
        switch (state) {
            case CommonEnum.CONNECT_STATE_SUCCESS:
                curPrinterInterface = printerInterface;//设置为当前连接， set current Printer Interface
                printerInterfaceArrayList.add(printerInterface);//多连接-添加到已连接列表
                rtPrinter.setPrinterInterface(printerInterface);
                BaseApplication.getInstance().setRtPrinter(rtPrinter);
                break;
        }

    }

    @Override
    public void printerReadMsgCallback(PrinterInterface printerInterface, byte[] bytes) {

    }

    @Override
    public void initView() {

    }

    @Override
    public void addListener() {

    }

    @Override
    public void init() {
        printerFactory = new UniversalPrinterFactory();
        rtPrinter = printerFactory.create();
        registerUsbReceiver();
    }
    public void escPrint() throws UnsupportedEncodingException {
        rtPrinter = BaseApplication.getInstance().getRtPrinter();
        if (rtPrinter != null) {
            CmdFactory escFac = new EscFactory();
            Cmd escCmd = escFac.create();
            escCmd.append(escCmd.getHeaderCmd());//初始化, Initial

            escCmd.setChartsetName(mChartsetName);

            TextSetting textSetting = new TextSetting();
            textSetting.setAlign(CommonEnum.ALIGN_MIDDLE);//对齐方式-左对齐，居中，右对齐
            textSetting.setBold(SettingEnum.Disable);
            textSetting.setUnderline(SettingEnum.Disable);
            textSetting.setIsAntiWhite(SettingEnum.Disable);
            textSetting.setDoubleHeight(SettingEnum.Disable);
            textSetting.setDoubleWidth(SettingEnum.Disable);

            textSetting.setEscFontType(ESCFontTypeEnum.FONT_A_12x24);

            escCmd.append(escCmd.getTextCmd(textSetting, printStr, mChartsetName));

            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getLFCRCmd());
            escCmd.append(escCmd.getHeaderCmd());//初始化, Initial
            escCmd.append(escCmd.getLFCRCmd());

            rtPrinter.writeMsg(escCmd.getAppendCmds());

            /*rtPrinter.writeMsgAsync(escCmd.getAppendCmds());*/
        }
    }
}
