package com.napcosecurity.android.ilock.ui.fragments;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.napcosecurity.android.ilock.R;
import com.napcosecurity.android.ilock.ble.BackgroundService;
import com.napcosecurity.android.ilock.ble.DeviceScanner;
import com.napcosecurity.android.ilock.ble.OnDeviceScanComplete;
import com.napcosecurity.android.ilock.constants.CommState;
import com.napcosecurity.android.ilock.constants.Constants;
import com.napcosecurity.android.ilock.constants.DataType;
import com.napcosecurity.android.ilock.constants.LockedState;
import com.napcosecurity.android.ilock.db.AppDatabaseFields;
import com.napcosecurity.android.ilock.lock.Lock;
import com.napcosecurity.android.ilock.ui.activities.KeyboardActivity;
import com.napcosecurity.android.ilock.utils.MiscUtils;

import java.util.List;

public class LockDetails extends Fragment implements View.OnClickListener {
    static BluetoothDevice bleDevice;
    static boolean isUnlocking = false;
    static boolean isUnlocked = false;
    StringBuilder logBuilder = new StringBuilder();
    BluetoothManager btMan;
    Lock lock;
    ImageView ibLockStat;
    ImageButton ibLockKeypad;
    Button btnStatSwitcher;
    Handler handler = new Handler();
    DeviceScanner scanner;
    BluetoothGatt bleGatt;
    String password;
    BackgroundService backgroundService;
    Runnable timedLockOperation = new Runnable() {
        @Override
        public void run() {
            if (!isUnlocked && MiscUtils.debugMode)
                showLogDialog();
            lock();

        }
    };
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            backgroundService = ((BackgroundService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void showLogDialog() {
        logBuilder.append("\nTimeout");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Dialog dialog = new Dialog(getActivity(), R.style.CustomDialogTheme);
                dialog.setContentView(R.layout.dialog_debug);
                final TextView etLockPass = (TextView) dialog.findViewById(R.id.logTv);
                final Button btnTry = (Button) dialog.findViewById(R.id.tryBtn);
                btnTry.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        btnStatSwitcher.performClick();
                    }
                });

                etLockPass.setText(logBuilder.toString());
                dialog.show();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btMan = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        lock = getArguments().getParcelable(getActivity().getPackageName() + AppDatabaseFields.FIELD_LOCK_NAME);
        Log.v("Fragment", "onCreate");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().bindService(new Intent(getActivity(), BackgroundService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        getActivity().unbindService(serviceConnection);
        super.onDestroy();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lockdetails, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ibLockStat = (ImageView) view.findViewById(R.id.status_indicator);
        ibLockKeypad = (ImageButton) view.findViewById(R.id.btn_lock_keypad);
        btnStatSwitcher = (Button) view.findViewById(R.id.btn_status_switcher);

    }

    @Override
    public void onResume() {
        super.onResume();
        updateViews();
        setViewControllers();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    private void updateViews() {
        if (isUnlocked) {
            System.out.println("Unlocked");
            ibLockStat.setImageResource(R.drawable.ic_unlocked);
            btnStatSwitcher.setText("Unlocked");
            btnStatSwitcher.setClickable(false);
        } else if (isUnlocking) {
            System.out.println("Unlocking");
            ibLockStat.setImageResource(R.drawable.ic_locked);
            btnStatSwitcher.setText("Unlocking...");
            btnStatSwitcher.setClickable(false);
        } else
            lock();

        ibLockKeypad.setVisibility(lock.lockSettingsKeypad == 0 ? View.GONE : View.VISIBLE);
    }

    private void setViewControllers() {
        btnStatSwitcher.setOnClickListener(this);
        ibLockKeypad.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_lock_keypad) {
            Intent intent = new Intent(getActivity(), KeyboardActivity.class);
            intent.putExtra(AppDatabaseFields.FIELD_LOCK_NAME, lock.lockName);
            startActivity(intent);
        }
        //New status switcher
        if (v.getId() == R.id.btn_status_switcher) {
            if (lock.lockSettingsSavePassword == 0) {
                final Dialog dialog = new Dialog(getActivity(), R.style.CustomDialogTheme);
                dialog.setContentView(R.layout.dialog_interpass);
                final EditText etLockName = (EditText) dialog.findViewById(R.id.etLockName);
                etLockName.setVisibility(View.GONE);
                final EditText etLockPass = (EditText) dialog.findViewById(R.id.etLockPass);
                final Button btnCheckPass = (Button) dialog.findViewById(R.id.btnCheckPass);
                dialog.show();


                btnCheckPass.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (etLockPass.getText().toString().compareTo(lock.lockPassword) == 0) {
                            dialog.dismiss();
                            if (lock.lockSettingsOperateUnattended == 1)
                                unlockUnattended();
                            else
                                unlock();
                        } else
                            etLockPass.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left));
                    }
                });
            } else {
                if (lock.lockSettingsOperateUnattended == 1)
                    unlockUnattended();
                else
                    unlock();
            }
        }
    }

    private void unlockUnattended() {
        backgroundService.initBTAndScanner();
        backgroundService.startUnattendedMode(lock);
    }

    private void unlock() {
        //  getActivity().unbindService(serviceConnection);
        logBuilder = new StringBuilder();
        isUnlocking = true;
        restartTimer(Constants.EVENT_TIMEOUT);
        /*handler.postDelayed(timedLockOperation, lock.lockSettingsOperateUnattended == 1 ? ((lock.lockSettingsUnattendedTimeoutPosition + 1) * 60 * 1000) : UNLOCK_ATTEMPT_TIMEOUT);*/

        btnStatSwitcher.setText("Unlocking...");
        btnStatSwitcher.setClickable(false);
        if (btMan == null)
            btMan = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);

        final BluetoothAdapter adapter = btMan.getAdapter();
        final String macFormatted = MiscUtils.formatMAC(lock.lockMac);
        final BluetoothDevice remoteDevice = adapter.getRemoteDevice(macFormatted);
        if (remoteDevice.getAddress().compareTo(macFormatted) == 0) {
            logBuilder.append("Device found from adapter.");
            getConnectionReady(remoteDevice);
            return;
        }

        scanner = new DeviceScanner(adapter);

        scanner.createScanCallback();
        scanner.beginScan(Constants.UUID.SERVICE_ALARM_LOCK_DATA.toString(), new OnDeviceScanComplete() {
            @Override
            public void onComplete(List<BluetoothDevice> foundDeviceList) {
                for (BluetoothDevice device : foundDeviceList) {
                    password = Constants.passwordList.get(device.getAddress().replace(":", ""));
                    if (password != null) {
                        scanner.endScan();
                        getConnectionReady(device);
                    }
                }
            }
        });

    }

    void getConnectionReady(BluetoothDevice device) {
        bleDevice = device;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Looper.myLooper() == Looper.getMainLooper())
                    connectToDevice();
            }
        });

    }

    private void connectToDevice() {
        bleGatt = bleDevice.connectGatt(getActivity(), false, new BluetoothGattCallback() {
            final byte[] lockFullStatus = new byte[3];
            int descWriteCount = 0;
            BluetoothGattCharacteristic charRxType, charRxBuffer, charRxCtrl, charTxType, charTxBuffer;

            int seqLB = -1;
            int delay = 20;

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);

                bleGatt = gatt;
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    Log.w("ALARM_LOCK", "=ignoring connection state event with status, status=" + status);
                    return;
                }
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    logBuilder.append("\nDevice Connected");

                    restartTimer(Constants.EVENT_TIMEOUT);

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
                    logBuilder.append("\nService Discovered.");
                    bleGatt = gatt;
                    BluetoothGattService service = gatt.getService(Constants.UUID.SERVICE_ALARM_LOCK_DATA);
                    charRxType = service.getCharacteristic(Constants.UUID.CHARACTERISTIC_DATA_RX_TYPE);
                    charRxBuffer = service.getCharacteristic(Constants.UUID.CHARACTERISTIC_DATA_RX_BUFFER);
                    charRxCtrl = service.getCharacteristic(Constants.UUID.CHARACTERISTIC_DATA_RX_CTRL);
                    charTxType = service.getCharacteristic(Constants.UUID.CHARACTERISTIC_DATA_TX_TYPE);
                    charTxBuffer = service.getCharacteristic(Constants.UUID.CHARACTERISTIC_DATA_TX_BUFFER);
                    logBuilder.append("\n6 Characteristics are found.");

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
                    logBuilder.append("\nEnabled notification on TT/TB/RC");

                    restartTimer(Constants.EVENT_TIMEOUT);
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                bleGatt = gatt;
                byte[] data = characteristic.getValue();
                if (characteristic.equals(charRxCtrl)) {
//                    Log.v("LockDetailsRxCtrl", "=" + Integer.toHexString(data[0]) + " " + Integer.toHexString(data[1]) + "  " + Integer.toHexString(data[2]));
                } else if (characteristic.equals(charTxType)) {
                    logBuilder.append("\nTxType notified with " + Integer.toHexString(data[0]) + "|" + Integer.toHexString(data[1]) + "|" + Integer.toHexString(data[2]));
                    restartTimer(Constants.EVENT_TIMEOUT);
//                    Log.v("LockDetailsTxType", "=" + Integer.toHexString(data[0]) + " " + Integer.toHexString(data[1]) + "  " + Integer.toHexString(data[2]));
                    lockFullStatus[0] = data[0];
                    seqLB = data[1];
                } else if (characteristic.equals(charTxBuffer)) {
                    logBuilder.append("\nTxBuffer notified with " + Integer.toHexString(data[0]) + "|" + Integer.toHexString(data[1]) + "|" + Integer.toHexString(data[2]));

//                    Log.v("LockDetailsTxBuffer", "=" + Integer.toHexString(data[0]) + " " + Integer.toHexString(data[1]) + "  " + Integer.toHexString(data[2]));
                    lockFullStatus[1] = data[0];
                    lockFullStatus[2] = data[1];

//                    Log.v("Battery", "=" + appDatabaseAdapter.updateBattery(lock._id + "", data[3]));

                    //TODO
                    if (lockFullStatus[0] == DataType.LockStatus.getValue() && lockFullStatus[1] == CommState.StateReady.getValue() && lockFullStatus[2] == LockedState.Locked.getValue()) {

                        byte writeRxType[] = new byte[3];
                        writeRxType[0] = DataType.Password.getValue();
                        writeRxType[1] = (byte) (seqLB + 1);

                        charRxType.setValue(writeRxType);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.v("WriteRxType", "=" + bleGatt.writeCharacteristic(charRxType));
                            }
                        });
                    }
                    if (data[1] == LockedState.UnlockedPassageMode.getValue() || data[1] == LockedState.UnlockedPassTime.getValue()) {
                        isUnlocking = false;
                        isUnlocked = true;
                        updateUnlockUI();
                        startTimedLock();

                    }
                }

            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    bleGatt = gatt;
                    if (characteristic.equals(charRxType)) {
                        Log.v("LockDetailsRxType", "write");
                        byte[] writableRxBuffer = lock.lockPassword.getBytes();
                        if (writableRxBuffer.length <= 20) {
                            charRxBuffer.setValue(writableRxBuffer);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.v("WriteRxBuffer3", "=" + bleGatt.writeCharacteristic(charRxBuffer));
                                }
                            });
                        }

                    } else if (characteristic.equals(charRxBuffer)) {
                        Log.v("LockDetailsRxBuffer", "=" + "called");
                    }
                }

            }
        });
    }

    private void restartTimer(int delayTime) {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(timedLockOperation, delayTime);
    }

    void updateUnlockUI() {
        if (getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disposeGatt();

                ibLockStat.setImageResource(R.drawable.ic_unlocked);
                btnStatSwitcher.setText("Unlocked");
            }
        });
    }

    private void startTimedLock() {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(timedLockOperation, Constants.LOCK_TIMEOUT);
    }

    private void lock() {
        if (!isUnlocked)
            disposeGatt();
        isUnlocking = false;
        isUnlocked = false;

        ibLockStat.setImageResource(R.drawable.ic_locked);
        btnStatSwitcher.setText("Unlock");
        btnStatSwitcher.setClickable(true);
    }

    private void disposeGatt() {
        if (bleGatt != null) {
            bleGatt.disconnect();
            bleGatt.close();
            bleGatt = null;
        }

    }

}
