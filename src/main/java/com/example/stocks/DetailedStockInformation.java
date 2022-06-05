package com.example.stocks;

import static com.example.stocks.R.color.GREEN;
import static com.example.stocks.R.color.Link;
import static com.example.stocks.R.color.RED;
import static com.example.stocks.R.color.TEXT_GRAY;
import static com.example.stocks.R.color.black;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.camera2.params.BlackLevelPattern;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;
/*
* Class that displays the main details information of the stocks
*
 */
public class DetailedStockInformation extends AppCompatActivity implements NewsTab_Adapter.OnNoteListener{
    private Toolbar toolbar;
    private String tickerVal;
    final String  BASE_URL ="https://androidstock.uw.r.appspot.com/";
    String imageURL;
    String comapnyName;
    String companyDescription;
    String currentPriceStr;
    String changePriceStr;
    float currentPriceVal;
    int changeIndicator;  // 0-red 1-Green 2-Balck

    ImageView tickerLogo;
    TextView ticker;
    TextView companyDetails;
    TextView currentPrice;
    TextView changePrice;
    ImageView trendingArrow;

    //Charts TabLayout
    private TabLayout chartsTabLayout;
    private ViewPager chartsViewPager;
    private ChartsAdapter chartFragmentAdapter;

    //CHARTS JSON DATA
    private JSONObject hourlyChartsData;
    private JSONObject historicChartsData;

    //Visibility for star Button
    boolean Visibility_UnSelected;

    //StatsData JSONOBJECT
    JSONObject companyStats;

    //AboutData JSONOBJECT
    JSONObject aboutData;

    //Peers JSONOBJECT
    JSONObject peers;

    //
    JSONObject recommendationChartJSON;

    //ArrayStoring Sentiments Data
    ArrayList<Integer> Reddit; // 0 tot 1 pos 2 neg
    ArrayList<Integer> Twitter;

    //Two decimal Formatter
    private static final DecimalFormat df = new DecimalFormat("0.00");

    //Dynamic TextView
    Map<TextView,String> myTextViews;
    View.OnClickListener clicks;

    //REcommendation WebView
    WebView RCWebView;



    //Historical EPS
    WebView HEPSWebView;

    //NEWS JSONDATA
    JSONObject newsTabJSON;

    //NewsModel ArrayList
    ArrayList<NewsModel> newModel;

    //News recycler View
    RecyclerView newsRecyclerView;

    View rootView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        setVisibility_UnSelected(true);
        //Getting Intent Params
        Intent intent = getIntent();
        this.tickerVal = intent.getExtras().getString("query").split(" ")[0];
       //Toolbar
        setContentView(R.layout.activity_detailedstockpage);
                toolbar = findViewById(R.id.myToolBarCustom);
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle(this.tickerVal);

        this.peers = new JSONObject();
        //Initilizing Social Sentiments Array
        this.Reddit = new ArrayList<>();
        this.Twitter = new ArrayList<>();
        //Recommendation Chart
        this.recommendationChartJSON = new JSONObject();
        this.newsTabJSON = new JSONObject();
        this.newModel = new ArrayList<>();
        this.newsRecyclerView = findViewById(R.id.NEWS_RECYCLER);


        //API CALLS
        //getPeersAPI();
        //getSocialSentimentsAPI();
        //getHourlyChartsData();
        //getRecomendationChartsAPI();
        getTickerDetails();
        //getStockPrices();

        //Listener for dynamic TextView
        this.clicks = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map TextView_Set = getMyTextViews();
                String tickerName = (String) TextView_Set.get(v);
                Intent intent = new Intent(DetailedStockInformation.this,DetailedStockInformation.class);
                intent.putExtra("query",tickerName);
                startActivity(intent);
                //Log.e(null, "onClick: "+tickerName+"TEXTVIEW VALUES");
            }
        };
     /*try {
            JSONObject data = getRecommendationChartJSON();
            Log.e(null, "onCreate: "+data.getString("data")+"RC JSON DATA");
        } catch (JSONException e) {
            Log.e(null, "onCreate: "+"ERROR");
            e.printStackTrace();
        }
        web= (WebView) findViewById(R.id.WEBVIEW_RC);
        web.getSettings().setJavaScriptEnabled(true);
        web.loadUrl("file:///android_asset/RecommendationChart.html");*/
        this.RCWebView = findViewById(R.id.WEBVIEW_RC);
        this.RCWebView.getSettings().setJavaScriptEnabled(true);
        this.HEPSWebView = findViewById(R.id.HISTORIC_WEBVIEW);
        this.HEPSWebView.getSettings().setJavaScriptEnabled(true);
        this.rootView = (ScrollView) findViewById(R.id.DETAILS_ACTIVITY);

        super.onCreate(savedInstanceState);
    }

    //MenuOption for Star icon (Favourites star icon)
   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.star_menu, menu);
        return true;
    }

   @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        //Log.e("onOptionsItemSelected: ","STAR OPTION"+item.getItemId() );
        //Log.e("onPrepareOptionsMenu: ","2");
        int valueClicked = item.getItemId();
        int selectedStar = R.id.StarUnselected;
        int unselectedStar = R.id.StarSelected;
        //Add to favorites
        if(valueClicked == selectedStar){
            SharedPreferenceManager.getInstance(this).setFavourites(getCompanyName());
            SharedPreferenceManager.getInstance(this).setComapnyDesc(getCompanyName(),getCompanyDescription());
            Toast.makeText(this, "Added to Favourites", Toast.LENGTH_SHORT).show();
            invalidateOptionsMenu();
        }
        else{
        // REMOVE from favourites
            Toast.makeText(this, "Removed from Favourites", Toast.LENGTH_SHORT).show();
            SharedPreferenceManager.getInstance(getApplicationContext()).removeFavouritesSet(this.comapnyName);
            invalidateOptionsMenu();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.e("onPrepareOptionsMenu: ","1");
        boolean Value = getVisibility_FLAG();
        if(Value){
            menu.findItem(R.id.StarSelected).setVisible(false);
            menu.findItem(R.id.StarUnselected).setVisible(true);
            setVisibility_UnSelected(false);
        }else{
            //WHEN VALUE IS FALSE THE SELECTED STAR WILL BE ACTIVATED -- ADDED TO FAVOURITES
            menu.findItem(R.id.StarSelected).setVisible(true);
            menu.findItem(R.id.StarUnselected).setVisible(false);
            setVisibility_UnSelected(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, HomeActivity.class));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }



    //gettingBasicDetails
    public void getTickerDetails(){
        String BasicData_url = BASE_URL+"getBasicSearch/"+this.tickerVal;
        String stockPrices_url = BASE_URL+"getCurrentPrices/"+this.tickerVal;
        //Log.e(null, "getTickerDetails: urlCHECK"+url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, BasicData_url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.e(null, "onResponse: "+response.toString());
                        try {
                            setImageURL(response.getString("logo"));
                            setCompanyName(response.getString("ticker"));
                            setCompanyDescription(response.getString("name"));
                            setAboutData(response);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        finally {
                            getStockPrices();
                            Log.e(null, "onResponse: "+"getTickerDetails DONE");
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e(null, "onErrorResponse: " +error.toString()+"getTickerDetails");
                    }
                });

        // Access the RequestQueue through your singleton class.
        APIFetch.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }
     //DISPLAY DATA CALLED HERE
    //API CALL TO GET STOCKPRICES
    private void getStockPrices(){
        String stockPrices_url = BASE_URL+"getCurrentPrices/"+this.tickerVal;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, stockPrices_url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.e(null, "onResponse: "+response.toString());
                        try {
                            setCurrentPrice(response.getString("c"));
                            setCurrentPriceVal(Float.parseFloat(response.getString("c")));
                            setChangePrice(response.getString("d"),response.getString("dp"));
                            setCompanyStats(response);
                            //DisplayData();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        finally{
                            getHourlyChartsData();
                            //Log.e(null, "onResponse: "+"getTickerDetails DONE");
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e(null, "onErrorResponse: " +error.toString()+"getStockPrices");
                    }
                });

        // Access the RequestQueue through your singleton class.
        APIFetch.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    //API CALL TO GET HOURLY CHARTS DATA
    private void getHourlyChartsData(){
        String stockPrices_url = BASE_URL+"getHourly/"+this.tickerVal;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, stockPrices_url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.e(null, "onResponse: "+response.toString());
                        setHourlyChartsData(response);
                        getPeersAPI();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e(null, "onErrorResponse: " +error.toString()+"getHourlyChartsData");
                    }
                });

        // Access the RequestQueue through your singleton class.
        APIFetch.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    //API CALL TO GET THE PEERS DATA
    private void getPeersAPI() {
        String stockPrices_url = BASE_URL+"getPeers/"+this.tickerVal;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, stockPrices_url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.e(null, "onResponse: "+response.toString());
                        setPeers(response);
                        getSocialSentimentsAPI();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e(null, "onErrorResponse: " +error.toString()+"getPeersAPI");
                    }
                });

        // Access the RequestQueue through your singleton class.
        APIFetch.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    //API CALL TO GET THE SENTIMENTS SECTION
    private void getSocialSentimentsAPI() {
        String stockPrices_url = BASE_URL+"getSocialSentiments/"+this.tickerVal;
        //String stockPrices_url = "https://finnhub.io/api/v1/stock/social-sentiment?symbol='+this.tickerVal+'&from=2022-01-01&token=c7roegaad3iel5ubfheg";
        Log.e(null, "getSocialSentimentsAPI: "+"SOCIAL SENTIMENTS CALLED");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, stockPrices_url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.e(null, "onResponse: "+response.toString());
                        try {
                            JSONArray reddit = response.getJSONArray("reddit");
                            JSONArray twitter = response.getJSONArray("twitter");
                            getRecomendationChartsAPI();

                            int redditLength = reddit.length();
                            int red_TotMentions = 0;
                            int red_posMentions = 0;
                            int red_negMentions = 0;


                            for(int i=0; i< redditLength;i++){
                                red_TotMentions = red_TotMentions+reddit.getJSONObject(i).getInt("mention");
                                red_posMentions = red_posMentions+reddit.getJSONObject(i).getInt("positiveMention");
                                red_negMentions = red_negMentions+reddit.getJSONObject(i).getInt("negativeMention");
                            }
                            //ArrayList list = getRedditArray();
                            getRedditArray().add(red_TotMentions);
                            getRedditArray().add(red_posMentions);
                            getRedditArray().add(red_negMentions);
                            //setRedditArray(list);
                            //Log.e(null, "onResponse: "+red_TotMentions+ "API CALL" );
                            Log.e(null, "onResponse: "+getTwitterArray().toString()+" "+getRedditArray().toString()+" "+"REDDIT TWITTER ARRAY");

                            int twitterLength = twitter.length();
                            int twi_TotMentions = 0;
                            int twi_posMentions = 0;
                            int twi_negMentions = 0;

                            for(int i=0; i< twitterLength;i++){
                                twi_TotMentions = twi_TotMentions+twitter.getJSONObject(i).getInt("mention");
                                twi_posMentions = twi_posMentions+twitter.getJSONObject(i).getInt("positiveMention");
                                twi_negMentions = twi_negMentions+twitter.getJSONObject(i).getInt("negativeMention");
                            }
                            //ArrayList list1 = getTwitterArray();
                            getTwitterArray().add(twi_TotMentions);
                            getTwitterArray().add(twi_posMentions);
                            getTwitterArray().add(twi_negMentions);
                            //setTwitterArray(list1);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                       try{
                        JSONObject response = new JSONObject("{\"reddit\":[{\"atTime\":\"2022-05-04 07:00:00\",\"mention\":3,\"positiveScore\":0,\"negativeScore\":-0.99959455,\"positiveMention\":0,\"negativeMention\":2,\"score\":0.99959455},{\"atTime\":\"2022-05-04 06:00:00\",\"mention\":2,\"positiveScore\":0.99541986,\"negativeScore\":-0.9992311,\"positiveMention\":1,\"negativeMention\":1,\"score\":0.99732548},{\"atTime\":\"2022-05-04 05:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":-0.9999672,\"positiveMention\":0,\"negativeMention\":1,\"score\":0.9999672},{\"atTime\":\"2022-05-04 04:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":0,\"positiveMention\":0,\"negativeMention\":0,\"score\":0},{\"atTime\":\"2022-05-04 03:00:00\",\"mention\":6,\"positiveScore\":0.9679194999999999,\"negativeScore\":-0.99994915,\"positiveMention\":3,\"negativeMention\":2,\"score\":0.98073136},{\"atTime\":\"2022-05-04 02:00:00\",\"mention\":4,\"positiveScore\":0.9885771999999999,\"negativeScore\":-0.999962,\"positiveMention\":3,\"negativeMention\":1,\"score\":0.9914234},{\"atTime\":\"2022-05-04 01:00:00\",\"mention\":2,\"positiveScore\":0,\"negativeScore\":-0.99930985,\"positiveMention\":0,\"negativeMention\":2,\"score\":0.99930985},{\"atTime\":\"2022-05-04 00:00:00\",\"mention\":2,\"positiveScore\":0,\"negativeScore\":-0.9998187,\"positiveMention\":0,\"negativeMention\":2,\"score\":0.9998187},{\"atTime\":\"2022-05-03 23:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":-0.99535537,\"positiveMention\":0,\"negativeMention\":1,\"score\":0.99535537},{\"atTime\":\"2022-05-03 22:00:00\",\"mention\":2,\"positiveScore\":0.9947389,\"negativeScore\":-0.99339455,\"positiveMention\":1,\"negativeMention\":1,\"score\":0.9940667249999999},{\"atTime\":\"2022-05-03 21:00:00\",\"mention\":5,\"positiveScore\":0,\"negativeScore\":-0.9999180000000001,\"positiveMention\":0,\"negativeMention\":5,\"score\":0.9999180000000001},{\"atTime\":\"2022-05-03 20:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":-0.9999914,\"positiveMention\":0,\"negativeMention\":1,\"score\":0.9999914},{\"atTime\":\"2022-05-03 19:00:00\",\"mention\":2,\"positiveScore\":0,\"negativeScore\":-0.9985099,\"positiveMention\":0,\"negativeMention\":2,\"score\":0.9985099},{\"atTime\":\"2022-05-03 18:00:00\",\"mention\":9,\"positiveScore\":0.9874813,\"negativeScore\":-0.984193,\"positiveMention\":1,\"negativeMention\":5,\"score\":0.9847410499999999},{\"atTime\":\"2022-05-03 17:00:00\",\"mention\":15,\"positiveScore\":0.9229315,\"negativeScore\":-0.9511977272727272,\"positiveMention\":2,\"negativeMention\":11,\"score\":0.9468490769230768},{\"atTime\":\"2022-05-03 16:00:00\",\"mention\":2,\"positiveScore\":0,\"negativeScore\":-0.9990418,\"positiveMention\":0,\"negativeMention\":2,\"score\":0.9990418},{\"atTime\":\"2022-05-03 15:00:00\",\"mention\":2,\"positiveScore\":0,\"negativeScore\":-0.99989535,\"positiveMention\":0,\"negativeMention\":2,\"score\":0.99989535},{\"atTime\":\"2022-05-03 14:00:00\",\"mention\":1,\"positiveScore\":0.9913362,\"negativeScore\":0,\"positiveMention\":1,\"negativeMention\":0,\"score\":0.9913362},{\"atTime\":\"2022-05-03 13:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":-0.99991775,\"positiveMention\":0,\"negativeMention\":1,\"score\":0.99991775},{\"atTime\":\"2022-05-03 12:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":-0.99907637,\"positiveMention\":0,\"negativeMention\":1,\"score\":0.99907637},{\"atTime\":\"2022-05-03 11:00:00\",\"mention\":1,\"positiveScore\":0.72019905,\"negativeScore\":0,\"positiveMention\":1,\"negativeMention\":0,\"score\":0.72019905},{\"atTime\":\"2022-05-03 10:00:00\",\"mention\":9,\"positiveScore\":0,\"negativeScore\":-0.9845341999999999,\"positiveMention\":0,\"negativeMention\":7,\"score\":0.9845341999999999},{\"atTime\":\"2022-05-03 09:00:00\",\"mention\":6,\"positiveScore\":0.9985178,\"negativeScore\":-0.931975125,\"positiveMention\":1,\"negativeMention\":4,\"score\":0.94528366},{\"atTime\":\"2022-05-03 08:00:00\",\"mention\":11,\"positiveScore\":0,\"negativeScore\":-0.9836546,\"positiveMention\":0,\"negativeMention\":10,\"score\":0.9836546},{\"atTime\":\"2022-05-03 07:00:00\",\"mention\":9,\"positiveScore\":0,\"negativeScore\":-0.9667557777777778,\"positiveMention\":0,\"negativeMention\":9,\"score\":0.9667557777777778},{\"atTime\":\"2022-05-03 06:00:00\",\"mention\":5,\"positiveScore\":0,\"negativeScore\":-0.9225403999999999,\"positiveMention\":0,\"negativeMention\":5,\"score\":0.9225403999999999},{\"atTime\":\"2022-05-03 05:00:00\",\"mention\":11,\"positiveScore\":0.8610748,\"negativeScore\":-0.9887931428571429,\"positiveMention\":4,\"negativeMention\":7,\"score\":0.9423501090909091},{\"atTime\":\"2022-05-03 04:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":0,\"positiveMention\":0,\"negativeMention\":0,\"score\":0},{\"atTime\":\"2022-05-03 03:00:00\",\"mention\":6,\"positiveScore\":0,\"negativeScore\":-0.9983581666666667,\"positiveMention\":0,\"negativeMention\":6,\"score\":0.9983581666666667},{\"atTime\":\"2022-05-03 02:00:00\",\"mention\":6,\"positiveScore\":0,\"negativeScore\":-0.9999242666666667,\"positiveMention\":0,\"negativeMention\":6,\"score\":0.9999242666666667},{\"atTime\":\"2022-05-03 01:00:00\",\"mention\":1,\"positiveScore\":0.75362206,\"negativeScore\":0,\"positiveMention\":1,\"negativeMention\":0,\"score\":0.75362206},{\"atTime\":\"2022-05-03 00:00:00\",\"mention\":2,\"positiveScore\":0,\"negativeScore\":-0.9982209,\"positiveMention\":0,\"negativeMention\":2,\"score\":0.9982209},{\"atTime\":\"2022-05-02 23:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":-0.99244094,\"positiveMention\":0,\"negativeMention\":1,\"score\":0.99244094},{\"atTime\":\"2022-05-02 22:00:00\",\"mention\":2,\"positiveScore\":0,\"negativeScore\":0,\"positiveMention\":0,\"negativeMention\":0,\"score\":0},{\"atTime\":\"2022-05-02 21:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":-0.76634496,\"positiveMention\":0,\"negativeMention\":1,\"score\":0.76634496},{\"atTime\":\"2022-05-02 20:00:00\",\"mention\":5,\"positiveScore\":0.80155283,\"negativeScore\":-0.997046525,\"positiveMention\":1,\"negativeMention\":4,\"score\":0.9579477860000001},{\"atTime\":\"2022-05-02 19:00:00\",\"mention\":5,\"positiveScore\":0,\"negativeScore\":-0.9329588799999999,\"positiveMention\":0,\"negativeMention\":5,\"score\":0.9329588799999999},{\"atTime\":\"2022-05-02 18:00:00\",\"mention\":8,\"positiveScore\":0.8604629,\"negativeScore\":-0.9985317,\"positiveMention\":2,\"negativeMention\":4,\"score\":0.9525087666666666},{\"atTime\":\"2022-05-02 17:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":-0.9999229,\"positiveMention\":0,\"negativeMention\":1,\"score\":0.9999229},{\"atTime\":\"2022-05-02 16:00:00\",\"mention\":4,\"positiveScore\":0,\"negativeScore\":-0.9570467,\"positiveMention\":0,\"negativeMention\":4,\"score\":0.9570467},{\"atTime\":\"2022-05-02 15:00:00\",\"mention\":3,\"positiveScore\":0,\"negativeScore\":-0.999665,\"positiveMention\":0,\"negativeMention\":2,\"score\":0.999665},{\"atTime\":\"2022-05-02 14:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":-0.999995,\"positiveMention\":0,\"negativeMention\":1,\"score\":0.999995},{\"atTime\":\"2022-05-02 13:00:00\",\"mention\":78,\"positiveScore\":0,\"negativeScore\":-0.9992012987012988,\"positiveMention\":0,\"negativeMention\":77,\"score\":0.9992012987012988},{\"atTime\":\"2022-05-02 12:00:00\",\"mention\":5,\"positiveScore\":0.9140309,\"negativeScore\":-0.9911616666666667,\"positiveMention\":2,\"negativeMention\":3,\"score\":0.9603093600000001},{\"atTime\":\"2022-05-02 11:00:00\",\"mention\":1,\"positiveScore\":0.98271203,\"negativeScore\":0,\"positiveMention\":1,\"negativeMention\":0,\"score\":0.98271203},{\"atTime\":\"2022-05-02 10:00:00\",\"mention\":5,\"positiveScore\":0,\"negativeScore\":-0.9398296,\"positiveMention\":0,\"negativeMention\":4,\"score\":0.9398296},{\"atTime\":\"2022-05-02 09:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":-0.99998593,\"positiveMention\":0,\"negativeMention\":1,\"score\":0.99998593},{\"atTime\":\"2022-05-02 08:00:00\",\"mention\":5,\"positiveScore\":0,\"negativeScore\":-0.9943382,\"positiveMention\":0,\"negativeMention\":5,\"score\":0.9943382},{\"atTime\":\"2022-05-02 07:00:00\",\"mention\":14,\"positiveScore\":0.9677273333333334,\"negativeScore\":-0.9991192727272726,\"positiveMention\":3,\"negativeMention\":11,\"score\":0.9923924285714286},{\"atTime\":\"2022-05-02 06:00:00\",\"mention\":3,\"positiveScore\":0.8219736,\"negativeScore\":-0.9995626,\"positiveMention\":1,\"negativeMention\":2,\"score\":0.9403662666666666},{\"atTime\":\"2022-05-02 05:00:00\",\"mention\":4,\"positiveScore\":0,\"negativeScore\":-0.999637,\"positiveMention\":0,\"negativeMention\":4,\"score\":0.999637},{\"atTime\":\"2022-05-02 04:00:00\",\"mention\":6,\"positiveScore\":0.99363905,\"negativeScore\":-0.967728,\"positiveMention\":2,\"negativeMention\":4,\"score\":0.9763650166666666},{\"atTime\":\"2022-05-02 03:00:00\",\"mention\":2,\"positiveScore\":0,\"negativeScore\":-0.99913645,\"positiveMention\":0,\"negativeMention\":2,\"score\":0.99913645},{\"atTime\":\"2022-05-02 02:00:00\",\"mention\":7,\"positiveScore\":0,\"negativeScore\":-0.9970097571428571,\"positiveMention\":0,\"negativeMention\":7,\"score\":0.9970097571428571},{\"atTime\":\"2022-05-02 01:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":-0.8119641,\"positiveMention\":0,\"negativeMention\":1,\"score\":0.8119641},{\"atTime\":\"2022-05-02 00:00:00\",\"mention\":7,\"positiveScore\":0.87389684,\"negativeScore\":-0.9637316,\"positiveMention\":1,\"negativeMention\":5,\"score\":0.94875914},{\"atTime\":\"2022-05-01 23:00:00\",\"mention\":18,\"positiveScore\":0.921871125,\"negativeScore\":-0.9847520000000001,\"positiveMention\":4,\"negativeMention\":13,\"score\":0.9699565},{\"atTime\":\"2022-05-01 22:00:00\",\"mention\":16,\"positiveScore\":0.9703096714285715,\"negativeScore\":-0.9909491,\"positiveMention\":7,\"negativeMention\":8,\"score\":0.9813173666666667},{\"atTime\":\"2022-05-01 21:00:00\",\"mention\":8,\"positiveScore\":0.9581311,\"negativeScore\":-0.9070516599999999,\"positiveMention\":1,\"negativeMention\":5,\"score\":0.9155649},{\"atTime\":\"2022-05-01 20:00:00\",\"mention\":12,\"positiveScore\":0.92966497,\"negativeScore\":-0.928681125,\"positiveMention\":1,\"negativeMention\":8,\"score\":0.928790441111111},{\"atTime\":\"2022-05-01 19:00:00\",\"mention\":13,\"positiveScore\":0.8168926,\"negativeScore\":-0.9270585,\"positiveMention\":1,\"negativeMention\":10,\"score\":0.9170434181818181},{\"atTime\":\"2022-05-01 18:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":-0.9980386,\"positiveMention\":0,\"negativeMention\":1,\"score\":0.9980386},{\"atTime\":\"2022-05-01 17:00:00\",\"mention\":18,\"positiveScore\":0.9022916166666667,\"negativeScore\":-0.9523071818181819,\"positiveMention\":6,\"negativeMention\":11,\"score\":0.9346546294117647},{\"atTime\":\"2022-05-01 16:00:00\",\"mention\":12,\"positiveScore\":0.99788465,\"negativeScore\":-0.942537375,\"positiveMention\":2,\"negativeMention\":8,\"score\":0.95360683},{\"atTime\":\"2022-05-01 15:00:00\",\"mention\":12,\"positiveScore\":0.9765308,\"negativeScore\":-0.9500292,\"positiveMention\":4,\"negativeMention\":8,\"score\":0.9588630666666668},{\"atTime\":\"2022-05-01 14:00:00\",\"mention\":4,\"positiveScore\":0,\"negativeScore\":-0.999556125,\"positiveMention\":0,\"negativeMention\":4,\"score\":0.999556125},{\"atTime\":\"2022-05-01 13:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":0,\"positiveMention\":0,\"negativeMention\":0,\"score\":0},{\"atTime\":\"2022-05-01 12:00:00\",\"mention\":9,\"positiveScore\":0.9832338333333334,\"negativeScore\":-0.9756753333333333,\"positiveMention\":3,\"negativeMention\":6,\"score\":0.9781948333333335},{\"atTime\":\"2022-05-01 11:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":-0.99975556,\"positiveMention\":0,\"negativeMention\":1,\"score\":0.99975556},{\"atTime\":\"2022-05-01 10:00:00\",\"mention\":6,\"positiveScore\":0.9812718,\"negativeScore\":-0.94712365,\"positiveMention\":1,\"negativeMention\":2,\"score\":0.9585063666666667},{\"atTime\":\"2022-05-01 09:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":0,\"positiveMention\":0,\"negativeMention\":0,\"score\":0},{\"atTime\":\"2022-05-01 08:00:00\",\"mention\":15,\"positiveScore\":0.9898682,\"negativeScore\":-0.971978,\"positiveMention\":2,\"negativeMention\":9,\"score\":0.9752307636363636},{\"atTime\":\"2022-05-01 07:00:00\",\"mention\":9,\"positiveScore\":0.9962305,\"negativeScore\":-0.99951815,\"positiveMention\":4,\"negativeMention\":4,\"score\":0.997874325},{\"atTime\":\"2022-05-01 06:00:00\",\"mention\":7,\"positiveScore\":0.98950195,\"negativeScore\":-0.9890079999999999,\"positiveMention\":2,\"negativeMention\":5,\"score\":0.9891491285714284},{\"atTime\":\"2022-05-01 05:00:00\",\"mention\":4,\"positiveScore\":0,\"negativeScore\":-0.95477475,\"positiveMention\":0,\"negativeMention\":4,\"score\":0.95477475},{\"atTime\":\"2022-05-01 04:00:00\",\"mention\":16,\"positiveScore\":0.9768456000000001,\"negativeScore\":-0.9985768833333334,\"positiveMention\":10,\"negativeMention\":6,\"score\":0.9849948312500001},{\"atTime\":\"2022-05-01 03:00:00\",\"mention\":10,\"positiveScore\":0.9726566,\"negativeScore\":-0.9853301999999999,\"positiveMention\":2,\"negativeMention\":5,\"score\":0.9817091714285714},{\"atTime\":\"2022-05-01 02:00:00\",\"mention\":5,\"positiveScore\":0.9993668,\"negativeScore\":-0.96887905,\"positiveMention\":1,\"negativeMention\":4,\"score\":0.9749766},{\"atTime\":\"2022-05-01 01:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":-0.99931467,\"positiveMention\":0,\"negativeMention\":1,\"score\":0.99931467},{\"atTime\":\"2022-05-01 00:00:00\",\"mention\":21,\"positiveScore\":0.91432395,\"negativeScore\":-0.9729486842105264,\"positiveMention\":2,\"negativeMention\":19,\"score\":0.9673653761904762},{\"atTime\":\"2022-04-30 23:00:00\",\"mention\":11,\"positiveScore\":0.9910781,\"negativeScore\":-0.9975666999999999,\"positiveMention\":1,\"negativeMention\":10,\"score\":0.9969768272727272},{\"atTime\":\"2022-04-30 22:00:00\",\"mention\":2,\"positiveScore\":0,\"negativeScore\":-0.99485675,\"positiveMention\":0,\"negativeMention\":2,\"score\":0.99485675},{\"atTime\":\"2022-04-30 21:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":-0.999956,\"positiveMention\":0,\"negativeMention\":1,\"score\":0.999956},{\"atTime\":\"2022-04-30 20:00:00\",\"mention\":3,\"positiveScore\":0,\"negativeScore\":-0.9705385999999999,\"positiveMention\":0,\"negativeMention\":3,\"score\":0.9705385999999999},{\"atTime\":\"2022-04-30 19:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":-0.99999833,\"positiveMention\":0,\"negativeMention\":1,\"score\":0.99999833},{\"atTime\":\"2022-04-30 18:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":-0.9999819,\"positiveMention\":0,\"negativeMention\":1,\"score\":0.9999819},{\"atTime\":\"2022-04-30 17:00:00\",\"mention\":2,\"positiveScore\":0,\"negativeScore\":-0.9995514,\"positiveMention\":0,\"negativeMention\":2,\"score\":0.9995514},{\"atTime\":\"2022-04-30 16:00:00\",\"mention\":4,\"positiveScore\":0.9958806,\"negativeScore\":-0.9999378666666666,\"positiveMention\":1,\"negativeMention\":3,\"score\":0.99892355},{\"atTime\":\"2022-04-30 15:00:00\",\"mention\":6,\"positiveScore\":0,\"negativeScore\":-0.9271265,\"positiveMention\":0,\"negativeMention\":6,\"score\":0.9271265},{\"atTime\":\"2022-04-30 14:00:00\",\"mention\":9,\"positiveScore\":0.9127782,\"negativeScore\":-0.98718566,\"positiveMention\":2,\"negativeMention\":5,\"score\":0.9659263857142857},{\"atTime\":\"2022-04-30 13:00:00\",\"mention\":8,\"positiveScore\":0.9520579333333333,\"negativeScore\":-0.9997892,\"positiveMention\":3,\"negativeMention\":5,\"score\":0.9818899750000001},{\"atTime\":\"2022-04-30 12:00:00\",\"mention\":6,\"positiveScore\":0.9993405,\"negativeScore\":-0.978274,\"positiveMention\":1,\"negativeMention\":4,\"score\":0.9824873000000001},{\"atTime\":\"2022-04-30 11:00:00\",\"mention\":1,\"positiveScore\":0,\"negativeScore\":-0.70746356,\"positiveMention\":0,\"negativeMention\":1,\"score\":0.70746356},{\"atTime\":\"2022-04-30 10:00:00\",\"mention\":2,\"positiveScore\":0,\"negativeScore\":-0.96923805,\"positiveMention\":0,\"negativeMention\":2,\"score\":0.96923805},{\"atTime\":\"2022-04-30 09:00:00\",\"mention\":4,\"positiveScore\":0.94531285,\"negativeScore\":-0.99995685,\"positiveMention\":2,\"negativeMention\":2,\"score\":0.9726348499999999},{\"atTime\":\"2022-04-30 08:00:00\",\"mention\":2,\"positiveScore\":0,\"negativeScore\":-0.99993395,\"positiveMention\":0,\"negativeMention\":2,\"score\":0.99993395},{\"atTime\":\"2022-04-30 07:00:00\",\"mention\":7,\"positiveScore\":0.87372855,\"negativeScore\":-0.9877326,\"positiveMention\":2,\"negativeMention\":4,\"score\":0.94973125},{\"atTime\":\"2022-04-30 06:00:00\",\"mention\":8,\"positiveScore\":0,\"negativeScore\":-0.9616050714285714,\"positiveMention\":0,\"negativeMention\":7,\"score\":0.9616050714285714},{\"atTime\":\"2022-04-30 05:00:00\",\"mention\":4,\"positiveScore\":0,\"negativeScore\":-0.9997504666666667,\"positiveMention\":0,\"negativeMention\":3,\"score\":0.9997504666666667},{\"atTime\":\"2022-04-30 04:00:00\",\"mention\":1,\"positiveScore\":0.9990785,\"negativeScore\":0,\"positiveMention\":1,\"negativeMention\":0,\"score\":0.9990785}],\"symbol\":\"TSLA\",\"twitter\":[{\"atTime\":\"2022-05-04 07:00:00\",\"mention\":149,\"positiveScore\":0.9227935416666666,\"negativeScore\":-0.9567427604166667,\"positiveMention\":48,\"negativeMention\":96,\"score\":0.9454263541666665},{\"atTime\":\"2022-05-04 06:00:00\",\"mention\":160,\"positiveScore\":0.9030987857142857,\"negativeScore\":-0.9623162068965516,\"positiveMention\":56,\"negativeMention\":87,\"score\":0.9391261678321678},{\"atTime\":\"2022-05-04 05:00:00\",\"mention\":164,\"positiveScore\":0.8692743965517241,\"negativeScore\":-0.9373459183673469,\"positiveMention\":58,\"negativeMention\":98,\"score\":0.9120372756410257},{\"atTime\":\"2022-05-04 04:00:00\",\"mention\":139,\"positiveScore\":0.8073790425531915,\"negativeScore\":-0.9530134146341462,\"positiveMention\":47,\"negativeMention\":82,\"score\":0.8999528294573643},{\"atTime\":\"2022-05-04 03:00:00\",\"mention\":213,\"positiveScore\":0.7793068659793814,\"negativeScore\":-0.9491731132075473,\"positiveMention\":97,\"negativeMention\":106,\"score\":0.8680054975369458},{\"atTime\":\"2022-05-04 02:00:00\",\"mention\":251,\"positiveScore\":0.871874568627451,\"negativeScore\":-0.9567666911764706,\"positiveMention\":102,\"negativeMention\":136,\"score\":0.9203843529411765},{\"atTime\":\"2022-05-04 01:00:00\",\"mention\":314,\"positiveScore\":0.8507805882352941,\"negativeScore\":-0.9640401408450705,\"positiveMention\":85,\"negativeMention\":213,\"score\":0.9317345637583893},{\"atTime\":\"2022-05-04 00:00:00\",\"mention\":201,\"positiveScore\":0.828764304347826,\"negativeScore\":-0.9524208450704226,\"positiveMention\":46,\"negativeMention\":142,\"score\":0.9221644574468085},{\"atTime\":\"2022-05-03 23:00:00\",\"mention\":395,\"positiveScore\":0.8475126153846154,\"negativeScore\":-0.9069878102189781,\"positiveMention\":65,\"negativeMention\":274,\"score\":0.89558401179941},{\"atTime\":\"2022-05-03 22:00:00\",\"mention\":200,\"positiveScore\":0.8638644444444444,\"negativeScore\":-0.9474540157480316,\"positiveMention\":63,\"negativeMention\":127,\"score\":0.9197374736842105},{\"atTime\":\"2022-05-03 21:00:00\",\"mention\":294,\"positiveScore\":0.8664658139534884,\"negativeScore\":-0.9697950000000001,\"positiveMention\":129,\"negativeMention\":154,\"score\":0.9226944169611307},{\"atTime\":\"2022-05-03 20:00:00\",\"mention\":330,\"positiveScore\":0.9262536666666666,\"negativeScore\":-0.9484630487804879,\"positiveMention\":150,\"negativeMention\":164,\"score\":0.9378534713375797},{\"atTime\":\"2022-05-03 19:00:00\",\"mention\":405,\"positiveScore\":0.8878557142857143,\"negativeScore\":-0.9385793478260871,\"positiveMention\":105,\"negativeMention\":276,\"score\":0.9246003937007875},{\"atTime\":\"2022-05-03 18:00:00\",\"mention\":403,\"positiveScore\":0.8661105263157894,\"negativeScore\":-0.9387121074380165,\"positiveMention\":133,\"negativeMention\":242,\"score\":0.9129627466666668},{\"atTime\":\"2022-05-03 17:00:00\",\"mention\":442,\"positiveScore\":0.8937712030075189,\"negativeScore\":-0.9213798484848486,\"positiveMention\":133,\"negativeMention\":264,\"score\":0.9121306045340051},{\"atTime\":\"2022-05-03 16:00:00\",\"mention\":436,\"positiveScore\":0.8622127272727271,\"negativeScore\":-0.9447826568265681,\"positiveMention\":154,\"negativeMention\":271,\"score\":0.9148631999999999},{\"atTime\":\"2022-05-03 15:00:00\",\"mention\":524,\"positiveScore\":0.9012224460431655,\"negativeScore\":-0.9457130985915494,\"positiveMention\":139,\"negativeMention\":355,\"score\":0.9331944736842106},{\"atTime\":\"2022-05-03 14:00:00\",\"mention\":695,\"positiveScore\":0.8850833333333333,\"negativeScore\":-0.91336330472103,\"positiveMention\":180,\"negativeMention\":466,\"score\":0.9054834365325076},{\"atTime\":\"2022-05-03 13:00:00\",\"mention\":675,\"positiveScore\":0.9200211483253589,\"negativeScore\":-0.9640886621315192,\"positiveMention\":209,\"negativeMention\":441,\"score\":0.9499192615384614},{\"atTime\":\"2022-05-03 12:00:00\",\"mention\":425,\"positiveScore\":0.8703154945054946,\"negativeScore\":-0.971342075471698,\"positiveMention\":91,\"negativeMention\":318,\"score\":0.9488642787286063},{\"atTime\":\"2022-05-03 11:00:00\",\"mention\":309,\"positiveScore\":0.9480207580645161,\"negativeScore\":-0.979160593220339,\"positiveMention\":62,\"negativeMention\":236,\"score\":0.9726818355704698},{\"atTime\":\"2022-05-03 10:00:00\",\"mention\":178,\"positiveScore\":0.9390266842105263,\"negativeScore\":-0.9679864925373135,\"positiveMention\":38,\"negativeMention\":134,\"score\":0.9615883953488373},{\"atTime\":\"2022-05-03 09:00:00\",\"mention\":152,\"positiveScore\":0.9195306969696969,\"negativeScore\":-0.9626767391304348,\"positiveMention\":33,\"negativeMention\":115,\"score\":0.9530563378378378},{\"atTime\":\"2022-05-03 08:00:00\",\"mention\":188,\"positiveScore\":0.9163763725490196,\"negativeScore\":-0.9687069924812031,\"positiveMention\":51,\"negativeMention\":133,\"score\":0.9542023097826088},{\"atTime\":\"2022-05-03 07:00:00\",\"mention\":127,\"positiveScore\":0.9248745454545454,\"negativeScore\":-0.9704665384615384,\"positiveMention\":44,\"negativeMention\":78,\"score\":0.9540235245901638},{\"atTime\":\"2022-05-03 06:00:00\",\"mention\":140,\"positiveScore\":0.9398513513513514,\"negativeScore\":-0.9768704123711339,\"positiveMention\":37,\"negativeMention\":97,\"score\":0.9666487313432837},{\"atTime\":\"2022-05-03 05:00:00\",\"mention\":215,\"positiveScore\":0.9239647058823529,\"negativeScore\":-0.9721818300653595,\"positiveMention\":51,\"negativeMention\":153,\"score\":0.9601275490196078},{\"atTime\":\"2022-05-03 04:00:00\",\"mention\":201,\"positiveScore\":0.9078826666666667,\"negativeScore\":-0.9765308724832215,\"positiveMention\":45,\"negativeMention\":149,\"score\":0.9606073195876289},{\"atTime\":\"2022-05-03 03:00:00\",\"mention\":203,\"positiveScore\":0.8552538484848484,\"negativeScore\":-0.954200265625,\"positiveMention\":66,\"negativeMention\":128,\"score\":0.9205380824742267},{\"atTime\":\"2022-05-03 02:00:00\",\"mention\":221,\"positiveScore\":0.9298520212765958,\"negativeScore\":-0.9436410778443115,\"positiveMention\":47,\"negativeMention\":167,\"score\":0.9406126401869159},{\"atTime\":\"2022-05-03 01:00:00\",\"mention\":270,\"positiveScore\":0.9390243037974683,\"negativeScore\":-0.9412973446327684,\"positiveMention\":79,\"negativeMention\":177,\"score\":0.9405958984375},{\"atTime\":\"2022-05-03 00:00:00\",\"mention\":222,\"positiveScore\":0.9232094705882353,\"negativeScore\":-0.9710327607361963,\"positiveMention\":51,\"negativeMention\":163,\"score\":0.9596356214953271},{\"atTime\":\"2022-05-02 23:00:00\",\"mention\":346,\"positiveScore\":0.9237098749999999,\"negativeScore\":-0.9671498393574297,\"positiveMention\":80,\"negativeMention\":249,\"score\":0.9565869300911855},{\"atTime\":\"2022-05-02 22:00:00\",\"mention\":289,\"positiveScore\":0.9157260215053763,\"negativeScore\":-0.9671655248618785,\"positiveMention\":93,\"negativeMention\":181,\"score\":0.9497061313868612},{\"atTime\":\"2022-05-02 21:00:00\",\"mention\":438,\"positiveScore\":0.9321004895104895,\"negativeScore\":-0.9673670422535211,\"positiveMention\":143,\"negativeMention\":284,\"score\":0.9555564637002342},{\"atTime\":\"2022-05-02 20:00:00\",\"mention\":578,\"positiveScore\":0.9013642696629213,\"negativeScore\":-0.9557689839572193,\"positiveMention\":178,\"negativeMention\":374,\"score\":0.9382254347826087},{\"atTime\":\"2022-05-02 19:00:00\",\"mention\":612,\"positiveScore\":0.9125224675324675,\"negativeScore\":-0.9655131753554502,\"positiveMention\":154,\"negativeMention\":422,\"score\":0.9513455208333332},{\"atTime\":\"2022-05-02 18:00:00\",\"mention\":529,\"positiveScore\":0.9010895890410958,\"negativeScore\":-0.9618421229050279,\"positiveMention\":146,\"negativeMention\":358,\"score\":0.9442431746031745},{\"atTime\":\"2022-05-02 17:00:00\",\"mention\":412,\"positiveScore\":0.8749431188118811,\"negativeScore\":-0.9513279661016949,\"positiveMention\":101,\"negativeMention\":295,\"score\":0.9318459722222222},{\"atTime\":\"2022-05-02 16:00:00\",\"mention\":534,\"positiveScore\":0.9157281981981983,\"negativeScore\":-0.946176322418136,\"positiveMention\":111,\"negativeMention\":397,\"score\":0.9395232874015748},{\"atTime\":\"2022-05-02 15:00:00\",\"mention\":551,\"positiveScore\":0.9238604237288135,\"negativeScore\":-0.9453792909535452,\"positiveMention\":118,\"negativeMention\":409,\"score\":0.9405610246679317},{\"atTime\":\"2022-05-02 14:00:00\",\"mention\":498,\"positiveScore\":0.8870303539823009,\"negativeScore\":-0.9382470903954803,\"positiveMention\":113,\"negativeMention\":354,\"score\":0.9258541755888652},{\"atTime\":\"2022-05-02 13:00:00\",\"mention\":575,\"positiveScore\":0.9021561654135339,\"negativeScore\":-0.95125,\"positiveMention\":133,\"negativeMention\":418,\"score\":0.9393997640653359},{\"atTime\":\"2022-05-02 12:00:00\",\"mention\":434,\"positiveScore\":0.8820537640449438,\"negativeScore\":-0.9612461349693252,\"positiveMention\":89,\"negativeMention\":326,\"score\":0.9442627108433735},{\"atTime\":\"2022-05-02 11:00:00\",\"mention\":332,\"positiveScore\":0.8803222173913043,\"negativeScore\":-0.961690406504065,\"positiveMention\":69,\"negativeMention\":246,\"score\":0.9438668984126986},{\"atTime\":\"2022-05-02 10:00:00\",\"mention\":199,\"positiveScore\":0.9069694705882353,\"negativeScore\":-0.9568438271604939,\"positiveMention\":34,\"negativeMention\":162,\"score\":0.9481921530612245},{\"atTime\":\"2022-05-02 09:00:00\",\"mention\":134,\"positiveScore\":0.881002303030303,\"negativeScore\":-0.9667830612244898,\"positiveMention\":33,\"negativeMention\":98,\"score\":0.9451741679389313},{\"atTime\":\"2022-05-02 08:00:00\",\"mention\":151,\"positiveScore\":0.8499285000000001,\"negativeScore\":-0.9471701851851853,\"positiveMention\":40,\"negativeMention\":108,\"score\":0.9208886486486486},{\"atTime\":\"2022-05-02 07:00:00\",\"mention\":117,\"positiveScore\":0.8973031935483872,\"negativeScore\":-0.9291177777777777,\"positiveMention\":31,\"negativeMention\":81,\"score\":0.9203119553571428},{\"atTime\":\"2022-05-02 06:00:00\",\"mention\":105,\"positiveScore\":0.892816125,\"negativeScore\":-0.9270190307692308,\"positiveMention\":32,\"negativeMention\":65,\"score\":0.9157355979381443},{\"atTime\":\"2022-05-02 05:00:00\",\"mention\":162,\"positiveScore\":0.8818108461538461,\"negativeScore\":-0.9181418811881189,\"positiveMention\":52,\"negativeMention\":101,\"score\":0.9057940784313726},{\"atTime\":\"2022-05-02 04:00:00\",\"mention\":189,\"positiveScore\":0.8814232835820895,\"negativeScore\":-0.9258842500000001,\"positiveMention\":67,\"negativeMention\":112,\"score\":0.9092424357541901},{\"atTime\":\"2022-05-02 03:00:00\",\"mention\":169,\"positiveScore\":0.8942196458333332,\"negativeScore\":-0.9508661467889907,\"positiveMention\":48,\"negativeMention\":109,\"score\":0.9335474713375795},{\"atTime\":\"2022-05-02 02:00:00\",\"mention\":211,\"positiveScore\":0.8953353571428571,\"negativeScore\":-0.9362185616438355,\"positiveMention\":56,\"negativeMention\":146,\"score\":0.924884603960396},{\"atTime\":\"2022-05-02 01:00:00\",\"mention\":209,\"positiveScore\":0.8783059999999999,\"negativeScore\":-0.9090467333333333,\"positiveMention\":51,\"negativeMention\":150,\"score\":0.9012468457711443},{\"atTime\":\"2022-05-02 00:00:00\",\"mention\":190,\"positiveScore\":0.8741976458333333,\"negativeScore\":-0.9472567910447761,\"positiveMention\":48,\"negativeMention\":134,\"score\":0.9279884450549452},{\"atTime\":\"2022-05-01 23:00:00\",\"mention\":235,\"positiveScore\":0.8878825846153846,\"negativeScore\":-0.9502922666666666,\"positiveMention\":65,\"negativeMention\":150,\"score\":0.9314242232558139},{\"atTime\":\"2022-05-01 22:00:00\",\"mention\":200,\"positiveScore\":0.847752962962963,\"negativeScore\":-0.9454730935251798,\"positiveMention\":54,\"negativeMention\":139,\"score\":0.9181317098445596},{\"atTime\":\"2022-05-01 21:00:00\",\"mention\":273,\"positiveScore\":0.8739790322580645,\"negativeScore\":-0.9617955757575758,\"positiveMention\":93,\"negativeMention\":165,\"score\":0.9301407751937985},{\"atTime\":\"2022-05-01 20:00:00\",\"mention\":185,\"positiveScore\":0.8887127222222222,\"negativeScore\":-0.9444683177570093,\"positiveMention\":72,\"negativeMention\":107,\"score\":0.9220414860335195},{\"atTime\":\"2022-05-01 19:00:00\",\"mention\":145,\"positiveScore\":0.9358722549019607,\"negativeScore\":-0.9728124096385543,\"positiveMention\":51,\"negativeMention\":83,\"score\":0.9587530970149254},{\"atTime\":\"2022-05-01 18:00:00\",\"mention\":205,\"positiveScore\":0.9238429729729729,\"negativeScore\":-0.9306330701754386,\"positiveMention\":74,\"negativeMention\":114,\"score\":0.9279603723404255},{\"atTime\":\"2022-05-01 17:00:00\",\"mention\":196,\"positiveScore\":0.8837673725490196,\"negativeScore\":-0.9599162962962963,\"positiveMention\":51,\"negativeMention\":135,\"score\":0.939036752688172},{\"atTime\":\"2022-05-01 16:00:00\",\"mention\":203,\"positiveScore\":0.9097969696969697,\"negativeScore\":-0.9340561417322835,\"positiveMention\":66,\"negativeMention\":127,\"score\":0.9257602590673575},{\"atTime\":\"2022-05-01 15:00:00\",\"mention\":345,\"positiveScore\":0.9150327338129496,\"negativeScore\":-0.9062501020408164,\"positiveMention\":139,\"negativeMention\":196,\"score\":0.9098942388059702},{\"atTime\":\"2022-05-01 14:00:00\",\"mention\":332,\"positiveScore\":0.915078,\"negativeScore\":-0.9449197202797204,\"positiveMention\":160,\"negativeMention\":143,\"score\":0.9291617161716171},{\"atTime\":\"2022-05-01 13:00:00\",\"mention\":170,\"positiveScore\":0.8867334117647059,\"negativeScore\":-0.950431037735849,\"positiveMention\":51,\"negativeMention\":106,\"score\":0.9297394522292992},{\"atTime\":\"2022-05-01 12:00:00\",\"mention\":208,\"positiveScore\":0.9245926274509804,\"negativeScore\":-0.952525724137931,\"positiveMention\":51,\"negativeMention\":145,\"score\":0.9452574183673469},{\"atTime\":\"2022-05-01 11:00:00\",\"mention\":72,\"positiveScore\":0.9192817083333332,\"negativeScore\":-0.972231590909091,\"positiveMention\":24,\"negativeMention\":44,\"score\":0.9535433970588236},{\"atTime\":\"2022-05-01 10:00:00\",\"mention\":85,\"positiveScore\":0.9007913999999999,\"negativeScore\":-0.9438929310344828,\"positiveMention\":25,\"negativeMention\":58,\"score\":0.9309105421686746},{\"atTime\":\"2022-05-01 09:00:00\",\"mention\":104,\"positiveScore\":0.9247661764705882,\"negativeScore\":-0.963438870967742,\"positiveMention\":34,\"negativeMention\":62,\"score\":0.9497422916666666},{\"atTime\":\"2022-05-01 08:00:00\",\"mention\":111,\"positiveScore\":0.830366806451613,\"negativeScore\":-0.972582972972973,\"positiveMention\":31,\"negativeMention\":74,\"score\":0.930595342857143},{\"atTime\":\"2022-05-01 07:00:00\",\"mention\":115,\"positiveScore\":0.8490392666666666,\"negativeScore\":-0.9711198765432099,\"positiveMention\":30,\"negativeMention\":81,\"score\":0.938125117117117},{\"atTime\":\"2022-05-01 06:00:00\",\"mention\":125,\"positiveScore\":0.961893469387755,\"negativeScore\":-0.971891640625,\"positiveMention\":49,\"negativeMention\":64,\"score\":0.9675561504424779},{\"atTime\":\"2022-05-01 05:00:00\",\"mention\":65,\"positiveScore\":0.9685433999999999,\"negativeScore\":-0.9630407142857142,\"positiveMention\":15,\"negativeMention\":49,\"score\":0.96433040625},{\"atTime\":\"2022-05-01 04:00:00\",\"mention\":96,\"positiveScore\":0.9579166842105264,\"negativeScore\":-0.9556105714285715,\"positiveMention\":19,\"negativeMention\":70,\"score\":0.9561028876404495},{\"atTime\":\"2022-05-01 03:00:00\",\"mention\":57,\"positiveScore\":0.9483114545454545,\"negativeScore\":-0.9467877272727272,\"positiveMention\":11,\"negativeMention\":44,\"score\":0.9470924727272727},{\"atTime\":\"2022-05-01 02:00:00\",\"mention\":120,\"positiveScore\":0.96144288,\"negativeScore\":-0.9648254945054945,\"positiveMention\":25,\"negativeMention\":91,\"score\":0.9640964827586207},{\"atTime\":\"2022-05-01 01:00:00\",\"mention\":113,\"positiveScore\":0.9419765897435898,\"negativeScore\":-0.950320588235294,\"positiveMention\":39,\"negativeMention\":68,\"score\":0.9472793177570094},{\"atTime\":\"2022-05-01 00:00:00\",\"mention\":143,\"positiveScore\":0.940374742857143,\"negativeScore\":-0.9466814150943397,\"positiveMention\":35,\"negativeMention\":106,\"score\":0.9451159290780142},{\"atTime\":\"2022-04-30 23:00:00\",\"mention\":183,\"positiveScore\":0.9506534285714285,\"negativeScore\":-0.9530953623188406,\"positiveMention\":35,\"negativeMention\":138,\"score\":0.9526013294797687},{\"atTime\":\"2022-04-30 22:00:00\",\"mention\":138,\"positiveScore\":0.90527696,\"negativeScore\":-0.9644355660377358,\"positiveMention\":25,\"negativeMention\":106,\"score\":0.9531457557251908},{\"atTime\":\"2022-04-30 21:00:00\",\"mention\":120,\"positiveScore\":0.8838291111111112,\"negativeScore\":-0.9816110204081633,\"positiveMention\":18,\"negativeMention\":98,\"score\":0.9664379655172414},{\"atTime\":\"2022-04-30 20:00:00\",\"mention\":185,\"positiveScore\":0.9287200697674419,\"negativeScore\":-0.9745349242424243,\"positiveMention\":43,\"negativeMention\":132,\"score\":0.9632775600000001},{\"atTime\":\"2022-04-30 19:00:00\",\"mention\":199,\"positiveScore\":0.8905688076923077,\"negativeScore\":-0.9598865714285714,\"positiveMention\":52,\"negativeMention\":140,\"score\":0.9411130104166666},{\"atTime\":\"2022-04-30 18:00:00\",\"mention\":189,\"positiveScore\":0.95395432,\"negativeScore\":-0.970623046875,\"positiveMention\":50,\"negativeMention\":128,\"score\":0.965940820224719},{\"atTime\":\"2022-04-30 17:00:00\",\"mention\":182,\"positiveScore\":0.9354331578947369,\"negativeScore\":-0.9712119565217392,\"positiveMention\":38,\"negativeMention\":138,\"score\":0.9634869886363636},{\"atTime\":\"2022-04-30 16:00:00\",\"mention\":258,\"positiveScore\":0.9563025789473685,\"negativeScore\":-0.9582829015544041,\"positiveMention\":57,\"negativeMention\":193,\"score\":0.957831388},{\"atTime\":\"2022-04-30 15:00:00\",\"mention\":131,\"positiveScore\":0.9605266744186046,\"negativeScore\":-0.9647319512195122,\"positiveMention\":43,\"negativeMention\":82,\"score\":0.9632853359999999},{\"atTime\":\"2022-04-30 14:00:00\",\"mention\":141,\"positiveScore\":0.9491706585365853,\"negativeScore\":-0.9780797701149425,\"positiveMention\":41,\"negativeMention\":87,\"score\":0.9688198203125},{\"atTime\":\"2022-04-30 13:00:00\",\"mention\":135,\"positiveScore\":0.9178150416666666,\"negativeScore\":-0.9629985365853658,\"positiveMention\":48,\"negativeMention\":82,\"score\":0.9463153999999999},{\"atTime\":\"2022-04-30 12:00:00\",\"mention\":161,\"positiveScore\":0.9276721481481481,\"negativeScore\":-0.9497323232323233,\"positiveMention\":54,\"negativeMention\":99,\"score\":0.9419463790849673},{\"atTime\":\"2022-04-30 11:00:00\",\"mention\":94,\"positiveScore\":0.9429834782608696,\"negativeScore\":-0.9526205901639344,\"positiveMention\":23,\"negativeMention\":61,\"score\":0.9499818571428571},{\"atTime\":\"2022-04-30 10:00:00\",\"mention\":52,\"positiveScore\":0.97806328,\"negativeScore\":-0.97319848,\"positiveMention\":25,\"negativeMention\":25,\"score\":0.97563088},{\"atTime\":\"2022-04-30 09:00:00\",\"mention\":69,\"positiveScore\":0.9453665416666667,\"negativeScore\":-0.9677775116279069,\"positiveMention\":24,\"negativeMention\":43,\"score\":0.9597497014925374},{\"atTime\":\"2022-04-30 08:00:00\",\"mention\":94,\"positiveScore\":0.92364928,\"negativeScore\":-0.9768843809523811,\"positiveMention\":25,\"negativeMention\":63,\"score\":0.9617607727272728},{\"atTime\":\"2022-04-30 07:00:00\",\"mention\":128,\"positiveScore\":0.9337845333333333,\"negativeScore\":-0.9635351648351649,\"positiveMention\":30,\"negativeMention\":91,\"score\":0.9561589752066116},{\"atTime\":\"2022-04-30 06:00:00\",\"mention\":91,\"positiveScore\":0.9206969142857143,\"negativeScore\":-0.9716938181818182,\"positiveMention\":35,\"negativeMention\":55,\"score\":0.9518616888888889},{\"atTime\":\"2022-04-30 05:00:00\",\"mention\":75,\"positiveScore\":0.9491911,\"negativeScore\":-0.9672156923076923,\"positiveMention\":20,\"negativeMention\":52,\"score\":0.9622088611111111},{\"atTime\":\"2022-04-30 04:00:00\",\"mention\":132,\"positiveScore\":0.93205805,\"negativeScore\":-0.9620851136363636,\"positiveMention\":40,\"negativeMention\":88,\"score\":0.9527016562499999}]}");
                           JSONArray reddit = response.getJSONArray("reddit");
                           JSONArray twitter = response.getJSONArray("twitter");
                           getRecomendationChartsAPI();

                           int redditLength = reddit.length();
                           int red_TotMentions = 0;
                           int red_posMentions = 0;
                           int red_negMentions = 0;


                           for(int i=0; i< redditLength;i++){
                               red_TotMentions = red_TotMentions+reddit.getJSONObject(i).getInt("mention");
                               red_posMentions = red_posMentions+reddit.getJSONObject(i).getInt("positiveMention");
                               red_negMentions = red_negMentions+reddit.getJSONObject(i).getInt("negativeMention");
                           }
                           //ArrayList list = getRedditArray();
                           getRedditArray().add(red_TotMentions);
                           getRedditArray().add(red_posMentions);
                           getRedditArray().add(red_negMentions);
                           //setRedditArray(list);
                           //Log.e(null, "onResponse: "+red_TotMentions+ "API CALL" );
                           Log.e(null, "onResponse: "+getTwitterArray().toString()+" "+getRedditArray().toString()+" "+"REDDIT TWITTER ARRAY");

                           int twitterLength = twitter.length();
                           int twi_TotMentions = 0;
                           int twi_posMentions = 0;
                           int twi_negMentions = 0;

                           for(int i=0; i< twitterLength;i++){
                               twi_TotMentions = twi_TotMentions+twitter.getJSONObject(i).getInt("mention");
                               twi_posMentions = twi_posMentions+twitter.getJSONObject(i).getInt("positiveMention");
                               twi_negMentions = twi_negMentions+twitter.getJSONObject(i).getInt("negativeMention");
                           }
                           //ArrayList list1 = getTwitterArray();
                           getTwitterArray().add(twi_TotMentions);
                           getTwitterArray().add(twi_posMentions);
                           getTwitterArray().add(twi_negMentions);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        Log.e(null, "onErrorResponse: " +error.toString()+"getSocialSentimentsAPI");
                    }
                });

        // Access the RequestQueue through your singleton class.
        APIFetch.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    //API CALL TO GET RECOMMENDATION CHART DATA
    private void getRecomendationChartsAPI(){
        String stockPrices_url = BASE_URL+"getRecommendationChartData/"+this.tickerVal;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, stockPrices_url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        setRecommendationChartJSON(response);
                        //Log.e(null, "onResponse: "+response.toString()+"RECIEVED DATA");
                        //Log.e(null, "onResponse: "+getRecommendationChartJSON().toString()+"GOT DATA");
                        getNewsAPI();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                       /* try {
                            JSONObject temp = new JSONObject("{\"data\":[{\"buy\":16,\"hold\":10,\"period\":\"2022-05-01\",\"sell\":6,\"strongBuy\":14,\"strongSell\":4,\"symbol\":\"TSLA\"},{\"buy\":14,\"hold\":10,\"period\":\"2022-04-01\",\"sell\":7,\"strongBuy\":14,\"strongSell\":4,\"symbol\":\"TSLA\"},{\"buy\":13,\"hold\":11,\"period\":\"2022-03-01\",\"sell\":7,\"strongBuy\":14,\"strongSell\":4,\"symbol\":\"TSLA\"},{\"buy\":11,\"hold\":13,\"period\":\"2022-02-01\",\"sell\":7,\"strongBuy\":13,\"strongSell\":4,\"symbol\":\"TSLA\"}]}");
                            setRecommendationChartJSON(temp);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }*/

                        Log.e(null, "onErrorResponse: " +error.toString()+"getRecomendationChartsAPI");
                    }
                });

        // Access the RequestQueue through your singleton class.
        APIFetch.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    //API CALL TO GET NEWS DATA
    private void getNewsAPI(){
        String stockPrices_url = BASE_URL+"getNewsData/"+this.tickerVal;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, stockPrices_url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        setRecommendationChartJSON(response);
                        setNewsTabJSON(response);
                        //Log.e(null, "onResponse: "+getNewsTabJSON().toString()+"DATA SET");
                        //getNewModel().add(new NewsModel(response.))
                        try {
                            JSONArray newsData = response.getJSONArray("data");
                            //Log.e(null, "onResponse: "+"STAR"+newsData.get(1));
                            for(int i = 0; i < 19; i++){
                                try {
                                    JSONObject newsItem = (JSONObject) newsData.get(i);
                                    String source = newsItem.getString("source");
                                    String hoursElapsed = String.valueOf(newsItem.getInt("datetime"))+" "+"hours ago";
                                    String newsTitle = newsItem.getString("headline");
                                    String Imgsrc = newsItem.getString("image");
                                    String dateStr = newsItem.getString("datestr");
                                    String content = newsItem.getString("summary");
                                    String newsLink = newsItem.getString("url");
                                    if(Imgsrc.isEmpty()){
                                        Imgsrc = "https://s.yimg.com/uu/api/res/1.2/XNG71yKJYrzLvnAl8r6DBw--~B/aD0xNzQ7dz03NTY7YXBwaWQ9eXRhY2h5b24-/https://media.zenfs.com/en/investorplace_417/0f71a36052f2cd3722800e79bc9bfee0";
                                    }
                                    //Log.e(null, "onResponse: "+"IMAGE STRING"+Imgsrc);
                                    getNewModel().add(new NewsModel(source,hoursElapsed,newsTitle,Imgsrc,dateStr,content,newsLink));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.e(null, "onResponse: "+"NEWS SECTION ERROR");
                                }
                                //Log.e(null, "onResponse: "+"GET SIZE OF ARRAY"+ getNewModel().size());
                            }

                        } catch (JSONException e) {
                            Log.e(null, "onResponse: "+"NEWS DATA ERROR");
                            e.printStackTrace();
                        }
                        getHistoryChartsData();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e(null, "onErrorResponse: " +error.toString()+"getNewsAPI");
                    }
                });

        // Access the RequestQueue through your singleton class.
        APIFetch.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void getHistoryChartsData(){
        String stockPrices_url = BASE_URL+"getHistoricData/"+this.tickerVal;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, stockPrices_url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //Log.e(null, "onResponse: "+response.toString());
                        setHistoricChartsData(response);
                        DisplayData();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e(null, "onErrorResponse: " +error.toString()+"getHistoricChartData");
                    }
                });

        // Access the RequestQueue through your singleton class.
        APIFetch.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }


    public void DisplayData(){
        //Log.e(null, "DisplayData: ACTIVATED CHECK");
        //image
        tickerLogo = findViewById(R.id.logoImg);
        Picasso.get()
                .load(this.imageURL)
                .into(tickerLogo);

       //ticker
        ticker = findViewById(R.id.ticker);
        ticker.setText(this.comapnyName);
        //Log.e(null, "DisplayData: COMPANY NAME CHECK ");

       //Comapy description
        companyDetails = findViewById((R.id.comapnyDetails));
        companyDetails.setText(this.companyDescription);
        //Log.e(null, "DisplayData: COMPANY NAME CHECK ");

        //CurrentPrice
        currentPrice = findViewById(R.id.currentPrice);
        currentPrice.setText(this.currentPriceStr);
        currentPrice.setTextColor(Color.rgb(0,0,0));

        //Change Price
        changePrice = findViewById(R.id.changePrice);
        changePrice.setText(this.changePriceStr);

        //Treding Image
        trendingArrow = findViewById(R.id.trendingArrow);

        if(this.changeIndicator == 1){
            changePrice.setTextColor(Color.rgb(0,255,0));
            trendingArrow.setImageResource(R.drawable.ic_trendingup_green);
        }
        else if(this.changeIndicator == 0){
            changePrice.setTextColor(Color.rgb(255,0,0));
            trendingArrow.setImageResource(R.drawable.ic_trendingdown_red);
        }
        else{
            changePrice.setTextColor(Color.rgb(0,0,0));
        }

        //Charts.
        chartsTabLayout = findViewById(R.id.tabLayout);
        chartsViewPager = findViewById(R.id.viewPager);

        chartFragmentAdapter = new ChartsAdapter(getSupportFragmentManager());
        chartFragmentAdapter.addFragment(new HorlyChartFragment(this.hourlyChartsData, this.changeIndicator,this.comapnyName),"Tab1");
        chartFragmentAdapter.addFragment(new HistoricChartFragment(this.historicChartsData, this.comapnyName),"Tab2");

        chartsViewPager.setAdapter(chartFragmentAdapter);
        chartsTabLayout.setupWithViewPager(chartsViewPager);
        chartsTabLayout.getTabAt(0).setIcon(R.drawable.graph);
        chartsTabLayout.getTabAt(1).setIcon(R.drawable.clock);


        //PORTFOLIO SECTION
        Button TradeButton= findViewById(R.id.tradeButton);
        TradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.e(null, "onClick: "+"TRADE BUTTON CLICKED");
                showTradeDialouge();
            }
        });

        //Shares owned
        TextView sharesOwned = findViewById(R.id.SHARES);


        //AvgCost
        TextView avgCost = findViewById(R.id.AVGCOST);
        int quantity = SharedPreferenceManager.getInstance(getApplicationContext()).getQuantity(this.comapnyName);
        //Log.e(null, "DisplayData: "+quantity+"VALUE");
        float totCost = SharedPreferenceManager.getInstance(getApplicationContext()).getTotalCost(this.comapnyName);
        float avgCostVal;
        float changeVal;
        sharesOwned.setText(String.valueOf(quantity));
        //Change
        TextView change = findViewById(R.id.CHANGE);
        TextView marketPrice = findViewById(R.id.MARVAL_VALUE);

        if(quantity == 0) {
            avgCostVal = 0.0F;
            changeVal = 0.0F;

        }
        else{
            avgCostVal = totCost/quantity;
            changeVal = avgCostVal - getCurrentPriceVal();
            if(changeVal > currentPriceVal){
                change.setTextColor(getResources().getColor(RED));
                marketPrice.setTextColor(getResources().getColor(RED));
            }
            else if(changeVal < currentPriceVal){
                change.setTextColor(getResources().getColor(GREEN));
                marketPrice.setTextColor(getResources().getColor(GREEN));
            }
            else{
                change.setTextColor(getResources().getColor(black));
                marketPrice.setTextColor(getResources().getColor(black));
            }
        }
        avgCost.setText("$"+String.format("%.02f", avgCostVal));
        //Total Cost
        TextView totCostView = findViewById(R.id.TOTCOST);
        totCostView.setText("$"+String.format("%.02f", totCost));


        change.setText("$"+String.format("%.02f",changeVal));


        float marVAl = quantity*getCurrentPriceVal();
        marketPrice.setText("$"+String.format("%.02f",marVAl));

    //STATS PAGE
        TextView OpenPrice = findViewById(R.id.OpenPrice_Value);
        TextView LowPrice = findViewById(R.id.LowPrice_Value);
        TextView HighPrice = findViewById(R.id.HighPrice_Value);
        TextView PrevPrice = findViewById(R.id.PrevPrice_Value);
        JSONObject data = getCompanyStats();
        try {
            OpenPrice.setText("$"+df.format(data.getDouble("o")));
            LowPrice.setText("$"+df.format(data.getDouble("l")));
            HighPrice.setText("$"+df.format(data.getDouble("h")));
            PrevPrice.setText("$"+df.format(data.getDouble("pc")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //ABOUTS PAGE
        TextView IPO_Start_Date = findViewById(R.id.IPO_VALUE);
        TextView Industry = findViewById(R.id.industry_VALUE);
        TextView webpage = findViewById(R.id.webapge_VALUE);
        LinearLayout companyPeersLayout = findViewById(R.id.company_peer_VALUE);
        JSONObject aboutsData = getAboutData();

        try {
            IPO_Start_Date.setText(aboutsData.getString("ipo"));
            Industry.setText(aboutsData.getString("finnhubIndustry"));
            String URLtext=aboutsData.getString("weburl");
            webpage.setPaintFlags(webpage.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            webpage.setText(URLtext);
            webpage.setTextColor(getColor(Link));

            //Peers Section
            ArrayList<String> temp = new ArrayList<>();
            JSONArray PeersJSONArray= getPeers().getJSONArray("data");
            for (int i = 0; i < PeersJSONArray.length(); i++) {
                temp.add(PeersJSONArray.getString(i));
            }

            /*temp.add("ORCL");
            temp.add("MSFT");
            temp.add("VMW");
            temp.add("FTNT");
            temp.add("CRWD");
            temp.add("ZS");
            temp.add("NLOK");*/

            ArrayList peersArray = temp;

            LinearLayout Peerslayout = findViewById(R.id.company_peer_VALUE);

            this.myTextViews = new HashMap<TextView,String>(); // create an empty array;


            for (int i = 0; i < temp.size(); i++) {
                // create a new textview
                TextView peers = new TextView(this);
                String peerstr = temp.get(i);
                peers.setPaintFlags(webpage.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                // set some properties of rowTextView or something
                peers.setText(peerstr);
                peers.setTextColor(getColor(Link));
                peers.setPadding(10,10,0,0);
                peers.setOnClickListener(clicks);

                // add the textview to the linearlayout
                Peerslayout.addView(peers);

                // save a reference to the textview for later
                this.myTextViews.put(peers,temp.get(i));
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }
        //INSIGHTS SECTION
        TextView H_comapnyDesc = findViewById(R.id.Table_Heading1);
        TextView Row_22 = findViewById(R.id.Row2_2);
        TextView Row_23 = findViewById(R.id.Row2_3);
        TextView Row_32 = findViewById(R.id.Row3_2);
        TextView Row_33 = findViewById(R.id.Row3_3);
        TextView Row_42 = findViewById(R.id.Row4_2);
        TextView Row_43 = findViewById(R.id.Row4_3);

        H_comapnyDesc.setText(this.companyDescription);
        H_comapnyDesc.setTextColor(getColor(TEXT_GRAY));
        ArrayList redditArr = getRedditArray();
        Log.e(null, "DisplayData: "+redditArr.toString()+"REDIIT ARRAY");
        Row_22.setText(redditArr.get(0).toString());
        Row_32.setText(redditArr.get(1).toString());
        Row_42.setText(redditArr.get(2).toString());


        ArrayList twitterArr = getTwitterArray();
        //Log.e(null, "DisplayData: "+twitterArr.toString()+"TWITTER ARRAY");
        Row_23.setText(twitterArr.get(0).toString());
        Row_33.setText(twitterArr.get(1).toString());
        Row_43.setText(twitterArr.get(2).toString());

        //Recommendation Chart
            //WebView web= getWeb();
            //getWeb().loadUrl("https://stackoverflow.com/questions/7547154/interact-with-webview-from-method-outside-oncreate-in-android");

        //NEWS RECYCLER VIEW


        //LINK LISTENERS
       webpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = null;
                try {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getAboutData().getString("weburl")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        });

        //JSONObject dataRCJSON = getRecommendationChartJSON();
        //Log.e(null, "DisplayData: "+dataRCJSON);
        //Log.e(null, "DisplayData: "+dataRCJSON.toString()+"LOAD URL SECTION");
       DetailedStockInformation.this.runOnUiThread(new Runnable() {
            @Override
            public void run(){

                getRCWebView().loadUrl("file:///android_asset/RecommendationChart.html");
                //getRCWebView().loadUrl("javascript:setValue('Hello')");
                getRCWebView().setWebViewClient(new WebViewClient() {
                    public void onPageFinished(WebView view, String url) {
                        JSONObject data = getRecommendationChartJSON();
                        String JOSNString = data.toString();
                        //String tickerName = getCompanyName();
                        //getRCWebView().loadUrl("javascript:plot2("+JOSNString+")");
                        getRCWebView().loadUrl("javascript:plot()");
                    }
                });
            }
        });

        DetailedStockInformation.this.runOnUiThread(new Runnable() {
            @Override
            public void run(){

                getHEPSWebView().loadUrl("file:///android_asset/HistoricalEPS.html");
                //getRCWebView().loadUrl("javascript:setValue('Hello')");
                /*getRCWebView().setWebViewClient(new WebViewClient() {
                    public void onPageFinished(WebView view, String url) {
                        getRCWebView().loadUrl("javascript:plot("+dataRCJSON+")");
                    }
                });*/
            }
        });



       //News recycler View
        NewsTab_Adapter newsTabAdapter = new NewsTab_Adapter(getApplicationContext(),getNewModel(), this);
         getNewsRecyclerView().setAdapter(newsTabAdapter);
         getNewsRecyclerView().setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }




 //SETTERS
 private void setImageURL(String imageURL){
     this.imageURL = imageURL;
 }

 private void setCompanyName(String companyName){
     this.comapnyName = companyName;
 }

 private String getCompanyName(){
        return this.comapnyName;
    }

 private void setCompanyDescription(String companyDescription){
     this.companyDescription = companyDescription;
 }

 private void setCurrentPrice(String currentPrice){
     this.currentPriceStr = "$"+currentPrice;
 }

 private void setChangePrice(String changePrice, String changePercent){
        float cprice = Float.parseFloat(changePrice);
       if(cprice > 0){
           this.changeIndicator = 1;
       }
       else if(cprice <0){
           this.changeIndicator = 0;
       }
       else{
           this.changeIndicator = 2;
       }
       this.changePriceStr = changePrice+"("+changePercent+"%"+")";
 }

    public void setHistoricChartsData(JSONObject historicChartsData) {
        this.historicChartsData = historicChartsData;
    }

    public void setHourlyChartsData(JSONObject hourlyChartsData) {
        this.hourlyChartsData = hourlyChartsData;
    }

    public void setVisibility_UnSelected(boolean visibility_UnSelected) {
        Visibility_UnSelected = visibility_UnSelected;
    }

    public boolean getVisibility_FLAG(){
        return this.Visibility_UnSelected;
    }


    public void setCompanyStats(JSONObject companyStats) {
        this.companyStats = companyStats;
    }

    public JSONObject getCompanyStats() {
        return this.companyStats;
    }

    public void setAboutData(JSONObject aboutData) {
        this.aboutData = aboutData;
    }

    public JSONObject getAboutData() {
        return this.aboutData;
    }

    public Map<TextView,String> getMyTextViews() {
        return this.myTextViews;
    }

    public void setPeers(JSONObject peers) {
        this.peers = peers;
    }

    public JSONObject getPeers(){
        return this.peers;
    }

    public void setRedditArray(ArrayList<Integer> socialSentiments) {
        Log.e(null, "setRedditArray: "+socialSentiments.toString()+"REDIIT ARRAY SETTTER");
        this.Reddit = socialSentiments;
    }

    public ArrayList<Integer> getRedditArray() {
        return this.Reddit;
    }

    public void setTwitterArray(ArrayList<Integer> twitter) {
        Log.e(null, "setRedditArray: "+twitter.toString()+"TWITTER ARRAY SETTTER");
        this.Twitter = twitter;
    }

    public ArrayList<Integer> getTwitterArray() {
        return this.Twitter;
    }

    public JSONObject getRecommendationChartJSON() {
        return this.recommendationChartJSON;
    }

    public void setRecommendationChartJSON(JSONObject recommendationChart) {
        this.recommendationChartJSON = recommendationChart;
    }

    public WebView getRCWebView() {
        return this.RCWebView;
    }

    public void setNewsTabJSON(JSONObject newsTabJSON) {
        this.newsTabJSON = newsTabJSON;
    }

    public JSONObject getNewsTabJSON() {
        return this.newsTabJSON;
    }

    public ArrayList<NewsModel> getNewModel() {
        return this.newModel;
    }

    public RecyclerView getNewsRecyclerView() {
        return this.newsRecyclerView;
    }

    public void setCurrentPriceVal(float currentPriceVal) {
        this.currentPriceVal = currentPriceVal;
    }

    public float getCurrentPriceVal() {
        return currentPriceVal;
    }

    public String getCompanyDescription() {
        return companyDescription;
    }

    public WebView getHEPSWebView() {
        return HEPSWebView;
    }

    public void setHEPSWebView(WebView HEPSWebView) {
        this.HEPSWebView = HEPSWebView;
    }

    //ITERFACE FOR RECYCLER VIEW CLICK
    @Override
    public void onNoteClick(int position) {
        //NewsModel newsInstance =
        showNewsDialouge(position);
        //Log.e(null, "onNoteClick: "+"NEWS INSTANCE"+ newModel.get(position).getNewsTitle() );
        //Log.e(null, "onNoteClick: "+"CLICKED");
    }

    //Dialouge box for new Model
    public void showNewsDialouge(int position){
        Dialog dialog = new Dialog(this);
        //headline.setText(newsModelInstance.getNewsTitle());
        //dateStr.setText("5 May 2022");
        dialog.setContentView(R.layout.news_dialouge);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.newsdialouge);

        TextView sourceTitle = dialog.findViewById(R.id.NEWS_SOURCE_TITLE_D);
        TextView headline = dialog.findViewById(R.id.NEWS_HEADLINE_D);
        TextView dateStr = dialog.findViewById(R.id.NEWS_DATE_D);
        TextView content = dialog.findViewById(R.id.NEWS_CONTENT_D);

        ImageButton google = dialog.findViewById(R.id.Google);
        ImageButton twitter = dialog.findViewById(R.id.Twitter);
        ImageButton facebook = dialog.findViewById(R.id.Facebook);

        //setting data
        sourceTitle.setText(newModel.get(position).getNewsSource());
        headline.setText(newModel.get(position).getNewsTitle());
        dateStr.setText(newModel.get(position).getDateStr());
        content.setText(newModel.get(position).getContent());

        //Button Activities
        //Google
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String URL = newModel.get(position).getNewsLink();
                //dialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
                startActivity(intent);
            }
        });

        //Twitter
        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String BASE_URL = "https://twitter.com/intent/tweet?text=";
                String URL = BASE_URL+newModel.get(position).getNewsLink();
                //dialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
                startActivity(intent);
            }
        });

        //FaceBook
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String BASE_URL = "https://www.facebook.com/sharer/sharer.php?";
                String URL = BASE_URL+newModel.get(position).getNewsLink();
                Log.e(null, "onClick: "+newModel.get(position).getNewsLink());
                //dialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
                startActivity(intent);
            }
        });
        dialog.show();
    }

    public void showTradeDialouge(){
        Float value = SharedPreferenceManager.getInstance(getApplicationContext()).getCurrentWallet();
        int quantity = SharedPreferenceManager.getInstance(getApplicationContext()).getQuantity(this.comapnyName);
        TradeDialouge td = new TradeDialouge(this.companyDescription,value,this.comapnyName,quantity,getCurrentPriceVal(),getApplicationContext(),getRootView());
        td.show(getSupportFragmentManager(),"DIAG");
    }

    public void DataChange(){

    }

    public void ShowPurchedDialog(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.treadedialouge);
        dialog.show();
    }

    public View getRootView() {
        return this.rootView;
    }

    public void setRootView(View rootView) {
        this.rootView = rootView;
    }

    //Add favpurites to shared prefs.
    public void addtoFavourites(String tickerName){
        SharedPreferenceManager.getInstance(getApplicationContext()).setFavourites(tickerName);
    }



}
