/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Identity;

import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * A high level API to submit requests to the Bloomberg API or subscribe to real time data updates. <br>
 * The typical life cycle of a BloombergSession is:
 * <ul>
 * <li> start the session
 * <li> get data from Bloomberg:
 * <ul>
 * <li> submit requests, which are used to query static or historical data
 * <li> subscribe to real time updates, typically to track the real time price of a security
 * </ul>
 * <li> stop the session: this releases all the internal resources used, including thread pools. Not stopping a session
 * could prevent the JVM from exiting.
 * </ul>
 * <p>
 * Note that long running requests might delay real time subscriptions. It is recommended to use at least one session
 * for
 * real time subscriptions and one for static or historical requests.
 */
public interface BloombergSession {

    /**
     * Starts a Bloomberg session asynchronously. If the bbcomm process is not running, this method will try to start
     * it.
     *
     * @throws BloombergException    if the bbcomm process is not running or could not be started, or if the session
     *                               could not be started asynchronously
     * @throws IllegalStateException if the session is already started
     */
    void start() throws BloombergException;

    /**
     * Starts a Bloomberg session asynchronously. If the bbcomm process is not running, this method will try to start
     * it. If an error is encountered while running this method, an exception is thrown - if an error is encountered in
     * the starting task created by this method, the provided Consumer will be executed.
     *
     * @param onStartupFailure an operation to be run if the session fails to be started
     * @throws BloombergException    if the bbcomm process is not running or could not be started, or if the session
     *                               could not be started asynchronously
     * @throws IllegalStateException if the session is already started
     * @throws NullPointerException  if the argument is null
     */
    void start(Consumer<BloombergException> onStartupFailure) throws BloombergException;

    /**
     * Closes the session. If the session has not been started yet, does nothing. This call will block until the session
     * is actually stopped.
     */
    void stop();
    void subscribe(SubscriptionBuilder subscription,Identity identity);
    /**
     * Submits a request to the Bloomberg Session and returns immediately. The generic paramater enables to distinguish
     * between single and multiple securities requests. The RequestResult object will be based on that type.
     * <p>
     * Calling get() on the returned future may block forever - it is advised to use the get(timeout) version.<br>
     * Additional exceptions may be thrown within the future (causing an ExecutionException when calling
     * future.get()). It is the responsibility of the caller to check and handle those exceptions:
     * <ul>
     * <li><code>BloombergException</code> - if the session or the required service could not be started or if the
     * request execution could not be completed
     * <li><code>CancellationException</code> - if the request execution was cancelled (interrupted) before completion
     * </ul>
     *
     * @return a Future that contains the result of the request. The future can be cancelled to cancel a long running
     * request.
     * @throws IllegalStateException if the start method was not called before this method
     * @throws NullPointerException  if request is null
     */
    <T extends RequestResult> Future<T> submit(RequestBuilder<T> request);

    /**
     * Subscribes to a stream of real time update. The SubscriptionBuilder object is used to specify the securities and
     * fields that need to be monitored. It also specifies the DataChangeListener that will be informed of the updates.
     *
     * @param subscription contains the parameters of the real time data that needs to be monitored.
     */
    void subscribe(SubscriptionBuilder subscription);

    /**
     * Returns the current {@link SessionState} of this Session. Note that there may be a slight delay between a change in
     * the state of the underlying Bloomberg connection and this method reflecting the change.
     *
     * @return The current {@link SessionState} of this Session.
     * @throws UnsupportedOperationException if the operation is not supported.
     */
    default SessionState getSessionState() {
        throw new UnsupportedOperationException("Could not retrieve the SessionState");
    }

    Identity authorize();
}
