package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EasySplashScreen splashscreen = new EasySplashScreen(SplashScreenActivity.this)
                                                .withFullScreen()
                                                .withTargetActivity(MainActivity.class)
                                                .withSplashTimeOut(2500)
                                                .withBackgroundColor(Color.GRAY)
                                                .withHeaderText("WIFIdirect & data")
                                                .withBeforeLogoText("P2P connection and file transferring")
                                                .withAfterLogoText("Minimise the data cost")
                                                .withFooterText("v0.0.1")
                                                .withLogo(R.drawable.ic_splashscreenroundlogo);//png to svg

        splashscreen.getHeaderTextView().setTextSize(24);

        View easySplashScreen = splashscreen.create();
        setContentView(easySplashScreen);
    }
}