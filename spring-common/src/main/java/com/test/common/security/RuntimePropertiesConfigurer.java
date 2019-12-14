package com.test.common.security;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

public class RuntimePropertiesConfigurer implements
    ApplicationListener<ApplicationEnvironmentPreparedEvent> {
  public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
    ConfigurableEnvironment environment = event.getEnvironment();
    Properties props = new Properties();
    String appname = environment.getProperty("spring.application.name");
    props.put("spring.zipkin.service.name",appname + ":" + hostName());
    environment.getPropertySources().addFirst(new PropertiesPropertySource("myProps", props));
  }

  public static String hostName() {
    InetAddress inetAddress = null;
    try {
      inetAddress = InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    return inetAddress.getHostName();
  }
}
