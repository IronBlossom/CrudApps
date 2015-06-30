package com.napcosecurity.android.ilock.ui.viewcontrollers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.napcosecurity.android.ilock.db.AppDatabaseFields;
import com.napcosecurity.android.ilock.lock.Lock;
import com.napcosecurity.android.ilock.ui.fragments.LockDetails;

import java.util.ArrayList;

public class LockPagerAdapter extends FragmentStatePagerAdapter {
    ArrayList<Lock> locks;
    String packageName;

    public LockPagerAdapter(FragmentManager fm, ArrayList<Lock> locks, String packageName) {
        super(fm);
        this.locks = locks;
        this.packageName = packageName;
    }

    @Override
    public Fragment getItem(int position) {
        LockDetails lockDetails = new LockDetails();

        Bundle bundle = new Bundle();
        bundle.putParcelable(packageName + AppDatabaseFields.FIELD_LOCK_NAME, locks.get(position));
        lockDetails.setArguments(bundle);

        return lockDetails;
    }

    @Override
    public int getCount() {
        return locks.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return locks.get(position).lockName;
    }
}
