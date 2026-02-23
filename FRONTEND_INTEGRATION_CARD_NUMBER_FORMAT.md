# Frontend Integration Guide - Card Number Format

## Issue: "Card number must be 13-16 digits" Error

### Problem
The frontend is sending an encrypted payload successfully, but after decryption, the backend validation is rejecting the card number because it doesn't match the required format.

### Root Cause
The card number validation requires **13-16 consecutive digits with NO spaces, dashes, or other characters**.

**Validation Rule:**
```java
@Pattern(regexp = "\\d{13,16}", message = "Card number must be 13-16 digits")
```

### Examples

#### ✅ VALID Card Numbers
```json
{
  "cardNumber": "4532015112830366",    // 16 digits - VALID
  "cardNumber": "371449635398431",     // 15 digits - VALID
  "cardNumber": "6011111111111117",    // 16 digits - VALID
  "cardNumber": "5555555555554444"     // 16 digits - VALID
}
```

#### ❌ INVALID Card Numbers
```json
{
  "cardNumber": "4532-0151-1283-0366",  // Has dashes - INVALID
  "cardNumber": "4532 0151 1283 0366",  // Has spaces - INVALID
  "cardNumber": "4532_0151_1283_0366",  // Has underscores - INVALID
  "cardNumber": "123456789012",         // 12 digits - INVALID (too short)
  "cardNumber": "12345678901234567"     // 17 digits - INVALID (too long)
}
```

### Frontend Fix Required

**BEFORE ENCRYPTING:** Strip all non-digit characters from the card number.

#### React/JavaScript Example:
```javascript
// Remove all non-digit characters
const cleanCardNumber = cardNumber.replace(/\D/g, '');

// Validate length
if (cleanCardNumber.length < 13 || cleanCardNumber.length > 16) {
  throw new Error('Card number must be 13-16 digits');
}

// Create the payload
const payload = {
  cardNumber: cleanCardNumber,  // Use cleaned version!
  expiryDate: expiryDate,
  cardStatus: "IACT",
  creditLimit: 50000.00,
  cashLimit: 10000.00,
  availableCreditLimit: 50000.00,
  availableCashLimit: 10000.00
};

// Now encrypt and send
const encrypted = encryptPayload(JSON.stringify(payload), transportKey);
```

#### TypeScript Example:
```typescript
interface CardData {
  cardNumber: string;
  expiryDate: string;
  cardStatus: string;
  creditLimit: number;
  cashLimit: number;
  availableCreditLimit: number;
  availableCashLimit: number;
}

function cleanCardNumber(cardNumber: string): string {
  // Remove all non-digit characters
  const cleaned = cardNumber.replace(/\D/g, '');
  
  // Validate length
  if (cleaned.length < 13 || cleaned.length > 16) {
    throw new Error('Card number must be 13-16 digits');
  }
  
  return cleaned;
}

// Usage
const cardData: CardData = {
  cardNumber: cleanCardNumber(userInputCardNumber), // Clean before using!
  expiryDate: "2029-12-31",
  cardStatus: "IACT",
  creditLimit: 50000.00,
  cashLimit: 10000.00,
  availableCreditLimit: 50000.00,
  availableCashLimit: 10000.00
};
```

### Complete Frontend Flow

```javascript
// 1. User enters card number (may have spaces/dashes)
const userInput = "4532-0151-1283-0366";

// 2. Clean the card number
const cleanedCardNumber = userInput.replace(/\D/g, ''); // "4532015112830366"

// 3. Validate length
if (cleanedCardNumber.length < 13 || cleanedCardNumber.length > 16) {
  alert('Invalid card number length');
  return;
}

// 4. Create payload with cleaned data
const cardData = {
  cardNumber: cleanedCardNumber,  // IMPORTANT: Use cleaned version
  expiryDate: "2029-12-31",       // Format: YYYY-MM-DD
  cardStatus: "IACT",             // Must be: IACT, CACT, or DACT
  creditLimit: 50000.00,
  cashLimit: 10000.00,
  availableCreditLimit: 50000.00,
  availableCashLimit: 10000.00
};

// 5. Encrypt with transport key
const encryptedData = await encryptWithAES256GCM(
  JSON.stringify(cardData),
  import.meta.env.VITE_ENCRYPTION_KEY
);

// 6. Send to backend
const response = await fetch('http://localhost:8080/api/v1/cards/encrypted', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    encryptedData: encryptedData,
    timestamp: new Date().toISOString()
  })
});
```

### Validation Rules Summary

| Field | Required | Format | Example |
|-------|----------|--------|---------|
| cardNumber | Yes | 13-16 digits (no spaces/dashes) | `4532015112830366` |
| expiryDate | Yes | Future date, format: YYYY-MM-DD | `2029-12-31` |
| cardStatus | Yes | One of: IACT, CACT, DACT | `IACT` |
| creditLimit | Yes | Non-negative number | `50000.00` |
| cashLimit | Yes | Non-negative number | `10000.00` |
| availableCreditLimit | Yes | Non-negative number | `50000.00` |
| availableCashLimit | Yes | Non-negative number | `10000.00` |

### Testing Your Integration

Use this curl command to test if your encrypted payload is correct:

```bash
# Generate a test encrypted payload
curl -X POST http://localhost:8080/api/v1/cards/encrypted \
  -H "Content-Type: application/json" \
  -d '{
    "encryptedData": "your-encrypted-data-here",
    "timestamp": "2026-02-23T12:00:00"
  }'
```

**Expected Response (Success):**
```json
{
  "cardNumber": "4532015112830366",
  "expiryDate": "2029-12-31",
  "cardStatus": "IACT",
  "cardStatusDescription": "Card Inactive - Initial/Pending state",
  "creditLimit": 50000.00,
  "cashLimit": 10000.00,
  "availableCreditLimit": 50000.00,
  "availableCashLimit": 10000.00,
  "lastUpdateTime": "2026-02-23T12:00:00.123456"
}
```

**Expected Response (Validation Error):**
```json
{
  "timestamp": "2026-02-23T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid card data: cardNumber: Card number must be 13-16 digits",
  "path": "/api/v1/cards/encrypted"
}
```

### Common Frontend Mistakes

1. **Sending card number with formatting:**
   - ❌ `"4532-0151-1283-0366"`
   - ✅ `"4532015112830366"`

2. **Sending expiry date in wrong format:**
   - ❌ `"12/29"` (MM/YY format)
   - ❌ `"2029-12"` (Missing day)
   - ✅ `"2029-12-31"` (YYYY-MM-DD format)

3. **Sending invalid card status:**
   - ❌ `"ACTIVE"`
   - ❌ `"INACTIVE"`
   - ✅ `"IACT"`, `"CACT"`, or `"DACT"`

4. **Wrong endpoint:**
   - ❌ `POST /api/v1/cards` (this is for unencrypted data)
   - ✅ `POST /api/v1/cards/encrypted` (use this for encrypted payloads)

### Need Help?

If you're still getting validation errors, enable debug logging in the backend and check:
```bash
tail -f logs/system.log | grep "Decrypted JSON"
```

This will show exactly what the backend received after decryption, so you can verify the format.

### Example Test Payload (Before Encryption)

```json
{
  "cardNumber": "4532015112830366",
  "expiryDate": "2029-12-31",
  "cardStatus": "IACT",
  "creditLimit": 50000.00,
  "cashLimit": 10000.00,
  "availableCreditLimit": 50000.00,
  "availableCashLimit": 10000.00
}
```

**After encrypting this with the transport key, send it as:**
```json
{
  "encryptedData": "<encrypted-base64-string>",
  "timestamp": "2026-02-23T12:00:00.000"
}
```
