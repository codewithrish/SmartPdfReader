package com.hackdevelopers.smartpdfreader.youtube;

import android.widget.Toast;

import com.hackdevelopers.smartpdfreader.Singleton;

/**
 * Created by ravikumar on 10/20/2014.
 */
public class AppUtils {

    public static void showToast(String iMessage) {
        Toast.makeText(Singleton.getAppContext(), iMessage, Toast.LENGTH_SHORT).show();
    }
}
