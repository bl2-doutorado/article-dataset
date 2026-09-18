package com.canalbl2.planner.features.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.canalbl2.planner.features.domain.Policy;
import com.canalbl2.planner.features.domain.SustainabilityPolicy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ToscaCarbonPolicyTest {

  @TempDir Path tempDir;

  private final ToscaParser parser = new ToscaParser();

  @Test
  void numericLimitIsParsedAsTco2e() throws Exception {
    Path yaml = write(Path.of("numeric.yaml"),
        "policies:\n"
            + "  - global_carbon:\n"
            + "      type: cloud_native.policies.Sustainability\n"
            + "      properties:\n"
            + "        max_total_carbon_footprint: 0.022920195\n");

    List<Policy> policies = parser.parseYamlPolicies(yaml.toString());
    SustainabilityPolicy carbon = (SustainabilityPolicy) policies.get(0);
    assertEquals(0.022920195f, carbon.getMaxTotalCarbonFootprint());
  }

  @Test
  void absentLimitDefaultsToUnlimitedSentinel() throws Exception {
    Path yaml = write(Path.of("absent.yaml"),
        "policies:\n"
            + "  - global_carbon:\n"
            + "      type: cloud_native.policies.Sustainability\n"
            + "      properties: {}\n");

    List<Policy> policies = parser.parseYamlPolicies(yaml.toString());
    SustainabilityPolicy carbon = (SustainabilityPolicy) policies.get(0);
    assertEquals(SustainabilityPolicy.UNLIMITED, carbon.getMaxTotalCarbonFootprint());
  }

  @Test
  void dynamicGetInputDefaultsToUnlimitedSentinel() throws Exception {
    Path yaml = write(Path.of("dynamic.yaml"),
        "policies:\n"
            + "  - global_carbon:\n"
            + "      type: cloud_native.policies.Sustainability\n"
            + "      properties:\n"
            + "        max_total_carbon_footprint: { get_input: carbon_limit }\n");

    List<Policy> policies = parser.parseYamlPolicies(yaml.toString());
    SustainabilityPolicy carbon = (SustainabilityPolicy) policies.get(0);
    assertTrue(SustainabilityPolicy.UNLIMITED.equals(carbon.getMaxTotalCarbonFootprint()));
  }

  private Path write(Path name, String content) throws Exception {
    Path file = tempDir.resolve(name);
    Files.writeString(file, content);
    return file;
  }
}