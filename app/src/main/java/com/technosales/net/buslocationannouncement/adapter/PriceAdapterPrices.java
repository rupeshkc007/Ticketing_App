package com.technosales.net.buslocationannouncement.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.hornet.dateconverter.DateConverter;
import com.hornet.dateconverter.Model;
import com.rt.printerlibrary.exception.SdkException;
import com.technosales.net.buslocationannouncement.R;
import com.technosales.net.buslocationannouncement.activity.TicketAndTracking;
import com.technosales.net.buslocationannouncement.helper.DatabaseHelper;
import com.technosales.net.buslocationannouncement.pojo.PriceList;
import com.technosales.net.buslocationannouncement.pojo.RouteStationList;
import com.technosales.net.buslocationannouncement.pojo.TicketInfoList;
import com.technosales.net.buslocationannouncement.utils.GeneralUtils;
import com.technosales.net.buslocationannouncement.utils.UtilStrings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PriceAdapterPrices extends RecyclerView.Adapter<PriceAdapterPrices.MyViewHolder> {
    private List<RouteStationList> routeStationLists;
    private Context context;
    private SharedPreferences preferences;


    public PriceAdapterPrices(List<RouteStationList> routeStationLists, Context context) {
        this.routeStationLists = routeStationLists;
        this.context = context;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_station_item_layout, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        RouteStationList routeStationList = routeStationLists.get(position);

        holder.routeStationItem.setText(routeStationList.station_name);


    }

    @Override
    public int getItemCount() {
        return routeStationLists.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {


        TextView routeStationItem;


        public MyViewHolder(View itemView) {
            super(itemView);


            routeStationItem = itemView.findViewById(R.id.routeStationItem);


        }
    }

    public Bitmap drawText(String text, int textWidth) {

        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
//        textPaint.setColor(Color.parseColor("#ffffff"));
        textPaint.setColor(Color.parseColor("#000000"));
        textPaint.setTextSize(45);

        StaticLayout mTextLayout = new StaticLayout(text, textPaint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

        // Create bitmap and canvas to draw to
        Bitmap b = Bitmap.createBitmap(textWidth, mTextLayout.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas c = new Canvas(b);

        // Draw background
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(Color.parseColor("#000000"));
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

}
