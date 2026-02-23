#!/bin/bash

# Test script to simulate frontend encrypting and sending card data to backend
# This demonstrates the complete two-layer encryption flow

echo "========================================="
echo "Testing Frontend -> Backend Encryption Flow"
echo "========================================="
echo ""

BASE_URL="http://localhost:8080/api/v1/cards"

echo "Step 1: Start the application if not running"
echo "Run: ./mvnw spring-boot:run"
echo ""

echo "Step 2: Once application is running, use this curl command to test:"
echo ""
echo "NOTE: The encrypted payload must be created using the EncryptionUtil with transport key"
echo "      We'll use the integration test to demonstrate this working"
echo ""

echo "Step 3: Run the integration test to see it working:"
echo "./mvnw test -Dtest=CardControllerEncryptionIntegrationTest#testCreateCardWithEncryptedPayload"
echo ""

echo "Step 4: Or create a manual test payload:"
echo "   - Use EncryptionKeyGenerator to create test encrypted data"
echo "   - Send POST request to /api/v1/cards/encrypted"
echo ""

echo "========================================="
echo "Integration Test Command:"
echo "========================================="
echo "./mvnw test -Dtest=CardControllerEncryptionIntegrationTest"
