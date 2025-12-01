# Kubernetes Deployment Guide
## Credit Enforcement Platform on OVH Kubernetes

---

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Architecture Overview](#architecture-overview)
3. [Pre-Deployment Checklist](#pre-deployment-checklist)
4. [Step-by-Step Deployment](#step-by-step-deployment)
5. [Verification & Testing](#verification--testing)
6. [Troubleshooting](#troubleshooting)
7. [Maintenance & Updates](#maintenance--updates)
8. [Rollback Procedures](#rollback-procedures)

---

## Prerequisites

### Required Tools
```bash
# Install kubectl (Kubernetes CLI)
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# Verify installation
kubectl version --client

# Install OVH kubeconfig (get from OVH control panel)
# Download kubeconfig file and set environment variable
export KUBECONFIG=/path/to/your/kubeconfig.yml
```

### Infrastructure Ready
- ✅ OVH Kubernetes cluster (3 nodes)
- ✅ OVH Container Registry with images
- ✅ OVH PostgreSQL database running
- ✅ kubectl configured with cluster access
- ✅ Domain name (for production) or testing without domain

---

## Architecture Overview

```
Internet
    ↓
[LoadBalancer (OVH)] ← (Automatic, created by Ingress Controller)
    ↓
[NGINX Ingress Controller] ← (Handles SSL, routing)
    ↓
[API Gateway - 3 replicas] ← (Entry point, auth, routing)
    ↓
[Eureka Server] → [Service Discovery]
    ↓
┌────────────────────────────────────────┐
│ Microservices (2 replicas each)        │
├────────────────────────────────────────┤
│ • Access Management Service (8081)     │
│ • Master Data Service (8085)           │
│ • Case Sourcing Service (8082)         │
│ • Communication Service (8084)         │
│ • Allocation Service (8083)            │
└────────────────────────────────────────┘
    ↓                    ↓
[Redis StatefulSet]  [PostgreSQL (OVH Managed)]
    ↓
[PersistentVolume (10GB)]
```

---

## Pre-Deployment Checklist

### 1. Verify Cluster Access
```bash
# Test cluster connectivity
kubectl cluster-info

# Check nodes are ready
kubectl get nodes

# Expected output: 3 nodes in "Ready" state
```

### 2. Verify Container Registry Access
```bash
# Test image pull (replace with your registry)
docker login qvwy18uw.c1.de1.container-registry.ovh.net

# List images
# Should see: eureka-server, api-gateway, access-management-service, etc.
```

### 3. Verify Database Connectivity
```bash
# Test PostgreSQL connection
psql -h postgresql-9011f0fc-o4187f84d.database.cloud.ovh.net -p 20184 -U avnadmin -d defaultdb

# Or using Docker
docker run --rm -it postgres:14 psql -h <your-db-host> -p <port> -U <user> -d <database>
```

### 4. Check Storage Classes (for Redis PersistentVolume)
```bash
kubectl get storageclass

# Expected: csi-cinder-high-speed or csi-cinder-classic
# If empty, OVH will use default
```

---

## Step-by-Step Deployment

### Phase 1: Setup Secrets (CRITICAL - DO THIS FIRST!)

**⚠️ SECURITY WARNING:** Never commit actual secrets to git!

```bash
cd k8s/base

# 1. Create secrets file from template
cp secrets.yaml secrets-actual.yaml

# 2. Generate base64 encoded values

# PostgreSQL credentials
echo -n "postgresql-9011f0fc-o4187f84d.database.cloud.ovh.net" | base64
echo -n "20184" | base64
echo -n "defaultdb" | base64
echo -n "avnadmin" | base64
echo -n "KE5PMicV9Z0aO1XJRNx3" | base64

# JWT Secret (generate new one!)
openssl rand -base64 32 | base64
echo -n "finxbridge" | base64

# CORS origins
echo -n "https://yourdomain.com,https://api.yourdomain.com" | base64

# 3. Edit secrets-actual.yaml and replace <BASE64_ENCODED_VALUE> with actual values

# 4. Create OVH registry secret (for pulling private images)
kubectl create secret docker-registry ovh-registry-secret \
  --docker-server=qvwy18uw.c1.de1.container-registry.ovh.net \
  --docker-username=<YOUR_OVH_USERNAME> \
  --docker-password=<YOUR_OVH_PASSWORD> \
  --namespace=credit-enforcement \
  --dry-run=client -o yaml > ovh-registry-secret.yaml

# 5. Apply secrets
kubectl apply -f secrets-actual.yaml
kubectl apply -f ovh-registry-secret.yaml

# 6. Verify secrets created
kubectl get secrets -n credit-enforcement

# 7. Add secrets-actual.yaml to .gitignore (IMPORTANT!)
echo "k8s/base/secrets-actual.yaml" >> ../../.gitignore
echo "k8s/base/ovh-registry-secret.yaml" >> ../../.gitignore
```

### Phase 2: Deploy Infrastructure Components

```bash
cd k8s/base

# 1. Create namespace
kubectl apply -f namespace.yaml

# 2. Create ConfigMaps
kubectl apply -f configmap.yaml

# 3. Deploy Redis (needs time to provision PersistentVolume)
kubectl apply -f redis-statefulset.yaml

# Wait for Redis to be ready (takes 2-5 minutes for PV provisioning)
kubectl wait --for=condition=ready pod -l app=redis -n credit-enforcement --timeout=300s

# Verify Redis is running
kubectl get pods -n credit-enforcement
kubectl get pvc -n credit-enforcement  # Check PersistentVolumeClaim
```

### Phase 3: Deploy Microservices (IN ORDER!)

**⚠️ ORDER MATTERS:** Deploy in this exact sequence!

```bash
cd k8s/base

# Step 1: Deploy Eureka Server (Service Discovery) - FIRST!
kubectl apply -f eureka-server.yaml

# Wait for Eureka to be ready (takes 1-2 minutes)
kubectl wait --for=condition=ready pod -l app=eureka-server -n credit-enforcement --timeout=300s

# Verify Eureka is running
kubectl get pods -n credit-enforcement -l app=eureka-server

# Step 2: Deploy Access Management Service (Authentication) - SECOND!
kubectl apply -f access-management-service.yaml

# Wait for Access Management to be ready
kubectl wait --for=condition=ready pod -l app=access-management-service -n credit-enforcement --timeout=300s

# Step 3: Deploy remaining services (can deploy in parallel)
kubectl apply -f master-data-service.yaml
kubectl apply -f case-sourcing-service.yaml
kubectl apply -f communication-service.yaml
kubectl apply -f allocation-reallocation-service.yaml

# Wait for all services to be ready
kubectl wait --for=condition=ready pod -l app=master-data-service -n credit-enforcement --timeout=300s
kubectl wait --for=condition=ready pod -l app=case-sourcing-service -n credit-enforcement --timeout=300s
kubectl wait --for=condition=ready pod -l app=communication-service -n credit-enforcement --timeout=300s
kubectl wait --for=condition=ready pod -l app=allocation-reallocation-service -n credit-enforcement --timeout=300s

# Step 4: Deploy API Gateway (Entry Point) - LAST!
kubectl apply -f api-gateway.yaml

# Wait for API Gateway to be ready
kubectl wait --for=condition=ready pod -l app=api-gateway -n credit-enforcement --timeout=300s

# Verify all pods are running
kubectl get pods -n credit-enforcement
```

### Phase 4: Setup Ingress (External Access)

```bash
cd k8s/base

# 1. Install NGINX Ingress Controller (one-time, cluster-wide)
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/cloud/deploy.yaml

# Wait for ingress controller to get external IP (takes 2-5 minutes)
kubectl get svc -n ingress-nginx ingress-nginx-controller -w

# 2. Get the external IP address
INGRESS_IP=$(kubectl get svc -n ingress-nginx ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
echo "Your Ingress External IP: $INGRESS_IP"

# 3. Configure DNS (if you have a domain)
# Create A records:
# - api.yourdomain.com → $INGRESS_IP
# - www.yourdomain.com → $INGRESS_IP

# 4. Apply Ingress configuration
# Option A: Testing without domain (HTTP only)
kubectl apply -f ingress.yaml

# Option B: Production with SSL (after DNS configured)
# First install cert-manager (for automatic SSL)
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.13.0/cert-manager.yaml

# Wait for cert-manager to be ready
kubectl wait --for=condition=ready pod -l app.kubernetes.io/instance=cert-manager -n cert-manager --timeout=300s

# Create Let's Encrypt issuer
kubectl apply -f cert-manager-issuer.yaml

# Apply Ingress with SSL
kubectl apply -f ingress.yaml

# 5. Verify Ingress
kubectl get ingress -n credit-enforcement
```

---

## Verification & Testing

### 1. Check All Pods are Running
```bash
kubectl get pods -n credit-enforcement

# Expected output: All pods in "Running" state with "2/2" or "1/1" READY
# Example:
# NAME                                              READY   STATUS    RESTARTS   AGE
# redis-0                                           1/1     Running   0          10m
# eureka-server-xxxxx                               1/1     Running   0          8m
# access-management-service-xxxxx                   1/1     Running   0          7m
# api-gateway-xxxxx                                 1/1     Running   0          5m
```

### 2. Check Services
```bash
kubectl get svc -n credit-enforcement

# Should see ClusterIP services for all microservices
```

### 3. Check Eureka Service Registry
```bash
# Port-forward Eureka dashboard
kubectl port-forward svc/eureka-server 8761:8761 -n credit-enforcement

# Open browser: http://localhost:8761
# Verify all services are registered
```

### 4. Test API Gateway Health
```bash
# Without domain (using LoadBalancer IP)
curl -H "Host: api.yourdomain.com" http://$INGRESS_IP/actuator/health

# With domain (after DNS configured)
curl https://api.yourdomain.com/actuator/health

# Expected response:
# {"status":"UP"}
```

### 5. Test Authentication
```bash
# Test login endpoint
curl -X POST https://api.yourdomain.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'

# Expected: JWT token response
```

### 6. Check Logs
```bash
# Check specific service logs
kubectl logs -f deployment/api-gateway -n credit-enforcement

# Check all services logs
kubectl logs -l app.kubernetes.io/name=credit-enforcement -n credit-enforcement --tail=100
```

---

## Troubleshooting

### Problem: Pods in "ImagePullBackOff" state
```bash
# Check pod events
kubectl describe pod <pod-name> -n credit-enforcement

# Common causes:
# 1. Missing or incorrect OVH registry secret
# 2. Wrong image name or tag
# 3. Registry credentials expired

# Solution: Verify registry secret
kubectl get secret ovh-registry-secret -n credit-enforcement
# Recreate if needed (see Phase 1)
```

### Problem: Pods in "CrashLoopBackOff" state
```bash
# Check logs
kubectl logs <pod-name> -n credit-enforcement --previous

# Common causes:
# 1. Database connection failure
# 2. Missing environment variables
# 3. Eureka server not reachable
# 4. Application startup errors

# Solution: Check environment variables
kubectl exec -it <pod-name> -n credit-enforcement -- env | grep -E "POSTGRES|REDIS|EUREKA"
```

### Problem: Service not registering with Eureka
```bash
# 1. Check Eureka server is running
kubectl get pods -n credit-enforcement -l app=eureka-server

# 2. Check service can reach Eureka
kubectl exec -it <service-pod> -n credit-enforcement -- curl http://eureka-server:8761/

# 3. Check EUREKA_SERVER_URL environment variable
kubectl exec -it <service-pod> -n credit-enforcement -- env | grep EUREKA
```

### Problem: Database connection errors
```bash
# Test database connectivity from pod
kubectl run -it --rm postgres-test --image=postgres:14 --restart=Never -n credit-enforcement -- \
  psql -h <your-db-host> -p <port> -U <user> -d <database>

# Check if database credentials are correct
kubectl get secret postgres-credentials -n credit-enforcement -o yaml
# Decode base64 values: echo "base64string" | base64 -d
```

### Problem: Ingress not working
```bash
# Check ingress status
kubectl describe ingress credit-enforcement-ingress -n credit-enforcement

# Check ingress controller logs
kubectl logs -n ingress-nginx deployment/ingress-nginx-controller

# Check LoadBalancer external IP
kubectl get svc -n ingress-nginx

# Test connectivity to API Gateway directly (bypass Ingress)
kubectl port-forward svc/api-gateway 8080:8080 -n credit-enforcement
curl http://localhost:8080/actuator/health
```

### Problem: SSL certificate not working
```bash
# Check certificate status
kubectl get certificate -n credit-enforcement
kubectl describe certificate credit-enforcement-tls -n credit-enforcement

# Check cert-manager logs
kubectl logs -n cert-manager deployment/cert-manager

# Check challenge status
kubectl get challenge -n credit-enforcement

# Common issues:
# 1. DNS not configured correctly
# 2. Firewall blocking HTTP-01 challenge
# 3. Using production issuer before testing with staging
```

---

## Maintenance & Updates

### Rolling Update (Zero Downtime)
```bash
# Update image tag in deployment file
# Example: change image tag from 1.0.0 to 1.1.0

# Apply changes
kubectl apply -f <service-deployment>.yaml

# Watch rollout
kubectl rollout status deployment/<service-name> -n credit-enforcement

# Check rollout history
kubectl rollout history deployment/<service-name> -n credit-enforcement
```

### Scale Services
```bash
# Scale up
kubectl scale deployment api-gateway --replicas=5 -n credit-enforcement

# Scale down
kubectl scale deployment communication-service --replicas=1 -n credit-enforcement

# Auto-scaling (Horizontal Pod Autoscaler)
kubectl autoscale deployment api-gateway --cpu-percent=70 --min=3 --max=10 -n credit-enforcement
```

### Update ConfigMap or Secret
```bash
# Edit ConfigMap
kubectl edit configmap app-config -n credit-enforcement

# Restart pods to pick up changes
kubectl rollout restart deployment/<service-name> -n credit-enforcement

# Or restart all deployments
kubectl rollout restart deployment -n credit-enforcement
```

---

## Rollback Procedures

### Rollback Deployment
```bash
# View rollout history
kubectl rollout history deployment/<service-name> -n credit-enforcement

# Rollback to previous version
kubectl rollout undo deployment/<service-name> -n credit-enforcement

# Rollback to specific revision
kubectl rollout undo deployment/<service-name> --to-revision=2 -n credit-enforcement

# Verify rollback
kubectl rollout status deployment/<service-name> -n credit-enforcement
```

### Emergency: Delete Everything and Redeploy
```bash
# Delete all resources in namespace (CAUTION!)
kubectl delete namespace credit-enforcement

# This deletes:
# - All pods, services, deployments
# - PersistentVolumeClaims (Redis data will be lost!)
# - Secrets and ConfigMaps

# Then redeploy from Phase 1
```

---

## Monitoring & Logging

### Resource Usage
```bash
# Check CPU and Memory usage
kubectl top nodes
kubectl top pods -n credit-enforcement

# Detailed resource usage
kubectl describe node <node-name>
```

### Live Logs
```bash
# Follow logs for specific service
kubectl logs -f deployment/api-gateway -n credit-enforcement

# All pods with label
kubectl logs -f -l app=access-management-service -n credit-enforcement

# Multi-pod logs (stern - install separately)
stern api-gateway -n credit-enforcement
```

### Events
```bash
# Watch cluster events
kubectl get events -n credit-enforcement --sort-by='.lastTimestamp'

# Watch for specific issues
kubectl get events -n credit-enforcement --field-selector type=Warning
```

---

## Production Readiness Checklist

Before going to production, ensure:

- [ ] All secrets are properly configured (not using template values)
- [ ] SSL certificates are configured (not using HTTP)
- [ ] Database backups are scheduled
- [ ] Redis persistence is enabled
- [ ] Resource limits are set on all pods
- [ ] Monitoring and alerting are configured
- [ ] Log aggregation is setup
- [ ] DNS is properly configured
- [ ] Firewall rules are in place
- [ ] Rate limiting is enabled on Ingress
- [ ] CORS origins are set to production domains only
- [ ] Eureka dashboard is not publicly accessible
- [ ] Database credentials are rotated from defaults
- [ ] All services are running 2+ replicas for HA
- [ ] Horizontal Pod Autoscaler is configured
- [ ] Backup and disaster recovery plan is documented

---

## Support & Contact

For issues or questions:
- Check logs: `kubectl logs <pod-name> -n credit-enforcement`
- Check status: `kubectl describe pod <pod-name> -n credit-enforcement`
- Review this guide's troubleshooting section
- Contact: DevOps team or platform administrator

---

**Last Updated:** 2024-11-26