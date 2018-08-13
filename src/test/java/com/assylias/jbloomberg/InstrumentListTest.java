package com.assylias.jbloomberg;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertTrue;

@Test(groups = "unit")
public class InstrumentListTest {

    private InstrumentList data;

    @BeforeMethod
    public void beforeMethod() {
        data = new InstrumentList();
        data.add("IBM", "IBM Inc");
        data.add("MSFT", "Microsoft");
    }

    @Test
    public void testIsEmpty() {
        assertTrue(new InstrumentList().isEmpty());
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testResultIsImmutable() {
        final List<InstrumentList.Instrument> instruments = data.get();
        instruments.remove(0);
    }
}
