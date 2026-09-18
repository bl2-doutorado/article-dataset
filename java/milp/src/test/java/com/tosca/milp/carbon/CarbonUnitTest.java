package com.tosca.milp.carbon;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CarbonUnitTest {

  @Test
  void scaledValueRoundTripsToTonnes() {
    double csvValue = 0.001331689050; // m5a.large (AWS) CSV value in tCO2e
    long scaled = CarbonUnit.toScaled(csvValue);
    // 0.001331689050 * 10^10 = 13316890.5 -> truncated to 13316890
    assertEquals(13316890L, scaled);
    assertEquals(csvValue, CarbonUnit.toTonnes(scaled), 1e-6);
  }

  @Test
  void toGramsDividesByTenThousand() {
    double csvValue = 0.002355435095; // OCI CSV value in tCO2e
    long scaled = CarbonUnit.toScaled(csvValue);
    // 1 tCO2e = 10^6 gCO2e, and scaled = tCO2e * 10^10, so grams = scaled / 10^4
    assertEquals(csvValue * 1_000_000.0, CarbonUnit.toGrams(scaled), 1e-2);
  }

  @Test
  void toKilogramsDividesByTenMillion() {
    double csvValue = 0.001811873150; // GCP CSV value in tCO2e
    long scaled = CarbonUnit.toScaled(csvValue);
    // 1 tCO2e = 10^3 kgCO2e, and scaled = tCO2e * 10^10, so kg = scaled / 10^7
    assertEquals(csvValue * 1_000.0, CarbonUnit.toKilograms(scaled), 1e-4);
  }

  @Test
  void overscaledLimitSaturatesToUnlimitedSentinel() {
    double hugeLimit = 3.4028235e38; // Float.MAX_VALUE region (parser unlimited sentinel)
    assertEquals(CarbonUnit.UNLIMITED_SCALED, CarbonUnit.toScaled(hugeLimit));
  }

  @Test
  void scaleConstantIsTenBillion() {
    assertEquals(10_000_000_000L, CarbonUnit.SCALE);
  }
}