package com.napcosecurity.android.ilock.constants;

/**
 * Created by
 * Ishtiaq Mahmood Amin (from imamin)
 * imamin@ael-bd.com
 * on 5/8/2015 at 4:58 PM.
 * Copyright (c) 2015 Napco Security Technologies, Inc. All rights reserved.
 */
public enum DataCTRL {
    ACK((byte) (0x06)),
    NACK((byte) 0x15);
    private final byte dataCtrl;

    DataCTRL(byte dataCtrl) {
        this.dataCtrl = dataCtrl;
    }

    public byte getValue() {
        return dataCtrl;
    }
}
