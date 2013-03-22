/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Datetime;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event.EventType;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.Name;
import com.bloomberglp.blpapi.Service;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import mockit.Mock;
import mockit.MockUp;

public class MockMessage extends Message {
    private String type;
    private String toString;
    private CorrelationID cId;

    public MockMessage setMessageType(String type) {
        this.type = type;
        return this;
    }

    public MockMessage setToString(String s) {
        this.toString = s;
        return this;
    }

    public MockMessage setCorrelationID(CorrelationID cId) {
        this.cId = cId;
        return this;
    }

    public MockMessage setCorrelationID(long cId) {
        this.cId = new CorrelationID(cId);
        return this;
    }

    @Override
    public Name messageType() {
        return new Name(type);
    }

    @Override
    public String topicName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Service service() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Fragment fragmentType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CorrelationID correlationID() {
        return cId;
    }

    @Override
    public CorrelationID correlationIDAt(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CorrelationID correlationID(int index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int numCorrelationIds() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Element asElement() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int numElements() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isValid() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasElement(Name name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasElement(Name name, boolean excludeNullElements) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasElement(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasElement(String name, boolean excludeNullElements) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Element getElement(Name name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Element getElement(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getElementAsBool(Name name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getElementAsBool(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getElementAsBytes(Name name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getElementAsBytes(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public char getElementAsChar(Name name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public char getElementAsChar(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getElementAsInt32(Name name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getElementAsInt32(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getElementAsInt64(Name name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getElementAsInt64(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getElementAsFloat64(Name name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getElementAsFloat64(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getElementAsFloat32(Name name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getElementAsFloat32(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getElementAsString(Name name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getElementAsString(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Datetime getElementAsDatetime(Name name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Datetime getElementAsDatetime(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Datetime getElementAsDate(Name name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Datetime getElementAsDate(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Datetime getElementAsTime(Name name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Datetime getElementAsTime(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Name getElementAsName(Name name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Name getElementAsName(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
        return toString;
    }

    @Override
    public void print(OutputStream output) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void print(Writer writer) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
