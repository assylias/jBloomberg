/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.assylias.jbloomberg.BloombergUtils;
import mockit.Mock;
import mockit.MockUp;

public class MockBloombergUtils extends MockUp<BloombergUtils> {

    private final boolean started;

    public MockBloombergUtils(boolean started) {
        super();
        this.started = started;
    }

    @Mock
    public boolean startBloombergProcessIfNecessary() {
        return this.started;
    }
}
