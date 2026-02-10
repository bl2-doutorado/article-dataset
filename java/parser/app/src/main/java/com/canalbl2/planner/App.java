package com.canalbl2.planner;

import com.canalbl2.planner.features.cloudcostclient.CloudCostClient;
import com.canalbl2.planner.features.domain.Policy;
import com.canalbl2.planner.features.domain.ServiceData;
import com.canalbl2.planner.features.domain.SustainabilityPolicy;
import com.canalbl2.planner.features.parser.CloudDistribuitionPolicy;
import com.canalbl2.planner.features.parser.ToscaParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "topology-price-estimation",
    mixinStandardHelpOptions = true,
    version = "1.0",
    description = "Generates a Physical TOSCA Template with Mininum Cost.")
public class App implements Callable<Integer> {

  static final Logger log = LoggerFactory.getLogger(App.class);

  // Injected by Picocli
  @CommandLine.Spec CommandLine.Model.CommandSpec spec;

  // --- Default values ---
  @Option(
      names = {"-t", "--timeout"},
      description = "Maximum execution time in seconds (default: ${DEFAULT-VALUE})")
  private Integer maxTimeInSeconds = 20;

  @Option(
      names = {"-m", "--mirroring"},
      description = "Enable mirroring (default: ${DEFAULT-VALUE})")
  private boolean mirroringEnabled = false;

  @Option(
      names = {"-c", "--carbon"},
      description = "Carbon Footprint Limit (default: ${DEFAULT-VALUE})")
  private Float maxCarbon = Float.MAX_VALUE;

  @Option(
      names = {"-n", "--clouds"},
      description = "Target Cloud Quantity (default: ${DEFAULT-VALUE})")
  private Integer targetCloudCount = 1;

  @Option(
      names = {"-f", "--fixed-cost"},
      description = "Fixed Cost (default: ${DEFAULT-VALUE})")
  private Float fixedCost = 0f;

  @Option(
      names = {"-r", "--results-dir"},
      description = "Results directory. (default: ${DEFAULT-VALUE})")
  private String outputDirectory = getJarExecutionPath() + "/results";

  @Option(
      names = {"-C", "--cluster-cost-per-cloud-provider"},
      description =
          "Cluster cost per cloud provider. Should have the same size of the analyzed cloud"
              + " providers. (default: ${DEFAULT-VALUE})")
  private List<Float> clusterCostPerCloudProvider = new ArrayList<>();

  @Option(
      names = {"-o", "--output"},
      description = "Optimization result file(JSON)")
  private String outputPath;

  @Option(
      names = {"-u", "--url"},
      description = "API url")
  private String url = "http://localhost:8183/milp";

  // --- Required Parameters ---
  @Parameters(index = "0", description = "TOSCA Template YAML file.")
  private String yamlFilePath;

  @Parameters(index = "1", description = "Costs and Carbon CSV File.")
  private String csvFilePath;

  /** Resolve paths that start with ~ or $HOME */
  private String resolvePath(String path) {
    if (path == null) return null;
    if (path.startsWith("~")) {
      path = System.getProperty("user.home") + path.substring(1);
    }
    if (path.contains("$HOME")) {
      path = path.replace("$HOME", System.getenv("HOME"));
    }

    return path;
  }

  @Override
  public Integer call() throws Exception {
    this.yamlFilePath = resolvePath(this.yamlFilePath);
    this.csvFilePath = resolvePath(this.csvFilePath);

    ToscaParser parser = new ToscaParser();
    List<ServiceData> services = parser.parseYamlServices(yamlFilePath);
    List<Policy> appliedPolicies = parser.parseYamlPolicies(yamlFilePath);
    Float templateMaxCarbon = null;
    for (Policy appliedPolicy : appliedPolicies) {
      if (appliedPolicy.getClass().isAssignableFrom(SustainabilityPolicy.class)) {
        templateMaxCarbon = ((SustainabilityPolicy) appliedPolicy).getMaxTotalCarbonFootprint();
      }
      if (appliedPolicy.getClass().isAssignableFrom(CloudDistribuitionPolicy.class)) {
        CloudDistribuitionPolicy cloudDistributionPolicy = (CloudDistribuitionPolicy) appliedPolicy;
        applyCloudDistribution(cloudDistributionPolicy);
      }
    }

    System.out.printf("Found template Max Carbon {%f}\n", templateMaxCarbon);
    Float finalMaxCarbon = this.defineFinalMaxCarbon(templateMaxCarbon);

    System.out.printf("Found final max carbon {%f}\n", finalMaxCarbon);
    printSummary(this.yamlFilePath, this.csvFilePath);
    Map<String, Object> config =
        generateConfiguration(
            maxTimeInSeconds,
            mirroringEnabled,
            finalMaxCarbon,
            targetCloudCount,
            clusterCostPerCloudProvider,
            fixedCost,
            csvFilePath,
            services);
    printConfiguration(config);

    Map<String, Object> response = CloudCostClient.computePlacement(url, config);
    // log.info("Output file {%s}", outputPath);
    saveMilpJson(response, outputPath);
    return 0;
  }

  /**
   * Print the generated configuration
   *
   * @param config the configuration
   */
  private void printConfiguration(Map<String, Object> config) {
    System.out.println("--- GENERATED CONFIGURATION ---");

    System.out.println("-".repeat(60));
    System.out.printf("%-30s | %-20s%n", "KEY", "VALUE");
    System.out.println("-".repeat(60));

    config.forEach(
        (k, v) -> {
          String valueStr = String.valueOf(v);
          if (valueStr.length() > 80) {
            valueStr = valueStr.substring(0, 77) + "...";
          }
          System.out.printf("%-30s | %-20s%n", k, valueStr);
        });

    System.out.println("-".repeat(60));
  }

  /**
   * Decides the final value of a parameter following this priority cliValue > templateValue >
   * defaultValue
   *
   * @param <T> Any value
   * @param optionName The cli option name
   * @param cliValue The value passed via the cli (can be null)
   * @param templateValue The value present in template (can be null)
   * @param defaultValue The default value
   * @return The final value.
   */
  private <T> T resolveParam(String optionName, T cliValue, T templateValue, T defaultValue) {
    if (this.spec.commandLine().getParseResult().hasMatchedOption(optionName)) {
      return cliValue;
    }
    return (templateValue != null) ? templateValue : defaultValue;
  }

  /**
   * Applies the cloud distribution policy.
   *
   * @param policy The policy to apply.
   */
  private void applyCloudDistribution(CloudDistribuitionPolicy policy) {
    this.targetCloudCount =
        resolveParam("--clouds", this.targetCloudCount, policy.getMinProviders(), 1);
    boolean templateMirror = policy.getStrategy().equalsIgnoreCase("mirror");
    this.mirroringEnabled =
        resolveParam("--mirroring", this.mirroringEnabled, templateMirror, false);
  }

  /**
   * Define the final max carbon
   *
   * @param templateMaxCarbon
   * @return
   */
  private Float defineFinalMaxCarbon(Float templateMaxCarbon) {
    Float finalMaxCarbon = Float.MAX_VALUE;
    boolean valueProvidedByCommandLine =
        spec.commandLine().getParseResult().hasMatchedOption("--carbon");
    if (valueProvidedByCommandLine) {
      finalMaxCarbon = this.maxCarbon;
    } else if (templateMaxCarbon != null) {
      finalMaxCarbon = templateMaxCarbon;
    }
    return finalMaxCarbon;
  }

  /**
   * Save the milp response
   *
   * @param finalResponse
   * @param outputPath
   */
  public void saveMilpJson(Map<String, Object> finalResponse, String outputPath) {
    try {
      // String baseDir = getJarExecutionPath();
      // File outputDir = new File(baseDir, "results");

      // if (!outputDir.exists()) {
      // outputDir.mkdirs(); // Cria a pasta se não existir
      // }
      Map<String, Object> inputMetrics = (Map<String, Object>) finalResponse.get("inputMetrics");
      // Integer applicationNumber = (Integer) inputMetrics.get("apps");

      // String fileName = String.format("milp_response_%d_apps.json", applicationNumber);
      File outputFile = new File(outputPath);

      ObjectMapper mapper = new ObjectMapper();
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
      mapper.writeValue(outputFile, finalResponse);

      log.info("✅ Result saved in: " + outputFile.getAbsolutePath());
    } catch (Exception e) {
      log.error("❌ Could not save milp response", e);
    }
  }

  /**
   * Generates the configuration that will be used to call the milp
   *
   * @param maxTime
   * @param isMirroring
   * @param maxCarbon
   * @param targetCloud
   * @param clusterCosts
   * @param fixedCost
   * @param filename
   * @param services
   * @return
   */
  private Map<String, Object> generateConfiguration(
      Integer maxTime,
      boolean isMirroring,
      Float maxCarbon,
      Integer targetCloud,
      List<Float> clusterCosts,
      Float fixedCost,
      String filename,
      List<ServiceData> services /* ,
      List<Policy> appliedPolicies */) {
    Map<String, Object> root = new LinkedHashMap<>();

    root.put("maxTimeInSeconds", maxTime);
    root.put("isMirroringEnabled", isMirroring);
    root.put("maxTotalCarbonFootprint", maxCarbon);
    root.put("targetCloudCount", targetCloud);
    root.put("applicationsQuantity", services.size());
    root.put(
        "vcpuPerApplication",
        services.stream().map(ServiceData::vcpu).collect(Collectors.toList()));
    root.put(
        "memoryPerApplication",
        services.stream().map(ServiceData::memory).collect(Collectors.toList()));
    root.put(
        "instancesPerApplication",
        services.stream().map(ServiceData::instances).collect(Collectors.toList()));
    root.put("clusterCostPerCloudProvider", clusterCosts);
    root.put("fixedCost", fixedCost);
    root.put("filename", filename);
    root.put(
        "applicationNames", services.stream().map(ServiceData::name).collect(Collectors.toList()));
    return root;
  }

  public static void main(String[] args) throws Exception {

    int exitCode = new CommandLine(new App()).execute(args);
    System.exit(exitCode);
  }

  /**
   * Gets the path where the jar of this application is executed.
   *
   * @return The path
   */
  public String getJarExecutionPath() {
    try {
      String path = App.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
      String directoryPath = new File(path).getParent().split("/java/parser")[0];

      return directoryPath;
    } catch (Exception e) {

      return System.getProperty("user.dir");
    }
  }

  /**
   * Prints a summary showing from where did the app got the value
   *
   * @param yamlFile
   * @param csvFile
   */
  private void printSummary(String yamlFile, String csvFile) {
    System.out.println("\n" + "=".repeat(60));
    System.out.println("📊 EXPERIMENT CONFIGURATION SUMMARY");
    System.out.println("-".repeat(60));
    System.out.printf("%-25s | %-15s | %-15s%n", "PARAMETER", "VALUE", "SOURCE");
    System.out.println("-".repeat(60));

    printSummaryRow("Max Carbon", this.maxCarbon, "--carbon", 180.017f);
    printSummaryRow("Target Clouds", this.targetCloudCount, "--clouds", 1);
    printSummaryRow("Mirroring", this.mirroringEnabled, "--mirroring", false);
    printSummaryRow("Timeout (s)", this.maxTimeInSeconds, "--timeout", 20);

    System.out.println("-".repeat(60));
    System.out.println("📂 FILES:");
    System.out.println("   YAML: " + yamlFile);
    System.out.println("   CSV:  " + csvFile);
    System.out.println("=".repeat(60) + "\n");
  }

  /**
   * Print a line of the summary for a specific value
   *
   * @param label
   * @param finalValue
   * @param cliOption
   * @param absoluteDefault
   */
  private void printSummaryRow(
      String label, Object finalValue, String cliOption, Object absoluteDefault) {
    String source;

    if (this.spec.commandLine().getParseResult().hasMatchedOption(cliOption)) {
      source = "CLI (" + cliOption + ")";
    } else if (finalValue != null && !finalValue.equals(absoluteDefault)) {
      source = "YAML Template";
    } else {
      source = "System Default";
    }

    System.out.printf("%-25s | %-15s | %-15s%n", label, finalValue, source);
  }
}
