package com.canalbl2.planner.features.parser;

import com.canalbl2.planner.features.domain.Policy;
import com.canalbl2.planner.features.domain.ServiceData;
import com.canalbl2.planner.features.domain.SustainabilityPolicy;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.Yaml;

/** Parses a TOSCA template to get the required data to call the MILP. */
public class ToscaParser {
  private static final Pattern RESOURCE_PATTERN = Pattern.compile("^(\\d+\\.?\\d*)\\s*([a-zA-Z]*)");

  /**
   * Parses the template to identify all declared applications, with the number of instances, vcpus
   * and memory of each application.
   *
   * @param filePath Template's file path
   * @return A list of applications
   * @throws Exception
   */
  public List<ServiceData> parseYamlServices(String filePath) throws Exception {
    Yaml yaml = new Yaml();
    List<ServiceData> services = new ArrayList<>();
    try (InputStream in = new FileInputStream(filePath)) {
      Map<String, Object> data = yaml.load(in);
      Map<String, Object> nodeTemplates =
          (Map<String, Object>)
              ((Map<String, Object>) data.get("service_template")).get("node_templates");

      for (Map.Entry<String, Object> entry : nodeTemplates.entrySet()) {
        Map<String, Object> node = (Map<String, Object>) entry.getValue();
        String nodeName = entry.getKey();
        String type = (String) node.get("type");
        List<String> applicationTypes =
            List.of(
                "cloud_native.nodes.Service",
                "cloud_native.nodes.BackingService",
                "cloud_native.nodes.StorageBackingService",
                "cloud_native.nodes.AbstractComponent",
                "cloud_native.nodes.Kubernetes.KubernetesApplication",
                "cloud_native.nodes.Kubernetes.KubernetesDependency",
                "cloud_native.nodes.Kubernetes.KubernetesStorageDependency");

        if (applicationTypes.contains(type)) {
          services.add(processServiceNode(nodeName, node));
        }
      }
    }
    return services;
  }

  /**
   * Parse the templates to identify policies used.
   *
   * @param filePath Template's file path
   * @return List of policies.
   * @throws Exception
   */
  public List<Policy> parseYamlPolicies(String filePath) throws Exception {
    Yaml yaml = new Yaml();
    List<Policy> appliedPolicies = new ArrayList<>();
    try (InputStream in = new FileInputStream(filePath)) {
      Map<String, Object> data = yaml.load(in);
      List<Map<String, Object>> policies = (List<Map<String, Object>>) data.get("policies");

      if (policies != null) {
        for (Map<String, Object> policyWrapper : policies) {
          for (Map.Entry<String, Object> entry : policyWrapper.entrySet()) {
            String policyName = entry.getKey();
            Map<String, Object> policyData = (Map<String, Object>) entry.getValue();

            String type = (String) policyData.get("type");
            System.out.println("Processing Policy: " + policyName + " of type: " + type);

            if (type != null) {
              switch (type) {
                case "cloud_native.policies.Sustainability":
                  SustainabilityPolicy sustainabilityPolicy =
                      createSustainabilityPolicy(policyName, policyData);
                  appliedPolicies.add(sustainabilityPolicy);
                  break;
                case "cloud_native.policies.CloudDistribution":
                  CloudDistribuitionPolicy mirroringPolicy =
                      createCloudDistribuitionPolicy(policyName, policyData);
                  appliedPolicies.add(mirroringPolicy);
                  break;
                default:
                  System.out.println("Policy Type Unknown: " + type);
                  break;
              }
            }
          }
        }
        System.out.println("Number of policies: {%d}".formatted(appliedPolicies.size()));
      }
    }
    return appliedPolicies;
  }

  /**
   * Creates an object to represent the cloud distribution policy
   *
   * @param policyName Name of the policy
   * @param policyData Policy data.
   * @return The policy object
   * @throws Exception
   */
  private CloudDistribuitionPolicy createCloudDistribuitionPolicy(
      String policyName, Map<String, Object> policyData) throws Exception {
    Map<String, Object> properties = (Map<String, Object>) policyData.get("properties");
    String strategy = (String) properties.get("strategy");
    Integer minProviders = (Integer) properties.get("min_providers");
    List<String> allowedProviders = (List<String>) properties.get("allowed_providers");
    return new CloudDistribuitionPolicy(policyName, strategy, minProviders, allowedProviders);
  }

  /**
   * Creates an object to represent the cloud distribution policy
   *
   * @param policyName Name of the policy
   * @param policyData Policy data.
   * @return The policy object
   * @throws Exception
   */
  private SustainabilityPolicy createSustainabilityPolicy(
      String policyName, Map<String, Object> entry) {
    Map<String, Object> props = (Map<String, Object>) entry.get("properties");
    Object footprint = props.get("max_total_carbon_footprint");

    // Se for um valor numérico direto
    if (footprint instanceof Number) {
      return new SustainabilityPolicy(policyName, ((Number) footprint).floatValue());
    }
    // Se for uma função como { $get_input: carbon_limit }
    else if (footprint instanceof Map) {
      // Lógica para buscar nos inputs do template se necessário
      System.out.println("Atenção: Valor de carbono é dinâmico (get_input)");
    }

    return new SustainabilityPolicy(policyName, Float.MAX_VALUE);
  }

  /**
   * Creates an object representing a specific service node.
   *
   * @param nodeName
   * @param node
   * @return The service node.
   */
  private ServiceData processServiceNode(String nodeName, Map<String, Object> node) {
    ServiceData serviceData;
    Map<String, Object> props = (Map<String, Object>) node.get("properties");
    List<Map<String, Object>> containers = (List<Map<String, Object>>) props.get("containers");
    Map<String, Integer> scalability = (Map<String, Integer>) props.get("scalability");
    float totalCpuM = 0; // Total em millicores
    float totalMemoryMi = 0; // Total em MiB
    for (Map<String, Object> container : containers) {
      Map<String, String> resources = (Map<String, String>) container.get("resources");
      if (resources != null) {
        totalCpuM += convertCpuToMillis(resources.get("cpu_min"));
        totalMemoryMi += convertMemoryToMiB(resources.get("mem_min"));
      }
    }
    Integer instances = scalability.get("default_instances");
    serviceData = new ServiceData(nodeName, totalCpuM, totalMemoryMi, instances);
    return serviceData;
  }

  /** Normalizes CPU to millicores (m). 1 core = 1000m */
  private double convertCpuToMillis(String input) {
    if (input == null) return 0;
    Matcher m = RESOURCE_PATTERN.matcher(input.trim());
    if (m.find()) {
      double value = Double.parseDouble(m.group(1));
      String unit = m.group(2).toLowerCase();

      return unit.equals("m") ? value : value * 1000;
    }
    return 0;
  }

  /** Normalizes Memory to MiB. Supports Ki, Mi, Gi, Ti. */
  private double convertMemoryToMiB(String input) {
    if (input == null) return 0;
    Matcher m = RESOURCE_PATTERN.matcher(input.trim());
    if (m.find()) {
      double value = Double.parseDouble(m.group(1));
      String unit = m.group(2);

      return switch (unit) {
        case "Ki" -> value / 1024.0;
        case "Gi" -> value * 1024.0;
        case "Ti" -> value * 1024.0 * 1024.0;
        case "Mi", "" -> value;
        default -> value;
      };
    }
    return 0;
  }
}
