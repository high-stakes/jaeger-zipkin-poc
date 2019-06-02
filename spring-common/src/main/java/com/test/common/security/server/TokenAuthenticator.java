package com.test.common.security.server;

public interface TokenAuthenticator {

  void authenticate(String tokenValue, String tokenType);
}
