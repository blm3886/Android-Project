package com.example.stocks;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;


public class Recommendation_Chart_Fragment extends Fragment {
  @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      try {
          View v = inflater.inflate(R.layout.fragment_recommendation__chart_, container, false);
          return v;
      }
      catch (Exception e){
          Log.e(null, "onCreateView: "+ "ERROR");
          throw e;
      }

        //Recommendation WebView Display
        /*WebView RecommendationChart = (WebView) v.findViewById(R.id.RecommendationChar_WebView);
        WebSettings webSettings_RecommendationChart = RecommendationChart.getSettings();
        RecommendationChart.getSettings().setJavaScriptEnabled(true);
        RecommendationChart.loadUrl("https://csci571.com/hw/hw9/HW9_Description_Android.pdf");*/

    }
}