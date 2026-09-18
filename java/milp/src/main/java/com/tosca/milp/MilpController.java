package com.tosca.milp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;

import com.tosca.milp.carbon.CarbonUnit;
import com.tosca.milp.domain.InfrastructureData;
import com.tosca.milp.domain.MachineDetails;
import com.tosca.milp.machineCostReader.MachineCostReader;

import io.micronaut.http.HttpResponse;
// import io.micronaut.core.util.ArrayUtils;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

@Controller("/milp")
public class MilpController {

    public MilpController(MilpService milpService) {
        this.milpService = milpService;
    }

    private MilpService milpService;

    @Post(uri="/", produces= MediaType.APPLICATION_JSON)
    public MutableHttpResponse<Map<String, Object>> index(
      @Body InfrastructureData infrastructureData)
      throws Exception {
    MachineCostReader reader = new MachineCostReader();
    List<MachineDetails> machineModels = reader.readFile(infrastructureData.filename());
    int cloudProvidersQuantity = (int)machineModels.stream()
    .map(MachineDetails::getProvider) // Transforma a stream de máquinas em stream de provedores
    .distinct()                      // Remove os repetidos
    .count();
    long[] machineModelsCosts = initializeMachineModelCosts(machineModels);
    long[] machineModelsMemory = initializeMachineModelsMemory(machineModels);
    long[] machineModelsVCPU = initializeMachineModelsVCPU(machineModels);
    long[] machineModelsCarbonFootPrint = initializeMachineModelsFootprint(machineModels);
    List<String> machineModelNames = machineModels.stream().map(MachineDetails::getName).toList();

    int machineModelsNumber = machineModels.size();
    List<String> cloudProviderNames = this.identifyCloudProviderNames(machineModels);
    Integer[] distribuitionMachineTypesPerCloudProvider = this.createDistributionVector(machineModels, cloudProviderNames);

    Map<String, Object> response = milpService.resolveModel(
        cloudProvidersQuantity,//infrastructureData.getCloudProvidersQuantity(),
        machineModelsNumber,
        infrastructureData.applicationsQuantity(),
        distribuitionMachineTypesPerCloudProvider,
        machineModelsVCPU,
        machineModelsMemory,
        machineModelsCosts,
        infrastructureData.vcpuPerApplication(),
        infrastructureData.memoryPerApplication(),
        infrastructureData.instancesPerApplication(),
        infrastructureData.clusterCostPerCloudProvider  (),
        infrastructureData.fixedCost(),
        machineModelsCarbonFootPrint,
        // infrastructureData.getCarbonFootprintPerMachineType(),
        scaleCarbonLimit(infrastructureData.maxTotalCarbonFootprint()),
        infrastructureData.isMirroringEnabled(),
        infrastructureData.maxTimeInSeconds(),
        infrastructureData.targetCloudCount(),
        machineModelNames,
      cloudProviderNames, infrastructureData.applicationNames());
    return HttpResponse.ok(
        response);
  }

  private long[] initializeMachineModelsFootprint(List<MachineDetails> machineDetailsList) {
    return ArrayUtils.toPrimitive(
        machineDetailsList.stream()
            .map(
                (MachineDetails machineDetails) -> {
                  Double carbonFootprint =
                      (Double) machineDetails.getCarbonFootprint();
                  return CarbonUnit.toScaled(carbonFootprint);
                })
            .collect(Collectors.toList())
            .toArray(new Long[0]));
  }

  /**
   * Scales a tCO2e carbon limit into the solver domain. A null limit (or one so large that it
   * saturates) is treated as unlimited by the estimator.
   */
  private static long scaleCarbonLimit(Double maxTotalCarbonFootprint) {
    if (maxTotalCarbonFootprint == null) {
      return CarbonUnit.UNLIMITED_SCALED;
    }
    return CarbonUnit.toScaled(maxTotalCarbonFootprint);
  }

  

  private static long[] initializeMachineModelCosts(List<MachineDetails> machineDetailsList) {
    return ArrayUtils.toPrimitive(
        machineDetailsList.stream()
            .map(
                (MachineDetails machineDetails) -> {
                  return ((Double) Math.ceil(machineDetails.getPrice() * 100)).longValue();
                })
            .collect(Collectors.toList())
            .toArray(new Long[0]));
  }

  private static long[] initializeMachineModelsMemory(List<MachineDetails> machineDetailsList) {
    return ArrayUtils.toPrimitive(
        machineDetailsList.stream()
            .map(
                (MachineDetails machineDetails) -> {
                  return ((Double) Math.ceil(machineDetails.getMemory())).longValue();
                })
            .collect(Collectors.toList())
            .toArray(new Long[0]));
  }

  private static long[] initializeMachineModelsVCPU(List<MachineDetails> machineDetailsList) {
    return ArrayUtils.toPrimitive(
        machineDetailsList.stream()
            .map(
                (MachineDetails machineDetails) -> {
                  return ((Double) Math.ceil(machineDetails.getVCPU())).longValue() * 1000;
                })
            .collect(Collectors.toList())
            .toArray(new Long[0]));
  }

  public List<String> identifyCloudProviderNames(List<MachineDetails> machineModels) {
    List<String> uniqueProviders = machineModels.stream()
                .map(MachineDetails::getProvider)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
                return uniqueProviders;
  } 

  /**
   * Given a list of machine models and a lsit of cloud providers, create a distribution vector
   * @param machineModels
   * @param uniqueProviders
   * @return
   */
  public Integer[] createDistributionVector(List<MachineDetails> machineModels, List<String> uniqueProviders) {
        Map<String, Integer> providerToIndex = IntStream.range(0, uniqueProviders.size())
                .boxed()
                .collect(Collectors.toMap(uniqueProviders::get, i -> i));
        return machineModels.stream()
                .map(m -> providerToIndex.getOrDefault(m.getProvider(), -1))
                .collect(Collectors.toList()).toArray(Integer[]::new);
    }
}