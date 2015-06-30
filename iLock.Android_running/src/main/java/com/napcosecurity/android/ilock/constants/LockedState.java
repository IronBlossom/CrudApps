package com.napcosecurity.android.ilock.constants;

/**
 * Created by
 * Ishtiaq Mahmood Amin (from imamin)
 * imamin@ael-bd.com
 * on 5/8/2015 at 7:05 PM.
 * Copyright (c) 2015 Napco Security Technologies, Inc. All rights reserved.
 */
public enum LockedState {
    Locked((byte) 0x33), UnlockedPassTime((byte) 0x34), UnlockedPassageMode((byte) 0x35);

    private final byte lockedState;

    LockedState(byte lockedState) {
        this.lockedState = lockedState;
    }

    public byte getValue() {
        return lockedState;
    }
}
