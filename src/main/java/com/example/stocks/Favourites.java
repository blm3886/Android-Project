package com.example.stocks;

public class Favourites {
    String tickerName;
    String tickerDescription;
    String currentvalue;
    String change;


    public Favourites(String tickerName, String tickerDescription, String currentvalue, String change) {
        this.tickerName = tickerName;
        this.tickerDescription = tickerDescription;
        this.currentvalue = currentvalue;
        this.change = change;
    }


    public String getTickerName() {
        return tickerName;
    }

    public void setTickerName(String tickerName) {
        this.tickerName = tickerName;
    }

    public String getTickerDescription() {
        return tickerDescription;
    }

    public void setTickerDescription(String tickerDescription) {
        this.tickerDescription = tickerDescription;
    }

    public String getCurrentvalue() {
        return currentvalue;
    }

    public void setCurrentvalue(String currentvalue) {
        this.currentvalue = currentvalue;
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }
}
