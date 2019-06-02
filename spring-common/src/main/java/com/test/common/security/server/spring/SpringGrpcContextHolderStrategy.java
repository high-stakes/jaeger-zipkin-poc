package com.test.common.security.server.spring;


import io.grpc.Context;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.util.Assert;

public class SpringGrpcContextHolderStrategy implements SecurityContextHolderStrategy {
  private static final Context.Key<SecurityContextWrapper> contextHolder = Context.key("spring.security.context");

  public static void register() {
    SecurityContextHolder.setStrategyName(SpringGrpcContextHolderStrategy.class.getName());
  }

  @Override public void clearContext() {
    contextHolder.get().setSecurityContext(null);
  }

  @Override public SecurityContext getContext() {
    SecurityContext ctx = contextHolder.get().getSecurityContext();
    if (ctx == null) {
      ctx = createEmptyContext();
      contextHolder.get().setSecurityContext(ctx);
    }
    return ctx;
  }

  @Override public void setContext(SecurityContext context) {
    Assert.notNull(context, "Only non-null SecurityContext instances are permitted");
    contextHolder.get().setSecurityContext(context);
  }

  @Override public SecurityContext createEmptyContext() {
    return new SecurityContextImpl();
  }

  private class SecurityContextWrapper {
    private SecurityContext securityContext;

    public SecurityContextWrapper(SecurityContext securityContext) {
      this.securityContext = securityContext;
    }

    public SecurityContext getSecurityContext() {
      return securityContext;
    }

    public void setSecurityContext(SecurityContext securityContext) {
      this.securityContext = securityContext;
    }
  }
}
