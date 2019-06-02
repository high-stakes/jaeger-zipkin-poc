package com.test.common.security.client;

import com.test.common.security.Constants;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

public class ClientTokenAuthterceptor implements ClientInterceptor {

    private TokenProvier tokenProvider;

    public ClientTokenAuthterceptor(TokenProvier tokenProvier) {
      this.tokenProvider = tokenProvier;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
        MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
      return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {
        @Override
        public void start(Listener<RespT> responseListener, Metadata headers) {
          Token token = tokenProvider.getToken();
          headers.put(Constants.TOKEN_CREDENTIAL_VALUE, token.getValue());
          headers.put(Constants.TOKEN_CREDENTIAL_TYPE, token.getType());
          super.start(responseListener, headers);
        }
      };
    }
  }