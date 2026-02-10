package com.tosca.milp;

import com.google.ortools.Loader;
import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.info.*;
import org.slf4j.bridge.SLF4JBridgeHandler;

@OpenAPIDefinition(info = @Info(title = "milp", version = "1.0"))
public class Application {

  public static void main(String[] args) {
    Loader.loadNativeLibraries();
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
    Micronaut.run(Application.class, args);
  }
}
