package com.assylias.jbloomberg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

@Test(groups = "unit")
public class PortfolioRequestBuilderTest {
  @Test(expectedExceptions = NullPointerException.class)
  public void testConstructor_NullTickers() {
    new PortfolioRequestBuilder((Collection<String>) null, Arrays.asList("a"));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testConstructor_TickersContainsNull() {
    new PortfolioRequestBuilder(Arrays.asList((String) null), Arrays.asList("a"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty.*")
  public void testConstructor_EmptyTickers() {
    new PortfolioRequestBuilder(Collections.emptyList(), Arrays.asList("a"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty\\sstrings.*")
  public void testConstructor_TickersContainsEmptyString() {
    new PortfolioRequestBuilder(Arrays.asList(""), Arrays.asList("a"));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testConstructor_NullFields() {
    new PortfolioRequestBuilder(Arrays.asList("a"), (Collection<String>) null);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testConstructor_FieldsContainsNull() {
    new PortfolioRequestBuilder(Arrays.asList("a"), Arrays.asList((String) null));
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty.*")
  public void testConstructor_EmptyFields() {
    new PortfolioRequestBuilder(Arrays.asList("a"), Collections.emptyList());
  }

  @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*empty\\sstrings.*")
  public void testConstructor_FieldsContainsEmptyString() {
    new PortfolioRequestBuilder(Arrays.asList("a"), Arrays.asList(""));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testOverride_Null() {
    PortfolioRequestBuilder builder = new PortfolioRequestBuilder("IBM US Equity", "PX_LAST");
    builder.at(null);
  }

  @Test
  public void testConstructor_AllOk() {
    new PortfolioRequestBuilder("IBM US Equity", "PX_LAST").at(LocalDate.of(2000, 1, 1));
  }

  @Test
  public void testServiceType() {
    assertEquals(new PortfolioRequestBuilder("ABC", "DEF").getServiceType(), BloombergServiceType.REFERENCE_DATA);
  }

  @Test
  public void testRequestType() {
    assertEquals(new PortfolioRequestBuilder("ABC", "DEF").getRequestType(), BloombergRequestType.PORTFOLIO_DATA);
  }
}

@Test(groups = "requires-bloomberg")
class PortfolioRequestParserTest {

  private static final Logger logger = LoggerFactory.getLogger(PortfolioRequestParserTest.class);

  /**
   * This test logs a warning message unless there is a Bloomberg portfolio with the given id.
   * To create such a portfolio: {@code PRTU <Go>}, create a portfolio and its ticker will appear at the top of the screen showing the portfolio constituents.
   */
  @Test public void parse() throws ExecutionException, InterruptedException {
    BloombergSession session = new DefaultBloombergSession();
    try {
      session.start();
      String portfolioId = "U12554798-1 Client";
      String field = "PORTFOLIO_MWEIGHT";
      RequestBuilder<ReferenceData> b = new PortfolioRequestBuilder(portfolioId, field);
      ReferenceData bbData = session.submit(b).get();
      if (bbData.hasErrors()) {
        logger.warn("The portfolio request returned no result because there is no portfolio with ID={}", portfolioId);
      } else {
        List<TypedObject> result = bbData.forField(field).forSecurity(portfolioId).asList();
        assertFalse(result.isEmpty());
      }
    } finally {
      session.stop();
    }
  }
}