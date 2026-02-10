<!-- # Título projeto -->
# TOSCA-based Multi-Cloud Optimization: Balancing FinOps and Green Cloud Policies

<!-- Resumo descrevendo o objetivo do artefato, com o respectivo título e resumo do artigo. -->
The project "TOSCA-based Multi-Cloud Optimization: Balancing FinOps and Green Cloud Policies" was developed to streamline decision-making regarding cloud computing infrastructure for cloud-native applications. It aims to achieve optimal (or near-optimal) pricing while adhering to monthly carbon footprint thresholds. The framework utilizes a newly proposed TOSCA profile tailored for defining cloud-native application topologies. The optimization problem is formulated as a Mixed-Integer Linear Program (MILP) and solved using the OR-Tools CP-SAT solver.

<!-- # Estrutura do readme.md

<!-- Apresenta a estrutura do readme.md, descrevendo como o repositório está organizado. -->

# Badges and Artifact Evaluation

<!-- Os autores devem descrever quais selos devem ser considerados no processo de avaliação. Como por exemplo: ``Os selos considerados são: Disponíveis e Funcionais.'' -->

The authors submit this repository for evaluation under the following badges:

* Artifacts Available (SeloD)
* Artifacts Functional (SeloF)
* Sustainable Artifacts (SeloS)
* Reproducible Experiments (SeloR)

These claims are based on the source code and documentation provided herein and in related repositories.

# Basic Information



<!-- Esta seção deve apresentar informações básicas de todos os componentes necessários para a execução e replicação dos experimentos. 
Descrevendo todo o ambiente de execução, com requisitos de hardware e software. -->

This section details the environment required to execute and replicate the experiments, including hardware and software prerequisites.

## Directory Structure

* clouds_data/ - Stores [cloud prices and carbon footprint data](clouds_data/cloud_machine_types_cost_and_monthly_carbon_footprint.csv)
* experiments/ - Contains [the YAML files](experiments/yamls/) used to conduct  the experiments
  * [article-results/](article-results): Contains the results presented in the paper, categorized by scenario (A, B, C).
  * [yamls/](yamls): Holds all TOSCA templates used as input for the MILP solver.
  * [results/](results): Directory where all results generated during test execution will be stored.
* tosca/ - Contains TOSCA-related files and specifications.
  * [profile/](tosca/profile/README.md): Includes the documentation and definitions for the cloud-native-applications profile.   
* [hvitops/](hvitops/README.md) - Contains the source code and configurations for HVitOps (Healthcare Microservices Application) used as the case study.

## Dependencies

<!-- Informações relacionadas a benchmarks utilizados e dependências para a execução devem ser descritas nesta seção. 
Busque deixar o mais claro possível, apresentando informações como versões de dependências e processos para acessar recursos de terceiros caso necessário. -->

### Hardware Requirements

* Operating System: Debian-based Linux (e.g., Ubuntu 24.04+, Linux Mint 22.3+)
* CPU: Minimum 4 cores (Recommended: 8+ cores)
* RAM: Minimum 8GB (Recommended: 16GB+)
* Storage: Minimum 10GB free space (Recommended: 20GB+ depending on data volume)

### Software Requirements

* Git
* Docker
* Java 21+
* Python 3.8+

## Installation Scripts

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

<!-- #### Node / fnn (https://nodejs.org/en/download)

```shell
# Download and install fnm:
curl -o- https://fnm.vercel.app/install | bash

# Download and install Node.js:
fnm install 22

# Important: restart the terminal to apply the changes.

# Verify the Node.js version:
node -v # Should print "v22.14.0".

# Verify npm version:
npm -v # Should print "10.9.2".
``` -->

# Security Concerns

<!-- Caso a execução do artefato ofereça algum tipo de risco para os avaliadores. Este risco deve ser descrito e o processo adequado para garantir a segurança dos revisores deve ser apresentado. -->

There are no significant security concerns. The program's operations are restricted to the 'experiments/results' directory, where it writes the output files generated during execution.

# Installation

<!-- O processo de baixar e instalar a aplicação deve ser descrito nesta seção. Ao final deste processo já é esperado que a aplicação/benchmark/ferramenta consiga ser executada. -->

To configure the environment, ensure all [Dependencies](#dependencies) are installed and operational, particularly the Docker engine.

The `docker-compose.yml` file defines the environment required to run the application (MILP). To simplify execution, we provide a `run.sh` script that automates the deployment and testing process.

Alternatively, services can be executed manually by following the instructions in each respective directory. 


## Exection via `run.sh`


It is recommended to create a dedicated workspace directory:

```shell
mkdir -p $HOME/git
```

Clone this repository:
```shell
cd $HOME/git
```

Clone this repository onto the machine where the services will be running:

```
git clone https://github.com/xxx/xxx-sbrc26.git
cd xxx-sbrc26 # Replace with actual folder name
```

 
# Minimal Working Example (MWE)



<!-- Esta seção deve apresentar um passo a passo para a execução de um teste mínimo.
Um teste mínimo de execução permite que os revisores consigam observar algumas funcionalidades do artefato. 
Este teste é útil para a identificação de problemas durante o processo de instalação. -->
This section provides a step-by-step guide to executing a minimal test case, allowing reviewers to verify the core functionalities of the artifact and identify potential installation issues.

## 1. Start the MILP Solver:
Once dependencies are configured, execute the following script in a terminal to start the MILP service on port 8183. Ensure no other processes are using this port.

```
./execute_milp.sh
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

<!-- Esta seção deve descrever um passo a passo para a execução e obtenção dos resultados do artigo. Permitindo que os revisores consigam alcançar as reivindicações apresentadas no artigo. 
Cada reivindicações deve ser apresentada em uma subseção, com detalhes de arquivos de configurações a serem alterados, comandos a serem executados, flags a serem utilizadas, tempo esperado de execução, expectativa de recursos a serem utilizados como 1GB RAM/Disk e resultado esperado. 

Caso o processo para a reprodução de todos os experimento não seja possível em tempo viável. Os autores devem escolher as principais reivindicações apresentadas no artigo e apresentar o respectivo processo para reprodução. -->



Please see this [README](experiments/README.md).

# LICENSE
<!-- Apresente a licença. -->

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.




<!-- Artefatos Funcionais (SeloF)

É esperado que o código e/ou artefato possa ser executado e o revisor consiga observar algumas de suas funcionalidades. Para adquirir este artefato, é importante que informações adicionais estejam presentes no README.md do repositório, como

  lista de dependências;
  lista de versões das dependências/linguagens/ambiente;
  descrição do ambiente de execução;
  instruções de instalação e execução;
  um exemplo de execução mínima.

Artefatos Sustentáveis (SeloS)

É esperado que o código e/ou artefato esteja modularizado, organizado, inteligível e de fácil compreensão. Para obter o selo é interessante que:

  exista uma documentação mínima do código (descrevendo arquivos, funções,..);
  legibilidade mínima de código;
  permita que os avaliadores consigam identificar as principais reivindicações do artigo no artefato.

Experimentos Reprodutíveis (SeloR)

É esperado que o revisor consiga reproduzir as principais reivindicações apresentadas no artigo. Para obter este selo é esperado:

  instrução para executar as principais reivindicações (e.g., resultados dos principais gráficos/tabelas);
  descrição de um processo de como foram executados os experimentos para chegar até o resultado do artigo. Para atender esses requisitos sugere-se a inclusão de script(s) que automatizem ao máximo todo o processo de reprodução. -->

