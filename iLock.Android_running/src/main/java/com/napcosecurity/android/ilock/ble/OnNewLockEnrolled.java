package com.napcosecurity.android.ilock.ble;

import com.napcosecurity.android.ilock.lock.Lock;

/**
 * Created by
 * Ishtiaq Mahmood Amin (from imamin)
 * imamin@ael-bd.com
 * on 5/26/2015 at 9:22 PM.
 * Copyright (c) 2015 Napco Security Technologies, Inc. All rights reserved.
 */
public interface OnNewLockEnrolled {
    void onEnroll(Lock lock);
}
