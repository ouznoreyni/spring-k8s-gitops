#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Wrapper to suppress kubectl warnings (unrecognized format "int64")
kctl() {
    kubectl "$@" 2>&1 | grep -v "unrecognized format" || true
}

# Configuration
CLUSTER_NAME="spring-k8s-gitops"
ARGOCD_NAMESPACE="argocd"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_prerequisites() {
    log_info "Checking prerequisites..."

    # Check kind
    if ! command -v kind &> /dev/null; then
        log_error "kind is not installed. Please install kind first."
        log_info "Install with: brew install kind"
        exit 1
    fi
    log_success "kind is installed: $(kind version)"

    # Check helm
    if ! command -v helm &> /dev/null; then
        log_error "helm is not installed. Please install helm first."
        log_info "Install with: brew install helm"
        exit 1
    fi
    log_success "helm is installed: $(helm version --short)"

    # Check kubectl
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed. Please install kubectl first."
        log_info "Install with: brew install kubectl"
        exit 1
    fi
    log_success "kubectl is installed: $(kubectl version --client --short 2>/dev/null || kubectl version --client)"
}

create_cluster() {
    log_info "Checking if Kind cluster '$CLUSTER_NAME' exists..."

    if kind get clusters 2>/dev/null | grep -q "^${CLUSTER_NAME}$"; then
        log_warning "Cluster '$CLUSTER_NAME' already exists."
        read -p "Do you want to delete and recreate it? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            log_info "Deleting existing cluster..."
            kind delete cluster --name "$CLUSTER_NAME"
        else
            log_info "Using existing cluster."
            kctl cluster-info --context "kind-${CLUSTER_NAME}"
            return
        fi
    fi

    log_info "Creating Kind cluster '$CLUSTER_NAME'..."
    kind create cluster --name "$CLUSTER_NAME" --config "$PROJECT_ROOT/infra/kind/kind-config.yaml"

    log_info "Waiting for cluster to be ready..."
    kctl wait --for=condition=Ready nodes --all --timeout=120s

    log_success "Kind cluster '$CLUSTER_NAME' created successfully!"
    kctl cluster-info --context "kind-${CLUSTER_NAME}"
}

add_helm_repos() {
    log_info "Adding Helm repositories..."

    helm repo add argo https://argoproj.github.io/argo-helm 2>/dev/null || true
    helm repo add bitnami https://charts.bitnami.com/bitnami 2>/dev/null || true
    helm repo add harbor https://helm.goharbor.io 2>/dev/null || true
    helm repo add prometheus-community https://prometheus-community.github.io/helm-charts 2>/dev/null || true
    helm repo add grafana https://grafana.github.io/helm-charts 2>/dev/null || true

    log_info "Updating Helm repositories..."
    helm repo update

    log_success "Helm repositories configured!"
}

update_argocd_dependencies() {
    log_info "Updating ArgoCD Helm dependencies..."

    cd "$SCRIPT_DIR/argocd"
    helm dependency update
    cd "$SCRIPT_DIR"

    log_success "ArgoCD dependencies updated!"
}

deploy_argocd() {
    log_info "Deploying ArgoCD to namespace '$ARGOCD_NAMESPACE'..."

    # Create namespace if it doesn't exist
    kubectl create namespace "$ARGOCD_NAMESPACE" --dry-run=client -o yaml 2>/dev/null | kctl apply -f -

    # Install or upgrade ArgoCD
    helm upgrade --install argocd "$SCRIPT_DIR/argocd" \
        --namespace "$ARGOCD_NAMESPACE" \
        --wait \
        --timeout 10m \
        2>&1 | grep -v "unrecognized format" || true

    log_success "ArgoCD deployed successfully!"
}

wait_for_argocd() {
    log_info "Waiting for ArgoCD pods to be ready..."

    kctl wait --for=condition=Available deployment --all -n "$ARGOCD_NAMESPACE" --timeout=300s

    log_success "All ArgoCD pods are ready!"
}

get_argocd_password() {
    log_info "Retrieving ArgoCD admin password..."

    # Wait for the secret to be created
    for i in {1..30}; do
        if kubectl get secret argocd-initial-admin-secret -n "$ARGOCD_NAMESPACE" &>/dev/null; then
            break
        fi
        sleep 2
    done

    ARGOCD_PASSWORD=$(kubectl -n "$ARGOCD_NAMESPACE" get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" 2>/dev/null | base64 -d)

    if [ -z "$ARGOCD_PASSWORD" ]; then
        log_warning "Could not retrieve initial admin password. It may have been deleted or changed."
        log_info "You can reset the password using: argocd admin initial-password -n $ARGOCD_NAMESPACE"
    fi
}

print_summary() {
    echo ""
    echo "=============================================="
    echo -e "${GREEN}Setup Complete!${NC}"
    echo "=============================================="
    echo ""
    echo -e "${BLUE}ArgoCD Access:${NC}"
    echo "  Username: admin"
    if [ -n "$ARGOCD_PASSWORD" ]; then
        echo "  Password: $ARGOCD_PASSWORD"
    else
        echo "  Password: Run 'kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath=\"{.data.password}\" | base64 -d'"
    fi
    echo ""
    echo -e "${BLUE}Access Options:${NC}"
    echo "  Option 1 (NodePort):    http://localhost:30080"
    echo "  Option 2 (Port-forward): Run the command below, then access http://localhost:8080"
    echo "    kubectl port-forward svc/argocd-argo-cd-server -n argocd 8080:80"
    echo ""
    echo -e "${BLUE}Useful Commands:${NC}"
    echo "  Check pods:     kubectl get pods -n argocd"
    echo "  Check services: kubectl get svc -n argocd"
    echo "  Get password:   kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath=\"{.data.password}\" | base64 -d"
    echo ""
    echo -e "${BLUE}Next Steps:${NC}"
    echo "  1. Access ArgoCD UI"
    echo "  2. Login with admin credentials"
    echo "  3. Deploy applications using: kubectl apply -f k8s/apps/<app>.yaml"
    echo ""
}

# Main execution
main() {
    echo ""
    echo "=============================================="
    echo -e "${BLUE}Spring K8s GitOps - Infrastructure Setup${NC}"
    echo "=============================================="
    echo ""

    check_prerequisites
    echo ""

    create_cluster
    echo ""

    add_helm_repos
    echo ""

    update_argocd_dependencies
    echo ""

    deploy_argocd
    echo ""

    wait_for_argocd
    echo ""

    get_argocd_password

    print_summary
}

# Run main function
main "$@"
