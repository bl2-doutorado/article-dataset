# Experiments

Three distinct scenarios were evaluated in the associated paper, as detailed in the following sections. This repository also includes the raw and processed data used to generate the results presented in the publication.

## Research Results

The comprehensive results and datasets discussed in the article are available in the [article-results directory](article-results).

## Scenario A - Single vs. Multi-Cloud Mirroring

**Objective:** To compare the trade-offs between cost and carbon emissions when utilizing a single-provider deployment versus a multi-cloud mirrored architecture.

**Execution:** [Command here]

**Expected Time:** [X] minutes

## Scenario B - The Pareto Frontier (Cost vs. Carbon)

**Objective:** To analyze the sensitivity of the MILP (Mixed-Integer Linear Programming) model by varying the carbon emission thresholds.

**Expected Result:** The generation of a Pareto frontier illustrating the **"Green Premium"** (the incremental cost required as carbon constraints become increasingly stringent).

## Scenario C - Computational Performance and Scalability
**Objective:** To benchmark the solver's execution time and resource consumption across varying levels of application topology complexity.

## Execution

It is possible to run all experiments once using the following command on repositories root directory:

```shell
./run.sh
```

The results will be generated on the [experiments/result directory](experiments/results).