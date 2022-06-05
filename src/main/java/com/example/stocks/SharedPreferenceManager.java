package com.example.stocks;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class SharedPreferenceManager {
    public static final String SHARED_PREFS="sharedPrefs";
    private static final String CURRENT_WALLET = "CURRENT_WALLET";
    private static final String NET_WORTH = "NET_WORTH";
    private static final String FAVOURITES = "FAVOURITES";
    private static final String PORTFOLIO = "PORTFOLIO";
    private static final String SHARES_OWNED = "SHARES_OWNED";
    private static final String TOTAL_COST = "TOATL_COST";
    private static final String COMPANY_DESC = "COMPANY_DESC";
    private static final String QUANTITY = "QUANTITY";



    private SharedPreferences sharedPrefs;
    private static SharedPreferenceManager instance;

    private SharedPreferenceManager(Context context) {
        sharedPrefs =
                context.getApplicationContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
    }
    public static synchronized SharedPreferenceManager getInstance(Context context){
        if(instance == null)
            instance = new SharedPreferenceManager(context);
        return instance;
    }

    //WALLET
    public void setCurrentWallet(Float CurrentWallet) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putFloat(CURRENT_WALLET,CurrentWallet);
        editor.apply();
        editor.commit();
    }

    public Float getCurrentWallet() {
        Float Value = sharedPrefs.getFloat(CURRENT_WALLET, 0.0F);
        return Value;
    }

    //NETWORTH
    public void setNetWorth(Float NetWorth) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        float Bal = sharedPrefs.getFloat(CURRENT_WALLET,0.0F);
        float net = Bal+ NetWorth;
        editor.putFloat(NET_WORTH,NetWorth);
        editor.apply();
        editor.commit();
    }

    public Float getNetWorth() {
        Float Value = sharedPrefs.getFloat(NET_WORTH, 0.0F);
        return Value;
    }
/*
    public void createPortfolio(){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        sharedPrefs.getStringSet(PORTFOLIO,null);
    }*/
/*
    public void createFavourites(){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        sharedPrefs.getStringSet(FAVOURITES,null);
        Log.e("createFavourites: ","FAVOURITES CREATED" );
    }
*/
    //Add to Favourites List
    public void setFavourites(String tickerName){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        if(sharedPrefs.contains(FAVOURITES)){
            Set<String> favourites = sharedPrefs.getStringSet(FAVOURITES, null);
            favourites.add(tickerName);
            editor.putStringSet(FAVOURITES, favourites);
            editor.apply();
            editor.commit();
        }
        else{
            Set<String> favourites = new HashSet<String>();
            favourites.add(tickerName);
            editor.putStringSet(FAVOURITES, favourites);
            editor.apply();
            editor.commit();
        }
        Log.e("setFavourites: ","ADDED FAV"+getFavouritesTickerNames());
    }

    public Set getFavouritesTickerNames(){
        Set<String> fav_set = sharedPrefs.getStringSet(FAVOURITES, null);
        Log.e("getFavouritesTickerNames: ","SHARED PREFS" );
        Log.e("getFavouritesTickerNames: ",""+fav_set);
        return fav_set;
    }

    public void removeFavouritesSet(String tickerName){
        Set<String> fav_Set = sharedPrefs.getStringSet(FAVOURITES, null);
        fav_Set.remove(tickerName);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putStringSet(FAVOURITES,fav_Set);
        editor.apply();
        editor.commit();
    }

    //PORTFOLIO
    //SHAES OWNED
    public void setSharesOwned(int sharesOwned, String tickerName) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        String SO = tickerName+SHARES_OWNED;
        if(sharedPrefs.contains(SO)){
            int val = sharedPrefs.getInt(SO,0);
            editor.putInt(SO,val+sharesOwned);
            editor.apply();
            editor.commit();
        }else{
            editor.putInt(SO,sharesOwned);
            editor.apply();
            editor.commit();
        }
    }

    public int getSharesOwned(String tickerName)
    {   String SO = tickerName+SHARES_OWNED;
        return sharedPrefs.getInt("SO",0);

    }

    //TOTAL COST
    public void setTotalCost(float value, String tickerName){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        String TC = tickerName+TOTAL_COST;
        if(sharedPrefs.contains(TC)){
            float val = sharedPrefs.getFloat(TC,0.0F);
            editor.putFloat(TC,val+value);
            editor.apply();
            editor.commit();
        }else{
            editor.putFloat(TC,value);
            editor.apply();
            editor.commit();
        }

    }

    public float getTotalCost(String tickerName){
        String TC = tickerName+TOTAL_COST;
        return sharedPrefs.getFloat(TC, 0.0F);
    }

    //PORTFOLIO
    public void setToPorfolio(String tickerName) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        if (sharedPrefs.contains(PORTFOLIO)) {
            Set<String> portfolio = sharedPrefs.getStringSet(PORTFOLIO, null);
            portfolio.add(tickerName);
            editor.putStringSet(PORTFOLIO, portfolio);
            editor.apply();
            editor.commit();
        } else {
            Set<String> portfolio = new HashSet<String>();
            portfolio.add(tickerName);
            editor.putStringSet(PORTFOLIO, portfolio);
            editor.apply();
            editor.commit();
        }
    }

    public void removeFromPortfolio(String tickerName){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        Set<String> port = sharedPrefs.getStringSet(PORTFOLIO,null);
        if(port.contains(tickerName)){
            port.remove(tickerName);
        }
        editor.putStringSet(PORTFOLIO,port);
    }

        public Set<String> getFromPortfolio(){
            Set<String> portfolioSet = sharedPrefs.getStringSet(PORTFOLIO,null);
            return portfolioSet;
        }

        public void setComapnyDesc(String ticker, String CompanyDesc){
            Log.e("getCompanyDesc: ","SET_VAL");
            SharedPreferences.Editor editor = sharedPrefs.edit();
            String val = sharedPrefs.getString(ticker,null);
            if(val == null) {
                editor.putString(ticker, CompanyDesc);
            }
        }

        public String getCompanyDesc(String ticker){

              String companyDesc = sharedPrefs.getString(ticker,null);
              Log.e("getCompanyDesc: ",companyDesc);
              return companyDesc;
        }

        public void setQuantity(String tickerName, int Quantity){
            String Quant = tickerName+QUANTITY;
            if(sharedPrefs.contains(Quant)){

                int val = sharedPrefs.getInt(Quant,0);
                Log.e(null, "setQuantity: "+val);
                val = val+Quantity;
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putInt(Quant,val);
                editor.apply();
                editor.commit();
            }else{
                Log.e(null, "setQuantity:"+Quantity);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putInt(Quant,Quantity);
                editor.apply();
                editor.commit();
            }
        }

        public int getQuantity(String tickerName){
            String Quant = tickerName+QUANTITY;
            //Log.e(null, "getQuantity: GETTING DATA"+ sharedPrefs.getInt(Quant,12));
            return sharedPrefs.getInt(Quant,0);

        }

}

