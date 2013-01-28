/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
/**
 * jBloomberg is a high-level API that wraps the low level Bloomberg API. Although most features of the
 * underlying Bloomberg API are available, some options might not be reachable through the jBloomberg API.
 * <p>
 * A typical use is to start a BloombergSession and to submit static or historical requests or to subscribe to real
 * time updates. Once the session is not in use any longer, it can be stopped. Note that in the below examples, sessions
 * are started then stopped right after a request has been sent. In practice, starting a session can be a time consuming
 * operation and once a session has been opened, it is preferable to reuse it unless it is not going to be used for a
 * long time.
 * <p>
 *
 * <b> Example: start and stop a session</b>
 * <pre> {@code
 * BloombergSession session = new DefaultBloombergSession();
 * session.start();
 * session.stop();
 * }</pre>
 *
 * <b> Example: retrieve the last price of the S&P 500 in Yen</b>
 * <pre> {@code
 * RequestBuilder<ReferenceData> hrb = new ReferenceRequestBuilder(
 *         "SPX Index", "CRNCY_ADJ_PX_LAST")
 *         .addOverride("EQY_FUND_CRNCY", "JPY");
 * ReferenceData result = session.submit(hrb).get();
 * double priceInYen = (double) result.forSecurity("SPX Index").forField("CRNCY_ADJ_PX_LAST");
 * System.out.println("SPX in Yen = " + priceInYen);
 * }</pre>
 *
 * <b> Example: retrieve historical data synchronously</b>
 * <pre> {@code
 * RequestBuilder<HistoricalData> hrb = new HistoricalRequestBuilder("SPX Index",
 *         "PX_LAST", DateTime.now().minusDays(7),
 *         DateTime.now())
 *         .fill(HistoricalRequestBuilder.Fill.NIL_VALUE)
 *         .days(HistoricalRequestBuilder.Days.ALL_CALENDAR_DAYS);
 * HistoricalData result = session.submit(hrb).get();
 * Map<DateTime, Object> data = result.forSecurity("SPX Index").forField("PX_LAST").get();
 * for (Map.Entry<DateTime, Object> e : data.entrySet()) {
 *     DateTime dt = e.getKey();
 *     double price = (Double) e.getValue();
 *     System.out.println("[" + dt + "] " + price);
 * }
 * }</pre>
 *
 * <b> Example: retrieve 60 minutes bar for the S&P 500 over the past week </b>
 * <pre> {@code
 * RequestBuilder<IntradayBarData> hrb = new IntradayBarRequestBuilder("SPX Index",
 *         DateTime.now().minusDays(7),
 *         DateTime.now())
 *         .adjustSplits()
 *         .fillInitialBar()
 *         .period(1, TimeUnit.HOURS);
 * IntradayBarData result = session.submit(hrb).get();
 * Map<DateTime, Object> data = result.forField(IntradayBarField.CLOSE).get();
 * for (Map.Entry<DateTime, Object> e : data.entrySet()) {
 *     DateTime dt = e.getKey();
 *     double price = (Double) e.getValue();
 *     System.out.println("[" + dt + "] " + price);
 * }
 * }</pre>
 *
 *
 * <b> Example: retrieve tick data for the S&P 500 over the past 2 hours </b>
 * <pre> {@code
 * RequestBuilder<IntradayTickData> hrb = new IntradayTickRequestBuilder("SPX Index",
 *         DateTime.now().minusHours(2),
 *         DateTime.now())
 *         .includeBrokerCodes()
 *         .includeConditionCodes();
 * IntradayTickData result = session.submit(hrb).get();
 * Multimap<DateTime, Object> data = result.forField(IntradayTickField.VALUE);
 * for (Map.Entry<DateTime, Object> e : data.entries()) {
 *     DateTime dt = e.getKey();
 *     double price = (Double) e.getValue();
 *     System.out.println("[" + dt + "] " + price);
 * }
 * }</pre>
 *
 * <b> Example: subscribe to real time price updates </b>
 * <pre> {@code
 * DataChangeListener lst = new DataChangeListener() {
 *     @Override
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
 * Thread.sleep(3000); //to allow data to start coming in
 * }</pre>
 */
package assylias.jbloomberg;