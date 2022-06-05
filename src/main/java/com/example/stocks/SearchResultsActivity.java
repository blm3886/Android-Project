package com.example.stocks;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class SearchResultsActivity extends Activity {
    Context context = getApplicationContext();
    int duration = Toast.LENGTH_SHORT;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.e(null, "handleIntent: "+"CALLED");
            //use the query to search your data somehow
            Toast toast = Toast.makeText(context, query, duration);
            toast.show();

        }
    }


}


