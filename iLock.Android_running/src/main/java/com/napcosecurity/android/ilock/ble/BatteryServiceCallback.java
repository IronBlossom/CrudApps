package com.napcosecurity.android.ilock.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.util.Log;

import com.napcosecurity.android.ilock.constants.Constants;
import com.napcosecurity.android.ilock.ui.activities.LockListActivity;

import java.util.UUID;

/**
 * Created by
 * Ishtiaq Mahmood Amin (from imamin)
 * imamin@ael-bd.com
 * on 6/16/2015 at 3:59 PM.
 * Copyright (c) 2015 Napco Security Technologies, Inc. All rights reserved.
 */
public class BatteryServiceCallback extends BluetoothGattCallback {
    LockListActivity activity;
Handler handler;
    public BatteryServiceCallback(LockListActivity activity) {
        this.activity = activity;
        this.handler=new Handler();
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if(status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED){
            final BluetoothGatt iGatt=gatt;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    iGatt.discoverServices();
                }
            },500);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        if(status==BluetoothGatt.GATT_SUCCESS){
            final BluetoothGattService batteryService = gatt.getService(Constants.UUID.SERVICE_BATTERY_REPORTING);

            final BluetoothGattCharacteristic batteryLevelCharacteristic = batteryService.getCharacteristic(UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb"));
            gatt.readCharacteristic(batteryLevelCharacteristic);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        if(status==BluetoothGatt.GATT_SUCCESS){
            Log.v("BatteryLevel","="+characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
        }
    }
}
