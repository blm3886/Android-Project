package com.example.stocks;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

public class HistoricChartFragment extends Fragment {

    WebView historySuprisesChartWebView;
    private JSONObject historicChartsData;
    private String tickerName;

    public HistoricChartFragment(JSONObject historicChartsData, String companyName) {
         this.historicChartsData = historicChartsData;
         this.tickerName = companyName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_historic_chart, container, false);
        historySuprisesChartWebView = (WebView) v.findViewById(R.id.historicalSuprisesChartWebView);
        historySuprisesChartWebView.getSettings().setJavaScriptEnabled(true);
        historySuprisesChartWebView.setWebViewClient(new WebViewClient());
        historySuprisesChartWebView.loadUrl("file:///android_asset/HistoricalSuprisesChart.html");
        historySuprisesChartWebView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                //hourlyChartWebView.loadUrl("javascript:getHourlyStockData('"+JSONData+","+getChangeIndicator()+","+getTickerName()+"')");
                //historySuprisesChartWebView.loadUrl("javascript:setChangeIndicator("+getChangeIndicator()+")");
                //historySuprisesChartWebView.loadUrl("javascript:setTickerName("+getTickerName()+")");
                //Log.e(null, "onPageFinished: "+getTickerName());
                Log.e(null, "onPageFinished: "+getHistoricChartsData() );
                //historySuprisesChartWebView.loadUrl("javascript:myFun("+ getHistoricChartsData()+")");
                //historySuprisesChartWebView.loadUrl("javascript:myFun("")");
            }
        });
        return v;
    }

    public JSONObject getHistoricChartsData() {
        return this.historicChartsData;
    }

    public void setHistoricChartsData(JSONObject historicChartsData) {
        this.historicChartsData = historicChartsData;
    }

    public String getTickerName() {
        return tickerName;
    }

    public void setTickerName(String tickerName) {
        this.tickerName = tickerName;
    }

}