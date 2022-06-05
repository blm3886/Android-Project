package com.example.stocks;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PortfolioAdapter extends ArrayAdapter<Portfolio> {

    private static final String TAG = "PortfolioAdapter";
    private Context mContext;
    int mResource;

    public PortfolioAdapter(Context context, int resource, ArrayList<Portfolio> Obj){
        super(context, resource, Obj);
        mContext = context;
        mResource = resource;
    }

   @Override
   //geting the view and attaching it to the list view.
    public View getView(int position, View convertView, ViewGroup parent){
        //getting the portfolio info
        String tickerName = getItem(position).getTickerName();
        String marketValue = getItem(position).getMarketValue();
        String totalShare = getItem(position).getTotalShares();
        String changePriceValues = getItem(position).getChangePriceValues();


        Portfolio portfo = new Portfolio(tickerName,marketValue,totalShare,changePriceValues);

       LayoutInflater inflater = LayoutInflater.from(mContext);
       convertView = inflater.inflate(mResource,parent,false);

       TextView tvtickerName = (TextView)convertView.findViewById(R.id.textView1);
       TextView tvshares = (TextView)convertView.findViewById(R.id.textView2);
       TextView tvprice = (TextView)convertView.findViewById(R.id.textView3);
       TextView tvChangePricesValues = (TextView)convertView.findViewById(R.id.textView4);

       tvtickerName.setText(tickerName);
       tvshares.setText(String.valueOf(totalShare)+"shares");
       tvprice.setText(marketValue);
       tvChangePricesValues.setText(changePriceValues);

       return convertView;
    }







}
