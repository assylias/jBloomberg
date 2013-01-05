/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */

package assylias.jbloomberg;

/**
 * jBloomberg is a high-level API that wraps the low level Bloomberg API. Although most features of the
 * underlying Bloomberg API are available, some options might not be reachable through the jBloomberg API.
 * <p>
 * A typical use is to start a BloombergSession and start submitting static or historical requests or subscribe to real
 * time updates. Once the session is not in use any longer, it can be stopped. Note that in the below examples, sessions
 * are started then stopped right after a request has been sent. In practice, starting a session can be a time consuming
 * operation and once a session has been opened, it is preferable to reuse it unless it is not going to be used for a
 * long time.
 * <p>
 * <b> Example: submit a request to retrieve historical data synchronously</b>
 * <pre> {@code
 * BloombergSession session = new DefaultBloombergSession();
 * session.start();
 * HistoricalRequestBuilder builder = new HistoricalRequestBuilder("SPX Index",
 *         "PX_LAST", DateTime.now().minusDays(7),
 *         DateTime.now())
 *         .fill(HistoricalRequestBuilder.Fill.NIL_VALUE)
 *         .days(HistoricalRequestBuilder.Days.ALL_CALENDAR_DAYS);
 * RequestResult result = session.submit(builder).get(); //one should check that no ExecutionException is thrown here
 * Map<DateTime, Object> data = result.forSecurity("SPX Index").forField("PX_LAST").get();
 * for (Map.Entry<DateTime, Object> e : data.entrySet()) {
 *     DateTime dt = e.getKey();
 *     double price = (Double) e.getValue();
 *     System.out.println("[" + dt + "] " + price);
 * }
 * session.stop();
 * } </pre>
 *
 * <b> Example: subscribe to real time price updates </b>
 * <pre> {@code
 * BloombergSession session = new DefaultBloombergSession();
 * session.start();
 * DataChangeListener lst = new DataChangeListener() {
 *     public void dataChanged(DataChangeEvent e) {
 *         System.out.println(e);
 *     }
 * };
 * SubscriptionBuilder builder = new SubscriptionBuilder()
 *         .addSecurity("ESA Index")
 *         .addField(RealtimeField.LAST_PRICE)
 *         .addField(RealtimeField.ASK)
 *         .addField(RealtimeField.ASK_SIZE)
 *         .addListener(lst);
 * session.subscribe(builder);
 * Thread.sleep(3000);
 * session.stop();
 * } </pre>
 */