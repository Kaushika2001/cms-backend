#!/bin/bash

# Test card creation with the provided data

echo "=========================================="
echo "Testing Card Creation"
echo "=========================================="
echo ""

# Card data
CARD_DATA='{
  "cardNumber": "4444444444444444",
  "expiryDate": "11-2026",
  "cardStatus": "IACT",
  "creditLimit": 55555555555,
  "cashLimit": 555555555,
  "availableCreditLimit": 55555555,
  "availableCashLimit": 555555
}'

echo "Card Data:"
echo "$CARD_DATA"
echo ""

# Test 1: Create card with plain endpoint (for testing)
echo "Test 1: Create card via /api/v1/cards endpoint"
echo "-------------------------------------------"
RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/cards \
  -H "Content-Type: application/json" \
  -d "$CARD_DATA")

echo "Response:"
echo "$RESPONSE"
echo ""

# Check if successful
if echo "$RESPONSE" | grep -q "cardNumber"; then
    echo "✓ Card created successfully"
else
    echo "✗ Card creation failed"
    echo "Error details:"
    echo "$RESPONSE"
fi

echo ""
echo "=========================================="
echo "Test completed!"
echo "=========================================="
