package com.assylias.jbloomberg;

import org.assertj.core.api.Condition;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;
import static org.assertj.core.api.Assertions.*;

public class InstrumentListResultParserTest {

    private DefaultBloombergSession session = null;

    @BeforeClass(groups = "requires-bloomberg")
    public void beforeClass() throws BloombergException {
        session = new DefaultBloombergSession();
        session.start();
    }

    @AfterClass(groups = "requires-bloomberg")
    public void afterClass() throws BloombergException {
        if (session != null) {
            session.stop();
        }
    }

    @Test(groups = "requires-bloomberg")
    public void testParse_OneInvalidSecurity() throws Exception {
        InstrumentListRequestBuilder ilrb = new InstrumentListRequestBuilder("NOT_A_REAL_TICKER");
        final InstrumentList instrumentList = session.submit(ilrb).get(2, TimeUnit.SECONDS);
        assertThat(instrumentList.get()).isEmpty();
    }

    @Test(groups = "requires-bloomberg")
    public void testParse_OneValidSecurity() throws Exception {
        InstrumentListRequestBuilder ilrb = new InstrumentListRequestBuilder("IBM US");
        final InstrumentList instrumentList = session.submit(ilrb).get(2, TimeUnit.SECONDS);
        final List<InstrumentList.Instrument> instruments = instrumentList.get();
//         INTL BUSINESS MACHINES CORP
        assertThat(instruments).isNotEmpty();
        assertThat(instruments).anyMatch((instrument) -> {
            return instrument.getSecurity().equalsIgnoreCase("IBM US<equity>");
        });
    }

    @Test(groups = "requires-bloomberg")
    public void testLimitsNumberOfResponses() throws Exception {
        InstrumentListRequestBuilder ilrb = new InstrumentListRequestBuilder("", 10);
        final InstrumentList instrumentList = session.submit(ilrb).get(2, TimeUnit.SECONDS);
        assertThat(instrumentList.get()).hasSize(10);
    }

    @Test(groups = "requires-bloomberg")
    public void testParse_WithYellowKeyFilterAndLanguageOverride() throws Exception {
        InstrumentListRequestBuilder ilrb = new InstrumentListRequestBuilder("123", 10)
                .withYellowKeyFilter(InstrumentListRequestBuilder.YellowKeyFilter.Equity)
                .withLanguageOverride(InstrumentListRequestBuilder.LanguageOverride.Kanji);
        final InstrumentList instrumentList = session.submit(ilrb).get(2, TimeUnit.SECONDS);
        assertThat(instrumentList.get()).isNotEmpty();
    }

}
