/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.InvalidConversionException;
import com.bloomberglp.blpapi.InvalidRequestException;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.Name;
import com.bloomberglp.blpapi.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.assylias.jbloomberg.AbstractResultParser.SecurityDataElements.FIELD_DATA;
import static com.assylias.jbloomberg.AbstractResultParser.SecurityDataElements.FIELD_EXCEPTIONS;
import static com.assylias.jbloomberg.AbstractResultParser.SecurityDataElements.SECURITY_ERROR;

/**
 * Base class to parse results from requests.
 * This implementation is thread safe as the Bloomberg API might send results through more than one thread.
 */
abstract class AbstractResultParser<T extends AbstractRequestResult> implements ResultParser<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractResultParser.class);

    protected static final DateTimeFormatter BB_RESULT_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; //'2011-12-03'
    protected static final DateTimeFormatter BB_RESULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * List of received messages - must be thread safe
     */
    private final Collection<Message> messages = new ConcurrentLinkedQueue<>();

    /**
     * boolean used to make sure noMoreMessages is only called once - initially false.
     */
    private final AtomicBoolean noMoreMessagesHasRun = new AtomicBoolean();

    /**
     * Whether additional messages should be expected or not.
     */
    private final CountDownLatch noMoreMessages = new CountDownLatch(1);

    /**
     * The result of the parsing operation - guarded by lock
     */
    private final AtomicReference<T> result = new AtomicReference<>();

    @Override
    public void addMessage(Message msg) {
        if (noMoreMessagesHasRun.get()) {
            throw new IllegalStateException("Can't add messages once noMoreMessages has been called");
        }
        messages.add(msg);
    }

    @Override
    public void noMoreMessages() {
        if (!noMoreMessagesHasRun.compareAndSet(false, true)) {
            throw new IllegalStateException("This method should not be called more than once");
        }
        noMoreMessages.countDown();
    }

    @Override
    public T getResult() throws InterruptedException {
        noMoreMessages.await();
        return setResultIfNull();
    }

    @Override
    public T getResult(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (!noMoreMessages.await(timeout, unit)) {
            throw new TimeoutException("Could not compute the result within " + timeout + " " + unit.toString().toLowerCase(Locale.ENGLISH));
        }
        return setResultIfNull();
    }

    private T setResultIfNull() {
        return result.updateAndGet(r -> {
            if (r == null) {
                T result = getRequestResult();
                parse(result, messages);
                return result;
            }
            return r;
        });
    }

    /**
     * Subclasses must implement this method by returning an empty {@link RequestResult} data structure that will be filled during the parsing process.
     */
    protected abstract T getRequestResult();

    /**
     * This method must parse the valid part of the response (e.g. the securityData or barData Element).
     * @param response the response received from Bloomberg - it does not contain any errors
     * @param result a {@link RequestResult} data structure that must be filled with the response data
     */
    protected abstract void parseResponseNoError(Element response, T result);

    private void parse(final T result, final Collection<Message> messages) {
        for (Message msg : messages) {
            Element response = msg.asElement();
            parseResponse(result, response);
        }
    }

    private void parseResponse(final T result, final Element response) {
        if (response.hasElement(RESPONSE_ERROR, true)) {
            Element errorInfo = response.getElement(RESPONSE_ERROR);
            logger.info("Response error: {}", errorInfo);
            String errorMessage = parseErrorInfo(errorInfo);
            if (isSecurityError(errorInfo)) { //Normally only happen for an IntradayBarRequest or IntradayTickRequest
                result.addSecurityError(errorMessage);
            } else {
                throw new InvalidRequestException("ResponseError received (generally caused by malformed request or service down): " + errorMessage);
            }
        }
        parseResponseNoError(response, result);
    }

    /**
     * @return a string representation of the error
     */
    private static String parseErrorInfo(Element errorInfo) {
        try {
            return errorInfo.getElementAsString(ErrorInfoElements.MESSAGE.asName());
        } catch (NotFoundException | InvalidConversionException e) {
            return "Could not parse the errorInfo element: " + errorInfo;
        }
    }

    /**
     * @param errorInfo an errorInfo element
     * @return true if this error is due to an invalid security
     */
    private static boolean isSecurityError(Element errorInfo) {
        return errorInfo.hasElement(ErrorInfoElements.CATEGORY.asName())
                && errorInfo.getElementAsString(ErrorInfoElements.CATEGORY.asName()).equals("BAD_SEC");
    }

    /**
     * A helper method to parse a SECURITY_DATA element
     *
     * @param securityData the SECURITY_DATA element
     * @param parser a parser for the FIELD_DATA element within the SECURITY_DATA element
     * @param result the {@link RequestResult} to be filled
     */
    protected static <T extends AbstractRequestResult> void parseSecurityData(Element securityData, SecurityDataParser<T> parser, T result) {
        String security = securityData.getElementAsString(SECURITY);
        if (securityData.hasElement(SECURITY_ERROR.asName(), true)) {
            Element errorInfo = securityData.getElement(SECURITY_ERROR.asName());
            logger.info("Security error on {}: {}", security, errorInfo);
            addSecurityError(result, security);
        } else if (securityData.hasElement(FIELD_EXCEPTIONS.asName(), true)) {
            Element fieldExceptionsArray = securityData.getElement(FIELD_EXCEPTIONS.asName());
            parseFieldExceptionsArray(result, fieldExceptionsArray);
        }
        if (securityData.hasElement(FIELD_DATA.asName(), true)) {
            Element fieldDataArray = securityData.getElement(FIELD_DATA.asName());
            parser.parse(fieldDataArray, security, result);
        }
    }

    private static <T extends AbstractRequestResult> void addSecurityError(final T result, final String security) {
        result.addSecurityError(security);
    }

    /**
     * Adds the field exceptions to the RequestResult object. Assumes that one field can't generate more than one exception.
     * In other words, we assume that if there are several exceptions, each corresponds to a different field.
     */
    private static <T extends AbstractRequestResult> void parseFieldExceptionsArray(final T result, final Element fieldExceptionsArray) {
        for (int i = 0; i < fieldExceptionsArray.numValues(); i++) {
            Element fieldException = fieldExceptionsArray.getValueAsElement(i);
            String field = fieldException.getElementAsString("fieldId");
            Element errorInfo = fieldException.getElement(ERROR_INFO);
            logger.info("Field exception on {}: {}", field, errorInfo);
            result.addFieldError(field);
        }
    }

    @FunctionalInterface
    protected interface SecurityDataParser<T extends AbstractRequestResult> {
        void parse(Element data, String security, T result);
    }

    /**
     * Some shared element names
     */
    protected static final Name ERROR_INFO = new Name("errorInfo");
    protected static final Name SECURITY_DATA = new Name("securityData");
    protected static final Name SECURITY = new Name("security");
    protected static final Name DATE = new Name("date");

    protected enum SecurityDataElements {

        SECURITY("security"),
        SEQUENCE_NUMBER("sequenceNumber"),
        FIELD_DATA("fieldData"),
        FIELD_EXCEPTIONS("fieldExceptions"),
        SECURITY_ERROR("securityError");
        private final Name elementName;

        SecurityDataElements(String elementName) {
            this.elementName = new Name(elementName);
        }

        protected Name asName() {
            return elementName;
        }
    }

    private static final Name RESPONSE_ERROR = new Name("responseError");

    private enum ErrorInfoElements {

        SOURCE("source"),
        CODE("code"),
        CATEGORY("category"),
        MESSAGE("message"),
        SUB_CATEGORY("subcategory");
        private final Name elementName;

        ErrorInfoElements(String elementName) {
            this.elementName = new Name(elementName);
        }

        private Name asName() {
            return elementName;
        }
    }
}
