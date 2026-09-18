package com.tosca.milp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.ortools.Loader;
import com.tosca.milp.carbon.CarbonUnit;
import com.tosca.milp.infrastructureEstimator.CloudInfrastructureEstimator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Locks the carbon unit contract end-to-end: a machine type whose CSV footprint is known SHALL
 * report {@code carbonEmission == csv value} in tCO2e, and {@code totalCarbon} SHALL equal the sum
 * of per-machine emissions. An unlimited request SHALL still solve.
 */
class MilpCarbonEstimationTest {

  private static final double CSV_FOOTPRINT_M5A_LARGE = 0.001331689050; // AWS, tCO2e

  @BeforeAll
  static void loadOrtools() {
    Loader.loadNativeLibraries();
  }

  @Test
  void carbonEmissionMatchesCsvAndTotalSumsPerMachine() {
    Map<String, Object> response = solve(CarbonUnit.toScaled(0.005 /* tCO2e limit, feasible */));

    double totalCarbon = ((Number) response.get("totalCarbon")).doubleValue();
    List<Map<String, Object>> instances = instances(response);

    assertTrue(!instances.isEmpty(), "Expected at least one machine instance");
    for (Map<String, Object> instance : instances) {
      double emission = ((Number) instance.get("carbonEmission")).doubleValue();
      // The per-machine emission SHALL equal the CSV footprint (not 10x it).
      assertEquals(CSV_FOOTPRINT_M5A_LARGE, emission, 1e-6);
    }

    double sumEmissions =
        instances.stream()
            .mapToDouble(i -> ((Number) i.get("carbonEmission")).doubleValue())
            .sum();
    assertEquals(sumEmissions, totalCarbon, 1e-6);
  }

  @Test
  void unlimitedRequestStillSolves() {
    Map<String, Object> response = solve(CarbonUnit.UNLIMITED_SCALED);

    assertNotNull(response.get("totalCarbon"));
    assertTrue(!instances(response).isEmpty());
    // Unlimited reports the actual total carbon of the chosen allocation.
    assertTrue(((Number) response.get("totalCarbon")).doubleValue() > 0);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> infrastructure(Map<String, Object> response) {
    return (Map<String, Object>) response.get("infrastructure");
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> instances(Map<String, Object> response) {
    return (List<Map<String, Object>>) infrastructure(response).get("instances");
  }

  private Map<String, Object> solve(long maxCarbonScaled) {
    CloudInfrastructureEstimator estimator = new CloudInfrastructureEstimator();

    return estimator.resolveModel(
        1, // cloudProvidersQuantity
        1, // machineTypesQuantity
        1, // applicationsQuantity
        new Integer[] {0}, // distribuitionMachineTypesPerCloudProvider
        new long[] {2000}, // vcpuPerMachineType (2 vCPU * 1000)
        new long[] {8192}, // memoryPerMachineType (8 GiB in MiB)
        new long[] {6278}, // costPerMachineType (ceil(62.78 * 100))
        new long[] {1500}, // vcpuPerApplication (1.5 core)
        new long[] {4096}, // memoryPerApplication
        new long[] {1}, // instancesPerApplication
        null, // clusterCostPerCloudProvider (unset → estimator defaults to zeros)
        0L, // fixedCost
        new long[] {CarbonUnit.toScaled(CSV_FOOTPRINT_M5A_LARGE)}, // carbon scaling slot
        maxCarbonScaled,
        false, // isMirroringEnabled
        10, // maxTimeInSeconds
        1, // targetCloudCount
        List.of("m5a.large"),
        List.of("AWS"),
        List.of("app-01"));
  }
}