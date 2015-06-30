package com.napcosecurity.android.ilock.ui.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.napcosecurity.android.ilock.R;
import com.napcosecurity.android.ilock.ble.DEPGattCallback;
import com.napcosecurity.android.ilock.ble.DeviceScanner;
import com.napcosecurity.android.ilock.ble.OnDeviceScanComplete;
import com.napcosecurity.android.ilock.ble.OnNewLockEnrolled;
import com.napcosecurity.android.ilock.constants.Constants;
import com.napcosecurity.android.ilock.db.AppDatabaseAdapter;
import com.napcosecurity.android.ilock.db.AppDatabaseIntegrator;
import com.napcosecurity.android.ilock.lock.Lock;

import java.util.ArrayList;
import java.util.List;

public class LockListActivity extends BaseActivity implements OnNewLockEnrolled, AppDatabaseIntegrator {
    //Views
    ListView lvLockList;
    //General Api
    List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    String lockName, lockMac;
    //Bluetooth Api
    BluetoothAdapter btAdapter;
    BluetoothGatt btGatt;
    DeviceScanner scanner;
    //Database
    AppDatabaseAdapter db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        bluetoothDeviceList.clear();
        setContentView(R.layout.activity_lock_list);

        initViews();

        requestDb(this);//DBConnect
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
            initBTBasics();
        else
            Toast.makeText(this, "Device doesn't support bluetooth low energy", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void initViews() {
        lvLockList = (ListView) findViewById(R.id.lvLockList);
    }

    private void initBTBasics() {
        final BluetoothManager btMan = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        btAdapter = btMan.getAdapter();
        scanner = new DeviceScanner(btAdapter);

        if (btAdapter == null || !btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constants.REQUEST_CODE_BLUETOOTH);
        } else
            startScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == Constants.REQUEST_CODE_BLUETOOTH) {
            startScan();
        } else {
            Toast.makeText(this, "Bluetooth must be enabled, for BLE", Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void startScan() {
        scanner.createScanCallback();
        String serviceUUIDStr = Constants.UUID.SERVICE_ALARM_LOCK_DATA.toString();
        scanner.beginScan(serviceUUIDStr, new OnDeviceScanComplete() {
            @Override
            public void onComplete(List<BluetoothDevice> foundDeviceList) {
                bluetoothDeviceList = foundDeviceList;
                populateListView();
            }
        });
    }

    private void populateListView() {
        final List<String> bluetoothDeviceNameList = new ArrayList<>();
        for (BluetoothDevice bluetoothDevice : bluetoothDeviceList)
            bluetoothDeviceNameList.add(bluetoothDevice.getName());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lvLockList.setAdapter(new ArrayAdapter<String>(
                        LockListActivity.this,
                        android.R.layout.simple_list_item_1,
                        bluetoothDeviceNameList));
                lvLockList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        scanner.endScan();
                        connectToDevice(bluetoothDeviceList.get(position));
                    }
                });
            }
        });
    }

    private void changeActionBarTitle(final String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSupportActionBar().setTitle(title);

            }
        });
    }

    private void connectToDevice(BluetoothDevice btDevice) {
        changeActionBarTitle("Connecting..");
       /* *//*if (db.isLockEnrolled(btDevice.getAddress())) {
            finish();
            return;
        }*//*
        // btGatt = btDevice.connectGatt(LockListActivity.this, false, new DEPGattCallback(this));
        // btGatt = btDevice.connectGatt(LockListActivity.this, false, new NewDEPGattCallback(true,this,new Lock(),false));
        lockName = btDevice.getName();
        lockMac = btDevice.getAddress();
        NewDEPGattCallback._2nd = true;*/
        btGatt = btDevice.connectGatt(LockListActivity.this, false, new DEPGattCallback(this, new Lock())/*new BatteryServiceCallback(this)*/);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanner != null)
            scanner.endScan();
    }


    @Override
    public void onAppDatabaseIntegrated(AppDatabaseAdapter db) {
        this.db = db;
    }

    @Override
    public void onEnroll(Lock lock) {

        setLockDefaultValues(lock);
        db.insertLock(lock.lockName, lock.lockPassword, lock.lockMac, lock.lockId, lock.lockSlot, lock.lockSettingsSavePassword, lock.lockSettingsKeypad, lock.lockSettingsOperateUnattended, lock.lockSettingsUnattendedTimeoutPosition,lock.lockBatteryPercent);

        Intent intent = new Intent(this, LockPagerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finish();
        startActivity(intent);
    }

    private void setLockDefaultValues(Lock lock) {
        lock.lockSettingsSavePassword = 1;
        lock.lockSettingsKeypad = 0;
        lock.lockSettingsOperateUnattended = 0;
        lock.lockSettingsUnattendedTimeoutPosition = 2;
    }
}
