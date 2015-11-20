package com.example.imamin.ipcwithmessenger;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends Activity {
    public static final String TAG = "IPCMsg";
    public static final int MSG_FROM_CLIENT = 0x7777;
    public static final int MSG_FROM_CLIENT_STOP_LOOPING = 0x7121;
    SharedPreferences sharedPreferences;
    private Messenger remoteServiceMessenger;
    private boolean isBound = false;
    private static boolean isLooping = false;
    int FAILED_ATTEMPT = 0;
    @Bind(R.id.lvLock)
    public ListView lvLockList;
    @Bind(R.id.swLoop)
    public Switch swLoop;
    @Bind(R.id.btnUnlock)
    public Button btnUnlock;
    @Bind(R.id.btnStopLooping)
    public Button btnLooping;
    @Bind(R.id.rootView)
    public LinearLayout rootLinearLayout;
    private BluetoothAdapter btAdapter;
    private DeviceScanner scanner;

    private static final class IncomingServiceMessageHandler extends Handler {
        private final WeakReference<MainActivity> activityWeakReference;

        public IncomingServiceMessageHandler(MainActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final MainActivity activity = activityWeakReference.get();
            if(activity!=null){
                switch (msg.what) {
                    case MyRemoteService.MSG_FROM_SERVICE:
                        //TODO
                        break;
                    case MyRemoteService.MSG_FROM_SERVICE_STATUS:
                        Bundle data = msg.getData();
                        activity.sharedPreferences.edit().putBoolean("activityServiceRunning", ((boolean) data.get("serviceRunning"))).apply();
                        break;
                    case MyRemoteService.MSG_FROM_SERVICE_UNLOCKED:
                        activity.rootLinearLayout.setBackgroundColor(Color.GREEN);
                        break;
                    case MyRemoteService.MSG_FROM_SERVICE_DISCONNECTED:
                        if (activity.isBound) {
                            activity.unbindService(activity.remoteServiceConnection);
                            activity.isBound = false;
                        }
                        activity.rootLinearLayout.setBackgroundColor(Color.RED);
                    /*activity.handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            activity.btnUnlock.performClick();
                        }
                    }, 2000);*/
                        break;
                    case MyRemoteService.MSG_FROM_SERVICE_FAILED:
                        activity.FAILED_ATTEMPT++;
//                    activity.tvStatus.setText("" + activity.FAILED_ATTEMPT);
                        break;
                    default:
                        break;
                }
            }

            super.handleMessage(msg);
        }

        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
        }
    }

    private final Messenger incomingHandlerMessenger = new Messenger(new IncomingServiceMessageHandler(this));

    ServiceConnection remoteServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v(TAG, "Service Connected");
            try {
                remoteServiceMessenger = new Messenger(service);
                Message msg = Message.obtain(null, MyRemoteService.REPLY_TO_THIS_MESSENGER);
                msg.replyTo = incomingHandlerMessenger;

                remoteServiceMessenger.send(msg);
                isBound = true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }


            if (!isBound) return;
            // Create and send a message to the service, using a supported 'what' value

            if (!isLooping) {
                Bundle bundle = new Bundle();
                bundle.putString("pass", password);
                bundle.putString("Mac", Mac);
                bundle.putBoolean("loop", (swLoop.isChecked()));

                isLooping=swLoop.isChecked();

                Message msg = Message.obtain(null, MSG_FROM_CLIENT, 0, 0);
                msg.setData(bundle);
                try {
                    remoteServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.v(TAG, "Service Disconnected");
            remoteServiceMessenger = null;
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        initBTBasics();

        swLoop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                btnLooping.setEnabled(isChecked);
                btnLooping.setClickable(isChecked);

            }
        });

        btnLooping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLooping=false;
                swLoop.setChecked(false);
                sharedPreferences.edit().putBoolean("activityIsLooping", false).apply();
                Bundle bundle = new Bundle();
                bundle.putBoolean("loop", false);
                Message msg = Message.obtain(null, MSG_FROM_CLIENT_STOP_LOOPING, 0, 0);
                msg.setData(bundle);
                try {
                    remoteServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        if (!sharedPreferences.getBoolean("activityServiceRunning", false)) {
            startService(new Intent(this, MyRemoteService.class));
            sharedPreferences.edit().putBoolean("activityServiceRunning", true).apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLooping) {
            swLoop.setChecked(true);
            btnLooping.setEnabled(true);
            btnLooping.setClickable(true);


            bindService(new Intent(MainActivity.this, MyRemoteService.class), remoteServiceConnection,
                    Context.BIND_AUTO_CREATE);

        } else {

            swLoop.setChecked(false);
            btnLooping.setEnabled(false);
            btnLooping.setClickable(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();/*
        if (isBound)
            unbindService(remoteServiceConnection);*/
    }

    private void initBTBasics() {
        final BluetoothManager btMan = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        btAdapter = btMan.getAdapter();
        scanner = new DeviceScanner(btAdapter);

        if (btAdapter == null || !btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0x1122);
        } else {
            //startScan();
        }

    }

    List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();

    public void startScan() {
        isScanning = true;
        btnUnlock.setEnabled(false);
        btnUnlock.setClickable(false);
        scanner = new DeviceScanner(btAdapter);
        scanner.createScanCallback();
        scanner.beginScan("EE88DBB3-5EE0-4ED5-BE48-0F520407EC74", new OnDeviceScanComplete() {
            @Override
            public void onComplete(List<BluetoothDevice> foundDeviceList) {
                bluetoothDeviceList = foundDeviceList;
                populateListView();
            }
        });

        invalidateOptionsMenu();
    }

    private void populateListView() {
        final List<String> bluetoothDeviceNameList = new ArrayList<>();
        for (BluetoothDevice bluetoothDevice : bluetoothDeviceList) {
            bluetoothDeviceNameList.add(bluetoothDevice.getName() + "--" + bluetoothDevice.getAddress());
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_single_choice, bluetoothDeviceNameList);
                lvLockList.setAdapter(adapter);
                lvLockList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Mac = bluetoothDeviceNameList.get(position).split("--")[1];

                        isScanning = false;
                        invalidateOptionsMenu();
                        scanner.endScan();

                        btnUnlock.setEnabled(true);
                        btnUnlock.setClickable(true);
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);

        if (isScanning) {
            menu.getItem(0).setTitle("Stop");
        } else {
            menu.getItem(0).setTitle("Scan");
        }
        return true;
    }

    boolean isScanning = false;

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (item.getItemId() == R.id.mnScan) {
            if (isScanning) {
                item.setTitle("Start");
                isScanning = false;
                scanner.endScan();
            } else {
                item.setTitle("Stop");
                startScan();

            }
        }
        return true;
    }/*

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service

    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        *//*if (isBound) {
            unbindService(remoteServiceConnection);
            isBound = false;
        }*//*
    }*/

    String password;
    String Mac;

    public void unlock(View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Give Password");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                password = input.getText().toString();
                isBound = true;
                MainActivity.this.bindService(new Intent(MainActivity.this, MyRemoteService.class), remoteServiceConnection,
                        Context.BIND_AUTO_CREATE);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();


    }
}
