package com.napcosecurity.android.ilock.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.napcosecurity.android.ilock.lock.Lock;
import com.napcosecurity.android.ilock.ui.activities.BaseActivity;

import java.util.ArrayList;

public class AppDatabaseAdapter extends SQLiteOpenHelper {
    private static AppDatabaseAdapter sInstance = null;
    private SQLiteDatabase sqliteDB;

    private AppDatabaseAdapter(Context context) {
        super(context, AppDatabaseFields.DB_NAME, null, AppDatabaseFields.DB_VERSION);
        this.onCreate(getWritableDatabase());

    }

    public static AppDatabaseAdapter getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AppDatabaseAdapter(context.getApplicationContext());
        }
        return context instanceof BaseActivity ? sInstance : null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        sqliteDB = db;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + AppDatabaseFields.DB_NAME);
        onCreate(db);
    }

    public boolean isOpen() {
        return sqliteDB.isOpen();
    }

    //Insert||Update||Delete
    public long insertLock(String FIELD_LOCK_NAME, String FIELD_LOCK_PASSWORD, String FIELD_LOCK_MAC, String FIELD_LOCK_ID, String FIELD_LOCK_SLOT, int FIELD_LOCK_SETTINGS_SAVE_PASSWORD, int FIELD_LOCK_SETTINGS_KEYPAD, int FIELD_LOCK_SETTINGS_OPERATE_UNATTENDED, int FIELD_LOCK_SETTINGS_UNATTENDED_TIMEOUT, int FIELD_LOCK_BATTERY_PERCENT) {

        ContentValues values = new ContentValues();

        values.put(AppDatabaseFields.FIELD_LOCK_NAME, FIELD_LOCK_NAME);
        values.put(AppDatabaseFields.FIELD_LOCK_PASSWORD, FIELD_LOCK_PASSWORD);
        values.put(AppDatabaseFields.FIELD_LOCK_MAC, FIELD_LOCK_MAC);
        values.put(AppDatabaseFields.FIELD_LOCK_ID, FIELD_LOCK_ID);
        values.put(AppDatabaseFields.FIELD_LOCK_SLOT, FIELD_LOCK_SLOT);
        values.put(AppDatabaseFields.FIELD_LOCK_SETTINGS_SAVE_PASSWORD, FIELD_LOCK_SETTINGS_SAVE_PASSWORD);
        values.put(AppDatabaseFields.FIELD_LOCK_SETTINGS_KEYPAD, FIELD_LOCK_SETTINGS_KEYPAD);
        values.put(AppDatabaseFields.FIELD_LOCK_SETTINGS_OPERATE_UNATTENDED, FIELD_LOCK_SETTINGS_OPERATE_UNATTENDED);
        values.put(AppDatabaseFields.FIELD_LOCK_SETTINGS_UNATTENDED_TIMEOUT, FIELD_LOCK_SETTINGS_UNATTENDED_TIMEOUT);
        values.put(AppDatabaseFields.FIELD_LOCK_BATTERY_PERCENT, FIELD_LOCK_BATTERY_PERCENT);

        return sqliteDB.insert(AppDatabaseFields.TABLE_LOCKS, null, values);
    }

    public boolean updateLock(int FIELD_ID, String FIELD_LOCK_NAME, String FIELD_LOCK_PASSWORD, String FIELD_LOCK_MAC, String FIELD_LOCK_ID, String FIELD_LOCK_SLOT, int FIELD_LOCK_SETTINGS_SAVE_PASSWORD, int FIELD_LOCK_SETTINGS_KEYPAD, int FIELD_LOCK_SETTINGS_OPERATE_UNATTENDED, int FIELD_LOCK_SETTINGS_UNATTENDED_TIMEOUT, int FIELD_LOCK_BATTERY_PERCENT) {
        ContentValues values = new ContentValues();

        values.put(AppDatabaseFields.FIELD_LOCK_NAME, FIELD_LOCK_NAME);
        values.put(AppDatabaseFields.FIELD_LOCK_PASSWORD, FIELD_LOCK_PASSWORD);
        values.put(AppDatabaseFields.FIELD_LOCK_MAC, FIELD_LOCK_MAC);
        values.put(AppDatabaseFields.FIELD_LOCK_ID, FIELD_LOCK_ID);
        values.put(AppDatabaseFields.FIELD_LOCK_SLOT, FIELD_LOCK_SLOT);
        values.put(AppDatabaseFields.FIELD_LOCK_SETTINGS_SAVE_PASSWORD, FIELD_LOCK_SETTINGS_SAVE_PASSWORD);
        values.put(AppDatabaseFields.FIELD_LOCK_SETTINGS_KEYPAD, FIELD_LOCK_SETTINGS_KEYPAD);
        values.put(AppDatabaseFields.FIELD_LOCK_SETTINGS_OPERATE_UNATTENDED, FIELD_LOCK_SETTINGS_OPERATE_UNATTENDED);
        values.put(AppDatabaseFields.FIELD_LOCK_SETTINGS_UNATTENDED_TIMEOUT, FIELD_LOCK_SETTINGS_UNATTENDED_TIMEOUT);
        values.put(AppDatabaseFields.FIELD_LOCK_BATTERY_PERCENT, FIELD_LOCK_BATTERY_PERCENT);

        return sqliteDB.update(AppDatabaseFields.TABLE_LOCKS, values,
                AppDatabaseFields.FIELD_ID + "='" + FIELD_ID + "'", null) > 0;
    }

    public boolean deleteLock(int FIELD_ID) {
        return sqliteDB.delete(AppDatabaseFields.TABLE_LOCKS, AppDatabaseFields.FIELD_ID + "='"
                + FIELD_ID + "'", null) > 0;
    }

    public boolean isLockEnrolled(String FIELD_ID) {
        Cursor mCursor = sqliteDB.query(AppDatabaseFields.TABLE_LOCKS, new String[]{AppDatabaseFields.FIELD_ID}, AppDatabaseFields.FIELD_ID + "='" + FIELD_ID + "'", null, null, null, null,
                null);
        int count = mCursor.getCount();
        mCursor.close();
        return count > 0;
    }

    public String getLockPasswordByMac(String lockMac) {
        Cursor mCursor = sqliteDB.query(AppDatabaseFields.TABLE_LOCKS, new String[]{AppDatabaseFields.FIELD_LOCK_PASSWORD}, AppDatabaseFields.FIELD_LOCK_MAC + "='" + lockMac + "'", null, null, null, null,
                null);
        String lockPassword = null;
        if (mCursor.getCount() > 0) {
            lockPassword = mCursor.getString(0);
        }
        mCursor.close();
        return lockPassword;
    }

    public ArrayList<Lock> getAllLocks() {
        Cursor mCursor;
        mCursor = sqliteDB.query(AppDatabaseFields.TABLE_LOCKS, new String[]{AppDatabaseFields.FIELD_ID, AppDatabaseFields.FIELD_LOCK_NAME, AppDatabaseFields.FIELD_LOCK_PASSWORD, AppDatabaseFields.FIELD_LOCK_MAC, AppDatabaseFields.FIELD_LOCK_ID, AppDatabaseFields.FIELD_LOCK_SLOT, AppDatabaseFields.FIELD_LOCK_SETTINGS_SAVE_PASSWORD, AppDatabaseFields.FIELD_LOCK_SETTINGS_KEYPAD, AppDatabaseFields.FIELD_LOCK_SETTINGS_OPERATE_UNATTENDED, AppDatabaseFields.FIELD_LOCK_SETTINGS_UNATTENDED_TIMEOUT, AppDatabaseFields.FIELD_LOCK_BATTERY_PERCENT}, null, null, null, null, null,
                null);
        ArrayList<Lock> arrayList = new ArrayList<>();
        if (mCursor != null && mCursor.moveToFirst()) {
            for (int i = 0; i < mCursor.getCount(); i++) {
                Lock lock = new Lock();

                lock._id = (mCursor.getInt(0));
                lock.lockName = (mCursor.getString(1));
                lock.lockPassword = (mCursor.getString(2));
                lock.lockMac = (mCursor.getString(3));
                lock.lockId = (mCursor.getString(4));
                lock.lockSlot = (mCursor.getString(5));
                lock.lockSettingsSavePassword = (mCursor.getInt(6));
                lock.lockSettingsKeypad = (mCursor.getInt(7));
                lock.lockSettingsOperateUnattended = (mCursor.getInt(8));
                lock.lockSettingsUnattendedTimeoutPosition = (mCursor.getInt(9));
                lock.lockBatteryPercent = (mCursor.getInt(10));

                arrayList.add(lock);
                mCursor.moveToNext();
            }
            mCursor.close();
        }

        return arrayList;
    }

    public int updateBattery(String FIELD_ID, int FIELD_LOCK_BATTERY_PERCENT) {
        ContentValues values = new ContentValues();
        values.put(AppDatabaseFields.FIELD_LOCK_BATTERY_PERCENT, FIELD_LOCK_BATTERY_PERCENT);
        return sqliteDB.update(AppDatabaseFields.TABLE_LOCKS, values,
                AppDatabaseFields.FIELD_ID + "='" + FIELD_ID + "'", null) > 0 ? FIELD_LOCK_BATTERY_PERCENT : -1;
    }
}
