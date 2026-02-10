package com.tosca.milp.domain;


import java.util.List;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.annotation.Serdeable;

/** Infrastructure data */
@Introspected
@Serdeable
public record InfrastructureData(int cloudProvidersQuantity,
  int applicationsQuantity,
  int machineTypesQuantity,
  Integer[] distribuitionMachineTypesPerCloudProvider,
  long[] vcpuPerMachineType,
  long[] memoryPerMachineType,
  long[] costPerMachineType,
  long[] vcpuPerApplication,
  long[] memoryPerApplication,
  long[] instancesPerApplication,
  List<String> applicationNames,
  long[] clusterCostPerCloudProvider,
  long fixedCost,
  long[] carbonFootprintPerMachineType,
  Double maxTotalCarbonFootprint,
  int targetCloudCount,
  boolean isMirroringEnabled,
  int maxTimeInSeconds,
  @NonNull String filename) {

}