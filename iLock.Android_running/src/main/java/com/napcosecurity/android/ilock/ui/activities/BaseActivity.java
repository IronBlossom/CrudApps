package com.napcosecurity.android.ilock.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.napcosecurity.android.ilock.R;
import com.napcosecurity.android.ilock.db.AppDatabaseIntegrator;
import com.napcosecurity.android.ilock.db.AppDatabaseAdapter;

public class BaseActivity extends ActionBarActivity {
    private static ActionBar mActionBar;
    private AppDatabaseAdapter db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar = getSupportActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_base_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return false;

    }

    public static ActionBar getAppActionBar() {
        return mActionBar;
    }

    public void requestDb(AppDatabaseIntegrator appDatabaseIntegrator) {
        appDatabaseIntegrator.onAppDatabaseIntegrated(getDb());
    }

    private synchronized AppDatabaseAdapter getDb() {
        if (db == null)
            db = AppDatabaseAdapter.getInstance(this);
        db.getWritableDatabase();
        return db;
    }
}
