package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;

import java.util.function.Supplier;

final class StubResultParser<T extends AbstractRequestResult> extends AbstractResultParser<T> {
    private final Supplier<T> factory;

    StubResultParser(final Supplier<T> factory) {
        this.factory = factory;
    }

    @Override
    protected T getRequestResult() {
        return factory.get();
    }

    @Override protected void parseResponseNoError(Element response, T result) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
