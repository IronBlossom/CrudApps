package com.example.imamin.ipcwithmessenger;

import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * Created by
 * Ishtiaq Mahmood Amin (from imamin)
 * imamin@ael-bd.com
 * on 5/20/2015 at 1:57 PM.
 * Copyright (c) 2015 Napco Security Technologies, Inc. All rights reserved.
 */
public interface OnDeviceScanComplete {
    void onComplete(List<BluetoothDevice> foundDeviceList);
}
