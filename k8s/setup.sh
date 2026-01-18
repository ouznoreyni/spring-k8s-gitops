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

# Image configuration
IMAGE_REGISTRY="local"
GIT_SHORT_SHA=$(git -C "$PROJECT_ROOT" rev-parse --short HEAD 2>/dev/null || echo "dev")
BUILD_TIMESTAMP=$(date +%Y%m%d%H%M%S)
IMAGE_TAG="${GIT_SHORT_SHA}-${BUILD_TIMESTAMP}"

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

    # Check docker
    if ! command -v docker &> /dev/null; then
        log_error "docker is not installed. Please install docker first."
        exit 1
    fi
    log_success "docker is installed: $(docker --version)"

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

build_spring_api() {
    log_info "Building Spring API image with tag: $IMAGE_TAG"

    local SPRING_API_DIR="$PROJECT_ROOT/apps/spring-api"

    if [ ! -f "$SPRING_API_DIR/Dockerfile" ]; then
        log_error "Dockerfile not found at $SPRING_API_DIR/Dockerfile"
        return 1
    fi

    # Build the image
    docker build \
        -t "blog-api:${IMAGE_TAG}" \
        -t "blog-api:latest" \
        --label "git.commit=${GIT_SHORT_SHA}" \
        --label "build.timestamp=${BUILD_TIMESTAMP}" \
        "$SPRING_API_DIR"

    log_success "Spring API image built: blog-api:${IMAGE_TAG}"
}

build_frontend_ui() {
    log_info "Building Frontend UI image with tag: $IMAGE_TAG"

    local FRONTEND_DIR="$PROJECT_ROOT/apps/frontend-ui"

    # Check if Dockerfile exists, if not create one
    if [ ! -f "$FRONTEND_DIR/Dockerfile" ]; then
        log_warning "Dockerfile not found. Creating default Dockerfile for React/Vite app..."
        cat > "$FRONTEND_DIR/Dockerfile" << 'EOF'
# Build stage
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Production stage
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf 2>/dev/null || true
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
EOF
    fi

    # Create nginx.conf if not exists
    if [ ! -f "$FRONTEND_DIR/nginx.conf" ]; then
        log_info "Creating nginx.conf for SPA routing..."
        cat > "$FRONTEND_DIR/nginx.conf" << 'EOF'
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://blog-api:8080/;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
EOF
    fi

    # Build the image
    docker build \
        -t "blog-frontend:${IMAGE_TAG}" \
        -t "blog-frontend:latest" \
        --label "git.commit=${GIT_SHORT_SHA}" \
        --label "build.timestamp=${BUILD_TIMESTAMP}" \
        "$FRONTEND_DIR"

    log_success "Frontend UI image built: blog-frontend:${IMAGE_TAG}"
}

load_images_to_kind() {
    log_info "Loading images into Kind cluster..."

    kind load docker-image "blog-api:${IMAGE_TAG}" --name "$CLUSTER_NAME"
    kind load docker-image "blog-api:latest" --name "$CLUSTER_NAME"
    log_success "Loaded blog-api:${IMAGE_TAG}"

    kind load docker-image "blog-frontend:${IMAGE_TAG}" --name "$CLUSTER_NAME"
    kind load docker-image "blog-frontend:latest" --name "$CLUSTER_NAME"
    log_success "Loaded blog-frontend:${IMAGE_TAG}"

    log_success "All images loaded into Kind cluster!"
}

update_helm_values() {
    log_info "Updating Helm values with image tag: $IMAGE_TAG"

    # Update spring-api values
    local SPRING_VALUES="$SCRIPT_DIR/charts/spring-api/values.yaml"
    if [ -f "$SPRING_VALUES" ]; then
        # Use sed to update image repository and tag
        sed -i.bak "s|repository: .*|repository: blog-api|" "$SPRING_VALUES"
        sed -i.bak "s|tag: .*|tag: \"${IMAGE_TAG}\"|" "$SPRING_VALUES"
        rm -f "${SPRING_VALUES}.bak"
        log_success "Updated spring-api values.yaml"
    fi

    # Update frontend-ui values
    local FRONTEND_VALUES="$SCRIPT_DIR/charts/frontend-ui/values.yaml"
    if [ -f "$FRONTEND_VALUES" ]; then
        sed -i.bak "s|repository: .*|repository: blog-frontend|" "$FRONTEND_VALUES"
        sed -i.bak "s|tag: .*|tag: \"${IMAGE_TAG}\"|" "$FRONTEND_VALUES"
        rm -f "${FRONTEND_VALUES}.bak"
        log_success "Updated frontend-ui values.yaml"
    fi
}

commit_and_push() {
    log_info "Committing and pushing image tag updates..."

    cd "$PROJECT_ROOT"

    # Check if there are changes to commit
    if git diff --quiet k8s/charts/spring-api/values.yaml k8s/charts/frontend-ui/values.yaml 2>/dev/null; then
        log_warning "No changes to commit"
        return 0
    fi

    # Stage the changes
    git add k8s/charts/spring-api/values.yaml k8s/charts/frontend-ui/values.yaml

    # Commit with descriptive message
    git commit -m "chore(deploy): update image tags to ${IMAGE_TAG}

- blog-api: ${IMAGE_TAG}
- blog-frontend: ${IMAGE_TAG}

[skip ci]"

    # Push to remote
    if git push; then
        log_success "Changes pushed to remote. ArgoCD will sync automatically."
    else
        log_error "Failed to push changes. Please push manually."
        return 1
    fi

    cd "$SCRIPT_DIR"
}

build_and_load_images() {
    log_info "Building and loading application images..."
    log_info "Image tag: ${IMAGE_TAG}"
    echo ""

    build_spring_api
    echo ""

    build_frontend_ui
    echo ""

    load_images_to_kind
    echo ""

    update_helm_values
    echo ""

    commit_and_push
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
    echo -e "${BLUE}Image Tag:${NC} ${IMAGE_TAG}"
    echo ""
    echo -e "${BLUE}ArgoCD Access:${NC}"
    echo "  Username: admin"
    if [ -n "$ARGOCD_PASSWORD" ]; then
        echo "  Password: $ARGOCD_PASSWORD"
    else
        echo "  Password: Run 'kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath=\"{.data.password}\" | base64 -d'"
    fi
    echo ""
    echo -e "${BLUE}Access URLs:${NC}"
    echo "  ArgoCD:      http://localhost:30080"
    echo "  Prometheus:  http://localhost:30090"
    echo "  Grafana:     http://localhost:30030"
    echo "  Harbor:      http://localhost:30002"
    echo ""
    echo -e "${BLUE}Port-forward (if NodePort not working):${NC}"
    echo "  kubectl port-forward svc/argocd-argo-cd-server -n argocd 8080:80"
    echo ""
    echo -e "${BLUE}Useful Commands:${NC}"
    echo "  Check all pods:  kubectl get pods --all-namespaces"
    echo "  Check apps:      kubectl get applications -n argocd"
    echo "  Rebuild images:  ./setup.sh --build-only"
    echo ""
}

# Main execution
main() {
    echo ""
    echo "=============================================="
    echo -e "${BLUE}Spring K8s GitOps - Infrastructure Setup${NC}"
    echo "=============================================="
    echo ""

    # Parse arguments
    BUILD_ONLY=false
    SKIP_BUILD=false

    while [[ $# -gt 0 ]]; do
        case $1 in
            --build-only)
                BUILD_ONLY=true
                shift
                ;;
            --skip-build)
                SKIP_BUILD=true
                shift
                ;;
            *)
                shift
                ;;
        esac
    done

    check_prerequisites
    echo ""

    if [ "$BUILD_ONLY" = true ]; then
        build_and_load_images
        echo ""
        log_success "Images rebuilt and loaded. Push changes to trigger ArgoCD sync."
        exit 0
    fi

    create_cluster
    echo ""

    add_helm_repos
    echo ""

    if [ "$SKIP_BUILD" = false ]; then
        build_and_load_images
        echo ""
    fi

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
