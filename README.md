
# TOSCA-based Multi-Cloud Optimization: Balancing FinOps and Green Cloud Policies


The project "TOSCA-based Multi-Cloud Optimization: Balancing FinOps and Green Cloud Policies" was developed to streamline decision-making regarding cloud computing infrastructure for cloud-native applications. It aims to achieve optimal (or near-optimal) pricing while adhering to monthly carbon footprint thresholds. The framework utilizes a newly proposed TOSCA profile tailored for defining cloud-native application topologies. The optimization problem is formulated as a Mixed-Integer Linear Program (MILP) and solved using the OR-Tools CP-SAT solver.

# Badges and Artifact Evaluation

The authors submit this repository for evaluation under the following badges:

* Artifacts Available (SeloD)
* Artifacts Functional (SeloF)
* Sustainable Artifacts (SeloS)
* Reproducible Experiments (SeloR)

These claims are based on the source code and documentation provided herein and in related repositories.

# Basic Information

This section details the environment required to execute and replicate the experiments, including hardware and software prerequisites.

## Directory Structure

* clouds_data/ - Stores [cloud prices and carbon footprint data](clouds_data/cloud_machine_types_cost_and_monthly_carbon_footprint.csv)
* experiments/ - Contains [the YAML files](experiments/yamls/) used to conduct  the experiments
  * [article-results](article-results/README.md): Contains the results presented in the paper, categorized by scenario (A, B, C).
  * [yamls/](yamls/README.md): Holds all TOSCA templates used as input for the MILP solver.
  * [results/](results): Directory where all results generated during test execution will be stored.
* tosca/ - Contains TOSCA-related files and specifications.
  * [profile/](tosca/profile/README.md): Includes the documentation and definitions for the cloud-native-applications profile.   
* [hvitops/](hvitops/README.md) - Contains the source code and configurations for HVitOps (Healthcare Microservices Application) used as the case study.

## Dependencies

### Hardware Requirements

* Operating System: Debian-based Linux (e.g., Ubuntu 24.04+, Linux Mint 22.3+)
* CPU: Minimum 4 cores (Recommended: 8+ cores)
* RAM: Minimum 8GB (Recommended: 16GB+)
* Storage: Minimum 10GB free space (Recommended: 20GB+ depending on data volume)

### Software Requirements

* curl
* Git
* Docker
* Java 21+

## Installation Scripts

#### curl
```shell
sudo apt-get update
sudo apt-get install curl
```

#### Git (https://git-scm.com/downloads)
```shell
sudo apt-get update
sudo apt-get install git
```
#### Docker (https://docs.docker.com/get-docker/)

```shell
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
```

# Security Concerns

There are no significant security concerns. The program's operations are restricted to the 'experiments/results' directory, where it writes the output files generated during execution.

# Installation

To configure the environment, ensure all [Dependencies](#dependencies) are installed and operational, particularly the Docker engine.

There are two possible ways to execute the MILP solver. Using Java or Docker Compose.

The `docker-compose.yml` file defines the environment required to run the application (MILP). To simplify execution of the client that communitactes with the solver, we provide a `run.sh` script that automates the testing process.

## Exection via `run.sh`


It is recommended to create a dedicated workspace directory:

```shell
mkdir -p $HOME/git
```

Clone this repository:
```shell
cd $HOME/git
```

Download this repository onto the machine where the service will be running:

```
curl -o article-dataset.zip https://anonymous.4open.science/api/repo/article-dataset-E02E/zip                                                                                                                                     
unzip article-dataset.zip
cd article-dataset
```

 
# Minimal Working Example (MWE)
This section provides a step-by-step guide to executing a minimal test case, allowing reviewers to verify the core functionalities of the artifact and identify potential installation issues.

## 1. Start the MILP Solver:
Once dependencies are configured, the MILP Solver can be executed in two different ways:

### The Java way (Recommended)

To execute the solver using Java, run the following script in a terminal to start the service on port `8183`. Ensure no other processes are currently using this port.

```
./execute_milp.sh
```

### Using Docker Compose

Docker Compose can also deploy the MILP Solver as a service on port 8183. However, in our benchmarks (specifically using Docker Desktop), this method resulted in slower solver performance. Therefore, we **strongly recommend using the Java execution method for better efficiency**.

To start the solver using Docker Compose, run:

```
docker compose up
```


## 2. Run the Demonstration:
Open a new terminal and execute the demonstration script to run all experiments discussed in the paper:


### Usage

The `run.sh` script provides a complete workflow, testing the three scenarios presetend on the article.

```
./run.sh
```

## 3. Termination:
After the script finishes, return to the first terminal (where the solver is running) and press **CTRL + C** to terminate the process.

# Experiments
Please see this [README](experiments/README.md).

# LICENSE

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.