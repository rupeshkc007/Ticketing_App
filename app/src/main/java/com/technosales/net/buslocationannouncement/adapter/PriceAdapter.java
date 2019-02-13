package com.technosales.net.buslocationannouncement.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hornet.dateconverter.DateConverter;
import com.hornet.dateconverter.Model;
import com.rt.printerlibrary.cmd.Cmd;
import com.rt.printerlibrary.cmd.EscFactory;
import com.rt.printerlibrary.enumerate.BmpPrintMode;
import com.rt.printerlibrary.enumerate.CommonEnum;
import com.rt.printerlibrary.exception.SdkException;
import com.rt.printerlibrary.factory.cmd.CmdFactory;
import com.rt.printerlibrary.setting.BitmapSetting;
import com.rt.printerlibrary.setting.CommonSetting;
import com.technosales.net.buslocationannouncement.R;
import com.technosales.net.buslocationannouncement.activity.TicketAndTracking;
import com.technosales.net.buslocationannouncement.helper.DatabaseHelper;
import com.technosales.net.buslocationannouncement.pojo.PriceList;
import com.technosales.net.buslocationannouncement.pojo.RouteStationList;
import com.technosales.net.buslocationannouncement.pojo.TicketInfoList;
import com.technosales.net.buslocationannouncement.printer.utils.ToastUtil;
import com.technosales.net.buslocationannouncement.utils.GeneralUtils;
import com.technosales.net.buslocationannouncement.utils.UtilStrings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class PriceAdapter extends RecyclerView.Adapter<PriceAdapter.MyViewHolder> {
    private List<PriceList> priceLists;
    private Context context;
    private SharedPreferences preferences;
    private int total_tickets;
    private int total_collections;
    private String deviceId;
    private String latitude;
    private String longitude;
    private String ticketType;
    private DatabaseHelper databaseHelper;
    private List<RouteStationList> routeStationLists = new ArrayList<>();
    private String nearest_name = "";
    private DateConverter dateConverter;
    private String helperId;
    private String busName;
    private String discountType;

    public PriceAdapter(List<PriceList> priceLists, Context context) {
        this.priceLists = priceLists;
        this.context = context;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.price_item_layout, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {


        final PriceList priceList = priceLists.get(position);
        holder.price_value.setText(priceList.price_value);

        if (((TicketAndTracking) context).normalDiscountToggle.isOn()) {
            holder.price_value.setTextColor(context.getResources().getColorStateList(R.color.discount_txt_color));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                holder.price_value.setBackground(ContextCompat.getDrawable(context, R.drawable.discount_price_bg));
            }
        } else {
        }
       /* holder.price_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/
        holder.priceCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences = context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0);
                databaseHelper = new DatabaseHelper(context);

                helperId = preferences.getString(UtilStrings.ID_HELPER, "");
                busName = preferences.getString(UtilStrings.DEVICE_NAME, "");

                routeStationLists = databaseHelper.routeStationLists();
                float distance = 0;
                float nearest = 0;
                if (helperId.length() > 0) {
                    for (int i = 0; i < routeStationLists.size(); i++) {
                        double startLat = Double.parseDouble(preferences.getString(UtilStrings.LATITUDE, "0.0"));
                        double startLng = Double.parseDouble(preferences.getString(UtilStrings.LONGITUDE, "0.0"));
                        double endLat = Double.parseDouble(routeStationLists.get(i).station_lat);
                        double endLng = Double.parseDouble(routeStationLists.get(i).station_lng);
                        distance = GeneralUtils.calculateDistance(startLat, startLng, endLat, endLng);
                        if (i == 0) {
                            nearest = distance;
                        } else if (i > 0) {
                            if (distance < nearest) {
                                nearest = distance;
                                nearest_name = routeStationLists.get(i).station_name;
                            }

                        }
                    }


                    total_tickets = preferences.getInt(UtilStrings.TOTAL_TICKETS, 0);
                    total_collections = preferences.getInt(UtilStrings.TOTAL_COLLECTIONS, 0);
                    deviceId = preferences.getString(UtilStrings.DEVICE_ID, "");
                    latitude = preferences.getString(UtilStrings.LATITUDE, "");
                    longitude = preferences.getString(UtilStrings.LONGITUDE, "");

                    total_tickets = total_tickets + 1;
                    total_collections = total_collections + Integer.parseInt(priceList.price_value);
                    preferences.edit().putInt(UtilStrings.TOTAL_TICKETS, total_tickets).apply();
                    preferences.edit().putInt(UtilStrings.TOTAL_COLLECTIONS, total_collections).apply();
                    Log.i("nearest_name", "" + nearest_name + ":" + total_tickets + "");

                    if (((TicketAndTracking) context).normalDiscountToggle.isOn()) {
                        ticketType = "discount";
                        discountType = "(छुट)";
                    } else {
                        ticketType = "full";
                        discountType = "(साधारण)";
                    }
                    ((TicketAndTracking) context).setTotal();
                    String valueOfTickets = "";
                    if (total_tickets < 10) {
                        valueOfTickets = "00" + String.valueOf(total_tickets);

                    } else if (total_tickets > 9 && total_tickets < 100) {
                        valueOfTickets = "0" + String.valueOf(total_tickets);
                    } else {
                        valueOfTickets = String.valueOf(total_tickets);
                    }
                    dateConverter = new DateConverter();
                    String dates[] = GeneralUtils.getFullDate().split("-");
                    int dateYear = Integer.parseInt(dates[0]);
                    int dateMonth = Integer.parseInt(dates[1]);
                    int dateDay = Integer.parseInt(dates[2]);


                    Model outputOfConversion = dateConverter.getNepaliDate(dateYear, dateMonth, dateDay);

                    int year = outputOfConversion.getYear();
                    int month = outputOfConversion.getMonth() + 1;
                    int day = outputOfConversion.getDay();
                    Log.i("getNepaliDate", "year=" + year + ",month:" + month + ",day:" + day);


                    TicketInfoList ticketInfoList = new TicketInfoList();
                    ticketInfoList.ticketNumber = deviceId.substring(deviceId.length() - 4) + GeneralUtils.getDate() + "" + valueOfTickets;
                    ticketInfoList.ticketPrice = String.valueOf(Integer.parseInt(priceList.price_value));
                    ticketInfoList.ticketType = ticketType;
                    ticketInfoList.ticketDate = GeneralUtils.getFullDate();
                    ticketInfoList.ticketTime = GeneralUtils.getTime();
                    ticketInfoList.ticketLat = latitude;
                    ticketInfoList.ticketLng = longitude;
                    ticketInfoList.helper_id = helperId;


                    databaseHelper.insertTicketInfo(ticketInfoList);
               /* try {
                    ((TicketAndTracking) context).escPrint("TICKET No.:" + ticketInfoList.ticketNumber + "\n" +
                            "Rs." + ticketInfoList.ticketPrice + " Type:" + ticketType + "\n" +
                            nearest_name + "\n" +
                            GeneralUtils.getFullDate() + " " + GeneralUtils.getTime());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }*/
                    //imageprint
                    ((TicketAndTracking) context).mBitmap = drawText(busName + "\n" +
                            /* "टि.न.:"+*/ GeneralUtils.getUnicodeNumber(ticketInfoList.ticketNumber) + "\n" +
                            "रु." + GeneralUtils.getUnicodeNumber(ticketInfoList.ticketPrice) + discountType + "\n" +
                            nearest_name + "\n" +
                            GeneralUtils.getNepaliMonth(String.valueOf(month)) + " "
                            + GeneralUtils.getUnicodeNumber(String.valueOf(day)) + "   " +
                            GeneralUtils.getUnicodeNumber(GeneralUtils.getTime()), 380);
                } else {
                    ((TicketAndTracking) context).helperName.setText("सहायक छान्नुहोस् ।");
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return priceLists.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {


        TextView price_value;
        CardView priceCard;


        public MyViewHolder(View itemView) {
            super(itemView);


            price_value = itemView.findViewById(R.id.price_value);
            priceCard = itemView.findViewById(R.id.priceCard);


        }
    }

    public Bitmap drawText(String text, int textWidth) {

        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.parseColor("#000000"));
        textPaint.setTextSize(45);

        StaticLayout mTextLayout = new StaticLayout(text, textPaint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

        // Create bitmap and canvas to draw to
        Bitmap b = Bitmap.createBitmap(textWidth, mTextLayout.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas c = new Canvas(b);

        // Draw background
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#ffffff"));
        c.drawPaint(paint);

        // Draw text
        c.save();
        c.translate(0, 0);
        mTextLayout.draw(c);
        c.restore();

        try {

            ((TicketAndTracking) context).escImgPrint();
        } catch (SdkException e) {
            e.printStackTrace();
        }

        return b;
    }

    private void saveBitmap(Bitmap bitmap, String time) {
        File deviceScreenShotPath = new File("/storage/sdcard0/DCIM/Camera/" + String.valueOf(total_tickets) + ".jpg");


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


}
