#!/bin/bash

# Fix Eureka hostname configuration for Kubernetes
# This script updates all service deployments to use service names instead of pod names

set -e

cd "$(dirname "$0")/base"

echo "Fixing Eureka hostname configuration for Kubernetes..."

# Access Management Service
sed -i 's/fieldPath: metadata.name/value: "access-management-service"/' access-management-service.yaml

# Master Data Service
sed -i 's/fieldPath: metadata.name/value: "master-data-service"/' master-data-service.yaml

# Case Sourcing Service
sed -i 's/fieldPath: metadata.name/value: "case-sourcing-service"/' case-sourcing-service.yaml

# Communication Service
sed -i 's/fieldPath: metadata.name/value: "communication-service"/' communication-service.yaml

# Allocation Service
sed -i 's/fieldPath: metadata.name/value: "allocation-reallocation-service"/' allocation-reallocation-service.yaml

echo "✓ All services updated to use service names instead of pod names"
echo "✓ This fixes the DNS resolution issue in Kubernetes"