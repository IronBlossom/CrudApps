package com.napcosecurity.android.ilock.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.napcosecurity.android.ilock.R;
import com.napcosecurity.android.ilock.constants.Constants;
import com.napcosecurity.android.ilock.db.AppDatabaseAdapter;
import com.napcosecurity.android.ilock.db.AppDatabaseIntegrator;
import com.napcosecurity.android.ilock.lock.Lock;
import com.napcosecurity.android.ilock.ui.viewcontrollers.LockPagerAdapter;

import java.util.ArrayList;

public class LockPagerActivity extends BaseActivity implements AppDatabaseIntegrator {
    ArrayList<Lock> locks;
    ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockpager);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestDb(this);
    }

    @Override
    public void onAppDatabaseIntegrated(AppDatabaseAdapter db) {
        locks = db.getAllLocks();

        Constants.passwordList.clear();
        for(Lock lock:locks){
            Constants.passwordList.put(lock.lockMac,lock.lockPassword);
        }
        if (locks == null || locks.size() == 0) {
            Toast.makeText(getApplicationContext(), "Please add a lock first.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SettingsActivity.class));

        } else
            setPagerControllers();
    }

    private void initViews() {
        pager = (ViewPager) findViewById(R.id.viewPager);
    }

    private void setPagerControllers() {

        LockPagerAdapter lockPagerAdapter = new LockPagerAdapter(getSupportFragmentManager(), locks,getPackageName());
        pager.setAdapter(lockPagerAdapter);
    }
}
