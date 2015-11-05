/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */
package com.assylias.jbloomberg;

import static com.assylias.jbloomberg.AbstractResultParser.SecurityDataElements.FIELD_DATA;
import static com.assylias.jbloomberg.AbstractResultParser.SecurityDataElements.FIELD_EXCEPTIONS;
import static com.assylias.jbloomberg.AbstractResultParser.SecurityDataElements.SECURITY;
import static com.assylias.jbloomberg.AbstractResultParser.SecurityDataElements.SECURITY_ERROR;
import static com.assylias.jbloomberg.AbstractResultParser.SecurityDataElements.SEQUENCE_NUMBER;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.InvalidConversionException;
import com.bloomberglp.blpapi.InvalidRequestException;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.Name;
import com.bloomberglp.blpapi.NotFoundException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class to parse results from requests.
 * This implementation is thread safe as the Bloomberg API might send results through more than one thread.
 */
abstract class AbstractResultParser<T extends AbstractRequestResult> implements ResultParser<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractResultParser.class);
    protected final static DateTimeFormatter BB_RESULT_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; //'2011-12-03'
    protected final static DateTimeFormatter BB_RESULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    /**
     * lock used to access messages and parsedData
     */
    private final Object lock = new Object();
    /**
     * List of received messages - guarded by lock
     */
    private final List<Message> messages = new ArrayList<>();
    /**
     * Whether additional messages should be expected or not.
     */
    private final CountDownLatch noMoreMessages = new CountDownLatch(1);
    /**
     * The result of the parsing operation - guarded by lock
     */
    private T result;

    @Override
    public void addMessage(Message msg) {
        if (noMoreMessages.getCount() == 0) {
            throw new IllegalStateException("Can't add messages once noMoreMessages has been called");
        }
        synchronized (lock) {
            messages.add(msg);
        }
    }

    @Override
    public void noMoreMessages() {
        if (noMoreMessages.getCount() == 0) {
            throw new IllegalStateException("This method should not be called more than once");
        }
        noMoreMessages.countDown();
    }

    @Override
    public T getResult() throws InterruptedException {
        noMoreMessages.await();
        return getResultNoWait();
    }

    @Override
    public T getResult(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (!noMoreMessages.await(timeout, unit)) {
            throw new TimeoutException("Could not compute the result within " + timeout + " " + unit.toString().toLowerCase(Locale.ENGLISH));
        }
        return getResultNoWait();
    }

    private T getResultNoWait() {
        synchronized (lock) {
            if (result == null) {
                result = getRequestResult();
                parse(messages);
            }
        }
        return result;
    }
    /**
     * Some shared element names
     */
    protected static final Name ERROR_INFO = new Name("errorInfo");
    protected static final Name SECURITY_DATA = new Name("securityData");
    protected static final Name SECURITY = new Name("security");
    protected static final Name DATE = new Name("date");

    protected static enum SecurityDataElements {

        SECURITY("security"),
        SEQUENCE_NUMBER("sequenceNumber"),
        FIELD_DATA("fieldData"),
        FIELD_EXCEPTIONS("fieldExceptions"),
        SECURITY_ERROR("securityError");
        private final Name elementName;

        private SecurityDataElements(String elementName) {
            this.elementName = new Name(elementName);
        }

        protected Name asName() {
            return elementName;
        }
    }
    private static final Name RESPONSE_ERROR = new Name("responseError");

    private static enum ErrorInfoElements {

        SOURCE("source"),
        CODE("code"),
        CATEGORY("category"),
        MESSAGE("message"),
        SUB_CATEGORY("subcategory");
        private final Name elementName;

        private ErrorInfoElements(String elementName) {
            this.elementName = new Name(elementName);
        }

        private Name asName() {
            return elementName;
        }
    }

    protected abstract T getRequestResult();

    private void parse(List<Message> messages) {
        for (Message msg : messages) {
            Element response = msg.asElement();
            parseResponse(response);
        }
    }

    private void parseResponse(Element response) {
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
        parseResponseNoResponseError(response);
    }

    /**
     * @return a string representation of the error
     */
    protected String parseErrorInfo(Element errorInfo) {
        try {
            return errorInfo.getElementAsString(ErrorInfoElements.MESSAGE.asName());
        } catch (NotFoundException | InvalidConversionException e) {
            return "Could not parse the errorInfo element: " + errorInfo;
        }
    }

    /**
     *
     * @param errorInfo an errorInfo element
     *
     * @return true if this error is due to an invalid security
     */
    private boolean isSecurityError(Element errorInfo) {
        return errorInfo.hasElement(ErrorInfoElements.CATEGORY.asName())
                && errorInfo.getElementAsString(ErrorInfoElements.CATEGORY.asName()).equals("BAD_SEC");
    }

    /**
     * Trying to use the most specific primitive type.
     * Primitives will get auto-boxed.
     */
    protected void addField(LocalDate date, String security, Element field) {
        String fieldName = field.name().toString();
        Object value = BloombergUtils.getSpecificObjectOf(field);
        result.add(date, security, fieldName, value);
    }

    protected void addField(OffsetDateTime date, Element field) {
        String fieldName = field.name().toString();
        Object value = BloombergUtils.getSpecificObjectOf(field);
        result.add(date, fieldName, value);
    }

    protected void addField(String security, Element field) {
        String fieldName = field.name().toString();
        Object value = BloombergUtils.getSpecificObjectOf(field);
        result.add(security, fieldName, value);
    }

    protected void addSecurityError(String security) {
        result.addSecurityError(security);
    }

    protected void addFieldError(String field) {
        result.addFieldError(field);
    }

    /**
     * This method must parse the valid part of the response (typically, the securityData or barData Element
     *
     * @param response The whole response element, including the responseError element if any (in which case it was
     *                 empty).
     */
    protected abstract void parseResponseNoResponseError(Element response);

    protected void parseSecurityData(Element securityData) {
        String security = securityData.getElementAsString(SECURITY);
        if (securityData.hasElement(SECURITY_ERROR.asName(), true)) {
            Element errorInfo = securityData.getElement(SECURITY_ERROR.asName());
            logger.info("Security error on {}: {}", security, errorInfo);
            addSecurityError(security);
        } else if (securityData.hasElement(FIELD_EXCEPTIONS.asName(), true)) {
            Element fieldExceptionsArray = securityData.getElement(FIELD_EXCEPTIONS.asName());
            parseFieldExceptionsArray(fieldExceptionsArray);
        }
        if (securityData.hasElement(FIELD_DATA.asName(), true)) {
            Element fieldDataArray = securityData.getElement(FIELD_DATA.asName());
            parseFieldDataArray(security, fieldDataArray);
        }
    }

    /**
     * Adds the field exceptions to the MultipleRequestResult object. Assumes that one field can't generate more than
     * one
     * exception.
     * In other words, we assume that if there are several exceptions, each corresponds to a different field.
     */
    protected void parseFieldExceptionsArray(Element fieldExceptionsArray) {
        for (int i = 0; i < fieldExceptionsArray.numValues(); i++) {
            Element fieldException = fieldExceptionsArray.getValueAsElement(i);
            String field = fieldException.getElementAsString("fieldId");
            Element errorInfo = fieldException.getElement(ERROR_INFO);
            logger.info("Field exception on {}: {}", field, errorInfo);
            addFieldError(field);
        }
    }

    protected void parseFieldDataArray(String security, Element fieldDataArray) {
        //does nothing here - needs to be implemented by subclasses if required
    }
}
