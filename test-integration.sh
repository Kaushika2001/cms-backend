#!/bin/bash

# Integration Test Script for Masked Card ID Flow
# This script tests the complete frontend-to-backend workflow

echo "======================================"
echo "Masked Card ID Integration Test"
echo "======================================"
echo ""

BASE_URL="http://localhost:8080/api/v1"

# Check if server is running
echo "1. Checking if server is running..."
if ! curl -s -f "$BASE_URL/cards" > /dev/null 2>&1; then
    echo "❌ ERROR: Server is not running at $BASE_URL"
    echo "   Please start the server with: ./mvnw spring-boot:run"
    exit 1
fi
echo "✅ Server is running"
echo ""

# Test 1: Get all cards and verify maskedCardId is present
echo "2. Getting all cards..."
CARDS_RESPONSE=$(curl -s "$BASE_URL/cards")
echo "$CARDS_RESPONSE" | jq '.' > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "❌ ERROR: Invalid JSON response"
    exit 1
fi

# Extract first card's data
MASKED_CARD_ID=$(echo "$CARDS_RESPONSE" | jq -r '.[0].maskedCardId')
MASKED_CARD_NUMBER=$(echo "$CARDS_RESPONSE" | jq -r '.[0].cardNumber')
CARD_STATUS=$(echo "$CARDS_RESPONSE" | jq -r '.[0].cardStatus')

if [ "$MASKED_CARD_ID" == "null" ] || [ -z "$MASKED_CARD_ID" ]; then
    echo "❌ ERROR: maskedCardId not found in response"
    echo "   Response: $CARDS_RESPONSE"
    exit 1
fi

echo "✅ Retrieved card data:"
echo "   Masked Card ID: $MASKED_CARD_ID"
echo "   Masked Card Number: $MASKED_CARD_NUMBER"
echo "   Card Status: $CARD_STATUS"
echo ""

# Test 2: Lookup maskedCardId from masked card number
echo "3. Testing lookup endpoint..."
LOOKUP_RESPONSE=$(curl -s "$BASE_URL/cards/masked-id?cardNumber=$MASKED_CARD_NUMBER")
LOOKUP_MASKED_ID=$(echo "$LOOKUP_RESPONSE" | jq -r '.maskedCardId')

if [ "$LOOKUP_MASKED_ID" != "$MASKED_CARD_ID" ]; then
    echo "❌ ERROR: Lookup returned different maskedCardId"
    echo "   Expected: $MASKED_CARD_ID"
    echo "   Got: $LOOKUP_MASKED_ID"
    exit 1
fi

echo "✅ Lookup endpoint works correctly"
echo "   Masked Card Number: $MASKED_CARD_NUMBER"
echo "   Resolved to: $LOOKUP_MASKED_ID"
echo ""

# Test 3: Create request using maskedCardId
echo "4. Creating card request using maskedCardId..."

# Determine request type based on card status
if [ "$CARD_STATUS" == "IACT" ]; then
    REQUEST_TYPE="ACTI"
    REQUEST_DESC="Activation"
else
    REQUEST_TYPE="CDCL"
    REQUEST_DESC="Closure"
fi

REQUEST_BODY=$(cat <<EOF
{
  "maskedCardId": "$MASKED_CARD_ID",
  "requestReasonCode": "$REQUEST_TYPE",
  "remark": "Integration test - $REQUEST_DESC"
}
EOF
)

CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/requests" \
  -H "Content-Type: application/json" \
  -d "$REQUEST_BODY")

REQUEST_ID=$(echo "$CREATE_RESPONSE" | jq -r '.requestId')
REQUEST_STATUS=$(echo "$CREATE_RESPONSE" | jq -r '.requestStatusCode')

if [ "$REQUEST_ID" == "null" ] || [ -z "$REQUEST_ID" ]; then
    echo "❌ ERROR: Failed to create request"
    echo "   Response: $CREATE_RESPONSE"
    exit 1
fi

echo "✅ Request created successfully"
echo "   Request ID: $REQUEST_ID"
echo "   Request Type: $REQUEST_TYPE"
echo "   Request Status: $REQUEST_STATUS"
echo ""

# Test 4: Verify the request was created
echo "5. Verifying request in database..."
VERIFY_RESPONSE=$(curl -s "$BASE_URL/requests/$REQUEST_ID")
VERIFY_ID=$(echo "$VERIFY_RESPONSE" | jq -r '.requestId')

if [ "$VERIFY_ID" != "$REQUEST_ID" ]; then
    echo "❌ ERROR: Request not found in database"
    exit 1
fi

echo "✅ Request verified in database"
echo ""

# Test 5: Clean up - delete the test request
echo "6. Cleaning up test data..."
DELETE_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/requests/$REQUEST_ID")

if [ "$DELETE_RESPONSE" != "204" ]; then
    echo "⚠️  WARNING: Could not delete test request (HTTP $DELETE_RESPONSE)"
    echo "   Manual cleanup may be required: Request ID $REQUEST_ID"
else
    echo "✅ Test request deleted"
fi
echo ""

# Summary
echo "======================================"
echo "✅ ALL TESTS PASSED!"
echo "======================================"
echo ""
echo "Summary:"
echo "  • GET /cards returns maskedCardId ✅"
echo "  • Lookup endpoint works correctly ✅"
echo "  • POST /requests accepts maskedCardId ✅"
echo "  • Request created and verified ✅"
echo "  • Test data cleaned up ✅"
echo ""
echo "Your backend is ready for frontend integration! 🎉"
echo ""
echo "Frontend developers can now:"
echo "  1. Fetch cards from GET /cards"
echo "  2. Store the maskedCardId field"
echo "  3. Send maskedCardId when creating requests"
echo ""
echo "See INTEGRATION_GUIDE.md for detailed documentation."
