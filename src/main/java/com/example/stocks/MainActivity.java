package com.example.stocks;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ProgressBar;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    ProgressBar progressBarLoad;
    int counter = 0;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.myToolBarCustom);
        setSupportActionBar(toolbar);
        //progressBarLoad.setVisibility(View.VISIBLE);
        callHomeActivity();

    }


    public void callHomeActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(MainActivity.this,HomeActivity.class);
                startActivity(intent);
                finish();
            }
        },2000);

    }

    /*public void callHomeActivity(){

        Timer timer = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                  counter++;
                   progressBarLoad.setProgress(counter);

                  if(counter == 100){
                      timer.cancel();
                      Intent intent = new Intent(MainActivity.this,HomeActivity.class);
                      startActivity(intent);
                      //finish();
                  }
            }
        };

        timer.schedule(tt,100,100);
    }*/




}