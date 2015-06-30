package com.napcosecurity.android.ilock.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.napcosecurity.android.ilock.BuildConfig;
import com.napcosecurity.android.ilock.R;
import com.napcosecurity.android.ilock.db.AppDatabaseAdapter;
import com.napcosecurity.android.ilock.db.AppDatabaseFields;
import com.napcosecurity.android.ilock.db.AppDatabaseIntegrator;
import com.napcosecurity.android.ilock.lock.Lock;
import com.napcosecurity.android.ilock.ui.viewcontrollers.SettingsLocksAdapter;
import com.napcosecurity.android.ilock.utils.MiscUtils;

import java.util.ArrayList;

public class SettingsActivity extends BaseActivity implements AdapterView.OnItemClickListener, AppDatabaseIntegrator, View.OnClickListener {
    ArrayList<Lock> locks;
    TextView tvEnrollNewLock, tvBatteryStatus, tvVersionName;
    CheckBox ckDebugMode;
    ListView lockListView;
    ImageButton ibBack, ibHome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();
        updateVersionTV();

    }

    @Override
    protected void onResume() {
        super.onResume();
        requestDb(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onAppDatabaseIntegrated(AppDatabaseAdapter db) {
        locks = db.getAllLocks();
        tvBatteryStatus.setVisibility((locks == null || locks.size() == 0) ? View.GONE : View.VISIBLE);
        setViewControllers();
    }

    private void initViews() {
        tvEnrollNewLock = (TextView) findViewById(R.id.tvEnrollNewLock);
        tvBatteryStatus = (TextView) findViewById(R.id.tvBatteryStatus);
        tvVersionName = (TextView) findViewById(R.id.tvVersionName);
        ckDebugMode = (CheckBox) findViewById(R.id.ckDebugMode);
        lockListView = (ListView) findViewById(R.id.lvLockList);
        ibBack = (ImageButton) findViewById(R.id.ibBack);
        ibHome = (ImageButton) findViewById(R.id.ibHome);
    }

    private void updateVersionTV() {
        tvVersionName.setText("Version: " + BuildConfig.VERSION_NAME);
    }

    private void setViewControllers() {

        ibBack.setOnClickListener(this);
        ibHome.setOnClickListener(this);

        SettingsLocksAdapter adapter = new SettingsLocksAdapter(this, locks);
        lockListView.setAdapter(adapter);
        lockListView.setOnItemClickListener(this);


        //DEBUG MODE
        ckDebugMode.setChecked(MiscUtils.debugMode);
        ckDebugMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MiscUtils.debugMode = isChecked;
            }
        });

    }

    public void enrollNewLock(View view) {
        startActivity(new Intent(this, LockListActivity.class));
    }

    public void gotoBatteryStatus(View view) {
        startActivity(new Intent(this, BatteryStatusActivity.class));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Intent intent = new Intent(this, SettingsLockDetailsActivity.class);
        intent.putExtra(getPackageName() + AppDatabaseFields.FIELD_LOCK_NAME, locks.get(position));
        startActivity(intent);
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
