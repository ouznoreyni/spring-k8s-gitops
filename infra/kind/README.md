# kind cluster

This folder contains the configuration for the local Kubernetes cluster
used in the spring-k8s-gitops project.

## Create cluster
kind create cluster --name spring-k8s-gitops --config kind-config.yaml

## Delete cluster
kind delete cluster --name spring-k8s-gitops
