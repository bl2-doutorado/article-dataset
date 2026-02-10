package com.canalbl2.planner.features.parser;

import com.canalbl2.planner.features.domain.Policy;
import java.util.List;

public class CloudDistribuitionPolicy extends Policy {

  public CloudDistribuitionPolicy(
      String name, String strategy, Integer minProviders, List<String> allowedProviders)
      throws Exception {
    super(name);
    this.validateStrategy(strategy);
    this.strategy = strategy;
    this.validateAllowedProviders(allowedProviders);
    this.allowedProviders = allowedProviders;
    this.minProviders = minProviders;
    if (minProviders > 3) {
      throw new Exception("Maximum providers is 3");
    }
  }

  private void validateAllowedProviders(List<String> allowedProviders) throws Exception {
    List<String> knownProviders = List.of("aws", "gcp", "oci");
    for (String allowedProvider : allowedProviders) {

      if (!knownProviders.contains(allowedProvider)) {
        throw new Exception("Provider {%s} not allowed".formatted(allowedProvider));
      }
    }
  }

  private void validateStrategy(String strategy) throws Exception {
    boolean isValid = List.of("mirror", "distributed").contains(strategy);
    if (!isValid) {
      throw new Exception("Strategy not allowed");
    }
  }

  private String strategy;

  private Integer minProviders;

  private List<String> allowedProviders;

  public String getStrategy() {
    return strategy;
  }

  public Integer getMinProviders() {
    return minProviders;
  }

  public List<String> getAllowedProviders() {
    return allowedProviders;
  }
}
