package com.hvitops.records;

import io.micronaut.runtime.Micronaut;

public class HvitopsRecordsApplication {
  public static void main(String[] args) {
    // Micronaut.build(args)
    //     .mainClass(HvitopsRecordsApplication.class)
    //     .start()
    //     .getEnvironment()
    //     .getProperty("micronaut.server.port", Integer.class)
    //     .ifPresent(port -> System.out.println(">>> PORTA CONFIGURADA: " + port));
    Micronaut.run(HvitopsRecordsApplication.class, args);
  }
}
