package com.test.common.security.server.spring;

import com.test.common.security.server.TokenAuthenticator;
import java.util.LinkedHashMap;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.springframework.security.util.SpringSecurityHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.token.TokenService;

public class SpringTokenAuthenticator implements TokenAuthenticator {

  private AuthenticationManager authenticationManager;
  private Authenticator<TokenCredentials> credentialsAuthenticator;

  public SpringTokenAuthenticator(AuthenticationManager authenticationManager,
      Authenticator<TokenCredentials> credentialsAuthenticator) {
    this.credentialsAuthenticator = credentialsAuthenticator;
    this.authenticationManager = authenticationManager;
  }

  @Override public void authenticate(String tokenValue, String tokenType) {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    TokenCredentials tokenCredentials = new TokenCredentials(tokenValue);
    credentialsAuthenticator.validate(tokenCredentials,null);
    LinkedHashMap<String, CommonProfile> profileMap = new LinkedHashMap<>();
    profileMap.put("authenticatedUser", tokenCredentials.getUserProfile());
    SpringSecurityHelper.populateAuthentication(profileMap);
  }
}
