package com.canalbl2.planner.features.domain;

public class SustainabilityPolicy extends Policy {

  /** tCO2e sentinel meaning "no carbon limit" (matches the CLI default). */
  public static final Float UNLIMITED = Float.MAX_VALUE;

  public SustainabilityPolicy(String name, Float maxTotalCarbonFootprint) {
    super(name);
    this.maxTotalCarbonFootprint = maxTotalCarbonFootprint;
  }

  private Float maxTotalCarbonFootprint;

  public Float getMaxTotalCarbonFootprint() {
    return maxTotalCarbonFootprint;
  }
}
