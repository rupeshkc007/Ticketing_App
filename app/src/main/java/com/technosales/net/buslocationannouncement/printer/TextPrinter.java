package com.technosales.net.buslocationannouncement.printer;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.rt.printerlibrary.bean.UsbConfigBean;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.cmd.PinFactory;
import com.rt.printerlibrary.connect.PrinterInterface;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.enumerate.ESCFontTypeEnum;
import com.rt.printerlibrary.enumerate.SettingEnum;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.factory.connect.PIFactory;
import com.rt.printerlibrary.factory.connect.UsbFactory;
import com.rt.printerlibrary.factory.printer.PinPrinterFactory;
import com.rt.printerlibrary.factory.printer.ThermalPrinterFactory;
import com.rt.printerlibrary.printer.RTPrinter;
import com.rt.printerlibrary.setting.TextSetting;
import com.technosales.net.buslocationannouncement.printer.app.BaseActivity;
import com.technosales.net.buslocationannouncement.printer.app.BaseApplication;
import com.technosales.net.buslocationannouncement.printer.app.BaseEnum;

import java.io.UnsupportedEncodingException;

public class TextPrinter extends BaseActivity {
    private RTPrinter rtPrinter;
    private String printStr = "RUPESH KC";
    private String mChartsetName = "GBK";
    private Context context;
    private PinPrinterFactory printerFactory;
    private Object configObj;
    private UsbDeviceReceiver mUsbReceiver;

    public TextPrinter(Context context, String printStr) {
        this.printStr = printStr;
        this.context = context;
        init();

    }

    public void textPrint() throws UnsupportedEncodingException {
        escPrint();
    }

    public void pinTextPrint() throws UnsupportedEncodingException {

        if (rtPrinter == null) {
            return;
        }

        TextSetting textSetting = new TextSetting();
        textSetting.setBold(SettingEnum.Enable);//加粗
        textSetting.setAlign(CommonEnum.ALIGN_LEFT);
//        textSetting.setFontStyle(SettingEnum.FONT_STYLE_SHADOW);
//        textSetting.setItalic(SettingEnum.Enable);
        textSetting.setDoubleHeight(SettingEnum.Enable);//倍高
        textSetting.setDoubleWidth(SettingEnum.Enable);//倍宽
        textSetting.setDoublePrinting(SettingEnum.Enable);//重叠打印
//        textSetting.setPinPrintMode(CommonEnum.PIN_PRINT_MODE_Bidirectional);
        textSetting.setUnderline(SettingEnum.Enable);//下划线

        CmdFactory cmdFactory = new PinFactory();
        Cmd cmd = cmdFactory.create();
        cmd.append(cmd.getHeaderCmd());//初始化
        cmd.append(cmd.getTextCmd(textSetting, printStr, mChartsetName));
        cmd.append(cmd.getLFCRCmd());//换行
        cmd.append(cmd.getEndCmd());//退纸

        rtPrinter.writeMsgAsync(cmd.getAppendCmds());
    }

    public void escPrint() throws UnsupportedEncodingException {
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

    @Override
    public void initView() {

    }

    @Override
    public void addListener() {

    }

    @Override
    public void init() {

    }


}
