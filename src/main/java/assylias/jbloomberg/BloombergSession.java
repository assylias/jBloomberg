/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import java.util.concurrent.Future;

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
 *
 * Note that long running requests might delay real time subscriptions. It is recommended to use at least one session for
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
     * Closes the session. If the session has not been started yet, does nothing. This call will block until the session
     * is actually stopped.
     */
    void stop();

    /**
     * Submits a request to the Bloomberg Session and returns immediately.
     *
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
     *         request.
     *
     * @throws IllegalStateException if the start method was not called before this method
     * @throws NullPointerException  if request is null
     *
     */
    Future<RequestResult> submit(final RequestBuilder request);

    /**
     * Subscribes to a stream of real time update. The SubscriptionBuilder object is used to specify the securities and
     * fields that need to be monitored. It also specifies the DataChangeListener that will be informed of the updates.
     *
     * @param subscription contains the parameters of the real time data that needs to be monitored.
     */
    void subscribe(SubscriptionBuilder subscription);
}
