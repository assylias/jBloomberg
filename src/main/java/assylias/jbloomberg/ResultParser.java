/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package assylias.jbloomberg;

import com.bloomberglp.blpapi.InvalidRequestException;
import com.bloomberglp.blpapi.Message;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A ResultParser parses the responses received from the Bloomberg Session after having sent a request.
 * Once the final response has been received, the noMoreMessage method must be called to allow the parser to start its
 * parsing job.
 * </p>
 * This interface has been made public for convenience but users should not need to implement it.
 * </p>
 * All implementations are thread safe.
 */
public interface ResultParser {

    /**
     * @return the result of the parsing of all the received messages. This method blocks until a result is available
     *
     * @throws IllegalStateException   if the noMoreMessages has not been called yet
     * @throws InterruptedException    if the thread is interrupted before the result has been received and parsed
     * @throws InvalidRequestException if the response returned by Bloomberg reports an error - this can typically
     *                                 happen if
     *                                 the request was malformed (which should not happen if this API is used properly) or the service is down.
     */
    RequestResult getResult() throws InterruptedException;

    /**
     * @return the result of the parsing of all the received messages. This method blocks until a result is available or
     *         the specified waiting time elapses.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     *
     * @throws IllegalStateException   if the noMoreMessages has not been called yet
     * @throws InterruptedException    if the thread is interrupted before the result has been received and parsed
     * @throws InvalidRequestException if the response returned by Bloomberg reports an error - this can typically
     *                                 happen if
     *                                 the request was malformed (which should not happen if this API is used properly) or the service is down.
     * @throws TimeoutException        if the specified waiting time elapses before the result could be computed
     */
    RequestResult getResult(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;

    /**
     * Adds msg to the list of messages to parse
     *
     * @param msg a message received from Bloomberg
     */
    void addMessage(Message msg);

    /**
     * signals that there are no more messages to expect from Bloomberg and parsing can be started
     *
     * @throws IllegalStateException if called more than once
     */
    void noMoreMessages();
}
