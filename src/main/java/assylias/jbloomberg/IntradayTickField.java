/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import java.util.HashMap;
import java.util.Map;

/**
 * A list of the fields available when requesting IntradayTick data.
 */
public enum IntradayTickField {

    TIME("time"),
    TYPE("type"),
    VALUE("value"),
    SIZE("size"),
    CONDITION_CODE("conditionCode"),
    EXCHANGE_CODE("exchangeCode"),
    MIC_CODE("micCode"),
    BROKER_BUY_CODE("brokerBuyCode"),
    BROKER_SELL_CODE("brokerSellCode"),
    RPS_CODE("rpsCode");

    private final static Map<String, IntradayTickField> map = new HashMap<>(IntradayTickField.values().length, 1);

    static {
        for (IntradayTickField f : IntradayTickField.values()) {
            map.put(f.code, f);
        }
    }

    static IntradayTickField of(String field) {
        IntradayTickField f = map.get(field);
        if (f == null) throw new IllegalArgumentException("Not a valid field: " + field);
        else return f;
    }
    private String code;

    private IntradayTickField(String code) {
        this.code = code;
    }
}
