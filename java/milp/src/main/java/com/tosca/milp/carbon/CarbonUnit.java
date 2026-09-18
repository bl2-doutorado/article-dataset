package com.tosca.milp.carbon;

/**
 * Carbon unit contract shared by the MILP pipeline.
 *
 * <p>All carbon quantities are expressed in metric tonnes of CO2 equivalent (tCO2e). The CP-SAT
 * solver works on integers, so tCO2e values are scaled by {@link #SCALE} (10^10) on the way in and
 * unscaled on the way out.
 *
 * <p>Conversion table (applied to the solver-scaled value):
 *
 * <ul>
 *   <li>tCO2e: division by {@code 10^10}
 *   <li>kgCO2e: division by {@code 10^7}
 *   <li>gCO2e: division by {@code 10^4}
 * </ul>
 */
public final class CarbonUnit {

  /** Scale factor applied to tCO2e values to keep them integer in the solver (10^10). */
  public static final long SCALE = 10_000_000_000L;

  /** {@link #SCALE} as double, used for the solver-side arithmetic. */
  public static final double SCALE_AS_DOUBLE = 10_000_000_000.0;

  /**
   * Domain (tCO2e) sentinel meaning "no carbon limit". Any value that scales past the long range
   * saturates to {@link Long#MAX_VALUE} and is treated as unlimited. This matches the parser
   * default (Float.MAX_VALUE) forwarded when a template declares no carbon limit.
   */
  public static final long UNLIMITED_SCALED = Long.MAX_VALUE;

  private CarbonUnit() {}

  /**
   * Scales a tCO2e value into the solver domain (×10^10). Values large enough to overflow saturate
   * to {@link Long#MAX_VALUE} (the unlimited sentinel).
   */
  public static long toScaled(double tco2e) {
    return ((Double) (tco2e * SCALE_AS_DOUBLE)).longValue();
  }

  /** Converts a solver-scaled value back to tCO2e. */
  public static double toTonnes(long scaled) {
    return (double) scaled / SCALE_AS_DOUBLE;
  }

  /** Converts a solver-scaled value to kilograms of CO2e. */
  public static double toKilograms(long scaled) {
    return (double) scaled / 10_000_000.0;
  }

  /** Converts a solver-scaled value to grams of CO2e. */
  public static double toGrams(long scaled) {
    return (double) scaled / 10_000.0;
  }
}