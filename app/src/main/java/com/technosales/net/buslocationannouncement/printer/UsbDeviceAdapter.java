package com.technosales.net.buslocationannouncement.printer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rt.printerlibrary.bean.UsbConfigBean;
import com.technosales.net.buslocationannouncement.R;
import com.technosales.net.buslocationannouncement.activity.TicketAndTracking;
import com.technosales.net.buslocationannouncement.printer.apps.BaseApplication;

import java.util.List;

public class UsbDeviceAdapter extends BaseAdapter {

    private Context mContext;
    private List<UsbDevice> mList;
    private LayoutInflater mInflater;

    public UsbDeviceAdapter(Context context, List<UsbDevice> list) {
        this.mContext = context;
        this.mList = list;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        TextView tvText;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.usb_dialog_item, null);
            holder = new ViewHolder();
            holder.tvText = (TextView) convertView.findViewById(R.id.tv_usb_dialog_item_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        UsbDevice usbDevice = mList.get(position);
        holder.tvText.setText(mContext.getString(R.string.adapter_usbdevice) + "\n"
                        + "ProductID:" + usbDevice.getProductId() + "\n"
                        + "VendoID:" + usbDevice.getVendorId() + "\n"
//                + "DeviceClass:"+ usbDevice.getDeviceClass() + "\n"
//                        + "ProductName:" + usbDevice.getProductName() == null ? "null" : usbDevice.getProductName() + "\n"
        );
        if (String.valueOf(usbDevice.getProductId()).equals("4070") || String.valueOf(usbDevice.getVendorId()).equals("4070")) {
            UsbDevice mUsbDevice = mList.get(position);


            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(
                    mContext,
                    0,
                    new Intent(mContext.getApplicationInfo().packageName),
                    0);
/*
                tv_device_selected.setText(getString(R.string.adapter_usbdevice) + mUsbDevice.getDeviceId()); //+ (position + 1));
*/
            ((TicketAndTracking) mContext).configObj = new UsbConfigBean(BaseApplication.getInstance(), mUsbDevice, mPermissionIntent);

            try {
                ((TicketAndTracking) mContext).doConnect();
                ((TicketAndTracking) mContext).usbDeviceChooseDialog.dismiss();
            } catch (Exception ex) {

            }
        }

        return convertView;
    }

}