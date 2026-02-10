package com.canalbl2.planner.features.domain;


public record ServiceData(String name, Float vcpu, Float memory, Integer instances ) {

  @Override
  public final String toString() {
    return "name: {%s} - vcpu: {%.2f}, memory: {%.2f}, instances: {%d}".formatted(vcpu, memory, instances); 
  }

  public Float vcpu() {
    return vcpu;
  }

  public Float memory() {
    return memory;
  }

  public Integer instances() {
    return instances;
  }

  public String name() {
    return name;
  }
  
}
