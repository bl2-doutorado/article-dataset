package com.canalbl2.planner.features.domain;

public class SustainabilityPolicy extends Policy {

  public SustainabilityPolicy(String name, Float maxTotalCarbonFootprint) {
    super(name);
    this.maxTotalCarbonFootprint = maxTotalCarbonFootprint;
  }

  private Float maxTotalCarbonFootprint;

  public Float getMaxTotalCarbonFootprint() {
    return maxTotalCarbonFootprint;
  }
}
