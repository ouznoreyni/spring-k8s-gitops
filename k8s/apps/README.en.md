# ArgoCD Applications - GitOps Deployment

<p align="center">
  <a href="README.md">Français</a> |
  <strong>English</strong>
</p>

<p align="center">
  <img src="https://argo-cd.readthedocs.io/en/stable/assets/logo.png" alt="ArgoCD Logo" width="200"/>
</p>

<p align="center">
  <strong>Declarative GitOps Deployment for Kubernetes</strong><br>
  This folder contains ArgoCD Application manifests to orchestrate the automated deployment of all components.
</p>

---

## Table of Contents

- [Overview](#overview)
- [Understanding ArgoCD](#understanding-argocd)
- [Project Architecture](#project-architecture)
- [Application Catalog](#application-catalog)
- [Application Anatomy](#application-anatomy)
- [Deployment Guide](#deployment-guide)
- [Common Operations](#common-operations)
- [Troubleshooting](#troubleshooting)
- [Best Practices](#best-practices)

---

## Overview

### Purpose

This folder centralizes the **declarative configuration** of all applications deployed on the Kubernetes cluster via ArgoCD. Each YAML file represents an application managed according to GitOps principles.

### Applied GitOps Principles

| Principle | Description | Implementation |
|-----------|-------------|----------------|
| **Declarative** | Desired state is described in Git | Versioned YAML manifests |
| **Versioned** | Complete history of changes | Git as single source of truth |
| **Automated** | Synchronization without intervention | `syncPolicy.automated` enabled |
| **Auditable** | Complete traceability | ArgoCD logs + Git history |

---

## Understanding ArgoCD

### What is ArgoCD?

ArgoCD is a **Kubernetes controller** that implements the GitOps pattern. It continuously monitors Git repositories and automatically synchronizes the cluster state with the declared configuration.

### How It Works

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                             │
│    ┌──────────┐         ┌──────────┐         ┌──────────┐                  │
│    │          │  push   │          │  watch  │          │                  │
│    │Developer │────────▶│   Git    │◀────────│  ArgoCD  │                  │
│    │          │         │          │         │          │                  │
│    └──────────┘         └──────────┘         └─────┬────┘                  │
│                                                    │                        │
│                                                    │ sync                   │
│                                                    ▼                        │
│                                              ┌──────────┐                   │
│                                              │          │                   │
│                                              │Kubernetes│                   │
│                                              │ Cluster  │                   │
│                                              │          │                   │
│                                              └──────────┘                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Benefits

| Benefit | Description |
|---------|-------------|
| **Instant Rollback** | Return to any version via Git |
| **Complete Audit** | Every change is tracked in Git |
| **Self-healing** | Automatic drift correction |
| **Multi-cluster** | Centralized management of multiple clusters |
| **Security** | No direct cluster access required |

---

## Project Architecture

### Global View

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              ARGOCD SERVER                                   │
│                            (namespace: argocd)                               │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    Application Controller                            │   │
│  │          Watches Git → Compares → Syncs → Reports                   │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────┬──────────────────────────────────────────┘
                                   │
           ┌───────────────────────┼───────────────────────┐
           │                       │                       │
           ▼                       ▼                       ▼
┌─────────────────────┐ ┌─────────────────────┐ ┌─────────────────────┐
│                     │ │                     │ │                     │
│   🚀 APPLICATIONS   │ │  🔧 INFRASTRUCTURE  │ │   📊 MONITORING     │
│                     │ │                     │ │                     │
│   namespace: blog   │ │ namespace: varies   │ │ namespace: monitoring│
│                     │ │                     │ │                     │
├─────────────────────┤ ├─────────────────────┤ ├─────────────────────┤
│                     │ │                     │ │                     │
│  ┌───────────────┐  │ │  ┌───────────────┐  │ │  ┌───────────────┐  │
│  │  Frontend UI  │  │ │  │ Ingress NGINX │  │ │  │  Prometheus   │  │
│  │    (React)    │  │ │  │  (Gateway)    │  │ │  │  (Metrics)    │  │
│  └───────────────┘  │ │  └───────────────┘  │ │  └───────────────┘  │
│         │           │ │                     │ │         │           │
│         ▼           │ │  ┌───────────────┐  │ │         ▼           │
│  ┌───────────────┐  │ │  │    Harbor     │  │ │  ┌───────────────┐  │
│  │  Spring API   │  │ │  │  (Registry)   │  │ │  │    Grafana    │  │
│  │   (Backend)   │  │ │  └───────────────┘  │ │  │ (Dashboards)  │  │
│  └───────────────┘  │ │                     │ │  └───────────────┘  │
│         │           │ │                     │ │                     │
│         ▼           │ │                     │ │                     │
│  ┌───────────────┐  │ │                     │ │                     │
│  │  PostgreSQL   │  │ │                     │ │                     │
│  │  (Database)   │  │ │                     │ │                     │
│  └───────────────┘  │ │                     │ │                     │
│                     │ │                     │ │                     │
└─────────────────────┘ └─────────────────────┘ └─────────────────────┘
```

### Communication Flow

```
                                    Internet
                                       │
                                       ▼
                              ┌─────────────────┐
                              │  Ingress NGINX  │ ◀── infra-ingress-nginx
                              │   (Gateway)     │
                              └────────┬────────┘
                                       │
                    ┌──────────────────┼──────────────────┐
                    │                  │                  │
                    ▼                  ▼                  ▼
           ┌───────────────┐  ┌───────────────┐  ┌───────────────┐
           │   Frontend    │  │   Prometheus  │  │    Grafana    │
           │ frontend.local│  │ :30090        │  │ :30030        │
           └───────┬───────┘  └───────────────┘  └───────────────┘
                   │
                   │ /api/*
                   ▼
           ┌───────────────┐
           │  Spring API   │ ◀── app-blog-api
           │   (Backend)   │
           └───────┬───────┘
                   │
                   ▼
           ┌───────────────┐
           │  PostgreSQL   │ ◀── app-blog-postgresql
           │  (Database)   │
           └───────────────┘
```

---

## Application Catalog

### 📱 Business Applications

These applications form the functional core of the project.

<table>
<tr>
<th>Application</th>
<th>Details</th>
</tr>
<tr>
<td>

**Frontend UI**
`frontend-ui.yaml`

</td>
<td>

| Property | Value |
|----------|-------|
| **ArgoCD Name** | `app-blog-frontend` |
| **Namespace** | `blog` |
| **Helm Chart** | `k8s/charts/frontend-ui` |
| **Release** | `blog-frontend` |
| **Component** | `frontend` |

**Description:** React user interface with Nginx, SPA routing and integrated API proxy.

</td>
</tr>
<tr>
<td>

**Spring API**
`spring-api.yaml`

</td>
<td>

| Property | Value |
|----------|-------|
| **ArgoCD Name** | `app-blog-api` |
| **Namespace** | `blog` |
| **Helm Chart** | `k8s/charts/spring-api` |
| **Release** | `blog-api` |
| **Component** | `backend` |

**Description:** Reactive Spring Boot WebFlux REST API with JWT authentication and Clean Architecture.

</td>
</tr>
<tr>
<td>

**PostgreSQL**
`postgresql.yaml`

</td>
<td>

| Property | Value |
|----------|-------|
| **ArgoCD Name** | `app-blog-postgresql` |
| **Namespace** | `blog` |
| **Helm Chart** | `k8s/charts/postgresql` |
| **Release** | `blog-postgresql` |
| **Component** | `database` |

**Description:** PostgreSQL 15 relational database with persistence and metrics.

</td>
</tr>
</table>

### 🔧 Infrastructure

These components provide the base services required for cluster operation.

<table>
<tr>
<th>Application</th>
<th>Details</th>
</tr>
<tr>
<td>

**Ingress NGINX**
`ingress-nginx.yaml`

</td>
<td>

| Property | Value |
|----------|-------|
| **ArgoCD Name** | `infra-ingress-nginx` |
| **Namespace** | `ingress-nginx` |
| **Helm Chart** | `k8s/charts/ingress-nginx` |
| **Release** | `ingress-nginx` |
| **Component** | `ingress` |

**Description:** Kubernetes ingress controller managing HTTP/HTTPS routing and load balancing.

</td>
</tr>
<tr>
<td>

**Harbor**
`harbor.yaml`

</td>
<td>

| Property | Value |
|----------|-------|
| **ArgoCD Name** | `infra-harbor` |
| **Namespace** | `registry` |
| **Helm Chart** | `k8s/charts/harbor` |
| **Release** | `container-registry` |
| **Component** | `registry` |

**Description:** Private container registry with vulnerability scanning and project management.

</td>
</tr>
</table>

### 📊 Monitoring

Observability stack for performance monitoring and analysis.

<table>
<tr>
<th>Application</th>
<th>Details</th>
</tr>
<tr>
<td>

**Prometheus**
`prometheus.yaml`

</td>
<td>

| Property | Value |
|----------|-------|
| **ArgoCD Name** | `infra-prometheus` |
| **Namespace** | `monitoring` |
| **Helm Chart** | `k8s/charts/prometheus` |
| **Release** | `monitoring-stack` |
| **Component** | `monitoring` |

**Description:** Metrics collection and storage system with alerting via Alertmanager.

</td>
</tr>
<tr>
<td>

**Grafana**
`grafana.yaml`

</td>
<td>

| Property | Value |
|----------|-------|
| **ArgoCD Name** | `infra-grafana` |
| **Namespace** | `monitoring` |
| **Helm Chart** | `k8s/charts/grafana` |
| **Release** | `grafana` |
| **Component** | `visualization` |

**Description:** Visualization platform with preconfigured dashboards for Kubernetes.

</td>
</tr>
</table>

---

## Application Anatomy

### Fully Annotated Structure

```yaml
# ┌─────────────────────────────────────────────────────────────────────────┐
# │                        MANIFEST HEADER                                  │
# └─────────────────────────────────────────────────────────────────────────┘
apiVersion: argoproj.io/v1alpha1    # ArgoCD API version
kind: Application                    # Resource type

# ┌─────────────────────────────────────────────────────────────────────────┐
# │                           METADATA                                      │
# └─────────────────────────────────────────────────────────────────────────┘
metadata:
  name: app-blog-api                 # 🏷️  Unique application identifier
  namespace: argocd                  # 📍 Always in argocd namespace

  # Protection against accidental deletion
  finalizers:
    - resources-finalizer.argocd.argoproj.io

  # Labels for organization and filtering
  labels:
    app.kubernetes.io/component: backend           # Component type
    app.kubernetes.io/part-of: spring-k8s-gitops   # Parent project

# ┌─────────────────────────────────────────────────────────────────────────┐
# │                         SPECIFICATION                                   │
# └─────────────────────────────────────────────────────────────────────────┘
spec:
  project: default                   # ArgoCD project (isolation & RBAC)

  # ┌───────────────────────────────────────────────────────────────────────┐
  # │                            SOURCE                                     │
  # │              Where does the configuration come from?                  │
  # └───────────────────────────────────────────────────────────────────────┘
  source:
    repoURL: https://github.com/ouznoreyni/spring-k8s-gitops.git
    targetRevision: HEAD             # Branch, tag or commit SHA
    path: k8s/charts/spring-api      # Path to Helm chart

    # Helm configuration
    helm:
      releaseName: blog-api          # Helm release name
      valueFiles:
        - values.yaml                # Values file(s)

  # ┌───────────────────────────────────────────────────────────────────────┐
  # │                         DESTINATION                                   │
  # │              Where to deploy the resources?                           │
  # └───────────────────────────────────────────────────────────────────────┘
  destination:
    server: https://kubernetes.default.svc  # Target cluster
    namespace: blog                          # Deployment namespace

  # ┌───────────────────────────────────────────────────────────────────────┐
  # │                      SYNC POLICY                                      │
  # │              How to synchronize?                                      │
  # └───────────────────────────────────────────────────────────────────────┘
  syncPolicy:
    automated:
      prune: true      # 🗑️  Delete orphaned resources
      selfHeal: true   # 🔧 Auto-correct manual modifications

    syncOptions:
      - CreateNamespace=true    # Create namespace automatically
      - ServerSideApply=true    # Use Server-Side Apply (optional)
```

### Key Fields Explained

| Section | Field | Description |
|---------|-------|-------------|
| **metadata** | `name` | Unique identifier in ArgoCD |
| **metadata** | `namespace` | Always `argocd` |
| **metadata** | `finalizers` | Ensures clean resource deletion |
| **spec** | `project` | Isolation and access control |
| **source** | `repoURL` | Git repository URL |
| **source** | `targetRevision` | Branch (`HEAD`, `main`) or tag (`v1.0.0`) |
| **source** | `path` | Relative path to chart |
| **destination** | `server` | Kubernetes cluster URL |
| **destination** | `namespace` | Target namespace for resources |
| **syncPolicy** | `automated` | Enables automatic synchronization |
| **syncPolicy** | `prune` | Deletes resources not defined in Git |
| **syncPolicy** | `selfHeal` | Corrects manual changes |

---

## Deployment Guide

### Prerequisites

Before deploying, ensure you have:

| Component | Required | Verification |
|-----------|----------|--------------|
| Kubernetes Cluster | v1.28+ | `kubectl version` |
| ArgoCD | v2.8+ | `kubectl get pods -n argocd` |
| Git Access | Configured | Repository accessible |
| kubectl | Connected | `kubectl cluster-info` |

### Step 1: Verify ArgoCD

```bash
# Verify ArgoCD is operational
kubectl get pods -n argocd

# Expected output:
# NAME                                  READY   STATUS    RESTARTS   AGE
# argocd-application-controller-xxx    1/1     Running   0          1h
# argocd-repo-server-xxx               1/1     Running   0          1h
# argocd-server-xxx                    1/1     Running   0          1h
```

### Step 2: Deploy by Dependency Order

```bash
# ┌─────────────────────────────────────────────────────────────────────────┐
# │  PHASE 1: INFRASTRUCTURE (no dependencies)                              │
# └─────────────────────────────────────────────────────────────────────────┘

kubectl apply -f k8s/apps/ingress-nginx.yaml
kubectl apply -f k8s/apps/harbor.yaml

# Wait for infrastructure to be ready
kubectl wait --for=condition=Healthy application/infra-ingress-nginx -n argocd --timeout=300s

# ┌─────────────────────────────────────────────────────────────────────────┐
# │  PHASE 2: MONITORING                                                    │
# └─────────────────────────────────────────────────────────────────────────┘

kubectl apply -f k8s/apps/prometheus.yaml
kubectl apply -f k8s/apps/grafana.yaml

# ┌─────────────────────────────────────────────────────────────────────────┐
# │  PHASE 3: DATABASE (dependency for backend)                             │
# └─────────────────────────────────────────────────────────────────────────┘

kubectl apply -f k8s/apps/postgresql.yaml

# Wait for PostgreSQL
kubectl wait --for=condition=Healthy application/app-blog-postgresql -n argocd --timeout=300s

# ┌─────────────────────────────────────────────────────────────────────────┐
# │  PHASE 4: BUSINESS APPLICATIONS                                         │
# └─────────────────────────────────────────────────────────────────────────┘

kubectl apply -f k8s/apps/spring-api.yaml
kubectl apply -f k8s/apps/frontend-ui.yaml
```

### Step 3: Verify Deployment

```bash
# Overview of all applications
kubectl get applications -n argocd

# Expected output:
# NAME                    SYNC     HEALTH   STATUS
# app-blog-api            Synced   Healthy  Running
# app-blog-frontend       Synced   Healthy  Running
# app-blog-postgresql     Synced   Healthy  Running
# infra-ingress-nginx     Synced   Healthy  Running
# infra-harbor            Synced   Healthy  Running
# infra-prometheus        Synced   Healthy  Running
# infra-grafana           Synced   Healthy  Running
```

### Express Deployment (All in One Command)

```bash
# ⚠️  Use only if dependencies are already managed
kubectl apply -f k8s/apps/
```

---

## Common Operations

### CLI Management

```bash
# ┌─────────────────────────────────────────────────────────────────────────┐
# │                          VIEWING                                        │
# └─────────────────────────────────────────────────────────────────────────┘

# List all applications
kubectl get applications -n argocd

# Application details
kubectl describe application app-blog-api -n argocd

# Detailed status with argocd CLI
argocd app get app-blog-api

# ┌─────────────────────────────────────────────────────────────────────────┐
# │                        SYNCHRONIZATION                                  │
# └─────────────────────────────────────────────────────────────────────────┘

# Force synchronization
argocd app sync app-blog-api

# Synchronization with prune
argocd app sync app-blog-api --prune

# Sync all applications
argocd app sync -l app.kubernetes.io/part-of=spring-k8s-gitops

# ┌─────────────────────────────────────────────────────────────────────────┐
# │                           ROLLBACK                                      │
# └─────────────────────────────────────────────────────────────────────────┘

# View deployment history
argocd app history app-blog-api

# Rollback to previous version
argocd app rollback app-blog-api <REVISION_ID>

# ┌─────────────────────────────────────────────────────────────────────────┐
# │                          DELETION                                       │
# └─────────────────────────────────────────────────────────────────────────┘

# Delete application (and its resources)
kubectl delete application app-blog-api -n argocd

# Delete without removing Kubernetes resources
argocd app delete app-blog-api --cascade=false
```

### ArgoCD Web Interface

```bash
# 1. Get admin password
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d && echo

# 2. Access the interface
# URL: http://localhost:30080
# User: admin
# Password: (result of above command)
```

---

## Troubleshooting

### Common Issues

<details>
<summary><strong>❌ Application in "OutOfSync" state</strong></summary>

**Symptom:** Application shows `OutOfSync` in ArgoCD.

**Possible causes:**
1. Manual modifications on the cluster
2. Differences between Git and cluster
3. Error in Helm chart

**Solutions:**
```bash
# View differences
argocd app diff app-blog-api

# Force synchronization
argocd app sync app-blog-api --force

# If selfHeal is enabled, wait a few seconds
```
</details>

<details>
<summary><strong>❌ Application in "Degraded" state</strong></summary>

**Symptom:** Application shows `Degraded` or `Progressing` for too long.

**Possible causes:**
1. Pods in CrashLoopBackOff
2. Insufficient resources
3. Configuration problem

**Solutions:**
```bash
# Check pods
kubectl get pods -n blog

# View logs of problematic pod
kubectl logs -n blog <pod-name>

# Describe pod to see events
kubectl describe pod -n blog <pod-name>
```
</details>

<details>
<summary><strong>❌ "Unable to load manifests" error</strong></summary>

**Symptom:** ArgoCD cannot load manifests.

**Possible causes:**
1. Git repository inaccessible
2. Incorrect path to chart
3. Syntax error in values.yaml

**Solutions:**
```bash
# Check repo connection
argocd repo list

# Test Helm rendering locally
helm template k8s/charts/spring-api

# Check repo-server logs
kubectl logs -n argocd -l app.kubernetes.io/name=argocd-repo-server
```
</details>

### Diagnostic Commands

```bash
# ┌─────────────────────────────────────────────────────────────────────────┐
# │                         ARGOCD LOGS                                     │
# └─────────────────────────────────────────────────────────────────────────┘

# Application Controller (manages synchronization)
kubectl logs -n argocd -l app.kubernetes.io/name=argocd-application-controller -f

# Repo Server (clones Git repos)
kubectl logs -n argocd -l app.kubernetes.io/name=argocd-repo-server -f

# API Server
kubectl logs -n argocd -l app.kubernetes.io/name=argocd-server -f

# ┌─────────────────────────────────────────────────────────────────────────┐
# │                          EVENTS                                         │
# └─────────────────────────────────────────────────────────────────────────┘

# ArgoCD events
kubectl get events -n argocd --sort-by='.lastTimestamp'

# Application events
kubectl get events -n blog --sort-by='.lastTimestamp'
```

---

## Best Practices

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Business applications | `app-{project}-{component}` | `app-blog-api` |
| Infrastructure | `infra-{service}` | `infra-ingress-nginx` |
| Monitoring | `infra-{tool}` | `infra-prometheus` |

### Recommended Labels

```yaml
labels:
  # Standard Kubernetes labels
  app.kubernetes.io/name: spring-api
  app.kubernetes.io/component: backend
  app.kubernetes.io/part-of: spring-k8s-gitops
  app.kubernetes.io/managed-by: argocd

  # Custom labels
  environment: production
  team: platform
```

### Deployment Checklist

- [ ] ArgoCD is installed and accessible
- [ ] Git repository is configured in ArgoCD
- [ ] Required secrets are created
- [ ] Deployment order respects dependencies
- [ ] Health checks are configured
- [ ] Monitoring is in place

---

## File Structure

```
k8s/apps/
│
├── 📄 README.md                 # Documentation (Français)
├── 📄 README.en.md              # Documentation (English)
│
├── 🚀 Business Applications
│   ├── frontend-ui.yaml         # React interface
│   ├── spring-api.yaml          # Spring Boot API
│   └── postgresql.yaml          # Database
│
├── 🔧 Infrastructure
│   ├── ingress-nginx.yaml       # Ingress controller
│   └── harbor.yaml              # Container registry
│
└── 📊 Monitoring
    ├── prometheus.yaml          # Metrics collection
    └── grafana.yaml             # Visualization
```

---

## Resources

### Official Documentation

| Resource | Link |
|----------|------|
| ArgoCD Documentation | [argo-cd.readthedocs.io](https://argo-cd.readthedocs.io/) |
| Application CRD | [Declarative Setup](https://argo-cd.readthedocs.io/en/stable/operator-manual/declarative-setup/) |
| Best Practices | [User Guide](https://argo-cd.readthedocs.io/en/stable/user-guide/best_practices/) |
| Helm Integration | [Helm Guide](https://argo-cd.readthedocs.io/en/stable/user-guide/helm/) |

### Project Links

| Service | URL | Credentials |
|---------|-----|-------------|
| ArgoCD UI | http://localhost:30080 | admin / (see command) |
| Grafana | http://localhost:30030 | admin / prom-operator |
| Prometheus | http://localhost:30090 | - |
| Harbor | http://localhost:30002 | admin / Harbor12345 |
| Frontend | http://frontend.local | - |

---

<p align="center">
  <sub>Maintained with ❤️ following GitOps principles</sub>
</p>
