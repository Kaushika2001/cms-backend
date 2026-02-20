# Complete Frontend-Backend Integration Guide

## Problem Statement
- **Frontend receives:** Masked card numbers from GET /cards (e.g., "589925******0233")
- **Frontend needs:** To create activation/deactivation requests
- **Issue:** Frontend never receives unmasked card numbers for security

## Solution: Use maskedCardId

The backend now provides `maskedCardId` which acts as a secure, unique identifier for cards.

---

## Backend API Reference

### 1. GET All Cards
**Endpoint:** `GET /api/v1/cards`

**Response:**
```json
[
  {
    "maskedCardId": "CRD-M-73404B6B",      // ← Use this for requests!
    "cardNumber": "589925******0233",      // ← Display this to users
    "expiryDate": "2025-12-31",
    "cardStatus": "IACT",
    "cardStatusDescription": "Card Inactive - Initial/Pending state",
    "creditLimit": 50000.00,
    "cashLimit": 10000.00,
    "availableCreditLimit": 50000.00,
    "availableCashLimit": 10000.00,
    "lastUpdateTime": "2026-02-20T12:00:00"
  }
]
```

**Key Fields:**
- `maskedCardId`: Use this to create requests
- `cardNumber`: Display this to users (masked for security)

---

### 2. Create Card Request
**Endpoint:** `POST /api/v1/requests`

**Request Body (OPTION 1 - Recommended):**
```json
{
  "maskedCardId": "CRD-M-73404B6B",
  "requestReasonCode": "ACTI",
  "remark": "Activate card"
}
```

**Request Body (OPTION 2 - Legacy, if you have unmasked card number):**
```json
{
  "cardNumber": "5899250123450233",
  "requestReasonCode": "ACTI",
  "remark": "Activate card"
}
```

**Response:**
```json
{
  "requestId": 1,
  "cardNumber": "5899250123450233",
  "requestReasonCode": "ACTI",
  "requestReasonDescription": "Card Activation Request",
  "requestStatusCode": "PEND",
  "requestStatusDescription": "Pending - Awaiting approval",
  "remark": "Activate card",
  "createdTime": "2026-02-20T12:30:00"
}
```

---

### 3. Lookup maskedCardId (Optional Helper Endpoint)
**Endpoint:** `GET /api/v1/cards/lookup?cardNumber={cardNumber}`

**Use Case:** If you only have the masked card number and need the maskedCardId

**Request:**
```http
GET /api/v1/cards/lookup?cardNumber=589925******0233
```

**Response:**
```json
{
  "maskedCardId": "CRD-M-73404B6B",
  "cardNumber": "589925******0233",
  "expiryDate": "2025-12-31",
  // ... all card fields
}
```

**Alternative (Lightweight):**
```http
GET /api/v1/cards/masked-id?cardNumber=589925******0233
```

**Response:**
```json
{
  "maskedCardId": "CRD-M-73404B6B",
  "cardNumber": "589925******0233"
}
```

---

## Frontend Implementation Guide

### Recommended Approach: Store maskedCardId

When you fetch cards, store the `maskedCardId` along with the card data:

```typescript
// Example: CardList Component

interface Card {
  maskedCardId: string;      // Store this!
  cardNumber: string;         // Display this
  cardStatus: string;
  // ... other fields
}

// 1. Fetch cards from backend
async function fetchCards(): Promise<Card[]> {
  const response = await fetch('http://localhost:8080/api/v1/cards');
  const cards = await response.json();
  return cards; // Already includes maskedCardId!
}

// 2. Display cards to user
function CardList({ cards }: { cards: Card[] }) {
  return (
    <div>
      {cards.map(card => (
        <div key={card.maskedCardId}>
          <p>Card: {card.cardNumber}</p>  {/* Show masked number */}
          <p>Status: {card.cardStatus}</p>
          
          {card.cardStatus === 'IACT' && (
            <button onClick={() => activateCard(card.maskedCardId)}>
              Activate
            </button>
          )}
        </div>
      ))}
    </div>
  );
}

// 3. Create request using maskedCardId
async function activateCard(maskedCardId: string) {
  const response = await fetch('http://localhost:8080/api/v1/requests', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      maskedCardId: maskedCardId,  // Use the stored maskedCardId!
      requestReasonCode: 'ACTI',
      remark: 'User requested activation'
    })
  });
  
  if (response.ok) {
    console.log('Request created successfully!');
  }
}
```

---

### Alternative Approach: Lookup on Demand

If you only have the masked card number stored, use the lookup endpoint:

```typescript
// If you only have masked card number
async function activateCardByMaskedNumber(maskedCardNumber: string) {
  // Step 1: Lookup maskedCardId
  const lookupResponse = await fetch(
    `http://localhost:8080/api/v1/cards/masked-id?cardNumber=${maskedCardNumber}`
  );
  const { maskedCardId } = await lookupResponse.json();
  
  // Step 2: Create request
  const response = await fetch('http://localhost:8080/api/v1/requests', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      maskedCardId: maskedCardId,
      requestReasonCode: 'ACTI',
      remark: 'User requested activation'
    })
  });
  
  if (response.ok) {
    console.log('Request created successfully!');
  }
}
```

---

## Complete Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                        FRONTEND                              │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  1. Fetch Cards                                              │
│     GET /api/v1/cards                                        │
│     ↓                                                        │
│     Receives:                                                │
│     {                                                        │
│       "maskedCardId": "CRD-M-73404B6B",  ← STORE THIS       │
│       "cardNumber": "589925******0233"   ← DISPLAY THIS     │
│     }                                                        │
│                                                              │
│  2. User clicks "Activate" button                            │
│                                                              │
│  3. Create Request                                           │
│     POST /api/v1/requests                                    │
│     {                                                        │
│       "maskedCardId": "CRD-M-73404B6B",  ← USE STORED ID    │
│       "requestReasonCode": "ACTI"                            │
│     }                                                        │
│                                                              │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                        BACKEND                               │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  1. Receives maskedCardId: "CRD-M-73404B6B"                  │
│                                                              │
│  2. Looks up card in database                                │
│     - Finds card with number: "5899250123450233"             │
│                                                              │
│  3. Creates request using UNMASKED card number internally    │
│                                                              │
│  4. Returns success                                          │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## Testing the Integration

### Test 1: Verify GET /cards returns maskedCardId

```bash
curl http://localhost:8080/api/v1/cards | jq '.[0]'
```

**Expected Output:**
```json
{
  "maskedCardId": "CRD-M-73404B6B",
  "cardNumber": "589925******0233",
  "cardStatus": "IACT",
  ...
}
```

✅ **Verify:** `maskedCardId` field is present

---

### Test 2: Create Request with maskedCardId

```bash
curl -X POST http://localhost:8080/api/v1/requests \
  -H "Content-Type: application/json" \
  -d '{
    "maskedCardId": "CRD-M-73404B6B",
    "requestReasonCode": "ACTI",
    "remark": "Test activation"
  }'
```

**Expected Output:**
```json
{
  "requestId": 1,
  "cardNumber": "5899250123450233",
  "requestReasonCode": "ACTI",
  "requestStatusCode": "PEND",
  ...
}
```

✅ **Verify:** Request created successfully

---

### Test 3: Lookup maskedCardId from masked card number

```bash
curl "http://localhost:8080/api/v1/cards/masked-id?cardNumber=589925******0233"
```

**Expected Output:**
```json
{
  "maskedCardId": "CRD-M-73404B6B",
  "cardNumber": "589925******0233"
}
```

✅ **Verify:** Returns correct maskedCardId

---

## Error Handling

### Error 1: maskedCardId not found
```json
{
  "timestamp": "2026-02-20T12:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Card not found with maskedCardId: CRD-M-INVALID"
}
```

**Frontend Action:** Display error to user

### Error 2: Neither maskedCardId nor cardNumber provided
```json
{
  "timestamp": "2026-02-20T12:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Either maskedCardId or cardNumber must be provided"
}
```

**Frontend Action:** Ensure you're sending maskedCardId in request body

---

## Security Benefits

✅ **No unmasked card numbers exposed to frontend**
- Frontend only sees masked card numbers (589925******0233)
- Frontend only sends maskedCardId (CRD-M-73404B6B)

✅ **Backend handles unmasked numbers securely**
- Lookup happens server-side
- Unmasked numbers stay in database

✅ **Consistent identifiers**
- Same card always has same maskedCardId
- No need to store sensitive data in frontend

---

## Summary

**What Frontend Should Do:**
1. ✅ Fetch cards from GET /api/v1/cards
2. ✅ Store the `maskedCardId` field from response
3. ✅ Display the `cardNumber` field to users (already masked)
4. ✅ Send `maskedCardId` when creating requests

**What Backend Does:**
1. ✅ Returns maskedCardId in all card responses
2. ✅ Accepts maskedCardId in request creation
3. ✅ Looks up unmasked card number internally
4. ✅ Creates request using actual card number

**Result:** Frontend never needs unmasked card numbers! 🎉
