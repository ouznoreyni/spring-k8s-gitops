# Kubernetes Manifests

This folder contains Helm charts and Kubernetes manifests for the spring-k8s-gitops project.

## Structure

```
k8s/
├── argocd/              # ArgoCD Helm chart (umbrella chart)
│   ├── Chart.yaml
│   ├── values.yaml
│   └── templates/
├── apps/                # ArgoCD Application manifests
│   ├── spring-api.yaml
│   ├── frontend-ui.yaml
│   ├── postgresql.yaml
│   ├── harbor.yaml
│   ├── prometheus.yaml
│   └── grafana.yaml
└── charts/              # Application Helm charts
    ├── spring-api/      # Spring Boot API (includes PostgreSQL subchart)
    ├── frontend-ui/     # React frontend
    ├── postgresql/      # Standalone PostgreSQL database
    ├── harbor/          # Container registry
    ├── prometheus/      # Monitoring stack
    └── grafana/         # Visualization
```

## Prerequisites

- Kubernetes cluster (Kind, Minikube, or cloud provider)
- Helm v4.0.5+ installed
- kubectl configured

## Quick Start

### 1. Create the Kind cluster

```bash
cd infra/kind
kind create cluster --name spring-k8s-gitops --config kind-config.yaml
```

### 2. Add Helm repositories

```bash
helm repo add argo https://argoproj.github.io/argo-helm
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo add harbor https://helm.goharbor.io
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo add grafana https://grafana.github.io/helm-charts
helm repo update
```

### 3. Update all Helm dependencies

```bash
cd k8s/argocd && helm dependency update && cd ../..
cd k8s/charts/spring-api && helm dependency update && cd ../../..
cd k8s/charts/postgresql && helm dependency update && cd ../../..
cd k8s/charts/harbor && helm dependency update && cd ../../..
cd k8s/charts/prometheus && helm dependency update && cd ../../..
cd k8s/charts/grafana && helm dependency update && cd ../../..
```

## Setup ArgoCD

### Install ArgoCD

```bash
cd k8s/argocd
helm install argocd . -n argocd --create-namespace
```

### Get the initial admin password

```bash
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
```

### Access ArgoCD UI

```bash
# NodePort: http://localhost:30080
# Or use port-forward:
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

Login: `admin` / (password from above)

## Deploy Applications

### With ArgoCD (recommended)

```bash
# Update repoURL in each file to your repository
kubectl apply -f k8s/apps/postgresql.yaml
kubectl apply -f k8s/apps/harbor.yaml
kubectl apply -f k8s/apps/prometheus.yaml
kubectl apply -f k8s/apps/grafana.yaml
kubectl apply -f k8s/apps/spring-api.yaml
kubectl apply -f k8s/apps/frontend-ui.yaml
```

### Without ArgoCD (direct Helm install)

```bash
# PostgreSQL (standalone database)
cd k8s/charts/postgresql
helm install postgresql . -n database --create-namespace

# Harbor
cd k8s/charts/harbor
helm install harbor . -n harbor --create-namespace

# Prometheus
cd k8s/charts/prometheus
helm install prometheus . -n monitoring --create-namespace

# Grafana
cd k8s/charts/grafana
helm install grafana . -n monitoring --create-namespace

# Spring API (includes its own PostgreSQL by default)
cd k8s/charts/spring-api
helm install spring-api . -n spring-api --create-namespace

# Frontend UI
cd k8s/charts/frontend-ui
helm install frontend-ui . -n frontend-ui --create-namespace
```

## Component Access

| Component   | URL                      | Default Credentials        |
|-------------|--------------------------|----------------------------|
| ArgoCD      | http://localhost:30080   | admin / (auto-generated)   |
| PostgreSQL  | localhost:5432           | bloguser / blogpassword    |
| Harbor      | http://localhost:30002   | admin / Harbor12345        |
| Prometheus  | http://localhost:30090   | N/A                        |
| Grafana     | http://localhost:30030   | admin / admin           |
| Alertmanager| http://localhost:30093   | N/A                     |

## Harbor - Container Registry

Harbor provides a private container registry with vulnerability scanning.

### Push images to Harbor

```bash
# Tag your image
docker tag spring-api:latest localhost:30002/library/spring-api:latest

# Login to Harbor
docker login localhost:30002 -u admin -p Harbor12345

# Push the image
docker push localhost:30002/library/spring-api:latest
```

### Configure apps to use Harbor

Update `values.yaml` in your app charts:

```yaml
image:
  repository: localhost:30002/library/spring-api
  tag: latest
```

## Prometheus - Monitoring

Prometheus is configured to scrape:
- Kubernetes cluster metrics
- Node metrics (via node-exporter)
- Spring Boot actuator metrics (via `/actuator/prometheus`)

### Enable metrics in Spring Boot

Add to your `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

Add the dependency to `pom.xml`:

```xml
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

## Grafana - Visualization

Grafana comes pre-configured with:
- Prometheus as the default datasource
- Kubernetes dashboards
- Spring Boot / JVM dashboards
- Node Exporter dashboard

### Access Grafana

1. Open http://localhost:30030
2. Login with `admin` / `admin`
3. Navigate to Dashboards

## Useful Commands

### Check status

```bash
kubectl get pods -n argocd
kubectl get pods -n harbor
kubectl get pods -n monitoring
kubectl get applications -n argocd
```

### View logs

```bash
kubectl logs -f deployment/spring-api -n spring-api
kubectl logs -f deployment/grafana -n monitoring
```

### Port-forward services

```bash
# ArgoCD
kubectl port-forward svc/argocd-server -n argocd 8080:443

# Harbor
kubectl port-forward svc/harbor-portal -n harbor 8081:80

# Prometheus
kubectl port-forward svc/prometheus-kube-prometheus-prometheus -n monitoring 9090:9090

# Grafana
kubectl port-forward svc/grafana -n monitoring 3000:80
```

## Customization

### Configure image registry

```yaml
image:
  repository: your-registry/spring-api
  tag: "1.0.0"
```

### Enable Ingress

```yaml
ingress:
  enabled: true
  className: nginx
  hosts:
    - host: api.example.com
      paths:
        - path: /
          pathType: Prefix
```

### Configure resources

```yaml
resources:
  limits:
    cpu: 500m
    memory: 512Mi
  requests:
    cpu: 250m
    memory: 256Mi
```
