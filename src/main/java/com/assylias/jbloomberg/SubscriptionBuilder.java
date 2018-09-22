/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Identity;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;

/**
 * A SubscriptionBuilder is used to build real time streaming subscription requests.
 *
 * <strong>This class is not thread safe.</strong>
 */
public final class SubscriptionBuilder {

    private final Set<DataChangeListener> dataListeners = new HashSet<>();
    private final Set<String> securities = new HashSet<> ();
    private final Set<RealtimeField> fields = EnumSet.noneOf(RealtimeField.class);
    private Identity identity = null;
    private SubscriptionErrorListener errorListener = e-> { /* no-op */ };
    private double throttle = 0;

    BloombergServiceType getServiceType() {
        return BloombergServiceType.MARKET_DATA;
    }

    /**
     * Adds a listener that will be informed of any changes to the registered fields / securities.
     *
     * @param lst a listener
     * @throws NullPointerException if lst is null
     */
    public SubscriptionBuilder addListener(DataChangeListener lst) {
        requireNonNull(lst, "lst can't be null");
        dataListeners.add(lst);
        return this;
    }

    /**
     * Adds a listener that will be informed of any errors received after the subscription has started.
     *
     * @param lst a listener
     * @throws NullPointerException if lst is null
     */
    public SubscriptionBuilder onError(SubscriptionErrorListener lst) {
        requireNonNull(lst, "lst can't be null");
        errorListener = lst;
        return this;
    }

    /**
     * Adds the specified security to the list of securities that will be monitored in real time.
     *
     * @param security a security's ID (ticker)
     * @throws NullPointerException if security is null
     */
    public SubscriptionBuilder addSecurity(String security) {
        return addSecurities(Arrays.asList(security));
    }

    /**
     * Adds those securities to the list of securities that will be monitored in real time.
     *
     * @param securities a collection of security IDs (tickers)
     * @throws NullPointerException if securities is null or contains null
     */
    public SubscriptionBuilder addSecurities(Collection<String> securities) {
        requireNonNull(securities, "securities can't be null");
        if (securities.contains(null)) {
            throw new NullPointerException("securities can't contain null");
        }
        Preconditions.checkArgument(!securities.contains(""), "securities can't contain empty strings");
        this.securities.addAll(securities);
        return this;
    }

    /**
     * Adds field to the list of fields that will be monitored in real time.
     *
     * @param field a field to monitor
     * @throws NullPointerException if field is null
     */
    public SubscriptionBuilder addField(RealtimeField field) {
        return addFields(Arrays.asList(field));
    }

    /**
     * Adds fields to the list of fields that will be monitored in real time.
     *
     * @param fields the list of fields to monitor
     * @throws NullPointerException if fields is null or contains null
     */
    public SubscriptionBuilder addFields(Collection<RealtimeField> fields) {
        requireNonNull(fields, "fields can't be null");
        if (fields.contains(null)) {
            throw new NullPointerException("fields can't contain null");
        }
        this.fields.addAll(fields);
        return this;
    }

    /**
     * Specify the {@link Identity} used when subscribing
     *
     * @param identity Identity to use when making API requests
     */
    public SubscriptionBuilder withIdentity(Identity identity) {
        this.identity = identity;
        return this;
    }

    /**
     * Throttles the real time data feed. This is useful to reduce bandwidth usage or CPU activity. <br>
     * If the feed is throttled, each event that is received will be a snapshot at the time of the refresh. If the
     * throttle is not set, all updates will be received. If the argument is 0, no throttle is applied.
     *
     * @param throttleFrequency the maximum frequency at which data is updated, in seconds.
     *
     * @throws IllegalArgumentException if throttle is not 0 or between 0.1 and 86,400.
     */
    public SubscriptionBuilder throttle(double throttleFrequency) {
        Preconditions.checkArgument(throttleFrequency >= 0.1 || throttleFrequency == 0, "frequency must be 0 or >= 0.1 (was " + throttleFrequency + ")");
        Preconditions.checkArgument(throttleFrequency <= 86400, "frequency must be <= 86,400 (was " + throttleFrequency + ")");
        throttle = throttleFrequency;
        return this;
    }

    Set<DataChangeListener> getListeners() {
        return ImmutableSet.copyOf(dataListeners);
    }

    SubscriptionErrorListener getErrorListener() {
        return errorListener;
    }

    Set<String> getSecurities() {
        return ImmutableSet.copyOf(securities);
    }

    Set<RealtimeField> getFields() {
        return Sets.immutableEnumSet(fields);
    }

    public Identity getIdentity() {
        return identity;
    }

    Set<String> getFieldsAsString() {
        ImmutableSet.Builder<String> set = ImmutableSet.builder();
        for (RealtimeField f : fields) {set.add(f.toString());}
        return set.build();
    }

    double getThrottle() {
        return throttle;
    }
}
