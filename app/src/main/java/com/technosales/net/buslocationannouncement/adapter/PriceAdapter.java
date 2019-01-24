package com.technosales.net.buslocationannouncement.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.technosales.net.buslocationannouncement.R;
import com.technosales.net.buslocationannouncement.activity.TicketAndTracking;
import com.technosales.net.buslocationannouncement.helper.DatabaseHelper;
import com.technosales.net.buslocationannouncement.pojo.PriceList;
import com.technosales.net.buslocationannouncement.pojo.TicketInfoList;
import com.technosales.net.buslocationannouncement.utils.GeneralUtils;
import com.technosales.net.buslocationannouncement.utils.UtilStrings;

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

        holder.price_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences = context.getSharedPreferences(UtilStrings.SHARED_PREFERENCES, 0);
                databaseHelper = new DatabaseHelper(context);
                total_tickets = preferences.getInt(UtilStrings.TOTAL_TICKETS, 0);
                total_collections = preferences.getInt(UtilStrings.TOTAL_COLLECTIONS, 0);
                deviceId = preferences.getString(UtilStrings.DEVICE_ID, "");
                latitude = preferences.getString(UtilStrings.LATITUDE, "");
                longitude = preferences.getString(UtilStrings.LONGITUDE, "");

                total_tickets = total_tickets + 1;
                total_collections = total_collections + Integer.parseInt(priceList.price_value);
                preferences.edit().putInt(UtilStrings.TOTAL_TICKETS, total_tickets).apply();
                preferences.edit().putInt(UtilStrings.TOTAL_COLLECTIONS, total_collections).apply();

                if (((TicketAndTracking) context).normalDiscountToggle.isOn()) {
                    ticketType = "discount";

                    /*((TicketAndTracking) context).normalDiscountToggle.setOn(false);
                    ((TicketAndTracking) context).setPriceLists(4);*/
                } else {
                    ticketType = "full";
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

                TicketInfoList ticketInfoList = new TicketInfoList();
                ticketInfoList.ticketNumber = deviceId.substring(deviceId.length() - 4) + GeneralUtils.getDate() + "" + valueOfTickets;
                ticketInfoList.ticketPrice = String.valueOf(Integer.parseInt(priceList.price_value));
                ticketInfoList.ticketType = ticketType;
                ticketInfoList.ticketDate = GeneralUtils.getFullDate();
                ticketInfoList.ticketTime = GeneralUtils.getTime();
                ticketInfoList.ticketLat = latitude;
                ticketInfoList.ticketLng = longitude;

                databaseHelper.insertTicketInfo(ticketInfoList);

                /*Log.i("TicketInfoSize", "" + String.valueOf(databaseHelper.ticketInfoLists().size()));*/

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


        public MyViewHolder(View itemView) {
            super(itemView);


            price_value = itemView.findViewById(R.id.price_value);


        }
    }


}
