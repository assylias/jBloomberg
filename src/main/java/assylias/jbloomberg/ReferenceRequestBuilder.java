/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Request;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class enables to build a ReferenceData request while ensuring argument safety. Typically, instead of passing
 * strings arguments (and typos) as with the standard Bloomberg API, the possible options used to override the behaviour
 * of the query have been wrapped in enums or relevant primitive types.
 * <p/>
 * All methods, including the constructors, throw NullPointerException when null arguments are passed in.
 * <p/>
 * Once the request has been built, the ReferenceRequestBuilder can be submitted to a BloombergSession.
 * <p/>
 * <b>This class is not thread safe.</b>
 */
public final class ReferenceRequestBuilder extends AbstractRequestBuilder<ReferenceData> {

    //Required parameters
    private final Set<String> tickers = new HashSet<>();
    private final Set<String> fields = new HashSet<>();
    //Optional parameters
    private final Map<String, String> overrides = new HashMap<>();

    /**
     * Equivalent to calling
     * <code> new ReferenceRequestBuilder(Arrays.asList(ticker), Arrays.asList(field));
     * </code>
     */
    public ReferenceRequestBuilder(String ticker, String field) {
        this(Arrays.asList(ticker), Arrays.asList(field));
    }

    /**
     * Equivalent to calling
     * <code> new ReferenceRequestBuilder(Arrays.asList(ticker), fields);
     * </code>
     */
    public ReferenceRequestBuilder(String ticker, Collection<String> fields) {
        this(Arrays.asList(ticker), fields);
    }

    /**
     * Equivalent to calling
     * <code> new ReferenceRequestBuilder(tickers, Arrays.asList(field));
     * </code>
     */
    public ReferenceRequestBuilder(Collection<String> tickers, String field) {
        this(tickers, Arrays.asList(field));
    }

    /**
     * Creates a ReferenceRequestBuilder. The fields parameters can be overriden with the provided methods.
     * <p/>
     * @param tickers a collection of tickers for which data needs to be retrieved - tickers must be valid Bloomberg
     *                symbols (for example: IBM US Equity)
     * @param fields  a collection of Bloomberg fields to retrieve for each ticker
     * <p/>
     * @throws NullPointerException     if any of the parameters is null or if the collections contain null items
     * @throws IllegalArgumentException if any of the collections is empty or contains empty strings
     */
    public ReferenceRequestBuilder(Collection<String> tickers, Collection<String> fields) {
        Preconditions.checkNotNull(tickers, "The collection of tickers cannot be null");
        Preconditions.checkNotNull(fields, "The collection of fields cannot be null");
        Preconditions.checkArgument(!tickers.isEmpty(), "The list of tickers must not be empty");
        Preconditions.checkArgument(!fields.isEmpty(), "The list of fields must not be empty");
        Preconditions.checkArgument(!tickers.contains(""), "The list of tickers must not contain empty strings");
        Preconditions.checkArgument(!fields.contains(""), "The list of fields must not contain empty strings");

        this.tickers.addAll(tickers);
        this.fields.addAll(fields);
    }

    public ReferenceRequestBuilder addOverride(String field, String value) {
        Preconditions.checkNotNull(field, "Field cannot be null when adding overrides");
        Preconditions.checkNotNull(value, "Value cannot be null when adding overrides");
        Preconditions.checkArgument(!field.isEmpty(), "Field cannot be empty when adding overrides");
        Preconditions.checkArgument(!value.isEmpty(), "Value cannot be empty when adding overrides");
        overrides.put(field, value);
        return this;
    }

    @Override
    public BloombergServiceType getServiceType() {
        return BloombergServiceType.REFERENCE_DATA;
    }

    @Override
    public BloombergRequestType getRequestType() {
        return BloombergRequestType.REFERENCE_DATA;
    }

    @Override
    protected void buildRequest(Request request) {
        addCollectionToElement(request, tickers, "securities");
        addCollectionToElement(request, fields, "fields");

        Element overridesElt = request.getElement("overrides");
        for (Map.Entry<String, String> e : overrides.entrySet()) {
            Element override = overridesElt.appendElement();
            override.setElement("fieldId", e.getKey());
            override.setElement("value", e.getValue());
        }
    }

    @Override
    public ResultParser<ReferenceData> getResultParser() {
        return new ReferenceResultParser();
    }
}
