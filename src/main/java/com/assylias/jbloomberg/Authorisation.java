/*
 * Copyright (C) 2012 - present by Yann Le Tallec.
 * Please see distribution for license.
 */

package com.assylias.jbloomberg;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Identity;
import com.bloomberglp.blpapi.Name;
import com.bloomberglp.blpapi.Request;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Authorisation {

  Identity getIdentity(Function<Consumer<Request>, Identity> identityProvider, Supplier<String> tokenProvider);

  final class Desktop implements Authorisation {
    @Override public Identity getIdentity(Function<Consumer<Request>, Identity> identityProvider, Supplier<String> tokenProvider) {
      return null;
    }

    @Override public String toString() {
      return "Desktop API";
    }
  }

  final class ServerApi implements Authorisation {
    private final int uuid;
    private final String ipAddress;

    public ServerApi(int uuid, String ipAddress) {
      this.uuid = uuid;
      this.ipAddress = ipAddress;
    }
    @Override public Identity getIdentity(Function<Consumer<Request>, Identity> identityProvider, Supplier<String> tokenProvider) {
      return identityProvider.apply(authRequest -> {
        authRequest.set("uuid", uuid);
        authRequest.set("ipAddress", ipAddress);
      });
    }
    @Override public String toString() {
      return "Server API: uuid = " + uuid + ", ipAddress = " + ipAddress;
    }
  }

  final class EnterpriseId implements Authorisation {
    private final String authId;
    private final String ipAddress;
    private final String appName;

    public EnterpriseId(String authId, String ipAddress) {
      this(authId, ipAddress, null);
    }

    public EnterpriseId(String authId, String ipAddress, String appName) {
      this.authId = authId;
      this.ipAddress = ipAddress;
      this.appName = appName;
    }

    @Override public Identity getIdentity(Function<Consumer<Request>, Identity> identityProvider, Supplier<String> tokenProvider) {
      return identityProvider.apply(authRequest -> {
        authRequest.set("authId", authId);
        authRequest.set("ipAddress", ipAddress);
        if (appName != null) authRequest.set("appName", appName);
      });
    }
    @Override public String toString() {
      return "Enterprise using authId: authId = " + authId + ", ipAddress = " + ipAddress + ", appName = " + appName;
    }
  }

  final class EnterpriseToken implements Authorisation {

    @Override public Identity getIdentity(Function<Consumer<Request>, Identity> identityProvider, Supplier<String> tokenProvider) {
      String token = tokenProvider.get();
      return identityProvider.apply(authRequest -> authRequest.set("token", token));
    }
    @Override public String toString() {
      return "Enterprise using token";
    }
  }
}

final class AuthorisationResultParser extends AbstractResultParser<AuthorisationResultParser.Result> {
  private static final Name AUTHORISATION_SUCCESS = Name.getName("AuthorizationSuccess");
  @Override protected Result getRequestResult() {
    return new Result();
  }
  @Override protected void parseResponseNoError(final Element response, final Result result) {
    if (response.name().equals(AUTHORISATION_SUCCESS)) {
      result.authorised = true;
    } else {
      Element reason = response.getElement("reason");
      result.error = String.format("%s - %s - %s", reason.getElementAsString("category"), reason.getElementAsString("subcategory"), reason.getElementAsString("message"));
    }
  }

  final class Result extends AbstractRequestResult {
    private boolean authorised;
    private String error;
    @Override public boolean isEmpty() { throw new UnsupportedOperationException("isEmpty is not a valid method in IdentityResult"); }
    public boolean isAuthorised() { return authorised; }
    public String getError() { return error; }
  }
}

final class TokenResultParser extends AbstractResultParser<TokenResultParser.Result> {
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