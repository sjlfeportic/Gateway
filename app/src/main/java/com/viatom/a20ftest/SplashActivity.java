package com.viatom.a20ftest;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        TextView textView = findViewById(R.id.textSplashScreen);
        textView.animate().translationX(1000).setDuration(1000).setStartDelay(2500);


        Thread thread = new Thread(){

            public void run() {
                try {
                    Thread.sleep(4000);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);

                    startActivity(intent);
                    finish();
                }

            }
        };
        thread.start();
    }
}