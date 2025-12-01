# Routing Debug Guide

## Current Configuration

### Access Management Service
- Controller: `@RequestMapping("/access/auth")`
- Method: `@PostMapping("/login")`
- **Expected Path**: `/access/auth/login`

### API Gateway Route
```yaml
- id: access-management-auth
  uri: lb://access-management-service
  predicates:
    - Path=/api/v1/access/auth/**
  filters:
    - StripPrefix=2
```

### Path Transformation
1. **Incoming**: `POST /api/v1/access/auth/login`
2. **After StripPrefix=2**: `/access/auth/login` (strips `/api/v1`)
3. **Forwarded to**: `access-management-service:8081/access/auth/login`
4. **Controller Match**: `@RequestMapping("/access/auth")` + `@PostMapping("/login")` = `/access/auth/login` ✅

## Troubleshooting Commands

### 1. Check if service is registered in Eureka
```bash
kubectl port-forward svc/eureka-server 8761:8761 -n credit-enforcement
# Visit: http://localhost:8761
# Look for: ACCESS-MANAGEMENT-SERVICE
```

### 2. Check API Gateway logs
```bash
kubectl logs -f deployment/api-gateway -n credit-enforcement --tail=100
```

### 3. Check Access Management Service logs
```bash
kubectl logs -f deployment/access-management-service -n credit-enforcement --tail=100
```

### 4. Test direct access to service (bypass gateway)
```bash
# Port forward to access-management-service
kubectl port-forward svc/access-management-service 8081:8081 -n credit-enforcement

# Test directly (in another terminal)
curl -X POST http://localhost:8081/access/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'

# This should work if the service itself is OK
```

### 5. Test via API Gateway (port-forward)
```bash
# Port forward to API Gateway
kubectl port-forward svc/api-gateway 8080:8080 -n credit-enforcement

# Test via gateway (in another terminal)
curl -X POST http://localhost:8080/api/v1/access/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'
```

### 6. Check if gateway can resolve service
```bash
kubectl exec -it deployment/api-gateway -n credit-enforcement -- /bin/sh

# Inside pod:
nslookup access-management-service
curl http://access-management-service:8081/access/auth/login
```

## Common Issues

### Issue 1: Service not registered in Eureka
**Symptom**: 404 or "No instances available"
**Fix**: Check service logs, verify EUREKA_SERVER_URL

### Issue 2: Wrong path forwarded
**Symptom**: 404 from service
**Fix**: Check StripPrefix value, verify controller mapping

### Issue 3: DNS resolution failure
**Symptom**: UnknownHostException
**Fix**: Use service names, not pod names (already fixed)

### Issue 4: Service not ready
**Symptom**: Connection refused or timeout
**Fix**: Check pod readiness: `kubectl get pods -n credit-enforcement`
