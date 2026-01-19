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
│  │  • Exposed ports: 80, 443                               │   │
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
| **80** | Ingress HTTP | HTTP traffic to all applications |
| **443** | Ingress HTTPS | HTTPS traffic to all applications |

> **Note**: All services are accessible via Ingress NGINX on ports 80/443. NodePorts are no longer needed.

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

Once the cluster is deployed with applications, all services are accessible via Ingress:

| Service | URL | Default Credentials |
|---------|-----|---------------------|
| Frontend | http://frontend.local | - |
| ArgoCD | http://argocd.local | admin / `kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" \| base64 -d` |
| Harbor | http://harbor.local | admin / Harbor12345 |
| Prometheus | http://prometheus.local | - |
| Alertmanager | http://alertmanager.local | - |
| Grafana | http://grafana.local | admin / admin |

### Hosts file configuration

To access services via their local domains, add these lines to `/etc/hosts`:

```bash
cat << 'EOF' | sudo tee -a /etc/hosts
# spring-k8s-gitops - Local services
127.0.0.1 frontend.local
127.0.0.1 argocd.local
127.0.0.1 harbor.local
127.0.0.1 prometheus.local
127.0.0.1 alertmanager.local
127.0.0.1 grafana.local
EOF
```

Or in a single line:

```bash
echo "127.0.0.1 frontend.local argocd.local harbor.local prometheus.local alertmanager.local grafana.local" | sudo tee -a /etc/hosts
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
