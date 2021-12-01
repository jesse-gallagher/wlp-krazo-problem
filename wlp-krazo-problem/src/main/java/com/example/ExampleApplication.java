package com.example;

import java.util.HashMap;
import java.util.Map;

import jakarta.mvc.security.Csrf;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/")
public class ExampleApplication extends Application {
  @Override
  public Map<String, Object> getProperties() {
      Map<String, Object> props = new HashMap<>();
      props.put(Csrf.CSRF_PROTECTION, Csrf.CsrfOptions.EXPLICIT);
      return props;
  }
}
