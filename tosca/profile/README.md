# Cloud-Native Applications (CNA) TOSCA Profiles

This repository contains a suite of TOSCA 2.0 profiles designed to model, orchestrate, and optimize microservices applications across multi-cloud Kubernetes environments (AWS, GCP, OCI). The primary focus is the integration of FinOps metrics and Sustainability policies directly into the application's logical model.

## 1. Structure and Profile Hierarchy

The profiles are organized modularly, ensuring a clear separation of concerns between logical definitions and physical implementation:

* **Core Profile (cna-core)**: Abstract definitions, global data types (Resources, FinOps), and policies.

* **K8S Profile (cna-k8s)**: Specialization of core types for Kubernetes-native constructs (Pods, Services, Volumes).

* **Cloud Profiles (AWS, GCP, OCI)**: Provider-specific implementations for managed services (EKS, GKE, OKE) and infrastructure resources.

## 2. Core Profile ([cna-core.profile.yaml](cna-core.profile.yaml))

The foundation of the framework. It defines the metadata required for optimization algorithms (such as MILP).

### Base Data Types

* cloud_native.datatypes.ResourceRequirements: Abstraction of CPU and Memory (limits and requests) to ensure portability across orchestrators.

* cloud_native.datatypes.FinOpsTags: Mandatory metadata for financial governance (cost_center, application, team).

* cloud_native.datatypes.HealthProbe: Definition of health checks (Liveness and Readiness).

* cloud_native.datatypes.ContainerDefinition: Full container model, including images, ports, and environment variables.

### Key Node Types

* AbstractComponent: The base for any microservice. Introduces the scheduling_mode (strict vs. flexible).

* StorageBackingService: Abstraction for databases and stateful services.

* AbstractContainerOrchestratorCluster: A cloud-agnostic logical model for container clusters.

### Policies

* Sustainability: Defines carbon footprint thresholds (max_total_carbon_footprint).

* SchedulingStrategy: Guides the orchestrator on physical mapping (e.g., consolidation vs. isolation).

## 3. Kubernetes Profile ([cna-k8s.profile.yaml](cna-k8s.profile.yaml))

Translates abstract definitions into concrete Kubernetes ecosystem objects.

    KubernetesApplication: Maps a TOSCA component to a Deployment or StatefulSet.

    NetworkService: Manages network exposure (ClusterIP, NodePort, LoadBalancer).

    Volumes: Specialized support for HostPath, Secret, ConfigMap, and PersistentVolumeClaims.

## 4. Cloud-Specific Profiles

These profiles enable Day 0 automation (infrastructure provisioning).


### AWS ([cna-aws-k8s.profile.yaml](cna-aws-k8s.profile.yaml))

* **EKSCluster**: Manages the Amazon EKS Control Plane, including role_arn and security configurations.

* **EKSWorkerNode**: Defines Node Groups and AMI types.

### GCP ([cna-gcp-k8s.profile.yaml](cna-gcp-k8s.profile.yaml))

    GKECluster: Support for GKE Autopilot and VPC network definitions.

    GKENodePool: Manages node pools with support for preemptible (Spot) instances.

### OCI ([cna-oci-k8s.profile.yaml](cna-oci-k8s.profile.yaml))

* **OKECluster**: Specialization for Oracle Container Engine.

* **OKENodePool**: Support for Flex Shapes, allowing the MILP solver to adjust OCPUs and Memory with granular precision.

## 5. Features and Capabilities

The combined use of these profiles enables:

1. **Cost Allocation (FinOps)**: FinOps tags are used to allow cost allocation to teams.

1. **Carbon Optimization**: The sustainability policy acts as a constraint in the solver, preventing deployments in regions with high carbon intensity.

1. **Automated Resilience**: With HealthProbes and Recovery Policies, the application lifecycle is managed without manual intervention.

1. **Multi-Cloud Agnosticism**: An application modeled as a KubernetesApplication can be migrated between EKS, GKE, and OKE by simply updating the target node in the template.

## 6. Usage

To use these profiles in your TOSCA blueprint, import the required layer:

```yaml
imports:
  - profiles/cna-aws-k8s.profile.yaml # Automatically imports K8S e Core
  - profiles/cna-gcp-k8s.profile.yaml

node_templates:
  my-app:
    type: cloud_native.nodes.Kubernetes.KubernetesApplication
    properties:
      finops:
        application: "my-system"
        team: "devops"
        cost_center: "CC-001"
      containers:
        - name: web
          image: "nginx:latest"
          resources:
            cpu_min: "100m"
            mem_min: "128Mi"
```