/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Name;
import com.bloomberglp.blpapi.Session;
import com.bloomberglp.blpapi.SessionOptions;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SessionState is an enum representing the possible states of a {@link BloombergSession} and its underlying Bloomberg {@link Session}. The typical lifecycle
 * is as follows:<br>
 * NEW -> STARTING -> CONNECTION_UP -> STARTED -> CONNECTION_DOWN -> TERMINATED<br>
 * <br>
 * If the connection is lost and recovered (because {@link SessionOptions#setAutoRestartOnDisconnection} has been set to true in the SessionOptions), the events
 * will be:<br>
 * CONNECTION_DOWN -> CONNECTION_UP<br>
 * <br>
 * Note that even if the CONNECTION_UP signal is sent, the session will not return anything until the user is logged on the local machine if using the local
 * Bloomberg terminal as a data source.
 */
public enum SessionState {
  /**
   * Indicates that the BloombergSession object has been instantiated but its {@link BloombergSession#start()} method has not been called yet.
   */
  NEW(null),
  /**
   * Indicates that the BloombergSession is in the process of being started (its {@link BloombergSession#start()} method has been called but the connection is
   * not established yet).
   */
  STARTING(null), //not an actual event
  /**
   * Indicates that the BloombergSession has been started and can be used.
   */
  STARTED("SessionStarted"),
  /**
   * Indicates that the BloombergSession could not be started - if the {@link BloombergSession#start(java.util.function.Consumer)} method has been used, an
   * exception has been passed to the provided Consumer.
   */
  STARTUP_FAILURE("SessionStartupFailure"),
  /**
   * Indicates that the BloombergSession has been started and the connection was subsequently lost or the session stopping sequence has started.
   * If AutoRestartOnDisconnection has been set to true in the SessionOptions attempts will be made to recover the connection (unless the session is being
   * stopped).
   */
  CONNECTION_DOWN("SessionConnectionDown"),
  /**
   * Indicates that the BloombergSession's connection is up and running - note that it may not be immediately available for request submissions.
   */
  CONNECTION_UP("SessionConnectionUp"),
  /**
   * Indicates that the BloombergSession has been stopped - it can't be used any longer and can't be restarted.
   */
  TERMINATED("SessionTerminated");

  private final static Logger logger = LoggerFactory.getLogger(SessionState.class);
  private final static Map<Name, SessionState> map = new HashMap<>(SessionState.values().length, 1);

  static {
    for (SessionState e : values()) {
      map.put(e.name, e);
    }
  }
  private final Name name;

  static SessionState from(BloombergEventHandler.BloombergConnectionState state) {
    switch (state) {
      case SESSION_CONNECTION_DOWN: return CONNECTION_DOWN;
      case SESSION_CONNECTION_UP: return CONNECTION_UP;
      case SESSION_STARTED: return STARTED;
      case SESSION_STARTUP_FAILURE: return STARTUP_FAILURE;
      case SESSION_TERMINATED: return TERMINATED;
      default: logger.error("Not a valid BloombergConnectionState: {}", state);
    }
    return null;
  }

  private SessionState(String s) {
    this.name = new Name(s);
  }

  /**
   * Returns a SessionState corresponding to the given name or null if the state is invalid.
   *
   * @param name the name to look for
   *
   * @return a SessionState corresponding to the given name or null if the state is invalid.
   */
  static SessionState get(Name name) {
    return map.get(name);
  }
}
