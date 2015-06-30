package com.napcosecurity.android.ilock.ble;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.napcosecurity.android.ilock.R;
import com.napcosecurity.android.ilock.constants.CommState;
import com.napcosecurity.android.ilock.constants.Constants;
import com.napcosecurity.android.ilock.constants.DataCTRL;
import com.napcosecurity.android.ilock.constants.DataType;
import com.napcosecurity.android.ilock.constants.SystemCommand;
import com.napcosecurity.android.ilock.lock.Lock;
import com.napcosecurity.android.ilock.ui.activities.LockListActivity;
import com.napcosecurity.android.ilock.utils.MiscUtils;

import java.util.Arrays;

/**
 * Created by
 * Ishtiaq Mahmood Amin (from imamin)
 * imamin@ael-bd.com
 * on 5/22/2015 at 11:38 AM.
 * Copyright (c) 2015 Napco Security Technologies, Inc. All rights reserved.
 */
public class DEPGattCallback extends BluetoothGattCallback {
    private static final long delay = 200;
    public static boolean _2nd = false;
    ProgressDialog progressDialog;
    Handler handler = new Handler();
    OnNewLockEnrolled onNewLockEnrolled;
    Activity activity;
    Lock lock;
    BluetoothGatt bt2Gatt;
    BluetoothGattService bt2Service;
    BluetoothGattCharacteristic bt2CharTxType, bt2CharTxBuffer, bt2CharTxCtrl, bt2CharRxType, bt2CharRxBuffer, bt2CharRxCtrl;
    //Flag
    int descWriteCount;
    int seqLB = 0, seqHB = 0;
    PreviousStage previousStage;
    Runnable stopEnrolling = new Runnable() {
        @Override
        public void run() {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                        if (bt2Gatt != null) {
                            bt2Gatt.disconnect();
                            bt2Gatt.close();
                        }
                    }
                }
            });
        }
    };

    public DEPGattCallback(LockListActivity activity, Lock lock) {
        this.activity = activity;
        this.lock = lock;
        this.descWriteCount = 0;
        previousStage = PreviousStage.WaitForConnection;
        progressDialog = new ProgressDialog(this.activity);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Connecting...");
        progressDialog.show();
        handler.postDelayed(stopEnrolling, 10000);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
//        Log.i("onConnectionStateChange", ">status=" + status + "  " + "newState=" + newState);
        if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
            if (previousStage == PreviousStage.WaitForConnection) {

                previousStage = PreviousStage.Connected;

                bt2Gatt = gatt;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bt2Gatt.discoverServices();
                    }
                }, 500);
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        if (status == BluetoothGatt.GATT_SUCCESS && previousStage == PreviousStage.Connected) {

            previousStage = PreviousStage.ServiceDiscovered;

            bt2Gatt = gatt;
            bt2Service = bt2Gatt.getService(Constants.UUID.SERVICE_ALARM_LOCK_DATA);

            //init char
            bt2CharTxType = bt2Service.getCharacteristic(Constants.UUID.CHARACTERISTIC_DATA_TX_TYPE);
            bt2CharTxBuffer = bt2Service.getCharacteristic(Constants.UUID.CHARACTERISTIC_DATA_TX_BUFFER);
            bt2CharTxCtrl = bt2Service.getCharacteristic(Constants.UUID.CHARACTERISTIC_DATA_TX_CTRL);
            bt2CharRxType = bt2Service.getCharacteristic(Constants.UUID.CHARACTERISTIC_DATA_RX_TYPE);
            bt2CharRxBuffer = bt2Service.getCharacteristic(Constants.UUID.CHARACTERISTIC_DATA_RX_BUFFER);
            bt2CharRxCtrl = bt2Service.getCharacteristic(Constants.UUID.CHARACTERISTIC_DATA_RX_CTRL);

            bt2Gatt.setCharacteristicNotification(bt2CharTxType, true);
            BluetoothGattDescriptor bt2TxTypeDesc = bt2CharTxType.getDescriptor(Constants.UUID.DESCRIPTOR_PRE_CLIENT_CONFIG);
            bt2TxTypeDesc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bt2Gatt.writeDescriptor(bt2TxTypeDesc);

        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        descWriteCount++;
        if (descWriteCount == 1) {
            bt2Gatt.setCharacteristicNotification(bt2CharTxBuffer, true);
            BluetoothGattDescriptor bt2TxBufferDesc = bt2CharTxBuffer.getDescriptor(Constants.UUID.DESCRIPTOR_PRE_CLIENT_CONFIG);
            bt2TxBufferDesc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bt2Gatt.writeDescriptor(bt2TxBufferDesc);
        } else if (descWriteCount == 2) {
            bt2Gatt.setCharacteristicNotification(bt2CharRxCtrl, true);
            BluetoothGattDescriptor bt2RxCtrlDesc = bt2CharRxCtrl.getDescriptor(Constants.UUID.DESCRIPTOR_PRE_CLIENT_CONFIG);
            bt2RxCtrlDesc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bt2Gatt.writeDescriptor(bt2RxCtrlDesc);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        bt2Gatt = gatt;
        byte[] data = characteristic.getValue();
//        Log.v("char", "changed=" + characteristic.getUuid() + " data[0/1/2]=" + data[0] + "/" + data[1] + "/" + data[2]);
        if (characteristic.equals(bt2CharTxType)) {
            if (data[0] == DataType.LockStatus.getValue() && previousStage == PreviousStage.ServiceDiscovered) {

                previousStage = PreviousStage.TxTypeNotify1;

                seqLB = data[1];
                seqHB = data[2];

                Log.v("1==>OCC==>TxType", "seqLB=" + seqLB);
            } else if (data[0] == DataType.EnrollmentData.getValue() && previousStage == PreviousStage.RxCtrlNotify1) {

                previousStage = PreviousStage.TxTypeNotify2;

                seqLB = data[1];
                seqHB = data[2];

                Log.v("7==>OCC==>TxType", "seqLB=" + seqLB);
            }
        } else if (characteristic.equals(bt2CharTxBuffer)) {
            if (data[0] == CommState.StateReady.getValue() && previousStage == PreviousStage.TxTypeNotify1) {

                previousStage = PreviousStage.TxBufferNotify1;

             /*   lock.lockBatteryPercent = data[3];
                lock.lockPassTime = data[4];*/

                byte[] writableTxCtrl = new byte[3];
                writableTxCtrl[0] = DataCTRL.ACK.getValue();
                writableTxCtrl[1] = (byte) seqLB;
                writableTxCtrl[2] = (byte) seqHB;

                bt2CharTxCtrl.setValue(writableTxCtrl);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bt2Gatt.writeCharacteristic(bt2CharTxCtrl);
//                            Log.v("WriteTxCtrl0", "=" + (bt2Gatt.writeCharacteristic(bt2CharTxCtrl)));
                    }
                }, delay);

                Log.v("2==>OCC==>TxBuff", "seqLB=" + seqLB);
            } else if (previousStage == PreviousStage.TxTypeNotify2 && (data[6] == 0x2d && data[11] == 0x2d)) {

                previousStage = PreviousStage.TxBufferNotify2;

                lock.lockMac = MiscUtils.bytesToHexString(Arrays.copyOfRange(data, 0, 6));
                lock.lockId = MiscUtils.bytesToHexString(Arrays.copyOfRange(data, 7, 11));
                lock.lockSlot = MiscUtils.bytesToHexString(Arrays.copyOfRange(data, 12, 16));

                byte[] writableTxCtrl = new byte[3];
                writableTxCtrl[0] = DataCTRL.ACK.getValue();
                writableTxCtrl[1] = (byte) seqLB;
                writableTxCtrl[2] = (byte) seqHB;

                bt2CharTxCtrl.setValue(writableTxCtrl);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bt2Gatt.writeCharacteristic(bt2CharTxCtrl);
//                        Log.v("WriteTxCtrl2", "=" + (bt2Gatt.writeCharacteristic(bt2CharTxCtrl)));
                    }
                }, delay);
                Log.v("8==>OCC==>TxBuff", "seqLB=" + seqLB);
            }
        } else if (characteristic.equals(bt2CharRxCtrl)) {
            if (previousStage == PreviousStage.RxBufferWrite1) {
                if (data[0] == DataCTRL.ACK.getValue()) {
                    previousStage = PreviousStage.RxCtrlNotify1;
                }
                Log.v("6==>OCC==>RxCtrl", "seqLB=" + seqLB);
            } else if (previousStage == PreviousStage.RxBufferWrite2) {
                if (data[0] == DataCTRL.ACK.getValue()) {

                    bt2Gatt.disconnect();
                    bt2Gatt.close();

                    onNewLockEnrolled = (OnNewLockEnrolled) activity;
                    onNewLockEnrolled.onEnroll(lock);
                }
                Log.v("12==>OCC==>RxCtrl", "seqLB=" + seqLB);
            }
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            byte data[] = characteristic.getValue();
            if (characteristic.equals(bt2CharTxCtrl)) {
                if (data[0] == DataCTRL.ACK.getValue() && previousStage == PreviousStage.TxBufferNotify1) {

                    previousStage = PreviousStage.TxCtrlWrite1;

                    byte[] writableRxType0 = new byte[3];
                    writableRxType0[0] = DataType.Command.getValue();
                    //seqLB++;
                    writableRxType0[1] = (byte) seqLB;
                    writableRxType0[2] = (byte) seqHB;

                    bt2CharRxType.setValue(writableRxType0);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bt2Gatt.writeCharacteristic(bt2CharRxType);
//                            Log.v("WriteRxType0", "=" + bt2Gatt.writeCharacteristic(bt2CharRxType));
                        }
                    }, delay);
                    Log.v("3==>OCW==>TxCtrl", "seqLB=" + seqLB);
                } else if (data[0] == DataCTRL.ACK.getValue() && previousStage == PreviousStage.TxBufferNotify2) {

                    previousStage = PreviousStage.TxCtrlWrite2;

                    byte[] writableRxType0 = new byte[3];
                    writableRxType0[0] = DataType.Password.getValue();
                    seqLB++;
                    writableRxType0[1] = (byte) seqLB;
                    writableRxType0[2] = (byte) seqHB;

                    bt2CharRxType.setValue(writableRxType0);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bt2Gatt.writeCharacteristic(bt2CharRxType);
//                            Log.v("WriteRxType1", "=" + bt2Gatt.writeCharacteristic(bt2CharRxType));
                        }
                    }, delay);
                    Log.v("9==>OCW==>TxCtrl", "seqLB=" + seqLB);
                }
            } else if (characteristic.equals(bt2CharRxType)) {
                if (previousStage == PreviousStage.TxCtrlWrite1) {

                    previousStage = PreviousStage.RxTypeWrite1;

                    byte[] writableRxBuffer1 = new byte[20];
                    writableRxBuffer1[0] = SystemCommand.BeginDEP.getValue();
                    System.arraycopy(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00}, 0, writableRxBuffer1, 1, 6);//Phone Mac
                    writableRxBuffer1[7] = 0;//CheckSum

                    for (int i = 0; i < writableRxBuffer1.length; i++) {
                        Log.i("w[" + i + "]", "=" + writableRxBuffer1[i]);
                    }

                    bt2CharRxBuffer.setValue(writableRxBuffer1);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bt2Gatt.writeCharacteristic(bt2CharRxBuffer);
//                            Log.v("WriteRxBuffer1", "=" + bt2Gatt.writeCharacteristic(bt2CharRxBuffer));
                        }
                    }, delay);
                    Log.v("4==>OCW==>RxType", "seqLB=" + seqLB);
                } else if (previousStage == PreviousStage.TxCtrlWrite2) {

                    previousStage = PreviousStage.RxTypeWrite2;

                    progressDialog.dismiss();

                    if (!activity.isFinishing()) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                handler.removeCallbacks(stopEnrolling);
                                final Dialog dialog = new Dialog(activity, R.style.CustomDialogTheme);
                                dialog.setContentView(R.layout.dialog_interpass);
                                dialog.setCancelable(false);
                                final EditText etLockPass = (EditText) dialog.findViewById(R.id.etLockPass);
                                final EditText etLockName = (EditText) dialog.findViewById(R.id.etLockName);
                                final Button btnCheckPass = (Button) dialog.findViewById(R.id.btnCheckPass);
                                dialog.show();
                                btnCheckPass.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                        lock.lockPassword = etLockPass.getText().toString();
                                        lock.lockName = etLockName.getText().toString();
                                        byte[] password = new byte[20];
                                        password = lock.lockPassword.getBytes();
                                        bt2CharRxBuffer.setValue(password);
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                bt2Gatt.writeCharacteristic(bt2CharRxBuffer);
//                                            Log.v("WriteRxBuffer2", "=" + bt2Gatt.writeCharacteristic(bt2CharRxBuffer));
                                            }
                                        }, delay);
                                    }
                                });
                                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        bt2Gatt.disconnect();
                                        bt2Gatt.close();
                                    }
                                });
                            }
                        });
                    }
                    Log.v("10==>OCW==>RxType", "seqLB=" + seqLB);
                }
            } else if (characteristic.equals(bt2CharRxBuffer)) {
                if (previousStage == PreviousStage.RxTypeWrite1) {

                    previousStage = PreviousStage.RxBufferWrite1;

                    Log.v("5==>OCW==>RxBuff", "seqLB=" + seqLB);
                } else if (previousStage == PreviousStage.RxTypeWrite2) {

                    previousStage = PreviousStage.RxBufferWrite2;

                    Log.v("11==>OCW==>RxBuff", "seqLB=" + seqLB);

                }
            }
        }
    }
}
