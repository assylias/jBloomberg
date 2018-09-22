package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Name;

public class AuthorizationResultParser extends AbstractResultParser<AuthorizationResultParser.Result> {
    private static final Name AUTHORIZATION_SUCCESS = Name.getName("AuthorizationSuccess");
    private static final Name AUTHORIZATION_FAILURE = Name.getName("AuthorizationFailure");

    @Override
    protected Result getRequestResult() {
        return new Result();
    }

    @Override
    protected void parseResponseNoError(final Element response, final Result result) {
        if (response.name().equals(AUTHORIZATION_SUCCESS)) {
            result.authorized = true;
        } else {
            Element reason = response.getElement("reason");
            result.error = String.format("%s - %s - %s", reason.getElementAsString("category"), reason.getElementAsString("subcategory"), reason.getElementAsString("description"));
        }
    }

    final class Result extends AbstractRequestResult {
        private boolean authorized;
        private String error;

        @Override
        public boolean isEmpty() {
            return !authorized && error == null;
        }

        public boolean isAuthorized() {
            return authorized;
        }

        public String getError() {
            return error;
        }
    }
}
