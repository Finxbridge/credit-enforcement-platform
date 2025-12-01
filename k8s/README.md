# Kubernetes Deployment Files
## Credit Enforcement Platform

This directory contains all Kubernetes manifests and deployment scripts for the Credit Enforcement Platform.

---

## 📁 Directory Structure

```
k8s/
├── base/                           # Base Kubernetes manifests (environment-agnostic)
│   ├── namespace.yaml              # Namespace definition
│   ├── configmap.yaml              # Non-sensitive configuration
│   ├── secrets.yaml                # Secrets template (DO NOT commit actual values!)
│   ├── redis-statefulset.yaml     # Redis cache with persistent storage
│   ├── eureka-server.yaml          # Service discovery server
│   ├── access-management-service.yaml
│   ├── master-data-service.yaml
│   ├── case-sourcing-service.yaml
│   ├── communication-service.yaml
│   ├── allocation-reallocation-service.yaml
│   ├── api-gateway.yaml            # API Gateway (entry point)
│   ├── ingress.yaml                # External access configuration
│   └── cert-manager-issuer.yaml    # SSL certificate automation
├── overlays/                       # Environment-specific overrides
│   ├── staging/                    # Staging environment configs
│   └── production/                 # Production environment configs
├── deploy.sh                       # Automated deployment script
├── QUICK-START.md                  # Quick deployment guide (TL;DR)
├── DEPLOYMENT-GUIDE.md             # Comprehensive deployment documentation
└── README.md                       # This file
```

---

## 🚀 Quick Start

### Option 1: Automated Deployment (Recommended)

```bash
# 1. Setup cluster infrastructure (one-time)
./deploy.sh setup

# 2. Create secrets (follow prompts)
cd base
cp secrets.yaml secrets-actual.yaml
# Edit secrets-actual.yaml and fill in actual values
kubectl apply -f secrets-actual.yaml

# Create registry secret
kubectl create secret docker-registry ovh-registry-secret \
  --docker-server=qvwy18uw.c1.de1.container-registry.ovh.net \
  --docker-username=<USERNAME> \
  --docker-password=<PASSWORD> \
  --namespace=credit-enforcement

# 3. Deploy all services
cd ..
./deploy.sh deploy

# 4. Verify deployment
./deploy.sh verify
```

### Option 2: Manual Deployment

See [QUICK-START.md](QUICK-START.md) for step-by-step manual deployment.

---

## 📚 Documentation

- **[QUICK-START.md](QUICK-START.md)** - Fast deployment guide (5-minute setup)
- **[DEPLOYMENT-GUIDE.md](DEPLOYMENT-GUIDE.md)** - Comprehensive guide with explanations
  - Architecture overview
  - Detailed deployment steps
  - Troubleshooting guide
  - Production readiness checklist
  - Maintenance procedures

---

## 🔐 Security Considerations

### ⚠️ CRITICAL - Secrets Management

**NEVER commit actual secrets to git!**

The following files contain sensitive data and are in `.gitignore`:
- `k8s/base/secrets-actual.yaml` (your actual secrets)
- `k8s/base/ovh-registry-secret.yaml` (registry credentials)
- Any `secrets-*.yaml` in overlays

### Creating Secrets

1. **Copy template:**
   ```bash
   cp base/secrets.yaml base/secrets-actual.yaml
   ```

2. **Generate base64 values:**
   ```bash
   echo -n "your-secret-value" | base64
   ```

3. **Replace ALL `<BASE64_ENCODED_VALUE>` placeholders**

4. **Apply secrets:**
   ```bash
   kubectl apply -f base/secrets-actual.yaml
   ```

5. **Verify `.gitignore` includes:**
   ```
   k8s/base/secrets-actual.yaml
   k8s/base/ovh-registry-secret.yaml
   ```

---

## 🏗️ Architecture

### Service Deployment Order

**ORDER MATTERS!** Deploy in this sequence:

1. **Infrastructure** (Namespace, ConfigMaps, Secrets, Redis)
2. **Eureka Server** ← Service discovery (FIRST!)
3. **Access Management Service** ← Authentication (SECOND!)
4. **Domain Services** (Master Data, Case Sourcing, Communication, Allocation)
5. **API Gateway** ← Entry point (LAST!)
6. **Ingress** ← External access

### Why This Order?

- **Eureka first**: All services register with Eureka on startup
- **Auth second**: Other services depend on authentication
- **Gateway last**: Routes requests to backend services (must be running first)

---

## 🔄 Common Operations

### View Status
```bash
kubectl get all -n credit-enforcement
kubectl get pods -n credit-enforcement
kubectl get svc -n credit-enforcement
```

### View Logs
```bash
# Specific service
kubectl logs -f deployment/api-gateway -n credit-enforcement

# All services
./deploy.sh logs
```

### Restart Service
```bash
kubectl rollout restart deployment/<service-name> -n credit-enforcement

# Restart all
./deploy.sh restart
```

### Scale Service
```bash
kubectl scale deployment api-gateway --replicas=5 -n credit-enforcement
```

### Update Configuration
```bash
# Edit ConfigMap
kubectl edit configmap app-config -n credit-enforcement

# Restart pods to pick up changes
kubectl rollout restart deployment -n credit-enforcement
```

### Rollback Deployment
```bash
kubectl rollout undo deployment/<service-name> -n credit-enforcement
```

---

## 🐛 Troubleshooting

### Pods Not Starting

```bash
# Check pod status
kubectl get pods -n credit-enforcement

# Check pod details
kubectl describe pod <pod-name> -n credit-enforcement

# Check logs
kubectl logs <pod-name> -n credit-enforcement
kubectl logs <pod-name> -n credit-enforcement --previous  # Previous crash
```

### Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| ImagePullBackOff | Registry credentials | Recreate `ovh-registry-secret` |
| CrashLoopBackOff | App startup failure | Check logs, verify DB connection |
| Pending | No resources available | Check node capacity |
| Service not in Eureka | Eureka unreachable | Verify Eureka is running |

See [DEPLOYMENT-GUIDE.md](DEPLOYMENT-GUIDE.md#troubleshooting) for detailed troubleshooting.

---

## 🌐 Networking

### Internal Communication (ClusterIP)
Services communicate using Kubernetes DNS:
- Short name: `eureka-server:8761`
- Full name: `eureka-server.credit-enforcement.svc.cluster.local:8761`

### External Access (Ingress)
```
Internet → LoadBalancer IP → Ingress Controller → API Gateway → Services
```

Get external IP:
```bash
kubectl get svc -n ingress-nginx ingress-nginx-controller
```

---

## 🔒 Production Readiness

Before going to production, ensure:

- [ ] All secrets are properly configured (not template values)
- [ ] SSL certificates configured (Let's Encrypt via cert-manager)
- [ ] DNS configured to point to LoadBalancer IP
- [ ] Resource limits set on all pods
- [ ] Database credentials rotated from defaults
- [ ] CORS origins set to production domains
- [ ] Monitoring and logging configured
- [ ] Backup strategy in place
- [ ] All services running 2+ replicas
- [ ] Rate limiting enabled on Ingress

See [DEPLOYMENT-GUIDE.md](DEPLOYMENT-GUIDE.md#production-readiness-checklist) for complete checklist.

---

## 📊 Resource Requirements

### Minimum Cluster Resources

| Service | Replicas | CPU (req/limit) | Memory (req/limit) |
|---------|----------|-----------------|---------------------|
| Redis | 1 | 250m/500m | 256Mi/512Mi |
| Eureka Server | 1 | 250m/500m | 512Mi/1Gi |
| API Gateway | 3 | 500m/1000m | 768Mi/1.5Gi |
| Access Management | 2 | 500m/1000m | 768Mi/1.5Gi |
| Other Services | 2 each | 250m/500m | 512Mi/1Gi |

**Total minimum**: ~8-10 CPU cores, 16-20GB RAM

**Recommended for OVH**: 3 nodes with 4 vCPU, 8GB RAM each

---

## 📝 Environment Variables

Configuration is split between:

- **ConfigMaps** (`configmap.yaml`): Non-sensitive config
  - Spring profiles
  - Logging levels
  - Flyway settings
  - Circuit breaker config

- **Secrets** (`secrets.yaml`): Sensitive data
  - Database credentials
  - JWT secrets
  - CORS origins
  - Registry credentials

---

## 🔄 CI/CD Integration

The GitHub Actions workflow ([.github/workflows/docker-build-push.yml](../.github/workflows/docker-build-push.yml)) automatically:
1. Builds Docker images
2. Pushes to OVH Container Registry
3. Tags with commit SHA

To deploy new version:
```bash
# Update image tag in deployment files
# For example, in api-gateway.yaml:
# image: qvwy18uw.c1.de1.container-registry.ovh.net/fincolreg/api-gateway:<NEW_TAG>

# Apply changes
kubectl apply -f base/api-gateway.yaml

# Watch rollout
kubectl rollout status deployment/api-gateway -n credit-enforcement
```

---

## 🆘 Support

For issues or questions:

1. Check logs: `kubectl logs <pod-name> -n credit-enforcement`
2. Check events: `kubectl get events -n credit-enforcement --sort-by='.lastTimestamp'`
3. Review [DEPLOYMENT-GUIDE.md](DEPLOYMENT-GUIDE.md) troubleshooting section
4. Run verification: `./deploy.sh verify`

---

## 📅 Maintenance Schedule

- **Daily**: Monitor pod health, check logs
- **Weekly**: Review resource usage, check for updates
- **Monthly**: Rotate credentials, review security
- **Quarterly**: Review and update resource limits

---

**Last Updated:** 2024-11-26
**Version:** 1.0.0
**Kubernetes Version:** 1.28+
**Tested on:** OVH Managed Kubernetes