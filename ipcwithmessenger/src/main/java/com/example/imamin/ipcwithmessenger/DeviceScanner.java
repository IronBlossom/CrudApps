package com.example.imamin.ipcwithmessenger;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

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
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bt21LeScanner;
    List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    private BluetoothAdapter.LeScanCallback btLeScanCallback;
    private ScanCallback bt21ScanCallback;
    private OnDeviceScanComplete onDeviceScanComplete = null;

    public DeviceScanner(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }



    public void createScanCallback() {
        if (Build.VERSION.SDK_INT >= 21) {
            bt21ScanCallback = new ScanCallback() {
//                List<BluetoothDevice> tempList = new ArrayList<>();

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    if (callbackType == ScanSettings.CALLBACK_TYPE_ALL_MATCHES) {
                        Log.v("scan21", "continues" + "RSSI=" + result.getRssi());

                        final BluetoothDevice device = result.getDevice();
/*                        if (!tempList.contains(device)) {
                            tempList.add(device);
                        }
*/

                        /**
                         *Becomes true only when new device found
                         *  */
                        if (/*bluetoothDeviceList.size() < tempList.size()*/ !bluetoothDeviceList.contains(device) && device.getName() != null && !device.getName().isEmpty()) {
//                            tempList.clear();
                            bluetoothDeviceList.add(device);
                            if (onDeviceScanComplete != null)
                                onDeviceScanComplete.onComplete(bluetoothDeviceList);
                        }
                    }
                }
            };
        } else {
            btLeScanCallback = new BluetoothAdapter.LeScanCallback() {
//                List<BluetoothDevice> tempList = new ArrayList<>();

                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Log.v("scan", "continues=");
/*
                    if (!tempList.contains(device)) {
                        tempList.add(device);
                    }
*/
                    /**
                     *Becomes true only when new device found
                     *  */

                    if (MiscUtils.parseUUIDs(scanRecord).contains(UUID.fromString("EE88DBB3-5EE0-4ED5-BE48-0F520407EC74")) && /*bluetoothDeviceList.size() < tempList.size()*/ !bluetoothDeviceList.contains(device) && device.getName() != null && !device.getName().isEmpty()) {
//                        tempList.clear();
                        bluetoothDeviceList.add(device);
                        if (onDeviceScanComplete != null)
                            onDeviceScanComplete.onComplete(bluetoothDeviceList);
                    }
                }
            };
        }
    }

    public String serviceUUID;
    public Handler handler = new Handler(Looper.getMainLooper());
    boolean isScanRunning = false;

    public void beginScan(String serviceUUID, OnDeviceScanComplete onDeviceScanComplete) {
        isScanRunning = true;
        this.serviceUUID = serviceUUID;
        bluetoothDeviceList.clear();
        this.onDeviceScanComplete = onDeviceScanComplete;

        handler.removeCallbacksAndMessages(null);
        handler.post(startScanRunnable);
    }


    public void endScan() {
        isScanRunning = false;
        handler.removeCallbacksAndMessages(null);
        handler.post(stopScanRunnable);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void scanWithApi21(String serviceUUID) {
        bt21LeScanner = bluetoothAdapter.getBluetoothLeScanner();
        ScanSettings bt21ScanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
        ParcelUuid serviceParcelUUID = ParcelUuid.fromString(serviceUUID);
        ScanFilter bt21ScanFilter = new ScanFilter.Builder().setServiceUuid(serviceParcelUUID).build();
        List<ScanFilter> bt21ScanFilterList = new ArrayList<>();
        bt21ScanFilterList.add(bt21ScanFilter);
        if (bt21LeScanner != null) {
            bt21LeScanner.startScan(null, bt21ScanSettings, bt21ScanCallback);
        }
        handler.postDelayed(stopScanRunnable, 4000);
    }

    @SuppressWarnings("deprecation")
    private void scanWithInitialApi(String serviceAlarmLockData) {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.startLeScan(new UUID[]{/*UUID.fromString(serviceAlarmLockData)*/}, btLeScanCallback);
        }
        handler.postDelayed(stopScanRunnable, 4000);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void stopScanWithApi21() {
        bt21LeScanner.stopScan(bt21ScanCallback);
        if (isScanRunning)
            handler.postDelayed(startScanRunnable, 1000);
    }

    @SuppressWarnings("deprecation")
    private void stopScanWithInitialApi() {
        bluetoothAdapter.stopLeScan(btLeScanCallback);
        if (isScanRunning)
            handler.postDelayed(startScanRunnable, 1000);
    }

    Runnable startScanRunnable = new Runnable() {
        @Override
        public void run() {
            if (Build.VERSION.SDK_INT >= 21) {
                scanWithApi21(serviceUUID);
            } else {
                scanWithInitialApi(serviceUUID);
            }
        }
    };

    Runnable stopScanRunnable = new Runnable() {
        @Override
        public void run() {
            if (Build.VERSION.SDK_INT >= 21)
                stopScanWithApi21();
            else
                stopScanWithInitialApi();
        }
    };
}
