#!/bin/bash

echo "=== Kubernetes Ingress Debugging ==="
echo ""

# 1. Check Ingress Controller is running
echo "1. Checking NGINX Ingress Controller status..."
kubectl get pods -n ingress-nginx
echo ""

# 2. Check Ingress Controller service and external IP
echo "2. Checking Ingress Controller Service..."
kubectl get svc -n ingress-nginx ingress-nginx-controller
echo ""

# 3. Check Ingress resource
echo "3. Checking Ingress resource..."
kubectl get ingress -n credit-enforcement
echo ""

# 4. Describe Ingress for details
echo "4. Ingress details..."
kubectl describe ingress -n credit-enforcement
echo ""

# 5. Check if API Gateway service exists
echo "5. Checking API Gateway service..."
kubectl get svc -n credit-enforcement api-gateway
echo ""

# 6. Check API Gateway endpoints
echo "6. Checking API Gateway endpoints (actual pods)..."
kubectl get endpoints -n credit-enforcement api-gateway
echo ""

# 7. Check Ingress Controller logs for recent errors
echo "7. Recent Ingress Controller logs (last 20 lines)..."
kubectl logs -n ingress-nginx deployment/ingress-nginx-controller --tail=20
echo ""

echo "=== Run a test request and check logs ==="
echo "In another terminal, run:"
echo "  curl -v http://\$INGRESS_IP/api/v1/access/auth/login"
echo ""
echo "Then check live logs:"
echo "  kubectl logs -f -n ingress-nginx deployment/ingress-nginx-controller"
