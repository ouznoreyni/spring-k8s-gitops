# Kind Cluster - spring-k8s-gitops

<p align="center">
  <a href="README.md">Français</a> |
  <strong>English</strong>
</p>

This folder contains the configuration for the local Kubernetes cluster used in the **spring-k8s-gitops** project.

## What is Kind?

[Kind](https://kind.sigs.k8s.io/) (Kubernetes IN Docker) is a tool for running local Kubernetes clusters using Docker containers as "nodes". It is ideal for:

- Local development
- CI/CD testing
- Learning Kubernetes

## Prerequisites

Before creating the cluster, make sure you have installed:

| Tool | Minimum Version | Installation |
|------|-----------------|--------------|
| Docker | 20.10+ | [docs.docker.com](https://docs.docker.com/get-docker/) |
| Kind | 0.20+ | `brew install kind` or [kind.sigs.k8s.io](https://kind.sigs.k8s.io/docs/user/quick-start/#installation) |
| kubectl | 1.28+ | `brew install kubectl` |
| Helm | 3.12+ | `brew install helm` |

## Cluster Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    Kind Cluster: spring-k8s-gitops              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                   CONTROL-PLANE                          │   │
│  │  • Label: ingress-ready=true                            │   │
│  │  • Exposed ports: 80, 443, 30080, 30002, 30090, 30030   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌────────────────────────┐  ┌────────────────────────┐        │
│  │       WORKER 1         │  │       WORKER 2         │        │
│  │  • Application workloads│  │  • Application workloads│        │
│  └────────────────────────┘  └────────────────────────┘        │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Exposed Ports

| Port | Service | Description |
|------|---------|-------------|
| **80** | Ingress HTTP | HTTP traffic to applications |
| **443** | Ingress HTTPS | HTTPS traffic to applications |
| **30080** | ArgoCD | GitOps web interface |
| **30002** | Harbor | Container registry |
| **30090** | Prometheus | Metrics and monitoring |
| **30030** | Grafana | Dashboards |
| **30093** | Alertmanager | Alert management |

## Cluster Management

### Create the cluster

```bash
kind create cluster --name spring-k8s-gitops --config kind-config.yaml
```

### Check cluster status

```bash
# Verify cluster is created
kind get clusters

# Check nodes
kubectl get nodes

# Check all pods
kubectl get pods -A
```

### Delete the cluster

```bash
kind delete cluster --name spring-k8s-gitops
```

## Service Access

Once the cluster is deployed with applications:

| Service | URL | Default Credentials |
|---------|-----|---------------------|
| Frontend | http://frontend.local | - |
| ArgoCD | http://localhost:30080 | admin / `kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" \| base64 -d` |
| Harbor | http://localhost:30002 | admin / Harbor12345 |
| Prometheus | http://localhost:30090 | - |
| Grafana | http://localhost:30030 | admin / prom-operator |

### Hosts file configuration

To access the application via `frontend.local`, add this line to `/etc/hosts`:

```bash
echo "127.0.0.1 frontend.local" | sudo tee -a /etc/hosts
```

## Useful Commands

```bash
# Load a Docker image into Kind
kind load docker-image my-image:tag --name spring-k8s-gitops

# Get kubeconfig
kind get kubeconfig --name spring-k8s-gitops

# Node logs
docker logs spring-k8s-gitops-control-plane

# Access node shell
docker exec -it spring-k8s-gitops-control-plane bash
```

## Troubleshooting

### Cluster won't start

```bash
# Verify Docker is running
docker info

# Check Docker resources (memory, CPU)
docker system info
```

### Ports already in use

```bash
# Identify process using the port
lsof -i :80
lsof -i :443

# Stop the process or modify kind-config.yaml
```

### Network issues

```bash
# Restart the cluster
kind delete cluster --name spring-k8s-gitops
kind create cluster --name spring-k8s-gitops --config kind-config.yaml

# Check container logs
docker logs spring-k8s-gitops-control-plane 2>&1 | tail -50
```

### Complete reset

```bash
# Delete cluster and clean Docker
kind delete cluster --name spring-k8s-gitops
docker system prune -f
```

## File Structure

```
infra/kind/
├── README.md           # French documentation
├── README.en.md        # English documentation (this file)
└── kind-config.yaml    # Kind cluster configuration
```

## Additional Resources

- [Official Kind Documentation](https://kind.sigs.k8s.io/)
- [Kind with Ingress NGINX](https://kind.sigs.k8s.io/docs/user/ingress/)
- [Kind with local registry](https://kind.sigs.k8s.io/docs/user/local-registry/)
