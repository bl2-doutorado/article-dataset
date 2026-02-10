package com.tosca.milp.infrastructureEstimator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverSolutionCallback;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.DecisionStrategyProto;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;
import com.google.ortools.sat.Literal;
import com.google.ortools.sat.SatParameters;
import io.micronaut.context.annotation.Bean;
import jakarta.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Bean
public class CloudInfrastructureEstimator extends CpSolverSolutionCallback {

  static final Logger log = LoggerFactory.getLogger(CloudInfrastructureEstimator.class);

  private int solutionCount = 0;
  private Instant start;
  private CpSolver solver;
  private int cloudProvidersQuantity;
  private int machineTypesQuantity;
  private int applicationsQuantity;
  private Integer[] distribuitionMachineTypesPerCloudProvider;
  private long[] vcpuPerMachineType;
  private long[] memoryPerMachineType;
  private long[] costPerMachineType;
  private long[] vcpuPerApplication;
  private long[] memoryPerApplication;
  private long[] instancesPerApplication;
  private long[] clusterCostPerCloudProvider;
  private long fixedCost;
  private double maxTimeInSeconds;
  private IntVar[] z;
  private IntVar[][] x;
  private IntVar[][] p;
  private IntVar[] y;
  private long[] carbonFootprintPerMachineType;
  private long maxTotalCarbonFootprint;
  private boolean isMirroringEnabled = true;
  private Integer targetCloudCount = 0;

  private List<String> machineModelNames;

  private List<String> cloudProviderNames;

  private List<String> applicationNames;

  @Override
  public void onSolutionCallback() {
    String eventTitle = "Solution " + solutionCount + " found.";
    log.info(eventTitle);
    long millis = Duration.between(start, Instant.now()).toMillis();
    log.info(
        String.format(
            "Execution time %02d:%02d:%02d:%d.",
            TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
            TimeUnit.MILLISECONDS.toSeconds(millis) % 60,
            millis % 1000));

    double bestSolution = this.objectiveValue();
    double bestBound = this.bestObjectiveBound();
    double gap = (bestSolution - bestBound) / bestSolution * 100;
    log.info("Current Minimum Total Cost: " + bestSolution / 100);
    log.info("Theorical Limit (Best Bound): " + bestBound / 100);
    log.info("Current Gap: " + String.format("%.2f", gap) + "%");
    log.info("----------------------------------------");
    solutionCount++; // TODO: Show Cost Evolution Per time
  }

  // @Async("e1")
  public Map<String, Object> resolveModel(
      int cloudProvidersQuantity,
      int machineTypesQuantity,
      int applicationsQuantity,
      Integer[] distribuitionMachineTypesPerCloudProvider,
      long[] vcpuPerMachineType,
      long[] memoryPerMachineType,
      long[] costPerMachineType,
      long[] vcpuPerApplication,
      long[] memoryPerApplication,
      long[] instancesPerApplication,
      long[] clusterCostPerCloudProvider,
      long fixedCost,
      long[] carbonFootprintPerMachineType,
      long maxTotalCarbonFootprint,
      boolean isMirroringEnabled,
      double maxTimeInSeconds,
      int targetCloudCount,
      List<String> machineModelNames,
      List<String> cloudProviderNames,
      List<String> applicationNames) {
    initializeLocalVariables(
        cloudProvidersQuantity,
        machineTypesQuantity,
        applicationsQuantity,
        distribuitionMachineTypesPerCloudProvider,
        vcpuPerMachineType,
        memoryPerMachineType,
        costPerMachineType,
        vcpuPerApplication,
        memoryPerApplication,
        instancesPerApplication,
        clusterCostPerCloudProvider,
        fixedCost,
        carbonFootprintPerMachineType,
        maxTotalCarbonFootprint,
        isMirroringEnabled,
        maxTimeInSeconds,
        targetCloudCount,
        machineModelNames,
        cloudProviderNames,
        applicationNames);

    CpModel model = new CpModel();

    log.info("Calculating required limits...");
    long maxInstancesPerMachineType =
        calculateMaxInstancesPerMachineType(
            machineTypesQuantity,
            applicationsQuantity,
            vcpuPerMachineType,
            memoryPerMachineType,
            vcpuPerApplication,
            memoryPerApplication);

    long totalRequiredInstances = computeTotalRequiredInstances(instancesPerApplication);

    log.info(
        "Calculated Limits:  - Total Required Instances: {}, maxInstancesPerMachineType: {}",
        totalRequiredInstances,
        maxInstancesPerMachineType);

    long maxCloudCost = Arrays.stream(this.clusterCostPerCloudProvider).max().getAsLong();
    long maxMachineCost = Arrays.stream(costPerMachineType).max().getAsLong();
    long totalDemanda = Arrays.stream(instancesPerApplication).sum();

    long redundanceMultiplier = cloudProvidersQuantity;

    long pessimisticMaximumCost =
        fixedCost
            + (maxCloudCost * redundanceMultiplier)
            + (totalDemanda * redundanceMultiplier * maxMachineCost);

    log.info("Pessimistic maximum cost: {}", pessimisticMaximumCost);

    // ---------------------------------------------------
    // 1. Decision Variables
    // ---------------------------------------------------
    IntVar[] z = createCloudProviderActivationVariables(cloudProvidersQuantity, model);
    IntVar[] y =
        createMachineTypeProvisionedUnitsVariables(
            machineTypesQuantity,
            applicationsQuantity,
            vcpuPerMachineType,
            memoryPerMachineType,
            vcpuPerApplication,
            memoryPerApplication,
            model,
            totalDemanda);
    IntVar[][] x =
        createApplicationInstancesPerMachineInstanceVariables(
            machineTypesQuantity, applicationsQuantity, model, maxInstancesPerMachineType);
    IntVar[][] v =
        createApplicationInstanceQuantityClusterProviderVariables(
            cloudProvidersQuantity, applicationsQuantity, model);

    IntVar[][] p =
        createApplicationInstancesMachineTypeQuantityVariables(
            machineTypesQuantity, applicationsQuantity, instancesPerApplication, model, y, x);

    for (int k = 0; k < cloudProvidersQuantity; k++) {
      LinearExprBuilder cloudProviderApplicationInstances = LinearExpr.newBuilder();
      for (int t = 0; t < machineTypesQuantity; t++) {
        if (distribuitionMachineTypesPerCloudProvider[t] == k) {
          for (int j = 0; j < applicationsQuantity; j++) {
            cloudProviderApplicationInstances.add(p[t][j]);
          }
        }
      }
      IntVar totalK = model.newIntVar(0, totalDemanda * redundanceMultiplier, "total_inst_k" + k);
      model.addEquality(totalK, cloudProviderApplicationInstances);
    }

    // ---------------------------------------------------
    // 2. Objective Function (Cost Minimization)
    // ---------------------------------------------------
    log.info("Defining Objective Function.");

    log.info("Adding Fixed Costs.");
    LinearExprBuilder objective =
        LinearExpr.newBuilder().addTerm(LinearExpr.constant(fixedCost), 1);
    log.info("Fixed Costs added with success.");

    log.info("Adding Cloud Providers Activation Costs.");
    for (int k = 0; k < cloudProvidersQuantity; ++k) {
      objective.addTerm(z[k], this.clusterCostPerCloudProvider[k]);
    }
    log.info("Cloud Providers Activation Costs added with success.");

    log.info("Adding Machine Costs.");
    // Custo dos tipos de máquinas (Ci * Ykt)
    for (int t = 0; t < machineTypesQuantity; ++t) {
      objective.addTerm(y[t], costPerMachineType[t]);
    }
    log.info("Machine Costs added with success.");
    log.info("Objective Function defined with success.");

    // ---------------------------------------------------
    // 3. Constraints
    // ---------------------------------------------------

    log.info("Adding Constraints.");
    // addApplicationIntancesMininumConstraints(
    //     machineTypesQuantity, applicationsQuantity, instancesPerApplication, model, p);

    log.info(
        "Adding Demand Restrictions (Mode: "
            + (isMirroringEnabled ? "Mirroring" : "Distribution")
            + ")");

    addMirroringOrConstraints(
        cloudProvidersQuantity,
        machineTypesQuantity,
        applicationsQuantity,
        instancesPerApplication,
        model,
        z,
        p);

    addNodeVCPUAndMemoryCapacityConstraints(
        machineTypesQuantity,
        applicationsQuantity,
        vcpuPerMachineType,
        memoryPerMachineType,
        vcpuPerApplication,
        memoryPerApplication,
        model,
        x);

    log.info("Adding Machine Types Hierarquical Dependency on Cloud Providers Restrictions.");
    for (int t = 0; t < machineTypesQuantity; ++t) {
      // Se Zk=0, Ykt deve ser 0
      int k = distribuitionMachineTypesPerCloudProvider[t];
      model
          .addLessOrEqual(y[t], LinearExpr.newBuilder().addTerm(z[k], totalRequiredInstances))
          .getBuilder()
          .setName("R_DepenTipoMáquinaCloudProvider_" + k + "_" + t)
          .build();
    }

    log.info(
        "Machine Types Hierarquical Dependency on Cloud Providers Restrictions added with"
            + " success.");

    log.info(
        "Adding Activated Cloud Provider with at least on Machine Instance activated"
            + " Restrictions.");

    for (int k = 0; k < cloudProvidersQuantity; ++k) {
      LinearExprBuilder sumY = LinearExpr.newBuilder();
      for (int t = 0; t < machineTypesQuantity; ++t) {
        if (distribuitionMachineTypesPerCloudProvider[t] == k) {
          sumY.add(y[t]);
        }
      }
      model
          .addGreaterOrEqual(sumY, z[k])
          .getBuilder()
          .setName("R_ConsistenciaCloudProvider_" + k)
          .build();
    }

    log.info(
        "Activated Cloud Provider with at least on Machine Instance activated Restrictions added"
            + " with success.");

    LinearExprBuilder cloudSum = LinearExpr.newBuilder();
    for (int k = 0; k < cloudProvidersQuantity; k++) {
      cloudSum.add(z[k]);
    }

    if (targetCloudCount > 0) {
      model.addEquality(cloudSum, targetCloudCount);
    } else {
      model.addGreaterOrEqual(cloudSum, 1);
    }

    int minInstancesPerCloud = 0;

    for (int k = 0; k < cloudProvidersQuantity; k++) {
      for (int j = 0; j < applicationsQuantity; j++) {
        LinearExprBuilder pInCloudK = LinearExpr.newBuilder();
        for (int t = 0; t < machineTypesQuantity; t++) {
          if (distribuitionMachineTypesPerCloudProvider[t] == k) {
            pInCloudK.add(p[t][j]);
          }
        }
        model.addGreaterOrEqual(
            pInCloudK, LinearExpr.newBuilder().addTerm(z[k], minInstancesPerCloud));
      }
    }

    log.info(
        "Adding Application Instances Relationship With Cloud Provider Number of Instances"
            + " Restrictions.");
    for (int k = 0; k < cloudProvidersQuantity; ++k) {
      for (int j = 0; j < applicationsQuantity; ++j) {
        LinearExprBuilder sumP = LinearExpr.newBuilder();
        for (int t = 0; t < machineTypesQuantity; ++t) {
          if (distribuitionMachineTypesPerCloudProvider[t] == k) {
            sumP.add(p[t][j]);
          }
        }

        model
            .addGreaterOrEqual(sumP, v[k][j])
            .getBuilder()
            .setName("R_LigacaoVkj_" + k + "_" + j)
            .build();
      }
    }

    log.info(
        "Application Instances Relationship With Cloud Provider Number of Instances Restrictions"
            + " added with success.");

    log.info(
        "Adding Application Instances Relationship With Cloud Provider Number of Instances"
            + " Restrictions.");
    for (int k = 0; k < cloudProvidersQuantity; ++k) {
      for (int j = 0; j < applicationsQuantity; ++j) {

        if (isMirroringEnabled) {
          model
              .addLessOrEqual(z[k], v[k][j])
              .getBuilder()
              .setName("R_Diversidade_HA_" + k + "_" + j)
              .build();
        } else {
          model
              .addLessOrEqual(v[k][j], z[k])
              .getBuilder()
              .setName("R_Distribuidora_Livre_" + k + "_" + j)
              .build();
        }
      }
    }

    log.info("Adding anti-affinity constraint");
    for (int t = 0; t < machineTypesQuantity; t++) {
      for (int j = 0; j < applicationsQuantity; j++) {
        model.addLessOrEqual(x[t][j], 1);
      }
    }

    model.addLessOrEqual(objective, pessimisticMaximumCost);

    LinearExprBuilder totalCarbonExpr = LinearExpr.newBuilder();

    for (int t = 0; t < machineTypesQuantity; t++) {

      totalCarbonExpr.addTerm(y[t], carbonFootprintPerMachineType[t]);
    }

    model.addLessOrEqual(totalCarbonExpr.build(), this.maxTotalCarbonFootprint);

    // ---------------------------------------------------
    // 4. RESOLUTION AND RESULTS PRESENTATION
    // ---------------------------------------------------

    CpSolver solver = new CpSolver();

    solver.getParameters().setMaxTimeInSeconds(this.maxTimeInSeconds);
    Instant start = Instant.now();
    this.solver = solver;
    this.z = z;
    this.x = x;
    this.p = p;
    this.y = y;
    this.start = start;
    solver.getParameters().setNumSearchWorkers(Runtime.getRuntime().availableProcessors());
    solver.getParameters().setExploitIntegerLpSolution(true);
    solver.getParameters().setLinearizationLevel(2);
    solver.getParameters().setSearchBranching(SatParameters.SearchBranching.FIXED_SEARCH);

    model.addDecisionStrategy(
        z,
        DecisionStrategyProto.VariableSelectionStrategy.CHOOSE_FIRST,
        DecisionStrategyProto.DomainReductionStrategy.SELECT_MAX_VALUE);

    model.addDecisionStrategy(
        y,
        DecisionStrategyProto.VariableSelectionStrategy.CHOOSE_HIGHEST_MAX,
        DecisionStrategyProto.DomainReductionStrategy.SELECT_MAX_VALUE);

    solver.getParameters().setExploitBestSolution(true);
    solver.getParameters().setLogSearchProgress(true);
    solver.setLogCallback(
        (String message) -> {
          log.debug(message);
        });
    IntVar objectiveVar = model.newIntVar(0, pessimisticMaximumCost * 10, "total_objective");

    this.applyLowerBoundDual(model, objectiveVar);
    model.addEquality(objectiveVar, objective);

    log.info("Adding Objective Function to Model.");
    model.minimize(objectiveVar);
    log.info("Adding Objective Function added to Model with success.");
    log.info("Demands: {}", Arrays.toString(instancesPerApplication));
    log.info("Target Clouds: {}", targetCloudCount);
    log.info("Carbon Limit: {}", maxTotalCarbonFootprint);

    for (int j = 0; j < applicationsQuantity; j++) {
      boolean foundProvider = false;
      for (int t = 0; t < machineTypesQuantity; t++) {
        if (vcpuPerApplication[j] <= vcpuPerMachineType[t]
            && memoryPerApplication[j] <= memoryPerMachineType[t]) {
          foundProvider = true;
          break;
        }
      }
      if (!foundProvider) {
        log.error(
            "🛑 INVIABLE APPLICATION: The Application_{} requirements are bigger than any machine!",
            j);
      }
    }

    // 2. Verificação de compatibilidade por Nuvem
    for (int k = 0; k < cloudProvidersQuantity; k++) {
      int compatibilidade = 0;
      for (int j = 0; j < applicationsQuantity; j++) {
        for (int t = 0; t < machineTypesQuantity; t++) {
          if (distribuitionMachineTypesPerCloudProvider[t] == k
              && vcpuPerApplication[j] <= vcpuPerMachineType[t]
              && memoryPerApplication[j] <= memoryPerMachineType[t]) {
            compatibilidade++;
            break;
          }
        }
      }
      log.info(
          "☁️ Cloud {} supports {} of {} the required applications.",
          k,
          compatibilidade,
          applicationsQuantity);
    }

    log.info(
        "Machnine distribution: {}", Arrays.toString(distribuitionMachineTypesPerCloudProvider));
    CpSolverStatus status = solver.solve(model, this);

    double bestSolution = solver.objectiveValue();
    double bestBound = solver.bestObjectiveBound();
    double gap = (bestSolution - bestBound) / bestSolution * 100;

    log.info("Cost: " + bestSolution / 100);
    log.info("Theorical Limit (Best Bound): " + bestBound / 100);
    log.info("Current Gap: " + String.format("%.2f", gap) + "%");
    if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {

      List<Map<String, Object>> chartData =
          generateChartData(
              machineTypesQuantity, applicationsQuantity, vcpuPerApplication, y, x, solver);
      // exportJsonResult(chartData);
      return reportSolutionDetails(
          solver::value,
          solver::booleanValue,
          solver::objectiveValue,
          solver::bestObjectiveBound,
          status.toString(),
          start);
    } else {
      HashMap<String, Object> response = new HashMap<>();
      response.put("errorMessage", "❌ It was not possible to found an optimal or viable solution.");
      log.info("❌ It was not possible to found an optimal or viable solution.");
      return response;
    }
  }

  private void initializeLocalVariables(
      int cloudProvidersQuantity,
      int machineTypesQuantity,
      int applicationsQuantity,
      Integer[] distribuitionMachineTypesPerCloudProvider,
      long[] vcpuPerMachineType,
      long[] memoryPerMachineType,
      long[] costPerMachineType,
      long[] vcpuPerApplication,
      long[] memoryPerApplication,
      long[] instancesPerApplication,
      long[] clusterCostPerCloudProvider,
      long fixedCost,
      long[] carbonFootprintPerMachineType,
      long maxTotalCarbonFootprint,
      boolean isMirroringEnabled,
      double maxTimeInSeconds,
      int targetCloudCount,
      List<String> machineModelNames,
      List<String> cloudProviderNames,
      List<String> applicationNames) {
    this.targetCloudCount = targetCloudCount;
    this.applicationsQuantity = applicationsQuantity;
    this.cloudProvidersQuantity = cloudProvidersQuantity;
    this.machineTypesQuantity = machineTypesQuantity;
    this.distribuitionMachineTypesPerCloudProvider = distribuitionMachineTypesPerCloudProvider;
    this.vcpuPerMachineType = vcpuPerMachineType;
    this.memoryPerMachineType = memoryPerMachineType;
    this.costPerMachineType = costPerMachineType;
    this.vcpuPerApplication = vcpuPerApplication;
    this.memoryPerApplication = memoryPerApplication;
    this.instancesPerApplication = instancesPerApplication;

    this.fixedCost = fixedCost;
    this.maxTimeInSeconds = maxTimeInSeconds;
    this.carbonFootprintPerMachineType = carbonFootprintPerMachineType;
    this.maxTotalCarbonFootprint = maxTotalCarbonFootprint;
    this.isMirroringEnabled = isMirroringEnabled;
    this.machineModelNames = machineModelNames;
    this.cloudProviderNames = cloudProviderNames;
    this.applicationNames = applicationNames;

    if (clusterCostPerCloudProvider == null || clusterCostPerCloudProvider.length == 0) {
      this.clusterCostPerCloudProvider = new long[cloudProviderNames.size()];
    }
  }

  public Map<String, Object> writeFinalResponse(
      String status,
      double bestSolution,
      double bestBound,
      double gap,
      double calculatedTotalCarbon,
      long hours,
      long minutes,
      long seconds,
      long milliseconds) {
    Map<String, Object> finalResponse = new HashMap<>();
    finalResponse.put("scenario", "Test_" + String.format("%02d", (applicationsQuantity / 10)));

    HashMap<String, Object> inputMetrics = new HashMap<>();
    inputMetrics.put("apps", applicationsQuantity);
    inputMetrics.put("targetCloudProviders", this.targetCloudCount);
    inputMetrics.put("analyzedCloudProviders", this.cloudProvidersQuantity);
    finalResponse.put("inputMetrics", inputMetrics);

    HashMap<String, Object> solverResults = new HashMap<>();
    solverResults.put("status", status);
    solverResults.put("costZ", bestSolution);
    solverResults.put("gap", String.format("%.3f", gap));
    finalResponse.put("solverResults", solverResults);

    HashMap<String, Object> performanceMetrics = new HashMap<>();
    performanceMetrics.put("wallTimeSeconds", seconds + (milliseconds / 1000.0));
    performanceMetrics.put(
        "detTime", String.format("%.3f", solver.response().getDeterministicTime()));
    performanceMetrics.put("branches", solver.numBranches());
    finalResponse.put("performanceMetrics", performanceMetrics);

    HashMap<String, Object> modelComplexity = new HashMap<>();
    long numIntegers = solver.response().getNumIntegers();
    modelComplexity.put("intVars", numIntegers);
    long numBooleans = solver.response().getNumBooleans();
    modelComplexity.put("boolVars", numBooleans);
    modelComplexity.put("totalVars", numIntegers + numBooleans);
    finalResponse.put("modelComplexity", modelComplexity);

    finalResponse.put("totalCarbon", calculatedTotalCarbon);
    finalResponse.put("infrastructure", generateFullAllocationJson());
    return finalResponse;
  }

  private void addNodeVCPUAndMemoryCapacityConstraints(
      int machineTypesQuantity,
      int applicationsQuantity,
      long[] vcpuPerMachineType,
      long[] memoryPerMachineType,
      long[] vcpuPerApplication,
      long[] memoryPerApplication,
      CpModel model,
      IntVar[][] x) {
    log.info("Adding Machine Types VCPUs and Memory Restrictions.");

    for (int t = 0; t < machineTypesQuantity; ++t) {

      LinearExprBuilder vcpusUsed = LinearExpr.newBuilder();
      for (int j = 0; j < applicationsQuantity; ++j) {
        vcpusUsed.addTerm(x[t][j], vcpuPerApplication[j]);
      }
      model
          .addLessOrEqual(vcpusUsed, vcpuPerMachineType[t])
          .getBuilder()
          .setName("R_VCPUType_" + t)
          .build();

      LinearExprBuilder memoryUsed = LinearExpr.newBuilder();
      for (int j = 0; j < applicationsQuantity; ++j) {
        memoryUsed.addTerm(x[t][j], memoryPerApplication[j]);
      }
      model
          .addLessOrEqual(memoryUsed, memoryPerMachineType[t])
          .getBuilder()
          .setName("R_MemoryType_" + t)
          .build();
    }
    log.info("Machine Types VCPUs and Memory Restrictions added with success.");
  }

  private void addMirroringOrConstraints(
      int cloudProvidersQuantity,
      int machineTypesQuantity,
      int applicationsQuantity,
      long[] instancesPerApplication,
      CpModel model,
      IntVar[] z,
      IntVar[][] p) {

    for (int k = 0; k < cloudProvidersQuantity; k++) {
      for (int j = 0; j < applicationsQuantity; j++) {
        LinearExprBuilder instancesInCloudK = LinearExpr.newBuilder();
        for (int t = 0; t < machineTypesQuantity; t++) {
          if (distribuitionMachineTypesPerCloudProvider[t] == k) {
            instancesInCloudK.add(p[t][j]);
          }
        }

        if (isMirroringEnabled) {
          model.addEquality(instancesInCloudK, LinearExpr.term(z[k], instancesPerApplication[j]));
        }
      }
    }
    if (!isMirroringEnabled) {
      for (int j = 0; j < applicationsQuantity; j++) {
        LinearExprBuilder totalGlobal = LinearExpr.newBuilder();
        for (int t = 0; t < machineTypesQuantity; t++) {
          totalGlobal.add(p[t][j]);
        }
        model.addEquality(totalGlobal, instancesPerApplication[j]);
      }
    }
  }

  private void addApplicationIntancesMininumConstraints(
      int machineTypesQuantity,
      int applicationsQuantity,
      long[] instancesPerApplication,
      CpModel model,
      IntVar[][] p) {
    log.info("Adding Application Instances Constraints.");
    for (int j = 0; j < applicationsQuantity; ++j) {
      LinearExprBuilder sumP = LinearExpr.newBuilder();
      for (int t = 0; t < machineTypesQuantity; ++t) {
        sumP.add(p[t][j]);
      }
      model
          .addEquality(sumP, instancesPerApplication[j])
          .getBuilder()
          .setName("R_Demand_" + j)
          .build();
    }
    log.info("Application Instances Constraints added with success.");
  }

  private IntVar[][] createApplicationInstancesMachineTypeQuantityVariables(
      int machineTypesQuantity,
      int applicationsQuantity,
      long[] instancesPerApplication,
      CpModel model,
      IntVar[] y,
      IntVar[][] x) {
    log.info("Adding Application Instances Total Quantityon Cluster Providers Quantity Variable.");
    IntVar[][] p = new IntVar[machineTypesQuantity][applicationsQuantity];
    for (int t = 0; t < machineTypesQuantity; ++t) {
      for (int j = 0; j < applicationsQuantity; ++j) {
        p[t][j] =
            model.newIntVar(
                0, instancesPerApplication[j] * cloudProvidersQuantity, "P_" + t + "_" + j);
        model.addMultiplicationEquality(p[t][j], y[t], x[t][j]);
      }
    }
    log.info(
        "Application Instances Total Quantityon Cluster Providers Quantity Variable added with"
            + " success.");
    return p;
  }

  private IntVar[][] createApplicationInstanceQuantityClusterProviderVariables(
      int cloudProvidersQuantity, int applicationsQuantity, CpModel model) {
    log.info("Adding Application Instances on Cluster Providers Quantity Variable.");
    IntVar[][] v = new IntVar[cloudProvidersQuantity][applicationsQuantity];
    for (int k = 0; k < cloudProvidersQuantity; ++k) {
      for (int j = 0; j < applicationsQuantity; ++j) {
        v[k][j] = model.newBoolVar("V_" + k + "_" + j);
      }
    }
    log.info("Application Instances on Cluster Providers Quantity Variable added with success.");
    return v;
  }

  private IntVar[][] createApplicationInstancesPerMachineInstanceVariables(
      int machineTypesQuantity,
      int applicationsQuantity,
      CpModel model,
      long maxInstancesPerMachineType) {
    log.info("Adding Application Instances on Machine Types Quantity Variable.");
    IntVar[][] x = new IntVar[machineTypesQuantity][applicationsQuantity];
    for (int t = 0; t < machineTypesQuantity; ++t) {
      for (int j = 0; j < applicationsQuantity; ++j) {
        x[t][j] = model.newIntVar(0, maxInstancesPerMachineType, "X_" + t + "_" + j);
      }
    }
    log.info("Application Instances on Machine Types Quantity Variable added with success.");
    return x;
  }

  private IntVar[] createMachineTypeProvisionedUnitsVariables(
      int machineTypesQuantity,
      int applicationsQuantity,
      long[] vcpuPerMachineType,
      long[] memoryPerMachineType,
      long[] vcpuPerApplication,
      long[] memoryPerApplication,
      CpModel model,
      long totalDemanda) {
    log.info("Adding Machine Instances Quantity Variable.");
    IntVar[] y = new IntVar[machineTypesQuantity];

    for (int t = 0; t < machineTypesQuantity; t++) {
      long minCapacidadeDeQualquerApp = Long.MAX_VALUE;
      for (int j = 0; j < applicationsQuantity; j++) {
        long cap =
            Math.min(
                vcpuPerMachineType[t] / vcpuPerApplication[j],
                memoryPerMachineType[t] / memoryPerApplication[j]);
        if (cap > 0 && cap < minCapacidadeDeQualquerApp) minCapacidadeDeQualquerApp = cap;
      }
      long limitYt =
          (minCapacidadeDeQualquerApp == Long.MAX_VALUE)
              ? 0
              : (totalDemanda / minCapacidadeDeQualquerApp) + 1;
      y[t] = model.newIntVar(0, limitYt * 100, "Y_" + t);
    }
    log.info("Machine Instances Quantity Variable added with success.");
    return y;
  }

  /**
   * Create binary variables to represent if a cloud provider is activated or not.
   *
   * @param cloudProvidersQuantity Number of cloud providers considered
   * @param model The Model
   * @return Array with binary variables
   */
  private IntVar[] createCloudProviderActivationVariables(
      int cloudProvidersQuantity, CpModel model) {
    log.info("Adding Cloud Provider Activation Variables.");
    IntVar[] z = new IntVar[cloudProvidersQuantity];
    for (int k = 0; k < cloudProvidersQuantity; ++k) {
      z[k] = model.newBoolVar("Z_" + k);
    }
    log.info("Cloud Provider Activation Variables added with success.");
    return z;
  }

  private long computeTotalRequiredInstances(long[] instancesPerApplication) {
    long totalRequiredInstances = Arrays.stream(instancesPerApplication).sum();
    return totalRequiredInstances;
  }

  /**
   * Evaluates machine capacity by calculating the minimum ratio between available and required
   * resources (vCPU/Memory). Keeps track of the highest capacity found across all machine types.
   *
   * @param machineTypesQuantity
   * @param applicationsQuantity
   * @param vcpuPerMachineType
   * @param memoryPerMachineType
   * @param vcpuPerApplication
   * @param memoryPerApplication
   * @return
   */
  private long calculateMaxInstancesPerMachineType(
      int machineTypesQuantity,
      int applicationsQuantity,
      long[] vcpuPerMachineType,
      long[] memoryPerMachineType,
      long[] vcpuPerApplication,
      long[] memoryPerApplication) {
    long maxInstancesPerMachineType = 0;
    for (int t = 0; t < machineTypesQuantity; t++) {
      for (int j = 0; j < applicationsQuantity; j++) {
        long capVcpu = vcpuPerMachineType[t] / vcpuPerApplication[j];
        long capMem = memoryPerMachineType[t] / memoryPerApplication[j];
        maxInstancesPerMachineType =
            Math.max(maxInstancesPerMachineType, Math.min(capVcpu, capMem));
      }
    }
    return maxInstancesPerMachineType;
  }

  public void exportJsonResult(List<Map<String, Object>> chartData) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(SerializationFeature.INDENT_OUTPUT);

    try {
      File jsonFile = new File("/home/bruno/git/doutorado/carbon/cloud_optimization_result.json");
      mapper.writeValue(jsonFile, chartData);

      log.info("✅ JSON file created with success: " + jsonFile.getAbsolutePath());
    } catch (IOException e) {
      System.err.println("❌ Could not save JSON: " + e.getMessage());
    }
  }

  private List<Map<String, Object>> generateChartData(
      int machineTypesQuantity,
      int applicationsQuantity,
      long[] vcpuPerApplication,
      IntVar[] y,
      IntVar[][] x,
      CpSolver solver) {
    List<Map<String, Object>> chartData = new ArrayList<>();

    for (int t = 0; t < machineTypesQuantity; t++) {
      int activeMachines = (int) solver.value(y[t]);
      if (activeMachines > 0) {
        for (int m = 0; m < activeMachines; m++) {
          Map<String, Object> machineNode = new HashMap<>();
          machineNode.put("type", "Machine_" + t);

          List<Map<String, Object>> appsInMachine = new ArrayList<>();
          for (int j = 0; j < applicationsQuantity; j++) {
            int qtdApp = (int) solver.value(x[t][j]);
            if (qtdApp > 0) {
              appsInMachine.add(
                  Map.of("app", "App_" + j, "vcpuUsed", qtdApp * vcpuPerApplication[j]));
            }
          }
          machineNode.put("allocations", appsInMachine);
          chartData.add(machineNode);
        }
      }
    }
    return chartData;
  }

  /**
   * Apply a lower bound based on the machine types vcpus and memory capacity
   *
   * @param model
   * @param objective
   */
  public void applyLowerBoundDual(CpModel model, IntVar objective) {
    int multiplier = isMirroringEnabled ? Math.max(1, targetCloudCount) : 1;
    long vcpuTotal = 0;
    long memTotal = 0;
    for (int j = 0; j < applicationsQuantity; j++) {
      vcpuTotal += (long) instancesPerApplication[j] * vcpuPerApplication[j];
      memTotal += (long) instancesPerApplication[j] * memoryPerApplication[j];
    }
    vcpuTotal *= multiplier;
    memTotal *= multiplier;
    double minCpuCost = Double.MAX_VALUE;
    double minMemCost = Double.MAX_VALUE;
    for (int t = 0; t < machineTypesQuantity; t++) {
      minCpuCost = Math.min(minCpuCost, (double) costPerMachineType[t] / vcpuPerMachineType[t]);
      minMemCost = Math.min(minMemCost, (double) costPerMachineType[t] / memoryPerMachineType[t]);
    }

    long lbCpu = (long) (vcpuTotal * minCpuCost);
    long lbMem = (long) (memTotal * minMemCost);
    long lbResources = Math.max(lbCpu, lbMem);
    long lbFixedCosts = 0;
    if (multiplier > 0) {
      lbFixedCosts = Arrays.stream(clusterCostPerCloudProvider).sorted().limit(multiplier).sum();
    }
    long finalLB = lbResources + lbFixedCosts + (long) fixedCost;
    model.addGreaterOrEqual(objective, finalLB);
    log.debug("DEBUG LB - vcpuTotal: " + vcpuTotal);
    log.debug("DEBUG LB - lbCpu: " + lbCpu);
    log.info("DEBUG LB - Final LB Value: " + finalLB / 100);
  }

  private Map<String, Object> reportSolutionDetails(
      ToLongFunction<IntVar> valueProvider,
      Predicate<Literal> boolProvider,
      Supplier<Double> objValueProvider,
      Supplier<Double> bestBoundValueProvider,
      String statusLabel,
      Instant start) {
    double totalCost = objValueProvider.get() / 100;
    double bestBound = bestObjectiveBound() / 100;
    double gap = (totalCost - bestBound) / bestBound / 100;
    log.info("✅ Solution Found (Status: " + statusLabel + ")");
    log.info("Minimum Total Cost: " + (totalCost));

    long millis = Duration.between(start, Instant.now()).toMillis();
    long hours = TimeUnit.MILLISECONDS.toHours(millis);
    long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
    long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
    long milliseconds = millis % 1000;
    log.info(
        String.format("Execution time %02d:%02d:%02d:%d.", hours, minutes, seconds, milliseconds));

    log.info("----------------------------------------");
    double carbonTotalSolucao = 0;

    for (int k = 0; k < cloudProvidersQuantity; ++k) {
      if (boolProvider.test((Literal) z[k])) {
        log.info(
            "  CLOUD PROVIDER {} ACTIVATED (Cost: %.2f)",
            (k + 1), String.format("%.2f", (double) clusterCostPerCloudProvider[k]));

        for (int t = 0; t < machineTypesQuantity; ++t) {
          long machineQuantity = valueProvider.applyAsLong(y[t]);
          if (machineQuantity > 0 && distribuitionMachineTypesPerCloudProvider[t] == k) {

            carbonTotalSolucao += machineQuantity * carbonFootprintPerMachineType[t];
            double formattedCarbon = (double) this.carbonFootprintPerMachineType[t] / 1000000000.0;
            log.info(
                "  Machine Type {}: {} instances | Carbon: {} gCO2e",
                t + 1,
                machineQuantity,
                formattedCarbon);

            double vcpusUtil = 0;
            double memUtil = 0;

            for (int j = 0; j < applicationsQuantity; ++j) {
              long instPorTipo = valueProvider.applyAsLong(x[t][j]);
              if (instPorTipo > 0) {
                vcpusUtil += instPorTipo * (double) vcpuPerApplication[j];
                memUtil += instPorTipo * (double) memoryPerApplication[j];

                log.info(
                    "      -> Application {}: {} inst/machine (Total Type: {})\n",
                    (j + 1),
                    instPorTipo,
                    valueProvider.applyAsLong(p[t][j]));
              }
            }

            log.info(
                "    - Resource Utilization: VCPUs {}% (de {}) | Memory {}% (de {})",
                String.format("%.2f", (vcpusUtil / vcpuPerMachineType[t]) * 100.0),
                String.format("%.2f", (double) vcpuPerMachineType[t] / 1000.0),
                String.format("%.2f", (memUtil / memoryPerMachineType[t]) * 100.0),
                String.format("%.2f", (double) memoryPerMachineType[t]));
          }
        }
      } else {
        log.info("  CLOUD PROVIDER {} INACTIVATED", (k + 1));
      }
    }
    log.info("-> Total Carbon Footprint: {} gCO2e", carbonTotalSolucao / 10000000000l);
    return writeFinalResponse(
        statusLabel,
        totalCost,
        bestBoundValueProvider.get(),
        gap,
        carbonTotalSolucao / 10000000000l,
        hours,
        minutes,
        seconds,
        milliseconds);
  }

  public Map<String, Object> generateFullAllocationJson() {
    Map<String, Object> root = new HashMap<>();
    List<Map<String, Object>> allInstances = new ArrayList<>();

    double globalTotalCpuUsed = 0;
    double globalTotalMemUsed = 0;
    double globalTotalCpuCapacity = 0;
    double globalTotalMemCapacity = 0;

    for (int k = 0; k < cloudProvidersQuantity; k++) {
      if (solver.booleanValue((Literal) z[k])) {
        for (int t = 0; t < machineTypesQuantity; t++) {
          if (distribuitionMachineTypesPerCloudProvider[t] == k) {
            long activeMachinesQuantity = solver.value(y[t]);
            for (int i = 0; i < activeMachinesQuantity; i++) {
              Map<String, Object> machineInstance = new HashMap<>();
              machineInstance.put(
                  "cloudProvider", /*"Cloud_" + (k + 1) + ": " + */ this.cloudProviderNames.get(k));
              machineInstance.put("type", "Machine_" + (t + 1) + ": " + machineModelNames.get(t));
              // Adicionando emissão de carbono individual do modelo
              machineInstance.put(
                  "carbonEmission", carbonFootprintPerMachineType[t] / 1000000000.0);

              double machineCpuUsed = 0;
              double machineMemUsed = 0;
              double machineCpuCap = vcpuPerMachineType[t];
              double machineMemCap = memoryPerMachineType[t];

              List<Map<String, Object>> allocations = new ArrayList<>();
              for (int j = 0; j < applicationsQuantity; j++) {
                long appInstancesInMachine = solver.value(x[t][j]);

                if (appInstancesInMachine > 0) {
                  double cpuUsed = appInstancesInMachine * vcpuPerApplication[j];
                  double memUsed = appInstancesInMachine * memoryPerApplication[j];
                  Map<String, Object> allocation = new HashMap<>();
                  allocation.put("app", /*"App_" + (j + 1)*/ this.applicationNames.get(j));
                  allocation.put("vcpuUsed", cpuUsed);
                  allocation.put("memUsed", memUsed);
                  allocations.add(allocation);
                  machineCpuUsed += cpuUsed;
                  machineMemUsed += memUsed;
                }
              }
              machineInstance.put("cpuUtilizationPct", (machineCpuUsed / machineCpuCap) * 100);
              machineInstance.put("memUtilizationPct", (machineMemUsed / machineMemCap) * 100);
              machineInstance.put("allocations", allocations);
              allInstances.add(machineInstance);
              globalTotalCpuUsed += machineCpuUsed;
              globalTotalMemUsed += machineMemUsed;
              globalTotalCpuCapacity += machineCpuCap;
              globalTotalMemCapacity += machineMemCap;
            }
          }
        }
      }
    }
    root.put("instances", allInstances);
    root.put("globalCpuUtilizationPct", (globalTotalCpuUsed / globalTotalCpuCapacity) * 100);
    root.put("globalMemUtilizationPct", (globalTotalMemUsed / globalTotalMemCapacity) * 100);

    return root;
  }
}
