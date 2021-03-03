package com.example.studienprojekt_android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

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
     * onUserLeaveHint()
     */
    @Override
    protected void onUserLeaveHint() {
        // When user presses home page
        super.onUserLeaveHint();
    }

    /**
     * onBackPressed()
     */
    @Override
    public void onBackPressed(){
        //disables onBackPressed()
    }
}

