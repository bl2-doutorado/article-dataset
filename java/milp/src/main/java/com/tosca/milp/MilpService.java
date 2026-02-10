package com.tosca.milp;

import java.util.List;
import java.util.Map;

import com.tosca.milp.infrastructureEstimator.CloudInfrastructureEstimator;

import jakarta.inject.Singleton;

@Singleton
public class MilpService {

    public Map<String,Object> resolveModel(int cloudProvidersQuantity, int machineModelsNumber, int applicationsQuantity,
        Integer[] distribuitionMachineTypesPerCloudProvider, long[] machineModelsVCPU, long[] machineModelsMemory,
        long[] machineModelsCosts, long[] vcpuPerApplication, long[] memoryPerApplication,
        long[] instancesPerApplication, long[] clusterCostPerCloudProvider, long fixedCost,
        long[] machineModelsCarbonFootPrint, long maxTotalCarbonFootprint, boolean mirroringEnabled, int maxTimeInSeconds,
        int targetCloudCount, List<String> machineModelNames, List<String> cloudProviderNames,
        List<String> applicationNames) {
      
      CloudInfrastructureEstimator cloudInfrastructureEstimator = new CloudInfrastructureEstimator();
      return cloudInfrastructureEstimator.resolveModel(
        cloudProvidersQuantity,
        machineModelsNumber,
        applicationsQuantity,
        distribuitionMachineTypesPerCloudProvider,
        machineModelsVCPU,
        machineModelsMemory,
        machineModelsCosts,
        vcpuPerApplication,
        memoryPerApplication,
        instancesPerApplication,
        clusterCostPerCloudProvider,
        fixedCost,
        machineModelsCarbonFootPrint,
        maxTotalCarbonFootprint,
        mirroringEnabled,
        maxTimeInSeconds,
        targetCloudCount, machineModelNames,cloudProviderNames,applicationNames);
      
    }
}