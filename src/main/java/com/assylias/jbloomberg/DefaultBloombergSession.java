/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of the BloombergSession interface.
 * <p>
 * See the documentation of the parent class for more details.
 * <p>
 * This implementation is thread safe. Memory consistency effects: actions in a thread prior to submitting an request or
 * subscribing to real time information happen-before actions subsequent to the access to the data returned by those
 * requests / subscriptions.
 */
public class DefaultBloombergSession implements BloombergSession {

    private final static Logger logger = LoggerFactory.getLogger(DefaultBloombergSession.class);
    /**
     * Default options are used for the session
     */
    private final static SessionOptions sessionOptions = new SessionOptions();
    /**
     * A unique session id generator to easily identify the sessions
     */
    private final static AtomicInteger sessionIdGenerator = new AtomicInteger();
    /**
     * The underlying Bloomberg session object
     */
    private final Session session;
    /**
     * This session's unique ID - used for logging essentially (in toString)
     */
    private final int sessionId = sessionIdGenerator.incrementAndGet();
    /**
     * Used to avoid starting the session more than once (which would throw an exception) - once set to true, it remains
     * true.
     */
    private volatile boolean isSessionStarting = false;
    /**
     * Used to check if the session is started
     */
    private final CountDownLatch sessionStarted = new CountDownLatch(1);
    /**
     * The queue that is used to transfer subscription data from Bloomberg to the interested parties
     */
    private final BlockingQueue<Data> subscriptionDataQueue = new LinkedBlockingQueue<>();
    /**
     * The event handler used by this session to process results asynchronously
     */
    private final BloombergEventHandler eventHandler = new BloombergEventHandler(subscriptionDataQueue);
    /**
     * Collection that keeps track of services that have been asynchronously started. They might not be started yet.
     * Each service is attributed a unique Correlation ID
     */
    private final Map<BloombergServiceType, CorrelationID> openingServices = new EnumMap<>(BloombergServiceType.class);
    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
        private final AtomicInteger threadId = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Bloomberg Session # " + sessionId + " - " + threadId.incrementAndGet());
        }
    });
    private final SubscriptionManager subscriptionManager = new SubscriptionManager(subscriptionDataQueue,
            new ConcurrentConflatedEventsManager());

    public DefaultBloombergSession() {
        session = new Session(sessionOptions, eventHandler);
    }

    /**
     * Starts a bloomberg session asynchronously. If the bbcomm process is not running, this method will try to start
     * it.<br> If the session has already been started, does nothing.
     * <p/>
     * @throws BloombergException    if the bbcomm process is not running or could not be started, or if the session
     *                               could not be started asynchronously
     * @throws IllegalStateException if the session is already started
     */
    @Override
    public synchronized void start() throws BloombergException {
        if (isSessionStarting) {
            throw new IllegalStateException("Session has already been started: " + this);
        }
        if (!BloombergUtils.startBloombergProcessIfNecessary()) { //could not be started for some reason
            isSessionStarting = false;
            throw new BloombergException("Failed to start session: bbcomm process could not be started");
        }
        logger.info("Starting Bloomberg session #{} with options: {}", sessionId, getOptions());
        try {
            eventHandler.onSessionStarted(new Runnable() {
                @Override
                public void run() {
                    subscriptionManager.start(DefaultBloombergSession.this); //needs to be before the countdown (see subscribe method)
                    sessionStarted.countDown();
                }
            });
            session.startAsync();
            isSessionStarting = true;
            logger.info("Session #{} started asynchronously", sessionId);
        } catch (IOException | IllegalStateException e) {
            throw new BloombergException("Failed to start session", e);
        }
    }

    Session getBloombergSession() {
        return session;
    }

    /**
     * Closes the session. If the session has not been started yet, does nothing. This call will block until the session
     * is actually stopped.
     */
    @Override
    public synchronized void stop() {
        if (!isSessionStarting) {
            logger.warn("Ignoring call to stop: session not started");
            return;
        }
        try {
            logger.info("Stopping Bloomberg session #{}", sessionId);
            executor.shutdown();
            subscriptionManager.stop(this);
            session.stop(AbstractSession.StopOption.SYNC);
            logger.info("Stopped Bloomberg session #{}", sessionId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Submits a request to the Bloomberg Session and returns immediately.
     *
     * Calling get() on the returned future may block for a very long time - it is advised to use the get(timeout)
     * version.<br>
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
    @Override
    public <T extends RequestResult> Future<T> submit(final RequestBuilder<T> request) {
        Objects.requireNonNull(request, "request cannot be null");
        if (!isSessionStarting) {
            throw new IllegalStateException("A request can't be submitted before the session is started");
        }
        logger.debug("Submitting request {}", request);
        Callable<T> task = new Callable<T>() {
            @Override
            public T call() throws Exception {
                BloombergServiceType serviceType = request.getServiceType();
                CorrelationID cId = getNextCorrelationId();
                try {
                    openService(serviceType);
                    ResultParser<T> parser = request.getResultParser();
                    eventHandler.setParser(cId, parser);
                    sendRequest(request, cId);
                    return parser.getResult();
                } catch (IOException | InvalidRequestException | RequestQueueOverflowException | DuplicateCorrelationIDException |
                        IllegalStateException e) {
                    throw new BloombergException("Could not process the request", e);
                } catch (InterruptedException e) {
                    session.cancel(cId);
                    throw new CancellationException("The request was cancelled");
                }
            }
        };
        return executor.submit(task);
    }

    @Override
    public void subscribe(SubscriptionBuilder subscription) {
        if (!isSessionStarting) {
            throw new IllegalStateException("A request can't be submitted before the session is started");
        }
        try {
            sessionStarted.await(); //once the latch counts down, we know that the session has been set.
            subscriptionManager.subscribe(subscription);
        } catch (IOException e) {
            throw new RuntimeException("Could not complete subscription request", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Opens the the service if it has not been opened before, otherwise does nothing. This call blocks until the
     * service is opened or an exception is thrown.
     * <p/>
     * @throws IOException          if the service could not be opened (Bloomberg API exception)
     * @throws InterruptedException if the current thread is interrupted while opening the service
     */
    private synchronized void openService(final BloombergServiceType serviceType) throws IOException, InterruptedException {
        if (openingServices.containsKey(serviceType)) {
            return; //only start the session once
        }

        logger.debug("Waiting for session to start while opening service {}", serviceType);
        sessionStarted.await();
        logger.debug("Opening service {}", serviceType);
        if (!session.openService(serviceType.getUri())) {
            throw new IllegalStateException("The service could not be opened (openService returned false)");
        }
    }

    /**
     *
     * @return the result of the request
     * <p/>
     * @throws IllegalStateException           If the session is not established
     * @throws InvalidRequestException         If the request is not compliant with the schema for the request
     * @throws RequestQueueOverflowException   If this session has too many enqueued requests
     * @throws IOException                     If any error occurs while sending the request
     * @throws DuplicateCorrelationIDException If the specified correlationId is already active for this Session
     */
    private CorrelationID sendRequest(final RequestBuilder<?> request, CorrelationID cId) throws IOException {
        Request bbRequest = request.buildRequest(session);
        session.sendRequest(bbRequest, cId);
        return cId;
    }

    CorrelationID getNextCorrelationId() {
        return new CorrelationID(); //returns a new unique correlation ID in a thread safe way
    }

    private Map<String, Object> getOptions() {
        Class<?> c = SessionOptions.class;
        Map<String, Object> options = new TreeMap<>();
        for (Method m : c.getMethods()) {
            if (m.getName().contains("get") && !m.getName().contains("getClass")) {
                String name = m.getName().substring(3);
                try {
                    options.put(name, m.invoke(sessionOptions));
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ignore) {
                    options.put(name, "n.a.");
                }
            }
        }
        return options;
    }

    @Override
    public String toString() {
        String status;
        if (sessionStarted.getCount() == 0) {
            status = "STARTED";
        } else if (isSessionStarting) {
            status = "STARTING";
        } else {
            status = "NOT STARTED";
        }
        return "Session #" + sessionId + " [" + status + "]";
    }
}
