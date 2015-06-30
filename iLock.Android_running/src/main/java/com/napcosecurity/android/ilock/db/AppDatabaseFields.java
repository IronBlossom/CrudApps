package com.napcosecurity.android.ilock.db;

public class AppDatabaseFields {
    public static final String DB_NAME = "iLock.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE_LOCKS = "Locks";

    public static final String FIELD_ID = "_id";
    public static final String FIELD_LOCK_NAME = "lock_name";
    public static final String FIELD_LOCK_PASSWORD = "lock_password";
    public static final String FIELD_LOCK_MAC = "lock_mac";
    public static final String FIELD_LOCK_ID = "lock_id";
    public static final String FIELD_LOCK_SLOT = "lock_slot";
    public static final String FIELD_LOCK_SETTINGS_SAVE_PASSWORD = "lock_save_password";
    public static final String FIELD_LOCK_SETTINGS_KEYPAD = "lock_keypad";
    public static final String FIELD_LOCK_SETTINGS_OPERATE_UNATTENDED = "lock_operate_unattended";
    public static final String FIELD_LOCK_SETTINGS_UNATTENDED_TIMEOUT = "lock_unattended_timeout";
    public static final String FIELD_LOCK_BATTERY_PERCENT = "lock_battery_percent";

}
