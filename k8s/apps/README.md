# Applications ArgoCD - GitOps Deployment

<p align="center">
  <strong>Français</strong> |
  <a href="README.en.md">English</a>
</p>

<p align="center">
  <img src="https://argo-cd.readthedocs.io/en/stable/assets/logo.png" alt="ArgoCD Logo" width="200"/>
</p>

<p align="center">
  <strong>Déploiement GitOps déclaratif pour Kubernetes</strong><br>
  Ce dossier contient les manifestes ArgoCD Application pour orchestrer le déploiement automatisé de tous les composants.
</p>

---

## Table des Matières

- [Vue d'Ensemble](#vue-densemble)
- [Comprendre ArgoCD](#comprendre-argocd)
- [Architecture du Projet](#architecture-du-projet)
- [Catalogue des Applications](#catalogue-des-applications)
- [Anatomie d'une Application](#anatomie-dune-application)
- [Guide de Déploiement](#guide-de-déploiement)
- [Opérations Courantes](#opérations-courantes)
- [Dépannage](#dépannage)
- [Bonnes Pratiques](#bonnes-pratiques)

---

## Vue d'Ensemble

### Objectif

Ce dossier centralise la **configuration déclarative** de toutes les applications déployées sur le cluster Kubernetes via ArgoCD. Chaque fichier YAML représente une application gérée selon les principes GitOps.

### Principes GitOps Appliqués

| Principe | Description | Implémentation |
|----------|-------------|----------------|
| **Déclaratif** | L'état souhaité est décrit dans Git | Manifestes YAML versionnés |
| **Versionné** | Historique complet des changements | Git comme source de vérité |
| **Automatisé** | Synchronisation sans intervention | `syncPolicy.automated` activé |
| **Auditable** | Traçabilité complète | Logs ArgoCD + historique Git |

---

## Comprendre ArgoCD

### Qu'est-ce qu'ArgoCD ?

ArgoCD est un **contrôleur Kubernetes** qui implémente le pattern GitOps. Il surveille en continu les repositories Git et synchronise automatiquement l'état du cluster avec la configuration déclarée.

### Flux de Fonctionnement

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

### Avantages

| Avantage | Description |
|----------|-------------|
| **Rollback instantané** | Retour à n'importe quelle version via Git |
| **Audit complet** | Chaque changement est tracé dans Git |
| **Self-healing** | Correction automatique des dérives |
| **Multi-cluster** | Gestion centralisée de plusieurs clusters |
| **Sécurité** | Pas d'accès direct au cluster nécessaire |

---

## Architecture du Projet

### Vue Globale

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              ARGOCD SERVER                                   │
│                            (namespace: argocd)                               │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    Application Controller                            │   │
│  │         Surveille Git → Compare → Synchronise → Rapporte            │   │
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

### Flux de Communication

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

## Catalogue des Applications

### 📱 Applications Métier

Ces applications constituent le cœur fonctionnel du projet.

<table>
<tr>
<th>Application</th>
<th>Détails</th>
</tr>
<tr>
<td>

**Frontend UI**
`frontend-ui.yaml`

</td>
<td>

| Propriété | Valeur |
|-----------|--------|
| **Nom ArgoCD** | `app-blog-frontend` |
| **Namespace** | `blog` |
| **Chart Helm** | `k8s/charts/frontend-ui` |
| **Release** | `blog-frontend` |
| **Composant** | `frontend` |

**Description:** Interface utilisateur React avec Nginx, routage SPA et proxy API intégré.

</td>
</tr>
<tr>
<td>

**Spring API**
`spring-api.yaml`

</td>
<td>

| Propriété | Valeur |
|-----------|--------|
| **Nom ArgoCD** | `app-blog-api` |
| **Namespace** | `blog` |
| **Chart Helm** | `k8s/charts/spring-api` |
| **Release** | `blog-api` |
| **Composant** | `backend` |

**Description:** API REST réactive Spring Boot WebFlux avec authentification JWT et Clean Architecture.

</td>
</tr>
<tr>
<td>

**PostgreSQL**
`postgresql.yaml`

</td>
<td>

| Propriété | Valeur |
|-----------|--------|
| **Nom ArgoCD** | `app-blog-postgresql` |
| **Namespace** | `blog` |
| **Chart Helm** | `k8s/charts/postgresql` |
| **Release** | `blog-postgresql` |
| **Composant** | `database` |

**Description:** Base de données relationnelle PostgreSQL 15 avec persistence et métriques.

</td>
</tr>
</table>

### 🔧 Infrastructure

Ces composants fournissent les services de base nécessaires au fonctionnement du cluster.

<table>
<tr>
<th>Application</th>
<th>Détails</th>
</tr>
<tr>
<td>

**Ingress NGINX**
`ingress-nginx.yaml`

</td>
<td>

| Propriété | Valeur |
|-----------|--------|
| **Nom ArgoCD** | `infra-ingress-nginx` |
| **Namespace** | `ingress-nginx` |
| **Chart Helm** | `k8s/charts/ingress-nginx` |
| **Release** | `ingress-nginx` |
| **Composant** | `ingress` |

**Description:** Contrôleur d'entrée Kubernetes gérant le routage HTTP/HTTPS et le load balancing.

</td>
</tr>
<tr>
<td>

**Harbor**
`harbor.yaml`

</td>
<td>

| Propriété | Valeur |
|-----------|--------|
| **Nom ArgoCD** | `infra-harbor` |
| **Namespace** | `registry` |
| **Chart Helm** | `k8s/charts/harbor` |
| **Release** | `container-registry` |
| **Composant** | `registry` |

**Description:** Registre de conteneurs privé avec scan de vulnérabilités et gestion des projets.

</td>
</tr>
</table>

### 📊 Monitoring

Stack d'observabilité pour la surveillance et l'analyse des performances.

<table>
<tr>
<th>Application</th>
<th>Détails</th>
</tr>
<tr>
<td>

**Prometheus**
`prometheus.yaml`

</td>
<td>

| Propriété | Valeur |
|-----------|--------|
| **Nom ArgoCD** | `infra-prometheus` |
| **Namespace** | `monitoring` |
| **Chart Helm** | `k8s/charts/prometheus` |
| **Release** | `monitoring-stack` |
| **Composant** | `monitoring` |

**Description:** Système de collecte et stockage de métriques avec alerting via Alertmanager.

</td>
</tr>
<tr>
<td>

**Grafana**
`grafana.yaml`

</td>
<td>

| Propriété | Valeur |
|-----------|--------|
| **Nom ArgoCD** | `infra-grafana` |
| **Namespace** | `monitoring` |
| **Chart Helm** | `k8s/charts/grafana` |
| **Release** | `grafana` |
| **Composant** | `visualization` |

**Description:** Plateforme de visualisation avec tableaux de bord préconfigurés pour Kubernetes.

</td>
</tr>
</table>

---

## Anatomie d'une Application

### Structure Complète Annotée

```yaml
# ┌─────────────────────────────────────────────────────────────────────────┐
# │                        EN-TÊTE DU MANIFESTE                             │
# └─────────────────────────────────────────────────────────────────────────┘
apiVersion: argoproj.io/v1alpha1    # Version de l'API ArgoCD
kind: Application                    # Type de ressource

# ┌─────────────────────────────────────────────────────────────────────────┐
# │                           MÉTADONNÉES                                   │
# └─────────────────────────────────────────────────────────────────────────┘
metadata:
  name: app-blog-api                 # 🏷️  Identifiant unique de l'application
  namespace: argocd                  # 📍 Toujours dans le namespace argocd

  # Protection contre la suppression accidentelle
  finalizers:
    - resources-finalizer.argocd.argoproj.io

  # Labels pour l'organisation et le filtrage
  labels:
    app.kubernetes.io/component: backend           # Type de composant
    app.kubernetes.io/part-of: spring-k8s-gitops   # Projet parent

# ┌─────────────────────────────────────────────────────────────────────────┐
# │                         SPÉCIFICATION                                   │
# └─────────────────────────────────────────────────────────────────────────┘
spec:
  project: default                   # Projet ArgoCD (isolation & RBAC)

  # ┌───────────────────────────────────────────────────────────────────────┐
  # │                            SOURCE                                     │
  # │              D'où vient la configuration ?                            │
  # └───────────────────────────────────────────────────────────────────────┘
  source:
    repoURL: https://github.com/ouznoreyni/spring-k8s-gitops.git
    targetRevision: HEAD             # Branche, tag ou commit SHA
    path: k8s/charts/spring-api      # Chemin vers le chart Helm

    # Configuration Helm
    helm:
      releaseName: blog-api          # Nom de la release Helm
      valueFiles:
        - values.yaml                # Fichier(s) de valeurs

  # ┌───────────────────────────────────────────────────────────────────────┐
  # │                         DESTINATION                                   │
  # │              Où déployer les ressources ?                             │
  # └───────────────────────────────────────────────────────────────────────┘
  destination:
    server: https://kubernetes.default.svc  # Cluster cible
    namespace: blog                          # Namespace de déploiement

  # ┌───────────────────────────────────────────────────────────────────────┐
  # │                      POLITIQUE DE SYNC                                │
  # │              Comment synchroniser ?                                   │
  # └───────────────────────────────────────────────────────────────────────┘
  syncPolicy:
    automated:
      prune: true      # 🗑️  Supprimer les ressources orphelines
      selfHeal: true   # 🔧 Auto-corriger les modifications manuelles

    syncOptions:
      - CreateNamespace=true    # Créer le namespace automatiquement
      - ServerSideApply=true    # Utiliser Server-Side Apply (optionnel)
```

### Explication des Champs Clés

| Section | Champ | Description |
|---------|-------|-------------|
| **metadata** | `name` | Identifiant unique dans ArgoCD |
| **metadata** | `namespace` | Toujours `argocd` |
| **metadata** | `finalizers` | Garantit la suppression propre des ressources |
| **spec** | `project` | Isolation et contrôle d'accès |
| **source** | `repoURL` | URL du repository Git |
| **source** | `targetRevision` | Branche (`HEAD`, `main`) ou tag (`v1.0.0`) |
| **source** | `path` | Chemin relatif vers le chart |
| **destination** | `server` | URL du cluster Kubernetes |
| **destination** | `namespace` | Namespace cible pour les ressources |
| **syncPolicy** | `automated` | Active la synchronisation automatique |
| **syncPolicy** | `prune` | Supprime les ressources non définies dans Git |
| **syncPolicy** | `selfHeal` | Corrige les changements manuels |

---

## Guide de Déploiement

### Prérequis

Avant de déployer, assurez-vous d'avoir :

| Composant | Requis | Vérification |
|-----------|--------|--------------|
| Cluster Kubernetes | v1.28+ | `kubectl version` |
| ArgoCD | v2.8+ | `kubectl get pods -n argocd` |
| Accès Git | Configuré | Repository accessible |
| kubectl | Connecté | `kubectl cluster-info` |

### Étape 1 : Vérifier ArgoCD

```bash
# Vérifier que ArgoCD est opérationnel
kubectl get pods -n argocd

# Résultat attendu :
# NAME                                  READY   STATUS    RESTARTS   AGE
# argocd-application-controller-xxx    1/1     Running   0          1h
# argocd-repo-server-xxx               1/1     Running   0          1h
# argocd-server-xxx                    1/1     Running   0          1h
```

### Étape 2 : Déploiement par Ordre de Dépendance

```bash
# ┌─────────────────────────────────────────────────────────────────────────┐
# │  PHASE 1 : INFRASTRUCTURE (pas de dépendances)                          │
# └─────────────────────────────────────────────────────────────────────────┘

kubectl apply -f k8s/apps/ingress-nginx.yaml
kubectl apply -f k8s/apps/harbor.yaml

# Attendre que l'infrastructure soit prête
kubectl wait --for=condition=Healthy application/infra-ingress-nginx -n argocd --timeout=300s

# ┌─────────────────────────────────────────────────────────────────────────┐
# │  PHASE 2 : MONITORING                                                   │
# └─────────────────────────────────────────────────────────────────────────┘

kubectl apply -f k8s/apps/prometheus.yaml
kubectl apply -f k8s/apps/grafana.yaml

# ┌─────────────────────────────────────────────────────────────────────────┐
# │  PHASE 3 : BASE DE DONNÉES (dépendance pour le backend)                 │
# └─────────────────────────────────────────────────────────────────────────┘

kubectl apply -f k8s/apps/postgresql.yaml

# Attendre PostgreSQL
kubectl wait --for=condition=Healthy application/app-blog-postgresql -n argocd --timeout=300s

# ┌─────────────────────────────────────────────────────────────────────────┐
# │  PHASE 4 : APPLICATIONS MÉTIER                                          │
# └─────────────────────────────────────────────────────────────────────────┘

kubectl apply -f k8s/apps/spring-api.yaml
kubectl apply -f k8s/apps/frontend-ui.yaml
```

### Étape 3 : Vérification du Déploiement

```bash
# Vue d'ensemble de toutes les applications
kubectl get applications -n argocd

# Résultat attendu :
# NAME                    SYNC     HEALTH   STATUS
# app-blog-api            Synced   Healthy  Running
# app-blog-frontend       Synced   Healthy  Running
# app-blog-postgresql     Synced   Healthy  Running
# infra-ingress-nginx     Synced   Healthy  Running
# infra-harbor            Synced   Healthy  Running
# infra-prometheus        Synced   Healthy  Running
# infra-grafana           Synced   Healthy  Running
```

### Déploiement Express (Tout en Une Commande)

```bash
# ⚠️  Utiliser uniquement si les dépendances sont déjà gérées
kubectl apply -f k8s/apps/
```

---

## Opérations Courantes

### Gestion via CLI

```bash
# ┌─────────────────────────────────────────────────────────────────────────┐
# │                          CONSULTATION                                   │
# └─────────────────────────────────────────────────────────────────────────┘

# Lister toutes les applications
kubectl get applications -n argocd

# Détails d'une application
kubectl describe application app-blog-api -n argocd

# Statut détaillé avec argocd CLI
argocd app get app-blog-api

# ┌─────────────────────────────────────────────────────────────────────────┐
# │                        SYNCHRONISATION                                  │
# └─────────────────────────────────────────────────────────────────────────┘

# Forcer une synchronisation
argocd app sync app-blog-api

# Synchronisation avec prune
argocd app sync app-blog-api --prune

# Synchroniser toutes les applications
argocd app sync -l app.kubernetes.io/part-of=spring-k8s-gitops

# ┌─────────────────────────────────────────────────────────────────────────┐
# │                           ROLLBACK                                      │
# └─────────────────────────────────────────────────────────────────────────┘

# Voir l'historique des déploiements
argocd app history app-blog-api

# Rollback vers une version précédente
argocd app rollback app-blog-api <REVISION_ID>

# ┌─────────────────────────────────────────────────────────────────────────┐
# │                          SUPPRESSION                                    │
# └─────────────────────────────────────────────────────────────────────────┘

# Supprimer une application (et ses ressources)
kubectl delete application app-blog-api -n argocd

# Supprimer sans supprimer les ressources Kubernetes
argocd app delete app-blog-api --cascade=false
```

### Interface Web ArgoCD

```bash
# 1. Récupérer le mot de passe admin
kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 -d && echo

# 2. Accéder à l'interface
# URL: http://localhost:30080
# User: admin
# Password: (résultat de la commande ci-dessus)
```

---

## Dépannage

### Problèmes Courants

<details>
<summary><strong>❌ Application en état "OutOfSync"</strong></summary>

**Symptôme:** L'application affiche `OutOfSync` dans ArgoCD.

**Causes possibles:**
1. Modifications manuelles sur le cluster
2. Différences entre Git et le cluster
3. Erreur dans le chart Helm

**Solutions:**
```bash
# Voir les différences
argocd app diff app-blog-api

# Forcer la synchronisation
argocd app sync app-blog-api --force

# Si selfHeal est activé, attendre quelques secondes
```
</details>

<details>
<summary><strong>❌ Application en état "Degraded"</strong></summary>

**Symptôme:** L'application affiche `Degraded` ou `Progressing` trop longtemps.

**Causes possibles:**
1. Pods en CrashLoopBackOff
2. Ressources insuffisantes
3. Problème de configuration

**Solutions:**
```bash
# Vérifier les pods
kubectl get pods -n blog

# Voir les logs du pod problématique
kubectl logs -n blog <pod-name>

# Décrire le pod pour voir les événements
kubectl describe pod -n blog <pod-name>
```
</details>

<details>
<summary><strong>❌ Erreur "Unable to load manifests"</strong></summary>

**Symptôme:** ArgoCD ne peut pas charger les manifests.

**Causes possibles:**
1. Repository Git inaccessible
2. Chemin incorrect vers le chart
3. Erreur de syntaxe dans values.yaml

**Solutions:**
```bash
# Vérifier la connexion au repo
argocd repo list

# Tester le rendu Helm localement
helm template k8s/charts/spring-api

# Vérifier les logs du repo-server
kubectl logs -n argocd -l app.kubernetes.io/name=argocd-repo-server
```
</details>

### Commandes de Diagnostic

```bash
# ┌─────────────────────────────────────────────────────────────────────────┐
# │                         LOGS ARGOCD                                     │
# └─────────────────────────────────────────────────────────────────────────┘

# Application Controller (gère la synchronisation)
kubectl logs -n argocd -l app.kubernetes.io/name=argocd-application-controller -f

# Repo Server (clone les repos Git)
kubectl logs -n argocd -l app.kubernetes.io/name=argocd-repo-server -f

# API Server
kubectl logs -n argocd -l app.kubernetes.io/name=argocd-server -f

# ┌─────────────────────────────────────────────────────────────────────────┐
# │                       ÉVÉNEMENTS                                        │
# └─────────────────────────────────────────────────────────────────────────┘

# Événements ArgoCD
kubectl get events -n argocd --sort-by='.lastTimestamp'

# Événements de l'application
kubectl get events -n blog --sort-by='.lastTimestamp'
```

---

## Bonnes Pratiques

### Conventions de Nommage

| Type | Convention | Exemple |
|------|------------|---------|
| Applications métier | `app-{projet}-{composant}` | `app-blog-api` |
| Infrastructure | `infra-{service}` | `infra-ingress-nginx` |
| Monitoring | `infra-{outil}` | `infra-prometheus` |

### Labels Recommandés

```yaml
labels:
  # Labels Kubernetes standards
  app.kubernetes.io/name: spring-api
  app.kubernetes.io/component: backend
  app.kubernetes.io/part-of: spring-k8s-gitops
  app.kubernetes.io/managed-by: argocd

  # Labels personnalisés
  environment: production
  team: platform
```

### Checklist de Déploiement

- [ ] ArgoCD est installé et accessible
- [ ] Le repository Git est configuré dans ArgoCD
- [ ] Les secrets nécessaires sont créés
- [ ] L'ordre de déploiement respecte les dépendances
- [ ] Les health checks sont configurés
- [ ] Le monitoring est en place

---

## Structure des Fichiers

```
k8s/apps/
│
├── 📄 README.md                 # Documentation (Français)
├── 📄 README.en.md              # Documentation (English)
│
├── 🚀 Applications Métier
│   ├── frontend-ui.yaml         # Interface React
│   ├── spring-api.yaml          # API Spring Boot
│   └── postgresql.yaml          # Base de données
│
├── 🔧 Infrastructure
│   ├── ingress-nginx.yaml       # Contrôleur Ingress
│   └── harbor.yaml              # Registre de conteneurs
│
└── 📊 Monitoring
    ├── prometheus.yaml          # Collecte de métriques
    └── grafana.yaml             # Visualisation
```

---

## Ressources

### Documentation Officielle

| Ressource | Lien |
|-----------|------|
| ArgoCD Documentation | [argo-cd.readthedocs.io](https://argo-cd.readthedocs.io/) |
| Application CRD | [Declarative Setup](https://argo-cd.readthedocs.io/en/stable/operator-manual/declarative-setup/) |
| Best Practices | [User Guide](https://argo-cd.readthedocs.io/en/stable/user-guide/best_practices/) |
| Helm Integration | [Helm Guide](https://argo-cd.readthedocs.io/en/stable/user-guide/helm/) |

### Liens Utiles du Projet

| Service | URL | Credentials |
|---------|-----|-------------|
| ArgoCD UI | http://localhost:30080 | admin / (voir commande) |
| Grafana | http://localhost:30030 | admin / prom-operator |
| Prometheus | http://localhost:30090 | - |
| Harbor | http://localhost:30002 | admin / Harbor12345 |
| Frontend | http://frontend.local | - |

---

<p align="center">
  <sub>Maintenu avec ❤️ selon les principes GitOps</sub>
</p>
