#!/bin/bash

# ============================================
# KUBERNETES DEPLOYMENT SCRIPT
# Credit Enforcement Platform
# ============================================
#
# Usage: ./deploy.sh [command]
#
# Commands:
#   setup     - Initial cluster setup (ingress controller, cert-manager)
#   deploy    - Deploy all services
#   verify    - Verify deployment
#   cleanup   - Delete all resources
#   logs      - View logs for all services
#   restart   - Restart all services
#

set -e  # Exit on error

NAMESPACE="credit-enforcement"
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/base" && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Helper functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

check_kubectl() {
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl not found. Please install kubectl first."
        exit 1
    fi
    log_info "kubectl found: $(kubectl version --client --short)"
}

check_cluster() {
    if ! kubectl cluster-info &> /dev/null; then
        log_error "Cannot connect to Kubernetes cluster. Check your kubeconfig."
        exit 1
    fi
    log_info "Connected to cluster: $(kubectl config current-context)"
}

# Command: setup
setup_cluster() {
    log_info "Setting up cluster infrastructure..."

    # Install NGINX Ingress Controller
    log_info "Installing NGINX Ingress Controller..."
    kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/cloud/deploy.yaml

    log_info "Waiting for Ingress Controller to be ready..."
    kubectl wait --namespace ingress-nginx \
        --for=condition=ready pod \
        --selector=app.kubernetes.io/component=controller \
        --timeout=300s

    log_info "Getting LoadBalancer external IP..."
    kubectl get svc -n ingress-nginx ingress-nginx-controller

    # Install cert-manager (optional)
    read -p "Install cert-manager for automatic SSL? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        log_info "Installing cert-manager..."
        kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml

        log_info "Waiting for cert-manager to be ready..."
        kubectl wait --namespace cert-manager \
            --for=condition=ready pod \
            --selector=app.kubernetes.io/instance=cert-manager \
            --timeout=300s
    fi

    log_info "Cluster setup complete!"
}

# Command: deploy
deploy_services() {
    log_info "Starting deployment to namespace: $NAMESPACE"

    cd "$BASE_DIR"

    # Check if secrets exist
    if ! kubectl get secret postgres-credentials -n $NAMESPACE &> /dev/null; then
        log_error "Secrets not found! Please create secrets first:"
        log_error "1. Copy secrets.yaml to secrets-actual.yaml"
        log_error "2. Replace all <BASE64_ENCODED_VALUE> with actual values"
        log_error "3. kubectl apply -f secrets-actual.yaml"
        exit 1
    fi

    # Create namespace and ConfigMaps
    log_info "Creating namespace and ConfigMaps..."
    kubectl apply -f namespace.yaml
    kubectl apply -f configmap.yaml

    # Deploy Redis
    log_info "Deploying Redis..."
    kubectl apply -f redis-statefulset.yaml
    log_info "Waiting for Redis to be ready..."
    kubectl wait --for=condition=ready pod -l app=redis -n $NAMESPACE --timeout=300s

    # Deploy Eureka Server
    log_info "Deploying Eureka Server..."
    kubectl apply -f eureka-server.yaml
    log_info "Waiting for Eureka Server to be ready..."
    kubectl wait --for=condition=ready pod -l app=eureka-server -n $NAMESPACE --timeout=300s

    # Deploy Access Management Service
    log_info "Deploying Access Management Service..."
    kubectl apply -f access-management-service.yaml
    log_info "Waiting for Access Management Service to be ready..."
    kubectl wait --for=condition=ready pod -l app=access-management-service -n $NAMESPACE --timeout=300s

    # Deploy remaining services
    log_info "Deploying remaining services..."
    kubectl apply -f master-data-service.yaml
    kubectl apply -f case-sourcing-service.yaml
    kubectl apply -f communication-service.yaml
    kubectl apply -f allocation-reallocation-service.yaml

    log_info "Waiting for all services to be ready..."
    kubectl wait --for=condition=ready pod -l app=master-data-service -n $NAMESPACE --timeout=300s
    kubectl wait --for=condition=ready pod -l app=case-sourcing-service -n $NAMESPACE --timeout=300s
    kubectl wait --for=condition=ready pod -l app=communication-service -n $NAMESPACE --timeout=300s
    kubectl wait --for=condition=ready pod -l app=allocation-reallocation-service -n $NAMESPACE --timeout=300s

    # Deploy API Gateway
    log_info "Deploying API Gateway..."
    kubectl apply -f api-gateway.yaml
    log_info "Waiting for API Gateway to be ready..."
    kubectl wait --for=condition=ready pod -l app=api-gateway -n $NAMESPACE --timeout=300s

    # Deploy Ingress
    log_info "Deploying Ingress..."
    kubectl apply -f ingress.yaml

    log_info "Deployment complete!"
    log_info ""
    log_info "Next steps:"
    log_info "1. Check deployment: ./deploy.sh verify"
    log_info "2. Get Ingress IP: kubectl get svc -n ingress-nginx"
    log_info "3. Configure DNS to point to Ingress IP"
    log_info "4. Test API: curl http://<INGRESS_IP>/actuator/health"
}

# Command: verify
verify_deployment() {
    log_info "Verifying deployment..."

    # Check namespace
    if ! kubectl get namespace $NAMESPACE &> /dev/null; then
        log_error "Namespace $NAMESPACE not found!"
        exit 1
    fi

    # Check pods
    log_info "Checking pods..."
    kubectl get pods -n $NAMESPACE

    # Count running pods
    TOTAL_PODS=$(kubectl get pods -n $NAMESPACE --no-headers | wc -l)
    RUNNING_PODS=$(kubectl get pods -n $NAMESPACE --field-selector=status.phase=Running --no-headers | wc -l)

    log_info "Pods: $RUNNING_PODS/$TOTAL_PODS running"

    if [ "$RUNNING_PODS" -ne "$TOTAL_PODS" ]; then
        log_warn "Not all pods are running. Check with: kubectl get pods -n $NAMESPACE"
    else
        log_info "All pods are running! ✓"
    fi

    # Check services
    log_info "Checking services..."
    kubectl get svc -n $NAMESPACE

    # Check Ingress
    log_info "Checking Ingress..."
    kubectl get ingress -n $NAMESPACE

    # Get Ingress IP
    INGRESS_IP=$(kubectl get svc -n ingress-nginx ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "Not ready yet")
    log_info "Ingress External IP: $INGRESS_IP"

    # Test API Gateway health
    if [ "$INGRESS_IP" != "Not ready yet" ]; then
        log_info "Testing API Gateway health..."
        HEALTH_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://$INGRESS_IP/actuator/health || echo "000")
        if [ "$HEALTH_RESPONSE" = "200" ]; then
            log_info "API Gateway health check: PASSED ✓"
        else
            log_warn "API Gateway health check: FAILED (HTTP $HEALTH_RESPONSE)"
        fi
    fi

    log_info "Verification complete!"
}

# Command: cleanup
cleanup() {
    log_warn "This will delete ALL resources in namespace: $NAMESPACE"
    log_warn "This includes:"
    log_warn "  - All pods, services, deployments"
    log_warn "  - PersistentVolumeClaims (Redis data will be LOST!)"
    log_warn "  - Secrets and ConfigMaps"
    read -p "Are you sure? (type 'yes' to confirm): " -r

    if [[ $REPLY = "yes" ]]; then
        log_info "Deleting namespace $NAMESPACE..."
        kubectl delete namespace $NAMESPACE
        log_info "Cleanup complete!"
    else
        log_info "Cleanup cancelled."
    fi
}

# Command: logs
view_logs() {
    log_info "Viewing logs for all services..."
    log_info "Press Ctrl+C to exit"

    # Use kubectl logs with selector
    kubectl logs -f -l app.kubernetes.io/name=credit-enforcement -n $NAMESPACE --all-containers=true --since=5m
}

# Command: restart
restart_services() {
    log_info "Restarting all services..."
    kubectl rollout restart deployment -n $NAMESPACE
    log_info "Restart initiated. Services will be updated with zero downtime."
}

# Main script
main() {
    check_kubectl
    check_cluster

    case "${1:-}" in
        setup)
            setup_cluster
            ;;
        deploy)
            deploy_services
            ;;
        verify)
            verify_deployment
            ;;
        cleanup)
            cleanup
            ;;
        logs)
            view_logs
            ;;
        restart)
            restart_services
            ;;
        *)
            echo "Usage: $0 {setup|deploy|verify|cleanup|logs|restart}"
            echo ""
            echo "Commands:"
            echo "  setup     - Initial cluster setup (ingress controller, cert-manager)"
            echo "  deploy    - Deploy all services"
            echo "  verify    - Verify deployment"
            echo "  cleanup   - Delete all resources"
            echo "  logs      - View logs for all services"
            echo "  restart   - Restart all services"
            exit 1
            ;;
    esac
}

main "$@"