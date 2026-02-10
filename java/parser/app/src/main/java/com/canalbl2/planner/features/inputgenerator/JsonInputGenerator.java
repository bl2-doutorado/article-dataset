package com.canalbl2.planner.features.inputgenerator;

import java.util.*;
import java.util.stream.Collectors;
import com.canalbl2.planner.features.domain.ServiceData;
public class JsonInputGenerator {


    public static Map<String, Object> generateConfiguration(Integer maxTImeInSeconds, boolean isMirroringEnabled, Float maxTotalCarbonFootprint, Integer targetCloudCount, List<Float> clusterCostPerCloudProvider, Float fixedCost, String costsAndCarbonFootprintFile, List<ServiceData> services) {
        // Criamos um LinkedHashMap para manter a ordem das chaves igual ao seu exemplo
        Map<String, Object> root = new LinkedHashMap<>();

        // Parâmetros fixos/parametrizados
        root.put("maxTimeInSeconds", maxTImeInSeconds);
        root.put("isMirroringEnabled", isMirroringEnabled);
        root.put("maxTotalCarbonFootprint", maxTotalCarbonFootprint);
        root.put("targetCloudCount", targetCloudCount);

        // Derivando dados da lista ServiceData
        root.put("applicationsQuantity", services.size());

        // Extraindo vcpuPerApplication
        root.put("vcpuPerApplication", services.stream()
                .map(ServiceData::vcpu)
                .collect(Collectors.toList()));

        // Extraindo memoryPerApplication
        root.put("memoryPerApplication", services.stream()
                .map(ServiceData::memory)
                .collect(Collectors.toList()));

        // Extraindo instancesPerApplication
        root.put("instancesPerApplication", services.stream()
                .map(ServiceData::instances)
                .collect(Collectors.toList()));

        root.put("applicationNames", services.stream()
                .map(ServiceData::name)
                .collect(Collectors.toList()));


        // Restante dos parâmetros fixos
        root.put("clusterCostPerCloudProvider", clusterCostPerCloudProvider);
        root.put("fixedCost", fixedCost);
        root.put("filename", costsAndCarbonFootprintFile);

        return root;
    }
}

