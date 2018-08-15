/*
 * Copyright (C) 2016 - present by Yann Le Tallec.
 * Please see distribution for license.
 */

package com.assylias.jbloomberg;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = "requires-bloomberg")
public class Issue17 {
  DefaultBloombergSession session = new DefaultBloombergSession();

  @BeforeMethod public void start() throws BloombergException {
    session.start();
  }

  @AfterMethod public void stop() {
    session.stop();
  }

  /**
   * When subscribing to an expired ticker twice, the second subscription sends an exception
   */
  @Test
  public void test() throws Exception {
    CountDownLatch errorLatch = new CountDownLatch(1);
    CountDownLatch dataLatch = new CountDownLatch(1);
    SubscriptionBuilder b = new SubscriptionBuilder()
            .addSecurity("VGZ14 Index")
            .addField(RealtimeField.LAST_PRICE)
            .onError(e -> errorLatch.countDown())
            .addListener(e -> dataLatch.countDown());
    session.subscribe(b);
    assertTrue(errorLatch.await(50, SECONDS));
    assertEquals(dataLatch.getCount(), 1L);
    session.subscribe(new SubscriptionBuilder().addSecurity("VGZ14 Index").addField(RealtimeField.LAST_PRICE));
  }
}
