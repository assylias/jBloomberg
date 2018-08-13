package com.assylias.jbloomberg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author yletallec
 */
public class BloombergSearchRequestBuilderTest {
  private static final Logger logger = LoggerFactory.getLogger(BloombergSearchRequestBuilderTest.class);

  /**
   * This test logs a warning message unless there is a Bloomberg search names "TEST-SRCH" that returns a non empty list of tickers.
   * To create such a search: {@code SRCH <Go>} and create a search called "TEST-SRCH".
   */
  @Test public void test() throws ExecutionException, InterruptedException {
    BloombergSession bb = new DefaultBloombergSession();
    try {
      bb.start();
      RequestBuilder<BloombergSearchData> b = new BloombergSearchRequestBuilder("FI:TEST-SRCH").maxSecurities(2);
      Set<String> data = bb.submit(b).get().get();
      //Here we don't check the result as it may be empty if no search called TEST-SRCH exist or it returns an empty list.
      //So we'll just log a warning if the result is empty.
      if (data.isEmpty()) logger.warn("The search returned no result, probably because no search called TEST-SRCH exist");
    } finally {
      bb.stop();
    }
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_null_domain() {
    new BloombergSearchRequestBuilder(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_limit_zero() {
    new BloombergSearchRequestBuilder("").maxSecurities(0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_limit_negative() {
    new BloombergSearchRequestBuilder("").maxSecurities(-10);
  }
}