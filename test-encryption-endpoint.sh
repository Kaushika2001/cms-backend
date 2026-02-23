#!/bin/bash

# Test script for encrypted card creation endpoint
# This simulates what the frontend would do

BASE_URL="http://localhost:8080/api/v1/cards"

echo "Testing Encrypted Card Creation Endpoint"
echo "========================================="
echo ""

# Test 1: Create a card using the encrypted endpoint
echo "Test 1: Creating card with encrypted payload..."
echo ""

# The frontend would encrypt this payload with the transport key
# For testing, we'll use the Java encryption utility through a test
# Instead, let's just verify the endpoint exists and returns proper errors

# Test with invalid payload (no encryption - should fail)
echo "Sending request to /api/v1/cards/encrypted (should fail without proper encryption)..."
curl -X POST "${BASE_URL}/encrypted" \
  -H "Content-Type: application/json" \
  -d '{
    "encryptedData": "invalid-encrypted-data",
    "iv": "invalid-iv"
  }' \
  -v

echo ""
echo ""
echo "========================================="
echo "Note: To properly test this endpoint, use the integration test:"
echo "./mvnw test -Dtest=CardControllerEncryptionIntegrationTest"
echo ""
