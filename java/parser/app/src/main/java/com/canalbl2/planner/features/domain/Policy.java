package com.canalbl2.planner.features.domain;

public abstract class Policy {

  public Policy(String name) {
    this.name = name;
  }

  private String name;

  public String getName() {
    return name;
  }
}
