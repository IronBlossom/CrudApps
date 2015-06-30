package com.napcosecurity.android.ilock.ble;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;

import com.napcosecurity.android.ilock.constants.CommState;
import com.napcosecurity.android.ilock.constants.Constants;
import com.napcosecurity.android.ilock.constants.DataType;
import com.napcosecurity.android.ilock.constants.LockedState;
import com.napcosecurity.android.ilock.lock.Lock;

import java.util.List;

public class BackgroundService extends Service {
    public static final String TAG = "INUNATTENDED";
    static boolean unattendedModeRunning = false;
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder binder = new LocalBinder();
    BluetoothManager btMan;
    BluetoothAdapter btAdapter;
    DeviceScanner scanner;
    BluetoothGatt bleGatt;
    BluetoothGattCallback bleGattCallback;
    String password;
    BroadcastReceiver stopUnattendedWakeup;
    private Handler handler = new Handler();
    private Lock lock;

    public BackgroundService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        stopUnattendedWakeup = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        scanner.endScan();
                        if (bleGatt != null) {
                            bleGatt.disconnect();
                            bleGatt.close();
                            bleGatt = null;
                        }
                    }
                });
            }
        };
        registerReceiver(stopUnattendedWakeup, new IntentFilter(getPackageName() + TAG));

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void initBTAndScanner() {
        if (btMan == null)
            btMan = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        if (btAdapter == null)
            btAdapter = btMan.getAdapter();

        scanner = new DeviceScanner(btAdapter);
    }

    public void startUnattendedMode(Lock lockToBeUnlocked) {
        unattendedModeRunning = true;
        this.lock = lockToBeUnlocked;
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(
                getPackageName() + TAG), 0);
        AlarmManager am = (AlarmManager) (this.getSystemService(Context.ALARM_SERVICE));
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + ((lock.lockSettingsUnattendedTimeoutPosition + 1) * 60 * 1000), pi);


        scanner.createScanCallback();
        scanner.beginScan(Constants.UUID.SERVICE_ALARM_LOCK_DATA.toString(), new OnDeviceScanComplete() {
            @Override
            public void onComplete(List<BluetoothDevice> foundDeviceList) {
                for (BluetoothDevice device : foundDeviceList) {
                    password = Constants.passwordList.get(device.getAddress().replace(":", ""));
                    if (password != null) {
                        scanner.endScan();
                        connectToDevice(device);
                    }
                }
            }
        });
    }

    private void connectToDevice(BluetoothDevice remoteDevice) {
        bleGatt = remoteDevice.connectGatt(this, false, new BluetoothGattCallback() {
            final byte[] lockFullStatus = new byte[3];
            int descWriteCount = 0;
            BluetoothGattCharacteristic charRxType, charRxBuffer, charRxCtrl, charTxType, charTxBuffer;

            int seqLB = -1;

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);

                bleGatt = gatt;
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.w("ALARM_LOCK", "FromService=ignoring connection state event with status, status=" + status);
                    return;
                }
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            bleGatt.discoverServices();
                        }
                    });
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    bleGatt = gatt;
                    BluetoothGattService service = gatt.getService(Constants.UUID.SERVICE_ALARM_LOCK_DATA);
                    charRxType = service.getCharacteristic(Constants.UUID.CHARACTERISTIC_DATA_RX_TYPE);
                    charRxBuffer = service.getCharacteristic(Constants.UUID.CHARACTERISTIC_DATA_RX_BUFFER);
                    charRxCtrl = service.getCharacteristic(Constants.UUID.CHARACTERISTIC_DATA_RX_CTRL);
                    charTxType = service.getCharacteristic(Constants.UUID.CHARACTERISTIC_DATA_TX_TYPE);
                    charTxBuffer = service.getCharacteristic(Constants.UUID.CHARACTERISTIC_DATA_TX_BUFFER);

                    if (descWriteCount == 0) {
                        gatt.setCharacteristicNotification(charTxType, true);
                        BluetoothGattDescriptor bt2TxTypeDesc = charTxType.getDescriptor(Constants.UUID.DESCRIPTOR_PRE_CLIENT_CONFIG);
                        bt2TxTypeDesc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        bleGatt.writeDescriptor(bt2TxTypeDesc);
                    }
                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
                bleGatt = gatt;
                descWriteCount++;
                if (descWriteCount == 1) {
                    bleGatt.setCharacteristicNotification(charTxBuffer, true);
                    BluetoothGattDescriptor bt2TxBufferDesc = charTxBuffer.getDescriptor(Constants.UUID.DESCRIPTOR_PRE_CLIENT_CONFIG);
                    bt2TxBufferDesc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    bleGatt.writeDescriptor(bt2TxBufferDesc);
                } else if (descWriteCount == 2) {
                    bleGatt.setCharacteristicNotification(charRxCtrl, true);
                    BluetoothGattDescriptor bt2RxCtrlDesc = charRxCtrl.getDescriptor(Constants.UUID.DESCRIPTOR_PRE_CLIENT_CONFIG);
                    bt2RxCtrlDesc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    bleGatt.writeDescriptor(bt2RxCtrlDesc);
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                bleGatt = gatt;
                byte[] data = characteristic.getValue();
                if (characteristic.equals(charRxCtrl)) {
                    Log.v("LockDetailsRxCtrl", "=" + Integer.toHexString(data[0]) + " " + Integer.toHexString(data[1]) + "  " + Integer.toHexString(data[2]));
                } else if (characteristic.equals(charTxType)) {
                    Log.v("LockDetailsTxType", "=" + Integer.toHexString(data[0]) + " " + Integer.toHexString(data[1]) + "  " + Integer.toHexString(data[2]));
                    lockFullStatus[0] = data[0];
                    seqLB = data[1];
                } else if (characteristic.equals(charTxBuffer)) {
                    Log.v("LockDetailsTxBuffer", "=" + Integer.toHexString(data[0]) + " " + Integer.toHexString(data[1]) + "  " + Integer.toHexString(data[2]));
                    lockFullStatus[1] = data[0];
                    lockFullStatus[2] = data[1];

                    if (lockFullStatus[0] == DataType.LockStatus.getValue() && lockFullStatus[1] == CommState.StateReady.getValue() && lockFullStatus[2] == LockedState.Locked.getValue()) {

                        byte writeRxType[] = new byte[3];
                        writeRxType[0] = DataType.Password.getValue();
                        writeRxType[1] = (byte) (seqLB + 1);

                        charRxType.setValue(writeRxType);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.v("WriteRxType", "FromService=" + bleGatt.writeCharacteristic(charRxType));
                            }
                        });
                    }
                    if (data[1] == LockedState.UnlockedPassageMode.getValue() || data[1] == LockedState.UnlockedPassTime.getValue()) {
                        unattendedModeRunning = false;
                        final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        vibrator.vibrate(1500);
                    }
                }

            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    bleGatt = gatt;
                    if (characteristic.equals(charRxType)) {
                        Log.v("LockDetailsRxType", "FromService=write");
                        byte[] writableRxBuffer = lock.lockPassword.getBytes();
                        if (writableRxBuffer.length <= 20) {
                            charRxBuffer.setValue(writableRxBuffer);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.v("WriteRxBuffer3", "FromService=" + bleGatt.writeCharacteristic(charRxBuffer));
                                }
                            });
                        }

                    } else if (characteristic.equals(charRxBuffer)) {
                        Log.v("LockDetailsRxBuffer", "FromService=" + "called");
                    }
                }

            }
        });
    }

    public class LocalBinder extends Binder {
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }
}
