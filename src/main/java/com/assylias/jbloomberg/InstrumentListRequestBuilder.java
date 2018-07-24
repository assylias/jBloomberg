package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Request;
import com.google.common.base.Preconditions;

/**
 * This class enables to build a InstrumentList request while ensuring argument safety. Typically, instead of passing
 * strings arguments (and typos) as with the standard Bloomberg API, the possible options used to override the behaviour
 * of the query have been wrapped in enums or relevant primitive types.
 * <p>
 * All methods, including the constructors, throw NullPointerException when null arguments are passed in.
 * <p>
 * Once the request has been built, the InstrumentListRequestBuilder can be submitted to a BloombergSession.
 * <p>
 * <b>This class is not thread safe.</b>
 */
public class InstrumentListRequestBuilder extends AbstractRequestBuilder<InstrumentList> {
    //Required parameters
    private final String query;
    private final int maxResults;
    //Optional parameters
    private YellowKeyFilter yellowKeyFilter = null;
    private LanguageOverride languageOverride = null;

    public InstrumentListRequestBuilder(final String query) {
        this(query, 10);
    }

    public InstrumentListRequestBuilder(final String query, final int maxResults) {
        this.query = Preconditions.checkNotNull(query);
        this.maxResults = maxResults;
    }

    public InstrumentListRequestBuilder withYellowKeyFilter(final YellowKeyFilter yellowKeyFilter) {
        this.yellowKeyFilter = Preconditions.checkNotNull(yellowKeyFilter);
        return this;
    }

    public InstrumentListRequestBuilder withLanguageOverride(final LanguageOverride languageOverride) {
        this.languageOverride = Preconditions.checkNotNull(languageOverride);
        return this;
    }

    @Override
    public BloombergServiceType getServiceType() {
        return BloombergServiceType.INSTRUMENTS;
    }

    @Override
    public BloombergRequestType getRequestType() {
        return BloombergRequestType.INSTRUMENT_LIST;
    }

    @Override
    protected void buildRequest(final Request request) {
        request.set("query", query);
        request.set("maxResults", maxResults);
        if (yellowKeyFilter != null) {
            request.set("yellowKeyFilter", yellowKeyFilter.filter);
        }
        if (languageOverride != null) {
            request.set("languageOverride", languageOverride.override);
        }
    }

    @Override
    public ResultParser<InstrumentList> getResultParser() {
        return new InstrumentListResultParser();
    }

    @Override
    public String toString() {
        return "InstrumentListRequestBuilder{query='" + query + '\'' + ", maxResults=" + maxResults + '}';
    }

    public enum YellowKeyFilter {
        None("YK_FILTER_NONE"),
        Comdty("YK_FILTER_CMDT"),
        Equity("YK_FILTER_EQTY"),
        Muni("YK_FILTER_MUNI"),
        Pfd("YK_FILTER_PRFD"),
        Client("YK_FILTER_CLNT"),
        MMkt("YK_FILTER_MMKT"),
        Govt("YK_FILTER_GOVT"),
        Corp("YK_FILTER_CORP"),
        Index("YK_FILTER_INDX"),
        Curncy("YK_FILTER_CURR"),
        Mtge("YK_FILTER_MTGE");

        private final String filter;

        YellowKeyFilter(final String filter) {
            this.filter = filter;
        }
    }

    public enum LanguageOverride {
        None("LANG_OVERRIDE_NONE"),
        English("LANG_OVERRIDE_ENGLISH"),
        Kanji("LANG_OVERRIDE_KANJI"),
        French("LANG_OVERRIDE_FRENCH"),
        German("LANG_OVERRIDE_GERMAN"),
        Spanish("LANG_OVERRIDE_SPANISH"),
        Portugese("LANG_OVERRIDE_PORTUGUESE"),
        Italian("LANG_OVERRIDE_ITALIAN"),
        ChineseTraditional("LANG_OVERRIDE_CHINESE_TRAD"),
        Korean("LANG_OVERRIDE_KOREAN"),
        ChineseSimplified("LANG_OVERRIDE_CHINESE_SIMP"),
        Russian("LANG_OVERRIDE_RUSSIAN");

        private final String override;

        LanguageOverride(final String override) {
            this.override = override;
        }
    }
}
