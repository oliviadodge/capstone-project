package com.example.olivi.maphap;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by olivi on 12/2/2015.
 */
public class MyApplication extends Application {
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
