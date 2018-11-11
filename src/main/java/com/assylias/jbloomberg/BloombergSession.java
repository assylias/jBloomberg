/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Identity;

import java.util.concurrent.CompletableFuture;
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
 *
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
     *
     * @throws BloombergException    if the bbcomm process is not running or could not be started, or if the session
     *                               could not be started asynchronously
     * @throws IllegalStateException if the session is already started
     * @throws NullPointerException if the argument is null
     */
    void start(Consumer<BloombergException> onStartupFailure) throws BloombergException;

    /**
     * Closes the session. If the session has not been started yet, does nothing. This call will block until the session
     * is actually stopped.
     */
    void stop();

    /**
     * In order to access permissioned data it is necessary to supply an identity with the request.  In a centralised
     * computation model the server itself may access data and permissions can be applied later. The Server MUST keep all
     * EIDs associated with any data received and associate these EIDs with any calculations made on the data.
     *
     * @return a Future that contains the identity corresponding to the server identity
     */
    CompletableFuture<Identity> getTokenIdentity();

    /**
     * In a client/Server application, the Server needs to permission its clients, but in some cases the client will not be
     * able to create a token. To list a few of them:
     * <ul>
     * <li>Client has no connectivity to B-PIPE</li>
     * <li>Remote display access — the IP is not always the display IP — the API will handle most Citrix-use cases</li>
     * <li>Web applications — client is a browser, so code cannot easily be added</li>
     * </ul>
     * For permissioning in these cases, a client Identity can be generated in the Server from any EMRS or SAPE
     * name or alias and the Display IP:
     * <ul>
     * <li>No token is used</li>
     * <li>The Identity is authorized using AUTH ID2 and IP instead of token</li>
     * <li>All other coding is the same</li>
     * <li>Note that all IPs must be dynamically and programmatically determined and not stored in config files or the like</li>
     * </ul>
     *
     * @param uuid User UUID
     * @param ipAddress User IP address
     * @return a Future that contains the identity corresponding to the supplied user uuid
     */
    CompletableFuture<Identity> getUserIdentity(int uuid, String ipAddress);

    /**
     * In a client/Server application, the Server needs to permission its clients, but in some cases the client will not be
     * able to create a token. To list a few of them:
     * <ul>
     * <li>Client has no connectivity to B-PIPE</li>
     * <li>Remote display access — the IP is not always the display IP — the API will handle most Citrix-use cases</li>
     * <li>Web applications — client is a browser, so code cannot easily be added</li>
     * </ul>
     * For permissioning in these cases, a client Identity can be generated in the Server from any EMRS or SAPE
     * name or alias and the Display IP:
     * <ul>
     * <li>No token is used</li>
     * <li>The Identity is authorized using AUTH ID2 and IP instead of token</li>
     * <li>All other coding is the same</li>
     * <li>Note that all IPs must be dynamically and programmatically determined and not stored in config files or the like</li>
     * </ul>
     *
     * As per the Enterprise Developer Guide the authId must the be same as entered in EMRS &lt;GO&gt; or SAPE &lt;GO&gt;
     *
     * @param authId User UserEntry
     * @param ipAddress User IP address
     * @return a Future that contains the identity corresponding to the supplied user authId
     */
    CompletableFuture<Identity> getUserIdentity(String authId, String ipAddress);

    /**
     * Submits a request to the Bloomberg Session and returns immediately. The generic paramater enables to distinguish
     * between single and multiple securities requests. The RequestResult object will be based on that type.
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
    <T extends RequestResult> CompletableFuture<T> submit(RequestBuilder<T> request);

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
     *
     * @throws UnsupportedOperationException if the operation is not supported.
     */
    default SessionState getSessionState() {
        throw new UnsupportedOperationException("Could not retrieve the SessionState");
    }
}
