package com.beviswang.logcapture.java;

import android.app.Application;

import com.beviswang.capturelib.handler.CrashHandler;

/**
 * Created by shize on 2018/3/20.
 */
public class DemoApplicationJava extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.Companion.getInstance().init(this);
    }
}
