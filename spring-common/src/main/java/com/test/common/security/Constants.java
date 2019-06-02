package com.test.common.security;

import io.grpc.Metadata;

public class Constants {

  public static final Metadata.Key<String> TOKEN_CREDENTIAL_VALUE = Metadata.Key.of("token.cred.value",
      Metadata.ASCII_STRING_MARSHALLER);
  public static final Metadata.Key<String> TOKEN_CREDENTIAL_TYPE = Metadata.Key.of("token.cred.type",
      Metadata.ASCII_STRING_MARSHALLER);
  public static final String TOKEN_TYPE_JWT = "jwt";
}
