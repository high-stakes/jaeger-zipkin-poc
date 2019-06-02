package com.test.common.security.client;

import com.test.common.security.Constants;

public class JWTToken implements Token {

  private String value;

  public JWTToken(String value) {
    this.value = value;
  }

  @Override public String getValue() {
    return value;
  }

  @Override public String getType() {
    return Constants.TOKEN_TYPE_JWT;
  }
}
