package com.napcosecurity.android.ilock.constants;

/**
 * Created by
 * Ishtiaq Mahmood Amin (from imamin)
 * imamin@ael-bd.com
 * on 5/8/2015 at 7:10 PM.
 * Copyright (c) 2015 Napco Security Technologies, Inc. All rights reserved.
 */
public enum OperationState {
    ProgramMode((byte) 0x36), NormalOperation((byte) 0x37);
    private final byte operationState;

    OperationState(byte operationState) {
        this.operationState = operationState;
    }
}
