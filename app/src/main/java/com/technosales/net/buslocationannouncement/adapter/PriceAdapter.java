package com.technosales.net.buslocationannouncement.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.technosales.net.buslocationannouncement.R;
import com.technosales.net.buslocationannouncement.activity.TicketAndTracking;
import com.technosales.net.buslocationannouncement.pojo.PriceList;

import java.util.List;

public class PriceAdapter extends RecyclerView.Adapter<PriceAdapter.MyViewHolder> {
    private List<PriceList> priceLists;
    private Context context;

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
        PriceList priceList = priceLists.get(position);
        holder.price_value.setText(priceList.price_value);
        holder.price_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((TicketAndTracking) context).normalDiscountToggle.isOn()) {
                    ((TicketAndTracking) context).normalDiscountToggle.setOn(false);
                    ((TicketAndTracking) context).setPriceLists(4);
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


        public MyViewHolder(View itemView) {
            super(itemView);


            price_value = itemView.findViewById(R.id.price_value);


        }
    }


}
