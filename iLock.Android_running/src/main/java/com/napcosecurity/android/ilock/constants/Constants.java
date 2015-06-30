package com.napcosecurity.android.ilock.constants;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by
 * Ishtiaq Mahmood Amin (from imamin)
 * imamin@ael-bd.com
 * on 5/8/2015 at 5:21 PM.
 * Copyright (c) 2015 Napco Security Technologies, Inc. All rights reserved.
 */
public class Constants {
    public static final int REQUEST_CODE_BLUETOOTH = 0x00ff;
    public static final Map<String, String> passwordList = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public static final int LOCK_TIMEOUT = 4000;
    public static final int EVENT_TIMEOUT = 5000;

    public static class UUID {
        //SERVICE
        public static final java.util.UUID SERVICE_DEVICE_INFO = java.util.UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB");
        public static final java.util.UUID SERVICE_BATTERY_REPORTING = java.util.UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB");
        public static final java.util.UUID SERVICE_ALARM_LOCK_DATA = java.util.UUID.fromString("EE88DBB3-5EE0-4ED5-BE48-0F520407EC74");
        //CHARACTERISTIC
        public static final java.util.UUID CHARACTERISTIC_DATA_TX_TYPE = java.util.UUID.fromString("BA67C742-3E49-4E0F-AD0D-141E0020F2A2");
        public static final java.util.UUID CHARACTERISTIC_DATA_TX_BUFFER = java.util.UUID.fromString("A8C993EB-1322-4A8E-9CB1-4501B9AE539B");
        public static final java.util.UUID CHARACTERISTIC_DATA_TX_CTRL = java.util.UUID.fromString("BCD6678B-55D0-47BC-AD27-EDB6EC455255");
        public static final java.util.UUID CHARACTERISTIC_DATA_RX_TYPE = java.util.UUID.fromString("74102D3D-0300-4E1B-ABB3-2D491AC12833");
        public static final java.util.UUID CHARACTERISTIC_DATA_RX_BUFFER = java.util.UUID.fromString("305B0556-78FC-4885-9567-E64C78D06467");
        public static final java.util.UUID CHARACTERISTIC_DATA_RX_CTRL = java.util.UUID.fromString("588AB4F2-612C-40B5-8169-83A13B632520");
        //DESCRIPTOR
        public static final java.util.UUID DESCRIPTOR_PRE_CLIENT_CONFIG = java.util.UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");

    }
}
