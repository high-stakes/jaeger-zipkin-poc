package com.test.common.security.server;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@GRpcGlobalInterceptor
@Order(100)
public class ServerAnonAuthInterceptor implements ServerInterceptor {

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
    if (Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
      SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("anonymous",
          "anonymousUser", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    }
    return next.startCall(call, headers);
  }
}