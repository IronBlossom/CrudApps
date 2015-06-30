package com.napcosecurity.android.ilock.constants;

/**
 * Created by
 * Ishtiaq Mahmood Amin (from imamin)
 * imamin@ael-bd.com
 * on 5/8/2015 at 7:11 PM.
 * Copyright (c) 2015 Napco Security Technologies, Inc. All rights reserved.
 */
public enum SystemCommand {
    BeginDEP((byte) 0x20), StatusRequest((byte) 0x21), EndOfTransmission((byte) 0x22);
    private final byte systemCommand;

    SystemCommand(byte systemCommand) {
        this.systemCommand = systemCommand;
    }

    public byte getValue() {
        return systemCommand;
    }
}
