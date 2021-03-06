package com.assylias.jbloomberg;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

@Test(groups = "requires-bloomberg")
public class InstrumentListResultParserTest {

    private DefaultBloombergSession session = null;

    @BeforeClass
    public void beforeClass() throws BloombergException {
        session = new DefaultBloombergSession();
        session.start();
    }

    @AfterClass
    public void afterClass() throws BloombergException {
        if (session != null) {
            session.stop();
        }
    }

    @Test
    public void testParse_OneInvalidSecurity() throws Exception {
        InstrumentListRequestBuilder ilrb = new InstrumentListRequestBuilder("NOT_A_REAL_TICKER");
        final InstrumentList instrumentList = session.submit(ilrb).get(2, TimeUnit.SECONDS);
        assertTrue(instrumentList.get().isEmpty());
    }

    @Test
    public void testParse_OneValidSecurity() throws Exception {
        InstrumentListRequestBuilder ilrb = new InstrumentListRequestBuilder("IBM US");
        final InstrumentList instrumentList = session.submit(ilrb).get(2, TimeUnit.SECONDS);
        final List<InstrumentList.Instrument> instruments = instrumentList.get();
//         INTL BUSINESS MACHINES CORP
        assertFalse(instruments.isEmpty());
        assertTrue(instruments.stream().anyMatch(instrument -> instrument.getSecurity().equalsIgnoreCase("IBM US<equity>")));
    }

    @Test
    public void testLimitsNumberOfResponses() throws Exception {
        InstrumentListRequestBuilder ilrb = new InstrumentListRequestBuilder("", 10);
        final InstrumentList instrumentList = session.submit(ilrb).get(2, TimeUnit.SECONDS);
        assertEquals(instrumentList.get().size(), 10);
    }

    @Test
    public void testParse_WithYellowKeyFilterAndLanguageOverride() throws Exception {
        InstrumentListRequestBuilder ilrb = new InstrumentListRequestBuilder("123", 10)
                .withYellowKeyFilter(InstrumentListRequestBuilder.YellowKeyFilter.Equity)
                .withLanguageOverride(InstrumentListRequestBuilder.LanguageOverride.Kanji);
        final InstrumentList instrumentList = session.submit(ilrb).get(2, TimeUnit.SECONDS);
        assertFalse(instrumentList.get().isEmpty());
      System.out.println(instrumentList.get());
    }

}
