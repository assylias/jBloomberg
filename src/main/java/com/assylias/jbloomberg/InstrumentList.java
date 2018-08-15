package com.assylias.jbloomberg;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.LinkedList;
import java.util.List;

/**
 * A class that represents the result returned by a Bloomberg InstrumentList request.
 * This implementation exposes a {@link ImmutableList} containing {@link Instrument} objects representing the matched
 * tickers.
 */
public class InstrumentList extends AbstractRequestResult {
    private final List<Instrument> data = new LinkedList<>();

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    synchronized void add(final String security, final String description) {
        data.add(new Instrument(security, description));
    }

    public List<Instrument> get() {
        return ImmutableList.copyOf(data);
    }

    public static final class Instrument {
        private final String security;
        private final String description;

        private Instrument(String security, String description) {
            this.security = Preconditions.checkNotNull(security);
            this.description = Preconditions.checkNotNull(description);
        }

        public String getSecurity() {
            return security;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "Instrument{" +
                    "security='" + security + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }
}
