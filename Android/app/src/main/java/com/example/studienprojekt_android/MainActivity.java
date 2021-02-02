//Lars Klee

package com.example.studienprojekt_android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    /**
     * onCreate()
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("main-thread", "MainActivity: " + (Looper.getMainLooper().getThread() == Thread.currentThread()));
        setContentView(R.layout.activity_main);
    }

    /**
     * onUserLeaveHint()
     */
    @Override
    protected void onUserLeaveHint() {
        // When user presses home page
        Log.v("", "Home Button Pressed");
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

