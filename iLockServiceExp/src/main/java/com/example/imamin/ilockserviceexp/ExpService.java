package com.example.imamin.ilockserviceexp;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.UUID;

public class ExpService extends Service {
    private static final String TAG = "iLockExp";
    private BluetoothManager expBlueMan;
    private BluetoothAdapter expBlueAdp;
    private String expBlueDevAddr;
    private BluetoothGatt expBlueGatt;
    private int expConState = STATE_DISCONNECTED;

    private static final int STATE_CONNECTING = 0;
    private static final int STATE_CONNECTED = 1;
    private static final int STATE_DISCONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "imamin.ilockserviceexp.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "imamin.ilockserviceexp.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "imamin.ilockserviceexp.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "imamin.ilockserviceexp.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "imamin.ilockserviceexp.EXTRA_DATA";

    public static final UUID UUID_ALARM_LOCK = UUID.fromString("EE88DBB3-5EE0-4ED5-BE48-0F520407EC74");
    private final BluetoothGattCallback expGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String expIntentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                expIntentAction = ACTION_GATT_CONNECTED;
                expConState = STATE_CONNECTED;
                broadcastUpdate(expIntentAction);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                expIntentAction = ACTION_GATT_DISCONNECTED;
                expConState = STATE_DISCONNECTED;
                broadcastUpdate(expIntentAction);
            }
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (expBlueMan == null) {
            expBlueMan = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (expBlueMan == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        expBlueAdp = expBlueMan.getAdapter();
        if (expBlueAdp == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public boolean connect(final String address) {
        if (expBlueAdp == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (expBlueDevAddr != null && address.equals(expBlueDevAddr)
                && expBlueGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (expBlueGatt.connect()) {
                expConState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = expBlueAdp.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        expBlueGatt = device.connectGatt(this, false, expGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        expBlueDevAddr = address;
        expConState = STATE_CONNECTING;
        return true;
    }

    public void disconnect() {
        if (expBlueAdp == null || expBlueGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        expBlueGatt.disconnect();
    }
    public void close() {
        if (expBlueGatt == null) {
            return;
        }
        expBlueGatt.close();
        expBlueGatt = null;
    }
    public ExpService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private final IBinder expBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return expBinder;
    }

    public class LocalBinder extends Binder {
        ExpService getService() {
            return ExpService.this;
        }
    }
}
