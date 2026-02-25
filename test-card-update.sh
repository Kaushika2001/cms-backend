#!/bin/bash

# Test Card Update Endpoint
# Tests the PUT /api/v1/cards/{cardNumber} endpoint with masked card number

echo "=========================================="
echo "Testing Card Update with Masked Card Number"
echo "=========================================="
echo ""

MASKED_CARD="558899******3333"
BASE_URL="http://localhost:8080"

echo "1. Testing PUT /api/v1/cards/${MASKED_CARD}"
echo "   - Request Body: UpdateCardDTO (no cardNumber field)"
echo ""

# Sample update request body (without cardNumber field)
UPDATE_PAYLOAD='{
  "expiryDate": "12-2027",
  "cardStatus": "CACT",
  "creditLimit": 100000,
  "cashLimit": 50000,
  "availableCreditLimit": 80000,
  "availableCashLimit": 40000
}'

echo "Request Body:"
echo "$UPDATE_PAYLOAD" | jq .
echo ""

echo "Sending request..."
RESPONSE=$(curl -s -X PUT "${BASE_URL}/api/v1/cards/${MASKED_CARD}" \
  -H "Content-Type: application/json" \
  -d "$UPDATE_PAYLOAD")

echo ""
echo "Response:"
echo "$RESPONSE" | jq .
echo ""

# Check if request was successful
if echo "$RESPONSE" | jq -e '.maskedCardId' > /dev/null 2>&1; then
  echo "✅ SUCCESS: Card updated successfully!"
  echo ""
  echo "Updated card details:"
  echo "  Masked Card ID: $(echo "$RESPONSE" | jq -r '.maskedCardId')"
  echo "  Expiry Date: $(echo "$RESPONSE" | jq -r '.expiryDate')"
  echo "  Card Status: $(echo "$RESPONSE" | jq -r '.cardStatus')"
  echo "  Credit Limit: $(echo "$RESPONSE" | jq -r '.creditLimit')"
else
  echo "❌ FAILED: Card update failed"
  echo ""
  echo "Error details:"
  echo "$RESPONSE" | jq .
fi

echo ""
echo "=========================================="
echo "Test Complete"
echo "=========================================="
