package com.example.stocks;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;


public class HorlyChartFragment extends Fragment {

    WebView hourlyChartWebView;
    JSONObject chartsJSONData;
    int changeIndicator;
    String tickerName;

    public HorlyChartFragment(JSONObject chartsJSONData, int changeIndicator, String tickerName) {
        this.chartsJSONData = chartsJSONData;
        this.changeIndicator = changeIndicator;
        this.tickerName = tickerName;
        //Log.e(null, "HorlyChartFragment: "+chartsJSONData.toString()+" "+this.changeIndicator+" "+this.tickerName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_horly_chart, container, false);
        String JSONData = this.chartsJSONData.toString();
        hourlyChartWebView = (WebView) v.findViewById(R.id.hourlyChartWebView);
        hourlyChartWebView.getSettings().setJavaScriptEnabled(true);
        hourlyChartWebView.loadUrl("file:///android_asset/HourlyChart.html");
        hourlyChartWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                //hourlyChartWebView.loadUrl("javascript:getHourlyStockData('"+JSONData+","+getChangeIndicator()+","+getTickerName()+"')");
                hourlyChartWebView.loadUrl("javascript:setChangeIndicator("+getChangeIndicator()+")");
                hourlyChartWebView.loadUrl("javascript:setTickerName("+getTickerName()+")");
                Log.e(null, "onPageFinished: "+getTickerName());
                hourlyChartWebView.loadUrl("javascript:getHourlyStockData("+JSONData+")");
            }
        });
      return v;
    }

    public int getChangeIndicator() {
        return this.changeIndicator;
    }

    public String getTickerName() {
        return this.tickerName;
    }
}