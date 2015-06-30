package com.napcosecurity.android.ilock.constants;

/**
 * Created by
 * Ishtiaq Mahmood Amin (from imamin)
 * imamin@ael-bd.com
 * on 5/8/2015 at 4:49 PM.
 * Copyright (c) 2015 Napco Security Technologies, Inc. All rights reserved.
 */
public enum DataType {
    DeviceMACaddress((byte) 0x10), EnrollmentData((byte) 0x11),
    LockStatus((byte) 0x12), Command((byte) 0x13),
    KeyData((byte) 0x14), EncryptionKeyData((byte) 0x15),
    Password((byte) 0x16);

    private final byte dataType;

    DataType(final byte dataType) {
        this.dataType = dataType;
    }

    public byte getValue(){
        return dataType;
    }
}
