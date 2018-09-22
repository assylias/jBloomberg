package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Name;

class TokenResultParser extends AbstractResultParser<TokenResultParser.Result> {
    private static final Name TOKEN_SUCCESS = Name.getName("TokenGenerationSuccess");
    private static final Name TOKEN_FAILURE = Name.getName("TokenGenerationFailure");

    @Override
    protected Result getRequestResult() {
        return new Result();
    }

    @Override
    protected void parseResponseNoError(final Element response, final Result result) {
        if (response.name().equals(TOKEN_SUCCESS)) {
            result.token = response.getElementAsString("token");
        } else if (response.name().equals(TOKEN_FAILURE)) {
            Element reason = response.getElement("reason");
            result.error = String.format("%s - %s - %s", reason.getElementAsString("category"), reason.getElementAsString("subcategory"), reason.getElementAsString("description"));
        }
    }

    final class Result extends AbstractRequestResult {
        private String token;
        private String error;

        @Override
        public boolean isEmpty() {
            return token == null;
        }

        public String getToken() {
            return token;
        }

        public String getError() {
            return error;
        }
    }
}
