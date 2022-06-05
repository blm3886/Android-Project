package com.example.stocks;

import android.content.Context;
import android.webkit.JavascriptInterface;

public class WebViewInterface {
    Context mcontext;

    /** Instantiate the interface and set the context */
    WebViewInterface(Context c) {
        mcontext = c;
    }

   /* *//** Show a toast from the web page *//*
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }*/


}
