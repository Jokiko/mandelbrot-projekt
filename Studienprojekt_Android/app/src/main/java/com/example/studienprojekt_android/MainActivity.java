package com.example.studienprojekt_android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    //Used to load the 'native-lib library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * onCreate()
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * onBackPressed()
     */
    @Override
    public void onBackPressed(){
        //disables onBackPressed()
    }
}

