package com.napcosecurity.android.ilock.lock;

import android.os.Parcel;
import android.os.Parcelable;

public class Lock implements Parcelable {
    public static final Creator<Lock> CREATOR = new Creator<Lock>() {
        @Override
        public Lock createFromParcel(Parcel in) {
            return new Lock(in);
        }

        @Override
        public Lock[] newArray(int size) {
            return new Lock[size];
        }
    };
    public int _id;
    public String lockName;
    public String lockPassword;
    public String lockMac;
    public String lockId;
    public String lockSlot;
    public int lockSettingsSavePassword;
    public int lockSettingsKeypad;
    public int lockSettingsOperateUnattended;
    public int lockSettingsUnattendedTimeoutPosition;
    public int lockBatteryPercent;
    public int lockPassTime; /*No Database field created for this*/

    public Lock() {

    }

    public Lock(Parcel in) {
        _id = in.readInt();
        lockName = in.readString();
        lockPassword = in.readString();
        lockMac = in.readString();
        lockId = in.readString();
        lockSlot = in.readString();
        lockSettingsSavePassword = in.readInt();
        lockSettingsKeypad = in.readInt();
        lockSettingsOperateUnattended = in.readInt();
        lockSettingsUnattendedTimeoutPosition = in.readInt();
        lockBatteryPercent = in.readInt();
        lockPassTime = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeString(lockName);
        dest.writeString(lockPassword);
        dest.writeString(lockMac);
        dest.writeString(lockId);
        dest.writeString(lockSlot);
        dest.writeInt(lockSettingsSavePassword);
        dest.writeInt(lockSettingsKeypad);
        dest.writeInt(lockSettingsOperateUnattended);
        dest.writeInt(lockSettingsUnattendedTimeoutPosition);
        dest.writeInt(lockBatteryPercent);
        dest.writeInt(lockPassTime);
    }
}
