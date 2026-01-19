# Cluster Kind - spring-k8s-gitops

<p align="center">
  <strong>Français</strong> |
  <a href="README.en.md">English</a>
</p>

Ce dossier contient la configuration du cluster Kubernetes local utilisé pour le projet **spring-k8s-gitops**.

## Qu'est-ce que Kind ?

[Kind](https://kind.sigs.k8s.io/) (Kubernetes IN Docker) est un outil permettant d'exécuter des clusters Kubernetes locaux en utilisant des conteneurs Docker comme "nœuds". Il est idéal pour :

- Le développement local
- Les tests CI/CD
- L'apprentissage de Kubernetes

## Prérequis

Avant de créer le cluster, assurez-vous d'avoir installé :

| Outil | Version minimale | Installation |
|-------|------------------|--------------|
| Docker | 20.10+ | [docs.docker.com](https://docs.docker.com/get-docker/) |
| Kind | 0.20+ | `brew install kind` ou [kind.sigs.k8s.io](https://kind.sigs.k8s.io/docs/user/quick-start/#installation) |
| kubectl | 1.28+ | `brew install kubectl` |
| Helm | 3.12+ | `brew install helm` |

## Architecture du Cluster

```
┌─────────────────────────────────────────────────────────────────┐
│                    Cluster Kind: spring-k8s-gitops              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                   CONTROL-PLANE                          │   │
│  │  • Label: ingress-ready=true                            │   │
│  │  • Ports exposés: 80, 443                               │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌────────────────────────┐  ┌────────────────────────┐        │
│  │       WORKER 1         │  │       WORKER 2         │        │
│  │  • Workloads applicatifs│  │  • Workloads applicatifs│        │
│  └────────────────────────┘  └────────────────────────┘        │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Ports Exposés

| Port | Service | Description |
|------|---------|-------------|
| **80** | Ingress HTTP | Trafic HTTP vers toutes les applications |
| **443** | Ingress HTTPS | Trafic HTTPS vers toutes les applications |

> **Note** : Tous les services sont accessibles via l'Ingress NGINX sur les ports 80/443. Les NodePorts ne sont plus nécessaires.

## Gestion du Cluster

### Créer le cluster

```bash
kind create cluster --name spring-k8s-gitops --config kind-config.yaml
```

### Vérifier l'état du cluster

```bash
# Vérifier que le cluster est créé
kind get clusters

# Vérifier les nœuds
kubectl get nodes

# Vérifier tous les pods
kubectl get pods -A
```

### Supprimer le cluster

```bash
kind delete cluster --name spring-k8s-gitops
```

## Accès aux Services

Une fois le cluster déployé avec les applications, tous les services sont accessibles via l'Ingress :

| Service | URL | Identifiants par défaut |
|---------|-----|-------------------------|
| Frontend | http://frontend.local | - |
| ArgoCD | http://argocd.local | admin / `kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" \| base64 -d` |
| Harbor | http://harbor.local | admin / Harbor12345 |
| Prometheus | http://prometheus.local | - |
| Alertmanager | http://alertmanager.local | - |
| Grafana | http://grafana.local | admin / admin |

### Configuration du fichier hosts

Pour accéder aux services via leurs domaines locaux, ajoutez ces lignes à `/etc/hosts` :

```bash
cat << 'EOF' | sudo tee -a /etc/hosts
# spring-k8s-gitops - Services locaux
127.0.0.1 frontend.local
127.0.0.1 argocd.local
127.0.0.1 harbor.local
127.0.0.1 prometheus.local
127.0.0.1 alertmanager.local
127.0.0.1 grafana.local
EOF
```

Ou en une seule ligne :

```bash
echo "127.0.0.1 frontend.local argocd.local harbor.local prometheus.local alertmanager.local grafana.local" | sudo tee -a /etc/hosts
```

## Commandes Utiles

```bash
# Charger une image Docker dans Kind
kind load docker-image mon-image:tag --name spring-k8s-gitops

# Obtenir le kubeconfig
kind get kubeconfig --name spring-k8s-gitops

# Logs d'un nœud
docker logs spring-k8s-gitops-control-plane

# Accéder au shell d'un nœud
docker exec -it spring-k8s-gitops-control-plane bash
```

## Dépannage

### Le cluster ne démarre pas

```bash
# Vérifier que Docker est en cours d'exécution
docker info

# Vérifier les ressources Docker (mémoire, CPU)
docker system info
```

### Les ports sont déjà utilisés

```bash
# Identifier le processus utilisant le port
lsof -i :80
lsof -i :443

# Arrêter le processus ou modifier kind-config.yaml
```

### Problèmes de réseau

```bash
# Redémarrer le cluster
kind delete cluster --name spring-k8s-gitops
kind create cluster --name spring-k8s-gitops --config kind-config.yaml

# Vérifier les logs du conteneur
docker logs spring-k8s-gitops-control-plane 2>&1 | tail -50
```

### Réinitialiser complètement

```bash
# Supprimer le cluster et nettoyer Docker
kind delete cluster --name spring-k8s-gitops
docker system prune -f
```

## Structure des Fichiers

```
infra/kind/
├── README.md           # Documentation en français
├── README.en.md        # Documentation en anglais
└── kind-config.yaml    # Configuration du cluster Kind
```

## Ressources Complémentaires

- [Documentation officielle Kind](https://kind.sigs.k8s.io/)
- [Kind avec Ingress NGINX](https://kind.sigs.k8s.io/docs/user/ingress/)
- [Kind avec registre local](https://kind.sigs.k8s.io/docs/user/local-registry/)
