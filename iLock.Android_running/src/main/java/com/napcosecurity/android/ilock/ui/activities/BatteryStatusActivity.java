package com.napcosecurity.android.ilock.ui.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import com.napcosecurity.android.ilock.R;
import com.napcosecurity.android.ilock.db.AppDatabaseAdapter;
import com.napcosecurity.android.ilock.db.AppDatabaseIntegrator;
import com.napcosecurity.android.ilock.lock.Lock;
import com.napcosecurity.android.ilock.ui.viewcontrollers.BatteryStatsAdapter;

import java.util.ArrayList;

public class BatteryStatusActivity extends BaseActivity implements AppDatabaseIntegrator, View.OnClickListener {
    ListView lvBattStatus;
    ImageButton ibBack, ibHome;
    ArrayList<Lock> locks = new ArrayList<Lock>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_status);
        initViews();
        requestDb(this);
    }

    private void initViews() {
        lvBattStatus = (ListView) findViewById(R.id.lvBattStatList);

        ibBack = (ImageButton) findViewById(R.id.ibBack);
        ibHome = (ImageButton) findViewById(R.id.ibHome);
    }

    private void setViewControllers() {
        ibBack.setOnClickListener(this);
        ibHome.setOnClickListener(this);

        BatteryStatsAdapter adapter = new BatteryStatsAdapter(this, locks);
        lvBattStatus.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    @Override
    public void onAppDatabaseIntegrated(AppDatabaseAdapter db) {
        locks = db.getAllLocks();
        setViewControllers();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ibBack)
            finish();
        if (v.getId() == R.id.ibHome) {
            finish();
        }
    }
}
