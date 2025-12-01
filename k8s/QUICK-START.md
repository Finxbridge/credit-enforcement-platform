# Quick Start Guide - TL;DR

For detailed explanations, see [DEPLOYMENT-GUIDE.md](DEPLOYMENT-GUIDE.md)

---

## Prerequisites Check
```bash
kubectl cluster-info
kubectl get nodes  # Should show 3 nodes
```

---

## Deploy in 5 Minutes (Staging/Testing)

### 1. Setup Secrets (CRITICAL!)
```bash
cd k8s/base

# Create registry secret
kubectl create secret docker-registry ovh-registry-secret \
  --docker-server=qvwy18uw.c1.de1.container-registry.ovh.net \
  --docker-username=<YOUR_USERNAME> \
  --docker-password=<YOUR_PASSWORD> \
  --namespace=credit-enforcement

# Create secrets from template
cp secrets.yaml secrets-actual.yaml
# Edit secrets-actual.yaml and replace ALL <BASE64_ENCODED_VALUE>
# Use: echo -n "your-value" | base64
kubectl apply -f secrets-actual.yaml
```

### 2. Deploy Everything
```bash
cd k8s/base

# Deploy in order
kubectl apply -f namespace.yaml
kubectl apply -f configmap.yaml
kubectl apply -f redis-statefulset.yaml

# Wait for Redis
kubectl wait --for=condition=ready pod -l app=redis -n credit-enforcement --timeout=300s

# Deploy services (IN ORDER!)
kubectl apply -f eureka-server.yaml
kubectl wait --for=condition=ready pod -l app=eureka-server -n credit-enforcement --timeout=300s

kubectl apply -f access-management-service.yaml
kubectl wait --for=condition=ready pod -l app=access-management-service -n credit-enforcement --timeout=300s

# Deploy remaining services
kubectl apply -f master-data-service.yaml
kubectl apply -f case-sourcing-service.yaml
kubectl apply -f communication-service.yaml
kubectl apply -f allocation-reallocation-service.yaml

# Wait for all
kubectl wait --for=condition=ready pod --all -n credit-enforcement --timeout=600s

# Deploy API Gateway last
kubectl apply -f api-gateway.yaml
kubectl wait --for=condition=ready pod -l app=api-gateway -n credit-enforcement --timeout=300s
```

### 3. Setup Ingress
```bash
# Install NGINX Ingress Controller
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.2/deploy/static/provider/cloud/deploy.yaml

# Get external IP (wait 2-5 minutes)
kubectl get svc -n ingress-nginx ingress-nginx-controller

# Apply ingress
kubectl apply -f ingress.yaml
```

### 4. Test
```bash
# Get Ingress IP
INGRESS_IP=$(kubectl get svc -n ingress-nginx ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

# Test API Gateway
curl http://$INGRESS_IP/actuator/health

# Expected: {"status":"UP"}
```

---

## Verify Deployment
```bash
# Check all pods
kubectl get pods -n credit-enforcement

# Check Eureka dashboard
kubectl port-forward svc/eureka-server 8761:8761 -n credit-enforcement
# Open: http://localhost:8761

# Check logs
kubectl logs -f deployment/api-gateway -n credit-enforcement
```

---

## Common Commands

### View Resources
```bash
kubectl get all -n credit-enforcement
kubectl get pods -n credit-enforcement
kubectl get svc -n credit-enforcement
kubectl get ingress -n credit-enforcement
```

### Check Logs
```bash
kubectl logs <pod-name> -n credit-enforcement
kubectl logs -f deployment/api-gateway -n credit-enforcement
kubectl logs --previous <pod-name> -n credit-enforcement  # Previous crash
```

### Debug Pod
```bash
kubectl describe pod <pod-name> -n credit-enforcement
kubectl exec -it <pod-name> -n credit-enforcement -- /bin/sh
kubectl exec -it <pod-name> -n credit-enforcement -- env | grep POSTGRES
```

### Restart Service
```bash
kubectl rollout restart deployment/<service-name> -n credit-enforcement
```

### Scale Service
```bash
kubectl scale deployment api-gateway --replicas=5 -n credit-enforcement
```

### Delete Everything
```bash
kubectl delete namespace credit-enforcement
```

---

## Troubleshooting Quick Fixes

### Pods won't start (ImagePullBackOff)
```bash
# Recreate registry secret
kubectl delete secret ovh-registry-secret -n credit-enforcement
kubectl create secret docker-registry ovh-registry-secret \
  --docker-server=qvwy18uw.c1.de1.container-registry.ovh.net \
  --docker-username=<USERNAME> \
  --docker-password=<PASSWORD> \
  --namespace=credit-enforcement
```

### Pods crash (CrashLoopBackOff)
```bash
# Check logs
kubectl logs <pod-name> -n credit-enforcement --previous

# Common fix: Check database connectivity
kubectl exec -it <pod-name> -n credit-enforcement -- env | grep POSTGRES
```

### Service not in Eureka
```bash
# Check Eureka is running
kubectl get pods -l app=eureka-server -n credit-enforcement

# Restart service
kubectl rollout restart deployment/<service-name> -n credit-enforcement
```

---

## Deployment Order (CRITICAL!)

1. ✅ Namespace + ConfigMap + Secrets
2. ✅ Redis
3. ✅ Eureka Server (FIRST!)
4. ✅ Access Management (SECOND!)
5. ✅ Other Services (parallel OK)
6. ✅ API Gateway (LAST!)
7. ✅ Ingress

---

## Production Checklist

Before production:
- [ ] Change secrets from template values
- [ ] Setup SSL (cert-manager)
- [ ] Configure DNS
- [ ] Update CORS_ALLOWED_ORIGINS
- [ ] Rotate database password from default
- [ ] Enable monitoring
- [ ] Setup backups
- [ ] Test rollback procedure

---

## Need Help?

See [DEPLOYMENT-GUIDE.md](DEPLOYMENT-GUIDE.md) for:
- Detailed explanations
- Architecture diagrams
- Troubleshooting guide
- Production setup
- SSL certificate setup
- Monitoring & logging