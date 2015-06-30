package com.napcosecurity.android.ilock.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import com.napcosecurity.android.ilock.constants.Constants;
import com.napcosecurity.android.ilock.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by
 * Ishtiaq Mahmood Amin (from imamin)
 * imamin@ael-bd.com
 * on 5/20/2015 at 1:10 PM.
 * Copyright (c) 2015 Napco Security Technologies, Inc. All rights reserved.
 */
public class DeviceScanner {
    private static DeviceScanner deviceScanner = null;
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bt21LeScanner;
    List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    private BluetoothAdapter.LeScanCallback btLeScanCallback;
    private ScanCallback bt21ScanCallback;
    private OnDeviceScanComplete onDeviceScanComplete = null;

    public DeviceScanner(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public void createScanCallback() {
        if (Build.VERSION.SDK_INT >= 21) {
            bt21ScanCallback = new ScanCallback() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    if (callbackType == ScanSettings.CALLBACK_TYPE_ALL_MATCHES) {
                        Log.v("scan21", "continues");
                        /**
                         *Becomes true only when new device found
                         *  */
                        if (!bluetoothDeviceList.contains(result.getDevice())) {
                            bluetoothDeviceList.add(result.getDevice());
                            if (onDeviceScanComplete != null)
                                onDeviceScanComplete.onComplete(bluetoothDeviceList);
                        }
                    }
                }
            };
        } else {
            btLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Log.v("scan", "continues=" );

                    /**
                     *Becomes true only when new device found
                     *  */

                    if (MiscUtils.parseUUIDs(scanRecord).contains(Constants.UUID.SERVICE_ALARM_LOCK_DATA) && !bluetoothDeviceList.contains(device)) {
                        bluetoothDeviceList.add(device);
                        if (onDeviceScanComplete != null)
                            onDeviceScanComplete.onComplete(bluetoothDeviceList);
                    }
                }
            };
        }
    }

    public void beginScan(String serviceUUID, OnDeviceScanComplete onDeviceScanComplete) {
        bluetoothDeviceList.clear();
        this.onDeviceScanComplete = onDeviceScanComplete;

        if (Build.VERSION.SDK_INT >= 21) {
            scanWithApi21(serviceUUID);
        } else {
            scanWithInitialApi(serviceUUID);
        }
    }


    public void endScan() {
        if (Build.VERSION.SDK_INT >= 21)
            stopScanWithApi21();
        else
            stopScanWithInitialApi();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void scanWithApi21(String serviceUUID) {
        bt21LeScanner = bluetoothAdapter.getBluetoothLeScanner();
        ScanSettings bt21ScanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
        ParcelUuid serviceParcelUUID = ParcelUuid.fromString(serviceUUID);
        ScanFilter bt21ScanFilter = new ScanFilter.Builder().setServiceUuid(serviceParcelUUID).build();
        List<ScanFilter> bt21ScanFilterList = new ArrayList<>();
        bt21ScanFilterList.add(bt21ScanFilter);
        bt21LeScanner.startScan(bt21ScanFilterList, bt21ScanSettings, bt21ScanCallback);
    }

    @SuppressWarnings("deprecation")
    private void scanWithInitialApi(String serviceAlarmLockData) {
        bluetoothAdapter.startLeScan(new UUID[]{/*UUID.fromString(serviceAlarmLockData)*/}, btLeScanCallback);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void stopScanWithApi21() {
        bt21LeScanner.stopScan(bt21ScanCallback);
    }

    @SuppressWarnings("deprecation")
    private void stopScanWithInitialApi() {
        bluetoothAdapter.stopLeScan(btLeScanCallback);
    }
}
