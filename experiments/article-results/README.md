# Article Results

All results presented in the article are stored here, organized by scenario. The data is provided in JSON format, following the naming convention below:

```
res_<SCENARIO>-<SCENARIO_ORDER>-applications-<CARBON_LIMIT>-CARBON-<STRATEGY>-<NUMBER_OF_CLOUDS>.json
```


Example:

```
res_a-1-10-applications-UNLIMITED-CARBON-distributed-single-cloud.json
```
Field Definitions:

* **SCENARIO**: Indicates the specific test case (a, b, or c).

* **SCENARIO_ORDER**: The specific execution sequence number.

* **CARBON_LIMIT**: Defines the environmental constraint (LIMITED or UNLIMITED).

* **NUMBER_OF_CLOUDS**: Specifies whether the topology is deployed in a single-cloud or two-clouds configuration.

For all scenarios, the three available providers (AWS, GCP, and OCI) were evaluated.

## Output JSON Schema Reference

Each result file contains detailed metrics regarding the optimization process, the chosen infrastructure, and the environmental impact. The schema is organized as follows:

### 1. solverResults

* gap: The relative difference between the best found integer solution and the objective bound (0.000 indicates an optimal solution).

* costZ: The value of the objective function, representing the total optimized cost (Z).

* status: The final state of the solver (e.g., OPTIMAL, FEASIBLE).

### 2. infrastructure

* globalCpuUtilizationPct: The average CPU utilization across all provisioned instances.

* globalMemUtilizationPct: The average RAM utilization across the entire cluster.

* instances: A list of provisioned virtual machines, where each entry includes:

  * type: The specific provider machine shape/type.

  * cloudProvider: The cloud vendor (AWS, GCP, or OCI).

  * cpuUtilizationPct / memUtilizationPct: Resource utilization specific to that instance.

  * carbonEmission: Estimated carbon footprint (CO2​e) for that specific unit.

  * allocations: A list of microservices (apps) mapped to this instance, including their specific resource consumption (vcpuUsed and memUsed).

### 3. performanceMetrics

* wallTimeSeconds: Real-world time (in seconds) taken to solve the problem.

* detTime: Deterministic time used by the CP-SAT solver.

* branches: Number of search tree branches explored during optimization.

### 4. inputMetrics

* analyzedCloudProviders: Total number of providers available in the inventory.

* targetCloudProviders: Number of providers selected for the final deployment.

* apps: Total number of microservices (components) optimized.

### 5. modelComplexity

* intVars / boolVars / totalVars: The number of integer, boolean, and total variables, respectively, within the MILP mathematical model.

### 6. totalCarbon

* Total estimated monthly carbon footprint for the entire application topology, used as a primary constraint or metric for Green Cloud policies.