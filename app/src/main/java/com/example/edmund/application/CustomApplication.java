package com.example.edmund.application;

import android.app.Application;
import android.util.Log;

import com.example.edmund.business.CameraWindow;

/**
 * Created by edmund on 2016/2/19.
 */
public class CustomApplication extends Application {

    private static final String TAG = "CustomApplication";
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            CameraWindow.show(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
    }
}
