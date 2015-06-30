package com.napcosecurity.android.ilock.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.napcosecurity.android.ilock.db.AppDatabaseFields;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class BaseApplication extends Application {
    public static final String APP_TAG = "ALBLE";
    public static final String APP_SPREFNAME = "alble";
    public static SharedPreferences sPref;

    @Override
    public void onCreate() {
        super.onCreate();
        sPref = createAppSPref();
        shiftDB();
        /*File filename = new File(Environment.getExternalStorageDirectory() + "/alble.log");
        try {
            if (!filename.exists())
                filename.createNewFile();
            String cmd = "logcat -d -f " + filename.getAbsolutePath();
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    private SharedPreferences createAppSPref() {
        return getSharedPreferences(APP_SPREFNAME,
                Context.MODE_PRIVATE);
    }

    private void shiftDB() {
        if (sPref.getInt("DBVersionChecked", 0) < AppDatabaseFields.DB_VERSION) {
            sPref.edit().putInt("DBVersionChecked", AppDatabaseFields.DB_VERSION).apply();

            File directory = new File(getDatabasePath(
                    AppDatabaseFields.DB_NAME).getParent()
                    + File.separator);
            if (directory.mkdir() || directory.isDirectory()) {
                try {
                    InputStream dbIn = getAssets().open(
                            AppDatabaseFields.DB_NAME);
                    OutputStream dbOut = new FileOutputStream(
                            directory.getAbsolutePath() + File.separator
                                    + AppDatabaseFields.DB_NAME);

                    byte[] buffer = new byte[1024];
                    int remainingData;
                    while ((remainingData = dbIn.read(buffer)) > 0) {
                        dbOut.write(buffer, 0, remainingData);
                    }
                    dbOut.flush();
                    dbOut.close();
                    Log.v(APP_TAG, "New DB created...");
                } catch (Exception e) {
                    Log.v(APP_TAG, "Could not create new DB...");
                    e.printStackTrace();
                }
            }
        }
    }
}