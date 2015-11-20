package com.example.imamin.ilockserviceexp;

import android.app.Application;
import android.content.Intent;

/**
 * Created by imamin on 11/3/2015.
 */
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        startService(new Intent(this, ExpService.class));
    }
}
