/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import assylias.jbloomberg.BloombergException;
import assylias.jbloomberg.DefaultBloombergSession;
import assylias.jbloomberg.RealtimeField;
import assylias.jbloomberg.DataChangeEvent;
import assylias.jbloomberg.DataChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Yann Le Tallec
 */
public class DataStreamNGTest {

    private DefaultBloombergSession session;

//    @BeforeClass(groups = "requires-bloomberg")
//    public void beforeClass() throws BloombergException {
//        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
//        session = new DefaultBloombergSession();
//        session.start();
//    }
//
//    @AfterClass(groups = "requires-bloomberg")
//    public void afterClass() throws BloombergException {
//        if (session != null) {
//            session.stop();
//        }
//    }
//
//    @Test
//    public void test1() {
//        DataChangeListener lst = new DataChangeListener() {
//
//            @Override
//            public void dataChanged(DataChangeEvent e) {
//                System.out.println(e);
//            }
//        };
//        RealTimeRequestBuilder builder = new RealTimeRequestBuilder()
//                .addSecurities("IBM US Equity")
//                .addFields(RealtimeField.LAST_PRICE)
//                .throttle(5, TimeUnit.SECONDS);
//        RequestResult result = session.subscribe(builder, lst).get();
//        session.unsubscribe(lst);
//    }

}
