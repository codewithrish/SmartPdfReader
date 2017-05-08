package com.hackdevelopers.smartpdfreader;

import android.app.Application;
import android.content.Context;

/**
 * Created by risha on 5/6/2017.
 */

public class Singleton extends Application {

    private static Context sContext = null;

    public static Context getAppContext() {
        return sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
    }

    public static String appName() {
        return getAppContext().getString(R.string.app_name);
    }

}
