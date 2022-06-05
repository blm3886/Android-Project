package com.example.stocks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class FavouritesAdapter extends ArrayAdapter<Favourites> {

    private Context mContext;
    int mResource;

    public FavouritesAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Favourites> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @Override
    //geting the view and attaching it to the list view.
    public View getView(int position, View convertView, ViewGroup parent){
        //getting the portfolio info
        String tickerName = getItem(position).getTickerName();
        String tickerDescription = getItem(position).getTickerDescription();
        String currentvalue = getItem(position).getCurrentvalue();
        String change = getItem(position).getChange();


        Favourites favourites = new Favourites(tickerName,tickerDescription,currentvalue,change);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent,false);

        TextView tvtickerName = (TextView)convertView.findViewById(R.id.textView1);
        TextView tvtickerDescription = (TextView)convertView.findViewById(R.id.textView2);
        TextView tvcurrentvalue = (TextView)convertView.findViewById(R.id.textView3);
        TextView tvchange = (TextView)convertView.findViewById(R.id.textView4);

        tvtickerName.setText(tickerName);
        tvtickerDescription.setText(tickerDescription);
        tvcurrentvalue.setText(currentvalue);
        tvchange.setText(change);

        return convertView;
    }



}
