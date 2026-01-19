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

check_hosts_entries() {
    log_info "Checking /etc/hosts entries..."

    local hosts_file="/etc/hosts"
    local required_hosts=("argocd.local" "harbor.local" "prometheus.local" "alertmanager.local" "grafana.local" "frontend.local")
    local missing_hosts=()

    for host in "${required_hosts[@]}"; do
        if ! grep -q "$host" "$hosts_file" 2>/dev/null; then
            missing_hosts+=("$host")
        fi
    done

    if [ ${#missing_hosts[@]} -gt 0 ]; then
        log_warning "Missing /etc/hosts entries for: ${missing_hosts[*]}"
        echo ""
        log_info "Please add the following line to your /etc/hosts file:"
        echo -e "${YELLOW}  127.0.0.1 argocd.local harbor.local prometheus.local alertmanager.local grafana.local frontend.local${NC}"
        echo ""
        log_info "Run: echo '127.0.0.1 argocd.local harbor.local prometheus.local alertmanager.local grafana.local frontend.local' | sudo tee -a /etc/hosts"
        echo ""
        read -p "Do you want to continue anyway? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log_error "Please configure /etc/hosts and run again."
            exit 1
        fi
    else
        log_success "All required /etc/hosts entries are configured"
    fi
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

# Harbor configuration
HARBOR_HOST="harbor.local"
HARBOR_PROJECT="library"
HARBOR_USER="admin"
HARBOR_PASSWORD="Harbor12345"

wait_for_harbor() {
    log_info "Waiting for Harbor to be ready..."

    local max_attempts=60
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -s -o /dev/null -w "%{http_code}" "http://${HARBOR_HOST}/api/v2.0/health" | grep -q "200"; then
            log_success "Harbor is ready!"
            return 0
        fi
        echo -n "."
        sleep 5
        attempt=$((attempt + 1))
    done

    log_warning "Harbor is not ready after ${max_attempts} attempts. Skipping Harbor push."
    return 1
}

push_images_to_harbor() {
    log_info "Pushing images to Harbor registry..."

    # Check if Harbor is accessible
    if ! wait_for_harbor; then
        log_warning "Skipping Harbor push. Images are still available in Kind cluster."
        return 0
    fi

    # Login to Harbor
    log_info "Logging into Harbor..."
    echo "${HARBOR_PASSWORD}" | docker login "${HARBOR_HOST}" -u "${HARBOR_USER}" --password-stdin

    if [ $? -ne 0 ]; then
        log_error "Failed to login to Harbor"
        return 1
    fi

    # Tag and push blog-api
    log_info "Pushing blog-api to Harbor..."
    docker tag "blog-api:${IMAGE_TAG}" "${HARBOR_HOST}/${HARBOR_PROJECT}/blog-api:${IMAGE_TAG}"
    docker tag "blog-api:latest" "${HARBOR_HOST}/${HARBOR_PROJECT}/blog-api:latest"
    docker push "${HARBOR_HOST}/${HARBOR_PROJECT}/blog-api:${IMAGE_TAG}"
    docker push "${HARBOR_HOST}/${HARBOR_PROJECT}/blog-api:latest"
    log_success "Pushed blog-api:${IMAGE_TAG} to Harbor"

    # Tag and push blog-frontend
    log_info "Pushing blog-frontend to Harbor..."
    docker tag "blog-frontend:${IMAGE_TAG}" "${HARBOR_HOST}/${HARBOR_PROJECT}/blog-frontend:${IMAGE_TAG}"
    docker tag "blog-frontend:latest" "${HARBOR_HOST}/${HARBOR_PROJECT}/blog-frontend:latest"
    docker push "${HARBOR_HOST}/${HARBOR_PROJECT}/blog-frontend:${IMAGE_TAG}"
    docker push "${HARBOR_HOST}/${HARBOR_PROJECT}/blog-frontend:latest"
    log_success "Pushed blog-frontend:${IMAGE_TAG} to Harbor"

    log_success "All images pushed to Harbor!"
}

update_helm_values() {
    log_info "Updating Helm values with image tag: $IMAGE_TAG"

    # Harbor registry path accessible from inside the cluster
    local HARBOR_REGISTRY="harbor.registry/${HARBOR_PROJECT}"

    # Update spring-api values
    local SPRING_VALUES="$SCRIPT_DIR/charts/spring-api/values.yaml"
    if [ -f "$SPRING_VALUES" ]; then
        # Use sed to update image repository and tag
        sed -i.bak "s|repository: .*|repository: ${HARBOR_REGISTRY}/blog-api|" "$SPRING_VALUES"
        sed -i.bak "s|tag: .*|tag: \"${IMAGE_TAG}\"|" "$SPRING_VALUES"
        rm -f "${SPRING_VALUES}.bak"
        log_success "Updated spring-api values.yaml"
    fi

    # Update frontend-ui values
    local FRONTEND_VALUES="$SCRIPT_DIR/charts/frontend-ui/values.yaml"
    if [ -f "$FRONTEND_VALUES" ]; then
        sed -i.bak "s|repository: .*|repository: ${HARBOR_REGISTRY}/blog-frontend|" "$FRONTEND_VALUES"
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
    log_info "Building application images..."
    log_info "Image tag: ${IMAGE_TAG}"
    echo ""

    build_spring_api
    echo ""

    build_frontend_ui
    echo ""

    # Load images to Kind as fallback (in case Harbor push fails)
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
    if ! helm upgrade --install argocd "$SCRIPT_DIR/argocd" \
        --namespace "$ARGOCD_NAMESPACE" \
        --wait \
        --timeout 10m; then
        log_error "ArgoCD Helm install failed. Checking status..."
        helm status argocd -n "$ARGOCD_NAMESPACE" || true
        kubectl get pods -n "$ARGOCD_NAMESPACE"
        return 1
    fi

    log_success "ArgoCD deployed successfully!"
}

wait_for_argocd() {
    log_info "Waiting for ArgoCD pods to be ready..."

    kctl wait --for=condition=Available deployment --all -n "$ARGOCD_NAMESPACE" --timeout=300s

    log_success "All ArgoCD pods are ready!"
}

wait_for_argocd_apps() {
    log_info "Waiting for ArgoCD to sync applications..."

    local max_attempts=60
    local attempt=1

    # Wait for the apps Application to exist
    while [ $attempt -le 30 ]; do
        if kubectl get application apps -n "$ARGOCD_NAMESPACE" &>/dev/null; then
            log_success "ArgoCD apps application found"
            break
        fi
        echo -n "."
        sleep 5
        attempt=$((attempt + 1))
    done

    # Wait for Harbor to be deployed (registry namespace and pods)
    log_info "Waiting for Harbor to be deployed by ArgoCD..."
    attempt=1
    while [ $attempt -le $max_attempts ]; do
        if kubectl get ns registry &>/dev/null; then
            # Check if Harbor pods are running
            local harbor_ready
            harbor_ready=$(kubectl get pods -n registry -l app.kubernetes.io/name=harbor --field-selector=status.phase=Running --no-headers 2>/dev/null | wc -l | tr -d '[:space:]')
            harbor_ready=${harbor_ready:-0}
            if [ "$harbor_ready" -ge 1 ]; then
                log_success "Harbor pods are starting..."
                break
            fi
        fi
        echo -n "."
        sleep 5
        attempt=$((attempt + 1))
    done

    if [ $attempt -gt $max_attempts ]; then
        log_warning "Harbor not yet deployed. Continuing anyway..."
    fi
}

configure_kind_for_harbor() {
    log_info "Configuring Kind nodes for Harbor registry access..."

    # Wait for Harbor service to get an IP
    local harbor_ip=""
    for i in {1..30}; do
        harbor_ip=$(kubectl get svc harbor -n registry -o jsonpath='{.spec.clusterIP}' 2>/dev/null || echo "")
        if [ -n "$harbor_ip" ]; then
            break
        fi
        sleep 2
    done

    if [ -z "$harbor_ip" ]; then
        log_warning "Could not get Harbor service IP. Skipping Kind node configuration."
        return 0
    fi

    log_info "Harbor service IP: $harbor_ip"

    # Configure each Kind node
    for node in $(kind get nodes --name "$CLUSTER_NAME" 2>/dev/null); do
        log_info "Configuring node: $node"

        # Add Harbor to /etc/hosts
        docker exec "$node" sh -c "grep -q 'harbor.registry' /etc/hosts || echo '$harbor_ip harbor.registry' >> /etc/hosts"

        # Configure containerd for insecure registry
        docker exec "$node" mkdir -p /etc/containerd/certs.d/harbor.registry
        docker exec "$node" sh -c 'cat > /etc/containerd/certs.d/harbor.registry/hosts.toml << EOF
server = "http://harbor.registry"

[host."http://harbor.registry"]
  capabilities = ["pull", "resolve"]
  skip_verify = true
EOF'

        # Add registry config path to containerd config if not present
        docker exec "$node" sh -c '
            if ! grep -q "config_path.*certs.d" /etc/containerd/config.toml 2>/dev/null; then
                cat >> /etc/containerd/config.toml << EOF

[plugins."io.containerd.grpc.v1.cri".registry]
  config_path = "/etc/containerd/certs.d"
EOF
            fi
        '

        # Restart containerd
        docker exec "$node" systemctl restart containerd
    done

    log_success "Kind nodes configured for Harbor registry!"

    # Give containerd time to restart
    sleep 5
}

get_argocd_password() {
    log_info "Retrieving ArgoCD admin password..."

    # Try different secret names used by different ArgoCD versions/charts
    local secret_names=("argocd-initial-admin-secret" "argocd-argo-cd-initial-admin-secret" "argocd-secret")

    for secret_name in "${secret_names[@]}"; do
        # Wait for the secret to be created
        for i in {1..10}; do
            if kubectl get secret "$secret_name" -n "$ARGOCD_NAMESPACE" &>/dev/null; then
                if [ "$secret_name" = "argocd-secret" ]; then
                    # argocd-secret stores bcrypt hash, not the actual password
                    log_info "Found argocd-secret but it contains hashed password"
                    break
                fi
                ARGOCD_PASSWORD=$(kubectl -n "$ARGOCD_NAMESPACE" get secret "$secret_name" -o jsonpath="{.data.password}" 2>/dev/null | base64 -d)
                if [ -n "$ARGOCD_PASSWORD" ]; then
                    log_success "Retrieved password from $secret_name"
                    return 0
                fi
            fi
            sleep 2
        done
    done

    if [ -z "$ARGOCD_PASSWORD" ]; then
        log_warning "Could not retrieve initial admin password."
        log_info "Try: kubectl -n $ARGOCD_NAMESPACE get secret -o name | xargs -I {} kubectl -n $ARGOCD_NAMESPACE get {} -o yaml"
        log_info "Or reset with: argocd admin initial-password -n $ARGOCD_NAMESPACE"
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
    echo -e "${BLUE}Access URLs (via Ingress):${NC}"
    echo "  ArgoCD:       http://argocd.local"
    echo "  Prometheus:   http://prometheus.local"
    echo "  Alertmanager: http://alertmanager.local"
    echo "  Grafana:      http://grafana.local"
    echo "  Harbor:       http://harbor.local"
    echo "  Frontend:     http://frontend.local"
    echo ""
    echo -e "${BLUE}Required /etc/hosts entries:${NC}"
    echo "  127.0.0.1 argocd.local harbor.local prometheus.local alertmanager.local grafana.local frontend.local"
    echo ""
    echo -e "${BLUE}Docker insecure registry (for pushing to Harbor):${NC}"
    echo "  Add to Docker Desktop settings or /etc/docker/daemon.json:"
    echo "  { \"insecure-registries\": [\"harbor.local\"] }"
    echo ""
    echo -e "${BLUE}Port-forward (if Ingress not working):${NC}"
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

    check_hosts_entries
    echo ""

    if [ "$BUILD_ONLY" = true ]; then
        build_and_load_images
        echo ""
        push_images_to_harbor
        echo ""
        log_success "Images rebuilt and pushed to Harbor. ArgoCD will sync automatically."
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

    # Wait for ArgoCD to sync and deploy apps (especially Harbor)
    wait_for_argocd_apps
    echo ""

    # Configure Kind nodes to access Harbor registry
    configure_kind_for_harbor
    echo ""

    get_argocd_password

    print_summary

    if [ "$SKIP_BUILD" = false ]; then
        push_images_to_harbor
        echo ""
    fi
}

# Run main function
main "$@"
