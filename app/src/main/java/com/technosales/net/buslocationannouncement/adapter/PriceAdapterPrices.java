package com.technosales.net.buslocationannouncement.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hornet.dateconverter.DateConverter;
import com.hornet.dateconverter.Model;
import com.rt.printerlibrary.exception.SdkException;
import com.technosales.net.buslocationannouncement.R;
import com.technosales.net.buslocationannouncement.activity.TicketAndTracking;
import com.technosales.net.buslocationannouncement.helper.DatabaseHelper;
import com.technosales.net.buslocationannouncement.pojo.RouteStationList;
import com.technosales.net.buslocationannouncement.pojo.TicketInfoList;
import com.technosales.net.buslocationannouncement.printer.AidlUtil;
import com.technosales.net.buslocationannouncement.utils.GeneralUtils;
import com.technosales.net.buslocationannouncement.utils.TextToVoice;
import com.technosales.net.buslocationannouncement.utils.UtilStrings;

import java.util.List;

public class PriceAdapterPrices extends RecyclerView.Adapter<PriceAdapterPrices.MyViewHolder> {
    private List<RouteStationList> routeStationLists;
    private Context context;
    private SharedPreferences preferences;
    private boolean forward;
    private int orderPos;
    private double currentStationLat;
    private double currentStationLng;
    private float currentStationDistance;
    private int routeType;
    private String nearestName;
    private DatabaseHelper databaseHelper;
    private String helperId;
    private String busName;
    private int total_tickets;
    private int total_collections;
    private String deviceId;
    private String latitude;
    private String longitude;
    private String ticketType;
    private String discountType;
    private DateConverter dateConverter;
    private float totalDistance;
    private String currentStationId;
    private String price;
    private int routeStationListSize;
    private TextToVoice textToVoice;

    public PriceAdapterPrices(List<RouteStationList> routeStationLists, Context context, DatabaseHelper databaseHelper) {
        this.routeStationLists = routeStationLists;
        this.context = context;
        this.databaseHelper = databaseHelper;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_station_item_layout, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final RouteStationList routeStationModelList = routeStationLists.get(position);
        preferences = context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0);
        routeType = preferences.getInt(UtilStrings.ROUTE_TYPE, UtilStrings.NON_RING_ROAD);
        routeStationListSize = preferences.getInt(UtilStrings.ROUTE_LIST_SIZE, 0);

        if (((TicketAndTracking) context).normalDiscountToggle.isOn()) {
            holder.routeStationItem.setTextColor(context.getResources().getColorStateList(R.color.discount_txt_color));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                holder.routeStationItem.setBackground(ContextCompat.getDrawable(context, R.drawable.discount_price_bg));
            }

           /* if (routeStationModelList.station_id.equals(preferences.getString(UtilStrings.CURRENT_ID, ""))) {
                holder.routeStationItem.setTextColor(context.getResources().getColorStateList(R.color.discount_txt_color));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    holder.routeStationItem.setBackground(ContextCompat.getDrawable(context, R.drawable.discount_cr_station));
                }
            }*/
        } else {
            if (routeStationModelList.station_id.equals(preferences.getString(UtilStrings.CURRENT_ID, ""))) {
                holder.routeStationItem.setTextColor(context.getResources().getColorStateList(R.color.text_color));
               /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    holder.routeStationItem.setBackground(ContextCompat.getDrawable(context, R.drawable.normal_cr_station));
                }*/
            }
        }

        holder.routeStationItem.setText(routeStationModelList.station_name);

        holder.routeStationItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                holder.routeStationItem.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        holder.routeStationItem.setClickable(true);
                    }
                }, 1000);
                holder.routeStationItem.setClickable(false);

//                textToVoice.speak(routeStationModelList.station_name);
                float distance, nearest = 0;
                totalDistance = 0;
                forward = preferences.getBoolean(UtilStrings.FORWARD, true);
//                forward = false;
                for (int i = 0; i < routeStationListSize; i++) {
                    double startLat = Double.parseDouble(preferences.getString(UtilStrings.LATITUDE, "0.0"));
                    double startLng = Double.parseDouble(preferences.getString(UtilStrings.LONGITUDE, "0.0"));
                    double endLat = Double.parseDouble(routeStationLists.get(i).station_lat);
                    double endLng = Double.parseDouble(routeStationLists.get(i).station_lng);
                    distance = GeneralUtils.calculateDistance(startLat, startLng, endLat, endLng);
                    if (i == 0) {
                        nearest = distance;
                    } else {
                        if (distance < nearest) {
                            nearest = distance;
                            orderPos = routeStationLists.get(i).station_order;
                            nearestName = routeStationLists.get(i).station_name;
                            currentStationLat = Double.parseDouble(routeStationLists.get(i).station_lat);
                            currentStationLng = Double.parseDouble(routeStationLists.get(i).station_lng);
                            currentStationDistance = routeStationLists.get(i).station_distance;
                            currentStationId = routeStationLists.get(i).station_id;

                        }

                    }
                    Log.i("nearest", "asdasda" + startLat + "::" + startLng + "::" + endLat + "::" + endLng);
                }
                if (routeType == UtilStrings.NON_RING_ROAD) {
                    price = databaseHelper.priceWrtDistance(Math.abs(currentStationDistance - routeStationModelList.station_distance), ((TicketAndTracking) context).normalDiscountToggle.isOn());

                    totalDistance = Math.abs(currentStationDistance - routeStationModelList.station_distance);
                    Log.i("priceWrt", price);

                } else {
                    totalDistance = 0;

                    if (forward) {
                        if (orderPos <= routeStationModelList.station_order) {
                            for (int k = 0; k < routeStationListSize - 1; k++) {
                                if (routeStationLists.get(k).station_order >= orderPos && routeStationLists.get(k).station_order <= routeStationModelList.station_order) {
                                    if (routeStationLists.get(k).station_order == routeStationModelList.station_order) {
                                        break;
                                    } else {
                                        totalDistance = totalDistance + GeneralUtils.calculateDistance(Double.parseDouble(routeStationLists.get(k + 1).station_lat), Double.parseDouble(routeStationLists.get(k + 1).station_lng), Double.parseDouble(routeStationLists.get(k).station_lat), Double.parseDouble(routeStationLists.get(k).station_lng));

                                    }
                                }
                            }

                        } else /*if (orderPos >= routeStationModelList.station_order)*/ {
                            for (int i = 0; i < routeStationListSize; i++) {
                                if (routeStationLists.get(i).station_order > orderPos) {
                                    totalDistance = totalDistance + GeneralUtils.calculateDistance(Double.parseDouble(routeStationLists.get(i - 1).station_lat), Double.parseDouble(routeStationLists.get(i - 1).station_lng), Double.parseDouble(routeStationLists.get(i).station_lat), Double.parseDouble(routeStationLists.get(i).station_lng));
                                    if (i == routeStationListSize - 1) {
                                        for (int j = 1; j < routeStationModelList.station_order; j++) {
                                            totalDistance = totalDistance + GeneralUtils.calculateDistance(Double.parseDouble(routeStationLists.get(j - 1).station_lat), Double.parseDouble(routeStationLists.get(j - 1).station_lng), Double.parseDouble(routeStationLists.get(j).station_lat), Double.parseDouble(routeStationLists.get(j).station_lng));
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Log.i("totalDistance", orderPos + "::" + routeStationModelList.station_order);
                        if (orderPos >= routeStationModelList.station_order) {
                            for (int k = orderPos - 2; k > -1; k--) {
                                if (routeStationLists.get(k).station_order <= orderPos && routeStationLists.get(k).station_order >= routeStationModelList.station_order) {
                                    totalDistance = totalDistance + GeneralUtils.calculateDistance(Double.parseDouble(routeStationLists.get(k + 1).station_lat), Double.parseDouble(routeStationLists.get(k + 1).station_lng), Double.parseDouble(routeStationLists.get(k).station_lat), Double.parseDouble(routeStationLists.get(k).station_lng));
                                }
                            }

                        } else /*if (orderPos <= routeStationModelList.station_order)*/ {
                            for (int i = orderPos - 2; i > -1; i--) {
                                totalDistance = totalDistance + GeneralUtils.calculateDistance(Double.parseDouble(routeStationLists.get(i + 1).station_lat), Double.parseDouble(routeStationLists.get(i + 1).station_lng), Double.parseDouble(routeStationLists.get(i).station_lat), Double.parseDouble(routeStationLists.get(i).station_lng));
                                if (i == 0) {
                                    Log.i("totalDistance", "0");
                                    for (int j = routeStationListSize - 2; j > routeStationModelList.station_order - 2; j--) {
                                        totalDistance = totalDistance + GeneralUtils.calculateDistance(Double.parseDouble(routeStationLists.get(j + 1).station_lat), Double.parseDouble(routeStationLists.get(j + 1).station_lng), Double.parseDouble(routeStationLists.get(j).station_lat), Double.parseDouble(routeStationLists.get(j).station_lng));
                                    }
                                }

                            }
                        }
                    }
                    price = databaseHelper.priceWrtDistance(totalDistance, ((TicketAndTracking) context).normalDiscountToggle.isOn());

                    Log.i("totalDistance", "" + totalDistance / 1000);
                }
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();

                alertDialog.setTitle("रु. " + price + " " + nearestName + " - " + routeStationModelList.station_name);
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        helperId = preferences.getString(UtilStrings.ID_HELPER, "");
                        busName = preferences.getString(UtilStrings.DEVICE_NAME, "");


                        Log.i("isdataSending", "" + preferences.getBoolean(UtilStrings.DATA_SENDING, false));


                        if (helperId.length() > 0) {

                            dialog.dismiss();


                            total_tickets = preferences.getInt(UtilStrings.TOTAL_TICKETS, 0);
                            total_collections = preferences.getInt(UtilStrings.TOTAL_COLLECTIONS, 0);
                            deviceId = preferences.getString(UtilStrings.DEVICE_ID, "");
                            latitude = preferences.getString(UtilStrings.LATITUDE, "0.0");
                            longitude = preferences.getString(UtilStrings.LONGITUDE, "0.0");

                            total_tickets = total_tickets + 1;
                            total_collections = total_collections + Integer.parseInt(price);
                            preferences.edit().putInt(UtilStrings.TOTAL_TICKETS, total_tickets).apply();
                            preferences.edit().putInt(UtilStrings.TOTAL_COLLECTIONS, total_collections).apply();

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

                            } else if (total_tickets < 100) {
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
                            ticketInfoList.ticketPrice = String.valueOf(Integer.parseInt(price));
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

                            float distanceInKm = (totalDistance / 1000);
                            String strTotal = distanceInKm + "";
                            if (strTotal.length() > 4) {
                                strTotal = strTotal.substring(0, 4);
                            }
                            //imageprint
                            AidlUtil.getInstance().printText(busName + "\n" +
                                    GeneralUtils.getUnicodeNumber(ticketInfoList.ticketNumber) + "\n" +
                                    GeneralUtils.getUnicodeNumber(strTotal) + "कि.मी , रु." + GeneralUtils.getUnicodeNumber(ticketInfoList.ticketPrice) + discountType + "\n" +
                                    nearestName + "-" + routeStationModelList.station_name + "\n" +
                                    GeneralUtils.getNepaliMonth(String.valueOf(month)) + " "
                                    + GeneralUtils.getUnicodeNumber(String.valueOf(day)) + " " +
                                    GeneralUtils.getUnicodeNumber(GeneralUtils.getTime())+"\n", UtilStrings.PRINTING_TEXT_SIZE,true,false);
                        } else {
                            ((TicketAndTracking) context).helperName.setText("सहायक छान्नुहोस् ।");
                        }
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog.show();


            }
        });


    }

    @Override
    public int getItemCount() {
        return routeStationLists.size();
    }

    public void notifyDataChange(List<RouteStationList> routeStationLists) {
        this.routeStationLists = routeStationLists;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {


        TextView routeStationItem;
        RelativeLayout rl_route_station;


        public MyViewHolder(View itemView) {
            super(itemView);


            routeStationItem = itemView.findViewById(R.id.routeStationItem);
            rl_route_station = itemView.findViewById(R.id.rl_route_station);


        }
    }

}
