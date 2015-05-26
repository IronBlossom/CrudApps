package ironblossom.bleclient;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


public class MainActivity extends ActionBarActivity {
    //Flags
    private static final UUID LOCK_SERVICE_UUID = UUID.fromString("EA86E3BD-3057-4A7A-BA30-343B61780604");
    private static final UUID LOCK_STATUS_CHARACTERISTIC_UUID = UUID.fromString("AA8B66DD-ADBA-4D03-9A49-C7A8304BBD72");
    private static final int REQ_ENABLE_BT = 0x05;
    //Views
    private ImageView ivStatus;
    private TextView tvStatus;
    private ImageButton ibKey;
    //Utils
    private BluetoothAdapter mBluetoothAdapter;
    private boolean isScanning = false;
    private String deviceAddress;
    @NotNull
    private BluetoothGattCallback mBlGattCallBack = new BluetoothGattCallback() {
        @Nullable
        BluetoothGattCharacteristic mBlGattCharacteristic = null;

        @Override
        public void onConnectionStateChange(@NotNull BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.i("Connected", "Device");
                gatt.discoverServices();
            }
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.i("Disconnected", "Device");
                startScan();
            }
        }

        @Override
        public void onServicesDiscovered(@NotNull BluetoothGatt gatt, int status) {
            BluetoothGattService mService = gatt.getService(LOCK_SERVICE_UUID);
            BluetoothGattCharacteristic mCharacteristic = mService.getCharacteristic(LOCK_STATUS_CHARACTERISTIC_UUID);
            gatt.readCharacteristic(mCharacteristic);
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, @NotNull BluetoothGattCharacteristic characteristic, int status) {
            Log.v("1", "2");
            @NotNull String val = new String(characteristic.getValue());
            Log.v("Read", "Char=" + val);
        }
    };
    @NotNull
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(@NotNull BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (rssi < -15 && rssi > -45) {
                deviceAddress = device.getAddress();
                device.connectGatt(MainActivity.this, true, mBlGattCallBack);
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initBl();
    }

    private void initViews() {
        ivStatus = (ImageView) findViewById(R.id.ivStatus);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        ibKey = (ImageButton) findViewById(R.id.ibKey);
    }

    private void initBl() {

        @SuppressWarnings("ResourceType")
        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Device doesn't have bluetooth low energy", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't have bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled())
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQ_ENABLE_BT);
        else
            startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQ_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Bluetooth needs to be enabled", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else if (requestCode == REQ_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            startScan();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startScan() {
        isScanning = true;
        mBluetoothAdapter.startLeScan(new UUID[]{LOCK_SERVICE_UUID}, mLeScanCallback);
    }

    private void stopScan() {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }
}
