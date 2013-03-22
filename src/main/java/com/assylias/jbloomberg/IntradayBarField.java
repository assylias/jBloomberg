/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import java.util.HashMap;
import java.util.Map;

/**
 * A list of the fields available when requesting IntradayBar data.
 */
public enum IntradayBarField {

    TIME("time"),
    OPEN("open"),
    HIGH("high"),
    LOW("low"),
    CLOSE("close"),
    VOLUME("volume"),
    NUM_EVENTS("numEvents");

    private final static Map<String, IntradayBarField> map = new HashMap<>(IntradayBarField.values().length, 1);

    static {
        for (IntradayBarField f : IntradayBarField.values()) {
            map.put(f.code, f);
        }
    }

    static IntradayBarField of(String field) {
        IntradayBarField f = map.get(field);
        if (f == null) throw new IllegalArgumentException("Not a valid field: " + field);
        else return f;
    }

    private String code;
    private IntradayBarField(String code) {
        this.code = code;
    }
}
