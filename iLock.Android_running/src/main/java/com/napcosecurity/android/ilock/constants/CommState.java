package com.napcosecurity.android.ilock.constants;

/**
 * Created by
 * Ishtiaq Mahmood Amin (from imamin)
 * imamin@ael-bd.com
 * on 5/8/2015 at 5:03 PM.
 * Copyright (c) 2015 Napco Security Technologies, Inc. All rights reserved.
 */
public enum CommState {
    StateReady((byte) 0x30), StateBusy((byte) 0x31), StateDenied((byte) 0x32);
    private final byte commState;

    CommState(byte commState) {
        this.commState = commState;
    }

    public byte getValue() {
        return commState;
    }
}
