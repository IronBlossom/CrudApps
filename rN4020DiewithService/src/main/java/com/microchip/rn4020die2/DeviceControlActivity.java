/*
 * Copyright (C) 2014 Microchip Technology Inc. and its subsidiaries.  You may use this software and any derivatives
 * exclusively with Microchip products.
 *
 * THIS SOFTWARE IS SUPPLIED BY MICROCHIP "AS IS".  NO WARRANTIES, WHETHER EXPRESS, IMPLIED OR STATUTORY, APPLY TO THIS
 * SOFTWARE, INCLUDING ANY IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY, AND FITNESS FOR A PARTICULAR
 * PURPOSE, OR ITS INTERACTION WITH MICROCHIP PRODUCTS, COMBINATION WITH ANY OTHER PRODUCTS, OR USE IN ANY APPLICATION. 
 *
 * IN NO EVENT WILL MICROCHIP BE LIABLE FOR ANY INDIRECT, SPECIAL, PUNITIVE, INCIDENTAL OR CONSEQUENTIAL LOSS, DAMAGE,
 * COST OR EXPENSE OF ANY KIND WHATSOEVER RELATED TO THE SOFTWARE, HOWEVER CAUSED, EVEN IF MICROCHIP HAS BEEN ADVISED OF
 * THE POSSIBILITY OR THE DAMAGES ARE FORESEEABLE.  TO THE FULLEST EXTENT ALLOWED BY LAW, MICROCHIP'S TOTAL LIABILITY ON
 * ALL CLAIMS IN ANY WAY RELATED TO THIS SOFTWARE WILL NOT EXCEED THE AMOUNT OF FEES, IF ANY, THAT YOU HAVE PAID
 * DIRECTLY TO MICROCHIP FOR THIS SOFTWARE.
 *
 * This file includes code modified from "The Android Open Source Project" copyright (C) 2013.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * MICROCHIP PROVIDES THIS SOFTWARE CONDITIONALLY UPON YOUR ACCEPTANCE OF THESE TERMS. 
 */

package com.microchip.rn4020die2;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * For a given BLE device, this Activity provides the user interface to connect,
 * display data, and display GATT services and characteristics supported by the
 * device. The Activity communicates with {@code BluetoothLeService}, which in
 * turn interacts with the Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";                      //Name passed by intent that lanched this activity
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";                //MAC address passed by intent that lanched this activity
    public static final String MLDP_DATA_PRIVATE_CHAR = "00035b03-58e6-07dd-021a-08123a000301"; //Characteristic for MLDP Data, properties - notify, write
    public static final String CHARACTERISTIC_NOTIFICATION_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";    //Special UUID for descriptor needed to enable notifications
    private final static String TAG = DeviceControlActivity.class.getSimpleName();      //Get name of activity to tag debug and warning messages
    private static final String MLDP_PRIVATE_SERVICE = "00035b03-58e6-07dd-021a-08123a000300"; //Private service for Microchip MLDP
    private static final String MLDP_CONTROL_PRIVATE_CHAR = "00035b03-58e6-07dd-021a-08123a0003ff"; //Characteristic for MLDP Control, properties - read, write
    private BluetoothLeService mBluetoothLeService;                                     //Service to handle BluetoothGatt connection to the RN4020 module
    // ----------------------------------------------------------------------------------------------------------------
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {        //Create new ServiceConnection interface to handle connection and disconnection

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {    //Service connects
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService(); //Get a link to the service
            if (!mBluetoothLeService.initialize()) {                                    //See if the service did not initialize properly
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();                                                                //End the application
            }
            mBluetoothLeService.connect(mDeviceAddress);                                //Connects to the device selected and passed to us by the DeviceScanActivity
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {                //Service disconnects
            mBluetoothLeService = null;                                                 //Not bound to a service
        }
    };
    private BluetoothGattCharacteristic mDataMDLP, mControlMLDP;                        //The BLE characteristic used for MLDP data transfers
    private Handler mHandler;                                                           //Handler used to send die roll after a time delay
    private TextView mConnectionState, redDieText;                                      //TextViews to show connection state and die roll number on the display
    private Button redDieButton;                                                        //Button to initiate a roll of the die
    private String incomingMessage;                                                     //String to hold the incoming message from the MLDP characteristic
    private String mDeviceName, mDeviceAddress;                                         //Strings for the Bluetooth device name and MAC address
    private boolean mConnected = false;                                                 //Indicator of an active Bluetooth connection
    private Die redDie;                                                                 //Die object for rolling a number from 1 to 6
    // ----------------------------------------------------------------------------------------------------------------
    // BroadcastReceiver handles various events fired by the BluetoothLeService service.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();                                   //Get the action that was broadcast by the intent that was received

            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {              //Service has connected to BLE device
                mConnected = true;                                                      //Record the new connection state
                updateConnectionState(R.string.connected);                              //Update the display to say "Connected"
                invalidateOptionsMenu();                                                //Force the Options menu to be regenerated to show the disconnect option
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {        //Service has disconnected from BLE device
                mConnected = false;                                                     //Record the new connection state
                updateConnectionState(R.string.disconnected);                           //Update the display to say "Disconnected"
                invalidateOptionsMenu();                                                //Force the Options menu to be regenerated to show the connect option
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) { //Service has discovered GATT services on BLE device
                findMldpGattService(mBluetoothLeService.getSupportedGattServices());    //Show all the supported services and characteristics on the user interface
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {         //Service has found new data available on BLE device
                String dataValue = intent.getStringExtra(BluetoothLeService.EXTRA_DATA); //Get the value of the characteristic
                processIncomingPacket(dataValue);                                       //Process the data that was received
            }

            //For information only. This application sends small packets infrequently and does not need to know what the previous write completed
            else if (BluetoothLeService.ACTION_DATA_WRITTEN.equals(action)) {            //Service has found new data available on BLE device
            }
        }
    };
    // ----------------------------------------------------------------------------------------------------------------
    // Listener for the roll red die button
    private final Button.OnClickListener redDieButtonClickListener = new Button.OnClickListener() {

        public void onClick(View view) {                                                //Button was clicked
            updateDieState();                                                           //Update the state of the die with a new roll and send over BLE
        }
    };
    // ----------------------------------------------------------------------------------------------------------------
    // Listener for the red die text
    private final TextView.OnClickListener redDieTextClickListener = new TextView.OnClickListener() {

        public void onClick(View view) {                                                //Die text was clicked
            updateDieState();                                                           //Update the state of the die with a new roll and send over BLE
        }
    };

    // ----------------------------------------------------------------------------------------------------------------
    // Intent filter to add Intent values that will be broadcast by the BluetoothLeService to the mGattUpdateReceiver BroadcastReceiver
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();                           //Create intent filter for actions received by broadcast receiver
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_WRITTEN);
        return intentFilter;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Activity launched
    // Invoked by Intent in onListItemClick method in DeviceScanActivity
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.die_screen);                                            //Show the screen with the die number and button

        final Intent intent = getIntent();                                              //Get the Intent that launched this activity
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);                        //Get the BLE device name from the Intent
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);                  //Get the BLE device address from the Intent
        mHandler = new Handler();                                                       //Create Handler to delay sending first roll after new connection

        ((TextView) findViewById(R.id.deviceAddress)).setText(mDeviceAddress);          //Display device address on the screen
        mConnectionState = (TextView) findViewById(R.id.connectionState);               //TextView that will display the connection state

        redDie = new Die();                                                             //Create a new Die
        redDieText = (TextView) findViewById(R.id.textRedDie);                          //TextView that will display the roll of the die
        redDieText.setLayerType(View.LAYER_TYPE_SOFTWARE, null);                        //Hardware acceleration does not have cache large enough for huge fonts
        redDieText.setOnClickListener(redDieTextClickListener);                         //Set onClickListener for when text is pressed
        redDieButton = (Button) findViewById(R.id.buttonRedDie);                        //Button that will roll the die when clicked
        redDieButton.setOnClickListener(redDieButtonClickListener);                     //Set onClickListener for when button is pressed

        incomingMessage = new String();                                                 //Create new string to hold incoming message data

        this.getActionBar().setTitle(mDeviceName);                                      //Set the title of the ActionBar to the name of the BLE device
        this.getActionBar().setDisplayHomeAsUpEnabled(true);                            //Make home icon clickable with < symbol on the left

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);          //Create Intent to start the BluetoothLeService
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);           //Create and bind the new service to mServiceConnection object that handles service connect and disconnect
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Activity resumed
    // Register the GATT receiver
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());            //Register broadcast receiver to handles events fired by the service: connected, disconnected, etc.
        if (mBluetoothLeService != null) {                                              //Check that service is running
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);         //Ask the service to connect to the GATT server hosted on the Bluetooth LE device
            Log.d(TAG, "Connect request result = " + result);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Activity paused
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);                                        //Activity paused so unregister the broadcast receiver
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Activity is ending
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);                                              //Activity ending so unbind the service (this will end the service if no other activities are bound to it)
        mBluetoothLeService = null;                                                     //Not bound to a service
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Options menu is different depending on whether connected or not
    // Show Connect option if not connected or show Disconnect option if we are connected
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);                          //Show the Options menu
        if (mConnected) {                                                               //See if connected
            menu.findItem(R.id.menu_connect).setVisible(false);                         // then dont show disconnect option
            menu.findItem(R.id.menu_disconnect).setVisible(true);                       // and do show connect option
        } else {                                                                          //If not connected
            menu.findItem(R.id.menu_connect).setVisible(true);                          // then show connect option
            menu.findItem(R.id.menu_disconnect).setVisible(false);                      // and don't show disconnect option
        }
        return true;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Menu item selected
    // Connect or disconnect to BLE device
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {                                                     //Get which menu item was selected
            case R.id.menu_connect:                                                     //Option to Connect chosen
                mBluetoothLeService.connect(mDeviceAddress);                            //Call method to connect to our selected device
                return true;
            case R.id.menu_disconnect:                                                  //Option to Disconnect chosen
                mBluetoothLeService.disconnect();                                       //Call method to disconnect
                return true;
            case android.R.id.home:                                                     //Option to go back was chosen
                onBackPressed();                                                        //Execute functionality of back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Update text with connection state
    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);                                   //Update text to say "Connected" or "Disconnected"
                redDieText.setText(null);                                               //Reset die text to blank when connection changes
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Update text roll of die and send over Bluetooth
    private void updateDieState() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDataMDLP.setValue("=>R" + redDie.Roll() + "\r\n");                     //Set value of MLDP characteristic to send die roll information
                mBluetoothLeService.writeCharacteristic(mDataMDLP);                     //Call method to write the characteristic
                redDieText.setText(Byte.toString(redDie.View()));                       //Set the die text to show the last die roll
                int height = redDieText.getHeight();                                    //Get the available height for the text object
                redDieText.setTextSize(TypedValue.COMPLEX_UNIT_PX, (height * 4 / 5));   //Set the size of the text to take up 80% available space
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Look for message with switch pressed indicator "->S1\n\r"
    private void processIncomingPacket(String data) {
        char switchState;
        int indexStart, indexEnd;
        incomingMessage = incomingMessage.concat(data);                                 //Add the new data to what is left of previous data
        if (incomingMessage.length() >= 6 && incomingMessage.contains("=>S") && incomingMessage.contains("\r\n")) { //See if we have the right nessage
            indexStart = incomingMessage.indexOf("=>S");                                //Get the position of the matching characters
            indexEnd = incomingMessage.indexOf("\r\n");                                 //Get the position of the end of frame "\r\n"
            if (indexEnd - indexStart == 4) {                                           //Check that the packet does not have missing or extra characters
                switchState = incomingMessage.charAt(indexStart + 3);                   //Get the character that represents the switch being pressed
                if (switchState == '1') {                                               //Is it a "1"
                    updateDieState();                                                   // if so then update the state of the die with a new roll and send over BLE
                }
            }
            incomingMessage = incomingMessage.substring(indexEnd + 2);                  //Thow away everything up to and including "\n\r"
        } else if (incomingMessage.contains("\r\n")) {                                    //See if we have an end of frame "\r\n" without a valid message
            incomingMessage = incomingMessage.substring(incomingMessage.indexOf("\r\n") + 2); //Thow away everything up to and including "\n\r"
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Iterate through the supported GATT Services/Characteristics to see if the MLDP srevice is supported
    private void findMldpGattService(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {                                                     //Verify that list of GATT services is valid
            Log.d(TAG, "findMldpGattService found no Services");
            return;
        }
        String uuid;                                                                    //String to compare received UUID with desired known UUIDs
        mDataMDLP = null;                                                               //Searching for a characteristic, start with null value

        for (BluetoothGattService gattService : gattServices) {                         //Test each service in the list of services
            uuid = gattService.getUuid().toString();                                    //Get the string version of the service's UUID
            if (uuid.equals(MLDP_PRIVATE_SERVICE)) {                                    //See if it matches the UUID of the MLDP service
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics(); //If so then get the service's list of characteristics
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) { //Test each characteristic in the list of characteristics
                    uuid = gattCharacteristic.getUuid().toString();                     //Get the string version of the characteristic's UUID
                    if (uuid.equals(MLDP_DATA_PRIVATE_CHAR)) {                          //See if it matches the UUID of the MLDP data characteristic
                        mDataMDLP = gattCharacteristic;                                 //If so then save the reference to the characteristic 
                        Log.d(TAG, "Found MLDP data characteristics");
                    } else if (uuid.equals(MLDP_CONTROL_PRIVATE_CHAR)) {                  //See if UUID matches the UUID of the MLDP control characteristic
                        mControlMLDP = gattCharacteristic;                              //If so then save the reference to the characteristic 
                        Log.d(TAG, "Found MLDP control characteristics");
                    }
                    final int characteristicProperties = gattCharacteristic.getProperties(); //Get the properties of the characteristic
                    if ((characteristicProperties & (BluetoothGattCharacteristic.PROPERTY_NOTIFY)) > 0) { //See if the characteristic has the Notify property
                        mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true); //If so then enable notification in the BluetoothGatt
                    }
                    if ((characteristicProperties & (BluetoothGattCharacteristic.PROPERTY_INDICATE)) > 0) { //See if the characteristic has the Indicate property
                        mBluetoothLeService.setCharacteristicIndication(gattCharacteristic, true); //If so then enable notification (and indication) in the BluetoothGatt
                    }
                    if ((characteristicProperties & (BluetoothGattCharacteristic.PROPERTY_WRITE)) > 0) { //See if the characteristic has the Write (acknowledged) property
                        gattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT); //If so then set the write type (write with acknowledge) in the BluetoothGatt
                    }
                    if ((characteristicProperties & (BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0) { //See if the characteristic has the Write (unacknowledged) property
                        gattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE); //If so then set the write type (write with no acknowledge) in the BluetoothGatt
                    }
                }
                break;
            }
        }
        if (mDataMDLP == null) {                                                        //See if the MLDP data characteristic was not found
            Toast.makeText(this, R.string.mldp_not_supported, Toast.LENGTH_SHORT).show(); //If so then show an error message
            Log.d(TAG, "findMldpGattService found no MLDP service");
            finish();                                                                   //and end the activity
        }
        mHandler.postDelayed(new Runnable() {                                           //Create delayed runnable that will send a roll of the die after a delay
            @Override
            public void run() {
                updateDieState();                                                       //Update the state of the die with a new roll and send over BLE
            }
        }, 200);                                                                        //Do it after 200ms delay to give the RN4020 time to configure the characteristic

    }

}
