package com.test.common.security.server;

import com.test.common.security.Constants;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

public class ServerTokenAuthInterceptor implements ServerInterceptor {

  private TokenAuthenticator tokenAuthenticator;

  public ServerTokenAuthInterceptor(TokenAuthenticator tokenAuthenticator) {
    this.tokenAuthenticator = tokenAuthenticator;
  }

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
      Metadata headers, ServerCallHandler<ReqT, RespT> next) {
    String tokenValue = headers.get(Constants.TOKEN_CREDENTIAL_VALUE);
    String tokenType = headers.get(Constants.TOKEN_CREDENTIAL_TYPE);
    tokenAuthenticator.authenticate(tokenValue, tokenType);

    return next.startCall(call, headers);
  }
}
