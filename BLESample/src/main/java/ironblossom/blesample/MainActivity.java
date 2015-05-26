package ironblossom.blesample;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends ActionBarActivity implements BluetoothAdapter.LeScanCallback {

    TextView textView;
    private final ArrayList<BluetoothDevice> bleDevices = new ArrayList<BluetoothDevice>();

    private static final int REQUEST_ENABLE_BT = 0x22;
    private static final UUID TRANSFER_SERVICE_UUID = UUID.fromString("E20A39F4-73F5-4BC4-A12F-17D1AD07A961");
    private static final UUID LOCK_SERVICE_UUID = UUID.fromString("EA86E3BD-3057-4A7A-BA30-343B61780604");
    private static final UUID LOCK_STATUS_CHARACTERISTIC_UUID = UUID.fromString("AA8B66DD-ADBA-4D03-9A49-C7A8304BBD72");

    private BluetoothAdapter mBluetoothAdapter;
    String address;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        @SuppressWarnings("ResourceType")
        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        //initViews
        textView = (TextView) findViewById(R.id.textView);


    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    public void enableBluetooth(View view) {
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            finish();
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @SuppressWarnings("deprecation")
    public void scanLeDevices(View view) {
        bleDevices.clear();
        mBluetoothAdapter.startLeScan(new UUID[]{/*TRANSFER_SERVICE_UUID, */LOCK_SERVICE_UUID}, this);
    }

    public void stopScanLeDevices(View view) {
        mBluetoothAdapter.stopLeScan(this);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (!bleDevices.contains(device)) {
            bleDevices.add(device);
            textView.setText("Device Name:" + device.getName() + "\n" + "Address:" + device.getAddress());
            address = device.getAddress();
            mBluetoothAdapter.stopLeScan(this);
            bindService(new Intent(this, BLEService.class), new ServiceConnection() {

                @Override
                public void onServiceConnected(ComponentName componentName, IBinder service) {
                    mBLEService = ((BLEService.LocalBinder) service).getService();
                    if (!mBLEService.initialize()) {
                        Log.e("BLEService", "Unable to initialize Bluetooth");
                        finish();
                    }
                    // Automatically connects to the device upon successful start-up initialization.
                    Log.v("isCon","="+mBLEService.connect(bleDevices.get(0).getAddress()));

                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    mBLEService = null;
                }
            }, BIND_AUTO_CREATE);
            registerReceiver(mGattUpdateReceiver,makeGattUpdateIntentFilter());
        }

    }


    public void connectToDevice(View view) {
//        bleDevices.get(0).connectGatt(this, true, bluetoothGattCallback);
    }

    private BLEService mBLEService;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBLEService = ((BLEService.LocalBinder) service).getService();
            if (!mBLEService.initialize()) {
                Log.e("BLEService", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBLEService.connect(bleDevices.get(0).getAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBLEService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
              /*  mConnected = true;
                updateConnectionState(R.string.connected);*/
            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                /*mConnected = false;
                updateConnectionState(R.string.disconnected);
                clearUI();*/
            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
//                displayGattServices(mBLEService.getSupportedGattServices());
            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
                textView.setText(intent.getStringExtra(BLEService.EXTRA_DATA));
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
