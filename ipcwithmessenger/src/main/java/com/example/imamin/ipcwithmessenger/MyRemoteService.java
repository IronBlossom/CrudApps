package com.example.imamin.ipcwithmessenger;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class MyRemoteService extends Service {
    public static final int MSG_FROM_SERVICE_UNLOCKED = 0x1122;
    public static final int MSG_FROM_SERVICE_DISCONNECTED = 0x2211;
    public static final int MSG_FROM_SERVICE_FAILED = 0x2121;
    public static final int MSG_FROM_SERVICE_STATUS = 0x8965;
    public static final int REPLY_TO_THIS_MESSENGER = 0x9999;
    protected Messenger sendingMessenger;
    protected Handler serviceTaskHandler = new Handler();
    int counter = 0;
    boolean isUnlocked = false;


    //BLUETOOTH STAFFS
    BluetoothManager btMan;
    BluetoothAdapter btAdapter;
    BluetoothDevice btRemoteDevice;
    BluetoothGatt btleGatt;
    String password;
    String Mac;
    boolean isLooping;
    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            bleInitiate();
            bleConnectToDevice(btRemoteDevice);
        }
    };

    private static final class IncomingClientMessageHandler extends Handler {
        private final WeakReference<MyRemoteService> serviceWeakReference;
        private MyRemoteService service;

        public IncomingClientMessageHandler(MyRemoteService service) {
            this.serviceWeakReference = new WeakReference<>(service);
            this.service = serviceWeakReference.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MainActivity.MSG_FROM_CLIENT:
                    Log.v(MainActivity.TAG, "Got message from client inside handleMessage");
                    Log.v(MainActivity.TAG, "Starts Unlocking...");

                    Bundle data = msg.getData();
                    service.password = (String) data.get("pass");
                    service.Mac = (String) data.get("Mac");
                    service.isLooping = (boolean) data.get("loop");

                    service.serviceTaskHandler.post(service.task);

                    break;
                case MainActivity.MSG_FROM_CLIENT_STOP_LOOPING:

                    Bundle data1 = msg.getData();
                    service.isLooping = (boolean) data1.get("loop");


                    break;
                case REPLY_TO_THIS_MESSENGER:
                    service.sendingMessenger = msg.replyTo;
                    break;
                default:
                    super.handleMessage(msg);

            }
        }
    }

    private final Messenger incomingHandlerMessenger = new Messenger(new IncomingClientMessageHandler(this));

    public MyRemoteService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(MainActivity.TAG, "onStartCommand--Service has started.");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.v(MainActivity.TAG, "onDestroy--Service has been destroyed.");

        Bundle data = new Bundle();
        data.putBoolean("serviceRunning", false);
        Message msg = Message.obtain(null, MSG_FROM_SERVICE_STATUS, 0, 0);
        msg.setData(data);

        try {
            sendingMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        serviceTaskHandler.removeCallbacksAndMessages(null);
        serviceTaskHandler = null;

        super.onDestroy();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.v(MainActivity.TAG, "onStart");
        super.onStart(intent, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(MainActivity.TAG, "Inside onBind");
        return incomingHandlerMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(MainActivity.TAG, "Inside onUnbind");
        return true;
    }


    //Bluetooth
    private void bleInitiate() {
        if (btMan == null)
            btMan = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        if (btAdapter == null)
            btAdapter = btMan.getAdapter();

        if (btRemoteDevice == null)
            btRemoteDevice = btAdapter.getRemoteDevice(Mac);
    }

    private void bleConnectToDevice(BluetoothDevice btRemoteDevice) {
        btleGatt = btRemoteDevice.connectGatt(this, false, new BleGattCallback(this));
    }

    private static class BleGattCallback extends BluetoothGattCallback {
        BluetoothGattCharacteristic charRxType, charRxBuffer, charRxCtrl, charTxType, charTxBuffer;
        int desWriteCount = 0;
        MyRemoteService myRemoteService;
        boolean hasEncryptionKey = false;

        byte[] lockFullStatus = new byte[4];
        byte[] encryptByte;

        public BleGattCallback(MyRemoteService myRemoteService) {
            this.myRemoteService = myRemoteService;
            hasEncryptionKey = false;
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(MainActivity.TAG, "Connected");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(MainActivity.TAG, "Disconnected");
                gatt.disconnect();
                gatt.close();
                if (myRemoteService.isLooping) {
                    Log.v("Again", "Started");
                    myRemoteService.serviceTaskHandler.removeCallbacksAndMessages(null);
                    myRemoteService.serviceTaskHandler.postDelayed(myRemoteService.task, 3000);
                }
                try {
                    Message msg = Message.obtain(null, MSG_FROM_SERVICE_DISCONNECTED);
                    myRemoteService.sendingMessenger.send(msg);

                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                if (!myRemoteService.isUnlocked) {
                    try {
                        Message msg = Message.obtain(null, MSG_FROM_SERVICE_FAILED);
                        myRemoteService.sendingMessenger.send(msg);

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService gattService = gatt.getService(UUID.fromString("EE88DBB3-5EE0-4ED5-BE48-0F520407EC74"));

                charRxType = gattService.getCharacteristic(UUID.fromString("74102D3D-0300-4E1B-ABB3-2D491AC12833"));
                charRxBuffer = gattService.getCharacteristic(UUID.fromString("305B0556-78FC-4885-9567-E64C78D06467"));
                charRxCtrl = gattService.getCharacteristic(UUID.fromString("588AB4F2-612C-40B5-8169-83A13B632520"));
                charTxType = gattService.getCharacteristic(UUID.fromString("BA67C742-3E49-4E0F-AD0D-141E0020F2A2"));
                charTxBuffer = gattService.getCharacteristic(UUID.fromString("A8C993EB-1322-4A8E-9CB1-4501B9AE539B"));

                if (desWriteCount == 0) {
                    gatt.setCharacteristicNotification(charTxType, true);
                    BluetoothGattDescriptor charTxTypeDescriptor = charTxType.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"));
                    charTxTypeDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(charTxTypeDescriptor);
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            desWriteCount++;
            if (desWriteCount == 1) {
                gatt.setCharacteristicNotification(charTxBuffer, true);
                BluetoothGattDescriptor charTxBufferDescriptor = charTxBuffer.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"));
                charTxBufferDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(charTxBufferDescriptor);
            } else if (desWriteCount == 2) {
                gatt.setCharacteristicNotification(charRxCtrl, true);
                BluetoothGattDescriptor charRxCtrlDescriptor = charRxCtrl.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805F9B34FB"));
                charRxCtrlDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(charRxCtrlDescriptor);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            byte[] data = characteristic.getValue();
            if (characteristic.equals(charRxCtrl)) {
                Log.v("RxCtrl", "Notified");

            } else if (characteristic.equals(charTxType)) {
                Log.v("TxType", "Notified");
                lockFullStatus[0] = data[0];
            } else if (characteristic.equals(charTxBuffer)) {
                Log.v("TxBuffer", "Notified");
                lockFullStatus[1] = data[0];
                lockFullStatus[2] = data[1];
                if (lockFullStatus[0] == ((byte) 0x15)) {
                    encryptByte = data;
                    hasEncryptionKey = true;
                }
                if (hasEncryptionKey) {
                    if (lockFullStatus[0] == ((byte) 0x12) && lockFullStatus[1] == ((byte) 0x30) && lockFullStatus[2] == ((byte) 0x33)) {
                        lockFullStatus = new byte[4];
                        final byte writeRxType[] = new byte[3];
                        writeRxType[0] = ((byte) 0x16);
                        charRxType.setValue(writeRxType);
                        gatt.writeCharacteristic(charRxType);

                    } else if (data[1] == ((byte) 0x35) || data[1] == ((byte) 0x34)) {
                        myRemoteService.isUnlocked = true;
                        try {
                            Message msg = Message.obtain(null, MSG_FROM_SERVICE_UNLOCKED);
                            myRemoteService.sendingMessenger.send(msg);

                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (characteristic.equals(charRxType)) {
                    Log.v("charRxType", "Written");
                    String padPassword = padPassword(myRemoteService.password);
                    try {
                        byte[] encryptPassword = encryptPassword(padPassword.getBytes("US-ASCII"), encryptByte);
                        if (encryptPassword.length <= 20) {
                            charRxBuffer.setValue(encryptPassword);
                            gatt.writeCharacteristic(charRxBuffer);
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                } else if (characteristic.equals(charRxBuffer)) {
                    Log.v("charRxBuffer", "Written");
                }
            }
        }
    }

    public static String padPassword(String password) {
        StringBuilder stringBuilder = new StringBuilder();
        int iterationLimit = (password == null ? 20 : 20 - password.length());
        stringBuilder.append((password == null ? "" : password));
        for (int i = 0; i < iterationLimit; i++) {
            stringBuilder.append(' ');
        }
        return stringBuilder.toString();
    }

    public static byte[] encryptPassword(byte[] passwordBytes, byte[] encryptByte) throws NullPointerException {
        byte[] result = new byte[20];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (passwordBytes[i] + encryptByte[i]);
        }

        return result;
    }
}
