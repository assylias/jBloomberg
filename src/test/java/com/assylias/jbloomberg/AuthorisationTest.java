/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */

package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Identity;
import com.bloomberglp.blpapi.SessionOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Test(groups = "requires-bloomberg")
public class AuthorisationTest {

  private static final Logger logger = LoggerFactory.getLogger(AuthorisationTest.class);
  private static final String CONFIG = "src/test/resources/auth_setup";
  private static final int TIMEOUT = 1000;
  private static final Properties props = new Properties();
  private boolean hasConfigFile = false;

  @BeforeClass public void readConfiguration() throws Exception {
    Path p = Paths.get(CONFIG);
    if (Files.exists(p)) {
      InputStream in = new FileInputStream(p.toFile());
      props.load(in);
      logger.debug("Found auth_setup: {}", props);
      hasConfigFile = true;
    } else {
      logger.warn("No auth_setup file found - no authorisation tests will be run");
    }
  }

  @Test
  public void desktopApi() throws Exception {
    run("localhost", new Authorisation.Desktop());
  }

  @Test
  public void serverApi() throws Exception {
    if (!hasConfigFile) return;
    String host = props.getProperty("sapi.serverHost");
    if (host == null) {
      logger.warn("Not running Server API test: no settings provided in auth_setup");
      return;
    }
    int uuid = Integer.parseInt(props.getProperty("sapi.uuid"));
    String ipAddress = props.getProperty("sapi.ipAddress");
    logger.debug("Testing Server API authorisation with {}/{}", uuid, ipAddress);
    run(host, new Authorisation.ServerApi(uuid, ipAddress));
  }

  @Test
  public void enterpriseAuthId() throws Exception {
    if (!hasConfigFile) return;
    String host = props.getProperty("enterprise.serverHost");
    if (host == null) {
      logger.warn("Not running Enterprise Auth ID test: no settings provided in auth_setup");
      return;
    }
    String authId = props.getProperty("enterprise.authId");
    String ipAddress = props.getProperty("enterprise.ipAddress");
    String appName = props.getProperty("enterprise.appName");
    logger.debug("Testing Enterprise Auth ID authorisation with {}/{}/{}", authId, ipAddress, appName);
    run(host, new Authorisation.EnterpriseId(authId, ipAddress, appName));
  }

  @Test
  public void enterpriseToken() throws Exception {
    if (!hasConfigFile) return;
    String host = props.getProperty("enterprise.serverHost");
    String token = props.getProperty("enterprise.token");
    if (host == null || token == null) {
      logger.warn("Not running Enterprise Token test: no settings provided in auth_setup");
      return;
    }
    logger.debug("Testing Enterprise Token authorisation");
    run(host, new Authorisation.EnterpriseToken());
  }

  private void run(String serverHost, Authorisation authorisation) throws ExecutionException, InterruptedException {
    BloombergSession session;
    if (serverHost != null) {
      SessionOptions options = new SessionOptions();
      options.setServerHost(serverHost);
      session = new DefaultBloombergSession(options);
    } else {
      session = new DefaultBloombergSession();
    }

    try {
      session.start();
      Identity identity = null;
      if (authorisation != null) identity = session.authorise(authorisation).get();

      ReferenceRequestBuilder b = new ReferenceRequestBuilder("IBM US Equity", "PX_LAST");

      double p = session.submit(b, identity).get().forField("PX_LAST").forSecurity("IBM US Equity").asDouble();

      CountDownLatch subscriptionStarted = new CountDownLatch(1);
      session.subscribe(new SubscriptionBuilder().addSecurity("ES1 Index").addField(RealtimeField.LAST_PRICE).addListener(e -> subscriptionStarted.countDown()));
      subscriptionStarted.await(TIMEOUT, TimeUnit.MILLISECONDS);
    } finally {
      session.stop();
    }
  }
}