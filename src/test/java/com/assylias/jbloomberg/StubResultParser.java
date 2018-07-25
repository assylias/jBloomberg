package com.assylias.jbloomberg;

import java.util.function.Supplier;

final class StubResultParser<T extends AbstractRequestResult> extends AbstractResultParser<T> {
    private final Supplier<T> factory;

    StubResultParser(final Supplier<T> factory) {
        super((res, response) -> {
            throw new UnsupportedOperationException("Not supported yet.");
        });
        this.factory = factory;
    }

    @Override
    protected T getRequestResult() {
        return factory.get();
    }
}
