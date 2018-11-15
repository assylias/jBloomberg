/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.DuplicateCorrelationIDException;
import com.bloomberglp.blpapi.Identity;
import com.bloomberglp.blpapi.InvalidRequestException;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.RequestQueueOverflowException;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;
import com.bloomberglp.blpapi.SessionOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.assylias.jbloomberg.SessionState.NEW;
import static com.assylias.jbloomberg.SessionState.STARTED;
import static com.assylias.jbloomberg.SessionState.STARTING;
import static com.assylias.jbloomberg.SessionState.STARTUP_FAILURE;
import static com.assylias.jbloomberg.SessionState.TERMINATED;
import static java.util.Objects.requireNonNull;

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
     * A unique session id generator to easily identify the sessions
     */
    private final static AtomicInteger sessionIdGenerator = new AtomicInteger();
    /**
     * Session options used to create the Bloomberg session
     */
    private final SessionOptions sessionOptions;
    /**
     * Called whenever the SessionState of this session changes
     */
    private Consumer<SessionState> sessionStateListener;
    /**
     * The underlying Bloomberg session object
     */
    private final Session session;
    /**
     * This session's unique ID - used for logging essentially (in toString)
     */
    private final int sessionId = sessionIdGenerator.incrementAndGet();
    /**
     * Used to check if the session startup process is over (in which case either the session is started or startup
     * failed)
     */
    private final CountDownLatch sessionStartup = new CountDownLatch(1);
    /**
     * The state of this session - Also used to avoid starting the session more than once (which would throw an
     * exception).
     */
    private final AtomicReference<SessionState> state = new AtomicReference<>(SessionState.NEW);
    /**
     * The queue that is used to transfer subscription data from Bloomberg to the interested parties
     */
    private final BlockingQueue<DataOrSubscriptionError> subscriptionDataQueue = new LinkedBlockingQueue<>();
    /**
     * The event handler used by this session to process results asynchronously
     */
    private final BloombergEventHandler eventHandler;
    /**
     * Collection that keeps track of services that have been asynchronously started. They might not be started yet.
     */
    private final Set<BloombergServiceType> openingServices = EnumSet.noneOf(BloombergServiceType.class);
    private final ExecutorService executor = Executors.newFixedThreadPool(10, new ThreadFactory() {
        private final AtomicInteger threadId = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Bloomberg Session # " + sessionId + " - " + threadId.incrementAndGet());
        }
    });
    private final EventsManager eventsManager = new ConcurrentConflatedEventsManager();
    private final SubscriptionManager subscriptionManager = new SubscriptionManager(subscriptionDataQueue,
            eventsManager);


    /**
     * Creates a new BloombergSession using default SessionOptions {@link SessionOptions#SessionOptions()}.
     */
    public DefaultBloombergSession() {
        this(new SessionOptions());
    }

    /**
     * Creates a new BloombergSession using the provided SessionOptions.
     *
     * @param sessionOptions a non null {@link SessionOptions}.
     *
     * @throws NullPointerException if the argument is null.
     */
    public DefaultBloombergSession(SessionOptions sessionOptions) {
        this(sessionOptions, x -> {/*no-op*/});
    }

    /**
     * Creates a new BloombergSession using the provided SessionOptions and SessionState listener. Note that the listener will be called promptly after a
     * state change of the underlying Bloomberg connection but there may be a slight delay (in particular if calling
     * {@link DefaultBloombergSession#start(java.util.function.Consumer)}, that consumer may be called first.<br><br>
     * See also {@link SessionState} for a description of a typical session lifecycle.
     *
     * @param sessionOptions       a non null {@link SessionOptions}.
     * @param sessionStateListener a listener that will be called every time the {@link SessionState} of this BloombergSession changes.
     *
     * @throws NullPointerException if any of the arguments are null.
     */
    public DefaultBloombergSession(SessionOptions sessionOptions, Consumer<SessionState> sessionStateListener) {
        this.sessionOptions = requireNonNull(sessionOptions);
        this.sessionStateListener = requireNonNull(sessionStateListener);
        this.eventHandler = new BloombergEventHandler(subscriptionDataQueue, sessionStateListener);
        session = new Session(sessionOptions, eventHandler);
        updateStateListener();
    }

    /**
     * WARNING: only call this for custom states (i.e. NEW, STARTING) - the other states are set by the EventHandler.
     */
    private void updateStateListener() {
        sessionStateListener.accept(state.get());
    }

    @Override
    public synchronized void start() throws BloombergException {
        start(x -> {/*no-op*/});
    }

    @Override
    public synchronized void start(Consumer<BloombergException> onStartupFailure) throws BloombergException {
        requireNonNull(onStartupFailure);
        if (state.get() != NEW) {
            throw new IllegalStateException("Session has already been started: " + this);
        }
        if (onlyConnectToLocalAddresses() && !BloombergUtils.startBloombergProcessIfNecessary()) { //could not be started for some reason
            state.set(STARTUP_FAILURE);
            updateStateListener();
            throw new BloombergException("Failed to start session: bbcomm process could not be started");
        }
        logger.info("Starting Bloomberg session #{} with options: {}", sessionId, getOptions());
        try {
            eventHandler.onSessionStarted(() -> {
                subscriptionManager.start(DefaultBloombergSession.this); //needs to be before the countdown (see subscribe method)
                state.set(STARTED);
                sessionStartup.countDown();
            });
            eventHandler.onSessionStartupFailure((BloombergException e) -> {
                state.set(STARTUP_FAILURE);
                sessionStartup.countDown();
                onStartupFailure.accept(e);
            });
            if (!state.compareAndSet(NEW, STARTING)) {
                throw new AssertionError("State was expected to be NEW but found " + state.get());
            }
            updateStateListener();
            session.startAsync();
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
     * is actually stopped.<br>
     * If the session startup encountered a problem (typically: can't connect to a Bloomberg session), this may block
     * for a few seconds....
     * A stopped session can't be restarted.
     */
    @Override
    public synchronized void stop() {
        if (state.get() == NEW) {
            logger.warn("Ignoring call to stop: session not started");
            return;
        }
        try {
            logger.info("Stopping Bloomberg session #{}", sessionId);
            boolean started = sessionStartup.await(1, TimeUnit.SECONDS); //with 3.6.1.0, if the session is not started yet, the call to stop can block
            if (!started) logger.info("I waited for 1 second but Bloomberg session #{} is still not started...");
            executor.shutdownNow();
            subscriptionManager.stop(this);
            session.stop();//started ? SYNC : ASYNC); //if not started, something's wrong, don't spend too much time here...
            state.set(TERMINATED);
            logger.info("Stopped Bloomberg session #{}", sessionId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override public CompletableFuture<Identity> authorise(Authorisation authorisation) {
        return CompletableFuture.supplyAsync(() -> {
            logger.debug("Authorising user using {}", authorisation);
            Identity identity = authorisation.getIdentity(this::getIdentity, this::getToken);
            logger.debug("Successfully authorised user {}", authorisation);
            return identity;
        });
    }

    Identity getIdentity(Consumer<Request> requestAuthSetter) {
        CorrelationID cId = getNextCorrelationId();
        try {
            // open service first to ensure we are connected
            openService(BloombergServiceType.API_AUTHORIZATION);
            Service service = session.getService(BloombergServiceType.API_AUTHORIZATION.getUri());
            Request authRequest = service.createAuthorizationRequest();

            requestAuthSetter.accept(authRequest);

            Identity identity = session.createIdentity();
            ResultParser<AuthorisationResultParser.Result> parser = new AuthorisationResultParser();
            eventHandler.setParser(cId, parser);
            session.sendAuthorizationRequest(authRequest, identity, cId);
            AuthorisationResultParser.Result auth = parser.getResult();

            if (!auth.isAuthorised()) throw new IllegalStateException("Failed to authorise user - " + auth.getError());
            return identity;
        } catch (IOException | InvalidRequestException | RequestQueueOverflowException | DuplicateCorrelationIDException | IllegalStateException e) {
            throw new BloombergException("Could not process the authorisation request", e);
        } catch (InterruptedException e) {
            session.cancel(cId);
            throw new CancellationException("The authorisation request was cancelled");
        }
    }

    String getToken()  {
        CorrelationID cId = getNextCorrelationId();
        try {
            //We open the authorisation service as a "hack" to wait for the session to be started.
            //Note that it's not a waste of time because that service will be open when creating an identity from the token anyway.
            openService(BloombergServiceType.API_AUTHORIZATION);
            TokenResultParser tokenResultParser = new TokenResultParser();
            eventHandler.setParser(cId, tokenResultParser);
            session.generateToken(cId);
            TokenResultParser.Result token = tokenResultParser.getResult();
            if (token.isEmpty()) throw new IllegalStateException("Failed to generate token - " + token.getError());
            logger.debug("Successfully generated authorisation token: {}", token.getToken());
            return token.getToken();
        } catch (IOException | InvalidRequestException | RequestQueueOverflowException | DuplicateCorrelationIDException | IllegalStateException e) {
            throw new BloombergException("Could not process the token request", e);
        } catch (InterruptedException e) {
            session.cancel(cId);
            throw new CancellationException("The token request was cancelled");
        }
    }

    @Override
    public <T extends RequestResult> CompletableFuture<T> submit(RequestBuilder<T> request, Identity identity) {
        requireNonNull(request, "request cannot be null");
        if (state.get() == NEW) {
            throw new IllegalStateException("A request can't be submitted before the session is started");
        }
        logger.debug("Submitting request {}", request);
        Supplier<T> task = () -> {
            BloombergServiceType serviceType = request.getServiceType();
            CorrelationID cId = getNextCorrelationId();
            try {
                openService(serviceType);
                ResultParser<T> parser = request.getResultParser();
                eventHandler.setParser(cId, parser);
                sendRequest(request, cId, identity);
                return parser.getResult();
            } catch (IOException | InvalidRequestException | RequestQueueOverflowException | DuplicateCorrelationIDException | IllegalStateException e) {
                throw new BloombergException("Could not process the request", e);
            } catch (InterruptedException e) {
                session.cancel(cId);
                throw new CancellationException("The request was cancelled");
            }
        };
        return CompletableFuture.supplyAsync(task, executor);
    }

    /**
     * Opens the the service if it has not been opened before, otherwise does nothing. This call blocks until the
     * service is opened or an exception is thrown.
     * <p>
     * @throws IOException          if the service could not be opened (Bloomberg API exception)
     * @throws InterruptedException if the current thread is interrupted while opening the service
     */
    private synchronized void openService(BloombergServiceType serviceType) throws IOException, InterruptedException, BloombergException {
        if (openingServices.contains(serviceType)) {
            return; //only start the session once
        }

        logger.debug("Waiting for session to start while opening service {}", serviceType);
        sessionStartup.await();
        if (state.get() != SessionState.STARTED) {
            throw new BloombergException("The Bloomberg session could not be started");
        }
        logger.debug("Opening service {}", serviceType);
        if (!session.openService(serviceType.getUri())) {
            throw new IllegalStateException("The service could not be opened (openService returned false)");
        }
        openingServices.add(serviceType);
    }

    /**
     * This method has been extracted to document the potential exceptions.
     *
     * @throws IllegalStateException           If the session is not established
     * @throws InvalidRequestException         If the request is not compliant with the schema for the request
     * @throws RequestQueueOverflowException   If this session has too many enqueued requests
     * @throws IOException                     If any error occurs while sending the request
     * @throws DuplicateCorrelationIDException If the specified correlationId is already active for this Session
     */
    private void sendRequest(RequestBuilder<?> request, CorrelationID cId, Identity identity) throws IOException {
        Request bbRequest = request.buildRequest(session);
        logger.trace("{}", bbRequest);
        session.sendRequest(bbRequest, identity, cId);
    }

    @Override
    public void subscribe(SubscriptionBuilder subscription, Identity identity) {
        if (state.get() == SessionState.NEW) {
            throw new IllegalStateException("A request can't be submitted before the session is started");
        }
        try {
            sessionStartup.await(); //once the latch counts down, we know that the session has been set.
            if (state.get() != SessionState.STARTED) {
                throw new BloombergException("The Bloomberg session could not be started");
            }
            subscriptionManager.subscribe(subscription, identity);
        } catch (IOException e) {
            throw new BloombergException("Could not complete subscription request", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override public SessionState getSessionState() {
        return state.get();
    }

    private boolean onlyConnectToLocalAddresses() {
        return Arrays.stream(sessionOptions.getServerAddresses())
                .map(SessionOptions.ServerAddress::host)
                .allMatch(NetworkUtils::isLocalhost);
    }

    CorrelationID getNextCorrelationId() {
        return new CorrelationID(); //returns a new unique correlation ID in a thread safe way
    }

    /**
     * Used for logging
     */
    private Map<String, Object> getOptions() {
        Class<?> c = SessionOptions.class;
        Map<String, Object> options = new TreeMap<>();
        for (Method m : c.getMethods()) {
            if (m.getName().contains("get") && !m.getName().contains("getClass")) {
                String name = m.getName().substring(3);
                try {
                    Object option = m.invoke(sessionOptions);
                    boolean isArray = option != null && option.getClass().isArray();
                    options.put(name, isArray ? Arrays.deepToString((Object[]) option) : Objects.toString(option));
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ignore) {
                    options.put(name, "n.a.");
                }
            }
        }
        return options;
    }

    @Override
    public String toString() {
        return "Session #" + sessionId + " [" + state.get() + "]";
    }
}