# Card Activation API - Frontend Integration Guide

## Overview

This guide explains how to implement the **encrypted card activation/status update** endpoint in your frontend application. The endpoint allows you to activate, deactivate, or change card status using encrypted payloads.

---

## Endpoint Details

**Endpoint:** `POST http://localhost:8080/api/v1/cards/activate/encrypted`

**Purpose:** Update card status (activate/deactivate) using encrypted card number (masked or full)

---

## Workflow

```
1. User wants to activate a card
   ↓
2. Frontend has masked card number from previous lookup
   (e.g., "453201******0366" from GET /api/v1/cards/masked-id)
   ↓
3. Frontend creates activation request: { cardNumber: "453201******0366", newStatus: "CACT" }
   ↓
4. Frontend encrypts the request with transport key
   ↓
5. Frontend sends encrypted payload to backend
   ↓
6. Backend decrypts payload
   ↓
7. Backend looks up card by masked pattern (or full number if provided)
   ↓
8. Backend decrypts stored card number to verify
   ↓
9. Backend updates card status in database
   ↓
10. Backend returns updated card with masked number
```

---

## Request Format

### Decrypted Payload Structure (What you encrypt)

```typescript
interface UpdateCardStatusDTO {
  cardNumber: string;  // Can be masked (453201******0366) OR full (4532015112830366)
  newStatus: "IACT" | "CACT" | "DACT";  // New status code
}
```

### Encrypted Request Body

```typescript
interface EncryptedPayload {
  encryptedData: string;  // Encrypted UpdateCardStatusDTO
  timestamp: string;       // ISO 8601 timestamp
}
```

---

## Status Codes

| Code | Description | Use Case |
|------|-------------|----------|
| `IACT` | Inactive | New card, not yet activated |
| `CACT` | Active | Card is active and can be used |
| `DACT` | Deactivated | Card has been deactivated (lost, stolen, expired) |

---

## Complete React Example

```javascript
import React, { useState } from 'react';
import { encryptData } from '../utils/encryptionUtil';  // From previous guide

function CardActivation({ maskedCardNumber }) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const activateCard = async (newStatus) => {
    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      // Step 1: Create status update payload
      const statusUpdatePayload = {
        cardNumber: maskedCardNumber,  // Use masked card number from props
        newStatus: newStatus           // CACT to activate, DACT to deactivate
      };

      console.log('Status Update Payload:', statusUpdatePayload);
      
      // Step 2: Convert to JSON string
      const jsonPayload = JSON.stringify(statusUpdatePayload);
      
      // Step 3: Encrypt the payload
      const transportKey = import.meta.env.VITE_ENCRYPTION_KEY;
      const encryptedData = encryptData(jsonPayload, transportKey);
      
      // Step 4: Create encrypted payload with timestamp
      const encryptedPayload = {
        encryptedData: encryptedData,
        timestamp: new Date().toISOString()
      };

      console.log('Encrypted Payload:', encryptedPayload);
      
      // Step 5: Send to backend
      const response = await fetch('http://localhost:8080/api/v1/cards/activate/encrypted', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(encryptedPayload)
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to update card status');
      }

      const updatedCard = await response.json();
      console.log('Card status updated successfully:', updatedCard);
      
      setSuccess(true);
      alert(`Card status updated to: ${newStatus}`);
      
    } catch (err) {
      console.error('Error updating card status:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card-activation">
      <h3>Card: {maskedCardNumber}</h3>
      
      <div className="action-buttons">
        <button 
          onClick={() => activateCard('CACT')} 
          disabled={loading}
          className="btn-activate"
        >
          {loading ? 'Processing...' : 'Activate Card'}
        </button>
        
        <button 
          onClick={() => activateCard('DACT')} 
          disabled={loading}
          className="btn-deactivate"
        >
          {loading ? 'Processing...' : 'Deactivate Card'}
        </button>
        
        <button 
          onClick={() => activateCard('IACT')} 
          disabled={loading}
          className="btn-inactive"
        >
          {loading ? 'Processing...' : 'Set Inactive'}
        </button>
      </div>

      {error && <div className="error">{error}</div>}
      {success && <div className="success">Card status updated successfully!</div>}
    </div>
  );
}

export default CardActivation;
```

---

## Complete Workflow Example

### Step 1: Get Masked Card Number

First, lookup the card to get its masked card number:

```javascript
// User enters full card number
const fullCardNumber = "4532015112830366";

// Lookup to get masked version
const response = await fetch(
  `http://localhost:8080/api/v1/cards/masked-id?cardNumber=${fullCardNumber}`
);

const cardData = await response.json();
const maskedCardNumber = cardData.cardNumber;  // "453201******0366"
```

### Step 2: Activate Card with Masked Number

```javascript
// Create activation payload
const activationPayload = {
  cardNumber: maskedCardNumber,  // "453201******0366"
  newStatus: "CACT"              // Activate
};

// Encrypt and send (see React example above)
```

---

## Vanilla JavaScript Example

```javascript
import CryptoJS from 'crypto-js';

// Encryption function
function encryptData(plainText, base64Key) {
  const keyBytes = CryptoJS.enc.Base64.parse(base64Key);
  const iv = CryptoJS.lib.WordArray.random(16);
  const encrypted = CryptoJS.AES.encrypt(plainText, keyBytes, {
    iv: iv,
    mode: CryptoJS.mode.CTR,
    padding: CryptoJS.pad.NoPadding
  });
  const ivBase64 = CryptoJS.enc.Base64.stringify(iv);
  const encryptedBase64 = encrypted.ciphertext.toString(CryptoJS.enc.Base64);
  return `${ivBase64}.${encryptedBase64}`;
}

// Activate card function
async function activateCard(maskedCardNumber, newStatus) {
  const transportKey = 'bPRw3dR0xfnHIn3Vr0mFS4BLjEvzDpWPW/0k2+P+72c=';
  
  // Create payload
  const payload = {
    cardNumber: maskedCardNumber,
    newStatus: newStatus
  };
  
  // Encrypt
  const jsonPayload = JSON.stringify(payload);
  const encryptedData = encryptData(jsonPayload, transportKey);
  
  const encryptedPayload = {
    encryptedData: encryptedData,
    timestamp: new Date().toISOString()
  };
  
  // Send to backend
  const response = await fetch('http://localhost:8080/api/v1/cards/activate/encrypted', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(encryptedPayload)
  });
  
  if (response.ok) {
    const card = await response.json();
    console.log('Card activated:', card);
    return card;
  } else {
    const error = await response.json();
    throw new Error(error.message);
  }
}

// Usage
activateCard('453201******0366', 'CACT')
  .then(card => console.log('Success:', card))
  .catch(error => console.error('Error:', error));
```

---

## Test with curl

### Example 1: Activate with Masked Card Number

```bash
curl -X POST http://localhost:8080/api/v1/cards/activate/encrypted \
  -H "Content-Type: application/json" \
  -d '{"encryptedData":"Y1HPJwMcUqzvu1yv.1TTS9Qg1SFPiHgSyxgnfKiq4Jhx6nj0aejWFaEBZnWZDoMnFlDFmMZOylfxaGs0nhhtrmrtFvFqxn6cIlfQ0lJgJGZ8=","timestamp":"2026-02-23T14:10:55.352"}'
```

**Decrypted content:**
```json
{
  "cardNumber": "453201******0366",
  "newStatus": "CACT"
}
```

### Example 2: Activate with Full Card Number

```bash
curl -X POST http://localhost:8080/api/v1/cards/activate/encrypted \
  -H "Content-Type: application/json" \
  -d '{"encryptedData":"FjiEyYKcrXN7JX3h.aKqJ/udviqUU/acYVxo37GMaOxY3B7Pnvg7oZxaZItHN2SjXzgWcV0SVCrBpxfQTmb+ZSusay0a9bhNBRRaC9w/brRM=","timestamp":"2026-02-23T14:10:55.350"}'
```

**Decrypted content:**
```json
{
  "cardNumber": "4532015112830366",
  "newStatus": "CACT"
}
```

---

## Response Format

### Success Response (200 OK)

```json
{
  "maskedCardId": "MSKD-453201-0366",
  "cardNumber": "453201******0366",
  "expiryDate": "2029-02-28",
  "cardStatus": "CACT",
  "cardStatusDescription": "Active",
  "creditLimit": 50000.00,
  "cashLimit": 10000.00,
  "availableCreditLimit": 50000.00,
  "availableCashLimit": 10000.00,
  "lastUpdateTime": "2026-02-23T14:11:00.123"
}
```

### Error Responses

#### 400 Bad Request - Invalid Status
```json
{
  "timestamp": "2026-02-23T14:11:00.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid status update data: newStatus: Card status must be IACT, CACT, or DACT",
  "path": "/api/v1/cards/activate/encrypted"
}
```

#### 404 Not Found - Card Not Found
```json
{
  "timestamp": "2026-02-23T14:11:00.123",
  "status": 404,
  "error": "Not Found",
  "message": "Card not found with maskedCardNumber: 453201******9999",
  "path": "/api/v1/cards/activate/encrypted"
}
```

#### 400 Bad Request - Decryption Failed
```json
{
  "timestamp": "2026-02-23T14:11:00.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid encrypted payload or status data: Decryption failed",
  "path": "/api/v1/cards/activate/encrypted"
}
```

---

## Validation Rules

| Field | Type | Rules | Example |
|-------|------|-------|---------|
| `cardNumber` | String | Required, can be masked (with `*`) or full (13-16 digits) | `"453201******0366"` or `"4532015112830366"` |
| `newStatus` | String | Required, must be `IACT`, `CACT`, or `DACT` | `"CACT"` |

---

## Security Considerations

### ✅ Best Practices

1. **Always use masked card numbers** when possible
   - Frontend should get masked number from lookup endpoint first
   - Only send full card number when absolutely necessary

2. **Validate on frontend before encryption**
   ```javascript
   const validStatuses = ['IACT', 'CACT', 'DACT'];
   if (!validStatuses.includes(newStatus)) {
     throw new Error('Invalid status');
   }
   ```

3. **Use HTTPS in production**

4. **Clear sensitive data after use**
   ```javascript
   // After successful activation
   statusUpdatePayload = null;
   jsonPayload = null;
   ```

5. **Handle errors gracefully**
   - Don't expose full card numbers in error messages
   - Log errors securely on backend

### ❌ Don't Do This

1. ❌ Store full card numbers in frontend state
2. ❌ Log decrypted card numbers in console (production)
3. ❌ Send unencrypted activation requests
4. ❌ Use GET requests for activation (security risk)

---

## Complete Integration Flow

```javascript
// Full workflow: Lookup → Activate → Verify

async function activateCardFlow(fullCardNumber) {
  try {
    // Step 1: Lookup card to get masked number
    console.log('Step 1: Looking up card...');
    const lookupResponse = await fetch(
      `http://localhost:8080/api/v1/cards/masked-id?cardNumber=${fullCardNumber}`
    );
    const cardData = await lookupResponse.json();
    const maskedCardNumber = cardData.cardNumber;
    
    console.log('Masked card number:', maskedCardNumber);
    
    // Step 2: Create activation payload
    console.log('Step 2: Creating activation payload...');
    const activationPayload = {
      cardNumber: maskedCardNumber,  // Use masked number
      newStatus: 'CACT'
    };
    
    // Step 3: Encrypt payload
    console.log('Step 3: Encrypting payload...');
    const transportKey = import.meta.env.VITE_ENCRYPTION_KEY;
    const encryptedData = encryptData(
      JSON.stringify(activationPayload), 
      transportKey
    );
    
    // Step 4: Send activation request
    console.log('Step 4: Sending activation request...');
    const activationResponse = await fetch(
      'http://localhost:8080/api/v1/cards/activate/encrypted',
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          encryptedData: encryptedData,
          timestamp: new Date().toISOString()
        })
      }
    );
    
    if (!activationResponse.ok) {
      throw new Error('Activation failed');
    }
    
    const updatedCard = await activationResponse.json();
    
    // Step 5: Verify status updated
    console.log('Step 5: Activation successful!');
    console.log('Updated card:', updatedCard);
    console.log('New status:', updatedCard.cardStatus);
    
    return updatedCard;
    
  } catch (error) {
    console.error('Activation flow failed:', error);
    throw error;
  }
}

// Usage
activateCardFlow('4532015112830366')
  .then(card => alert('Card activated!'))
  .catch(error => alert('Activation failed: ' + error.message));
```

---

## Debugging

### Enable Debug Logging

Add console logs at each step:

```javascript
console.log('1. Original payload:', activationPayload);
console.log('2. JSON payload:', jsonPayload);
console.log('3. Encrypted data length:', encryptedData.length);
console.log('4. Encrypted payload:', encryptedPayload);
console.log('5. Response status:', response.status);
console.log('6. Response data:', await response.json());
```

### Check Backend Logs

Look for these log entries in `logs/system.log`:

```
AUDIT: Encrypted payload received
Decrypted JSON: {"cardNumber":"453201******0366","newStatus":"CACT"}
Card number is masked, performing pattern lookup
Card status updated successfully to: CACT
```

### Common Issues

#### Issue 1: "Card not found with maskedCardNumber"
- **Cause**: Masked pattern doesn't match any card in database
- **Solution**: Verify the masked number format is correct (6 digits + `******` + last 4 digits)

#### Issue 2: "Invalid status update data: newStatus must be IACT, CACT, or DACT"
- **Cause**: Wrong status code sent
- **Solution**: Use only `IACT`, `CACT`, or `DACT` (case-sensitive)

#### Issue 3: "Decryption failed"
- **Cause**: Wrong encryption key or corrupted data
- **Solution**: Verify `VITE_ENCRYPTION_KEY` matches backend transport key

---

## API Summary

| Endpoint | Method | Purpose | Card Number Format |
|----------|--------|---------|-------------------|
| `/api/v1/cards/activate/encrypted` | POST | Update card status (encrypted) | Masked or full |
| `/api/v1/cards/{cardNumber}/status` | PATCH | Update card status (unencrypted) | Full only |
| `/api/v1/cards/masked-id` | GET | Lookup masked card number | Masked or full |

**Recommendation:** Use `/api/v1/cards/activate/encrypted` for all frontend activation requests.

---

## Production Checklist

Before deploying to production:

- [ ] Replace development encryption key with production key
- [ ] Enable HTTPS for all API calls
- [ ] Remove debug console.log statements
- [ ] Test all status transitions (IACT → CACT, CACT → DACT, etc.)
- [ ] Implement proper error handling and user feedback
- [ ] Add loading states during activation
- [ ] Test with various card number formats (masked/full)
- [ ] Verify audit logs are working
- [ ] Test error scenarios (invalid card, wrong status, etc.)
- [ ] Add rate limiting on frontend (prevent double-clicks)

---

## Support

For issues or questions:
1. Check backend logs at `logs/system.log`
2. Verify encryption key matches between frontend and backend
3. Test with curl commands provided in this guide
4. Review `FRONTEND_INTEGRATION_COMPLETE_GUIDE.md` for general encryption setup

**Transport Encryption Key (Development):**
```
VITE_ENCRYPTION_KEY=bPRw3dR0xfnHIn3Vr0mFS4BLjEvzDpWPW/0k2+P+72c=
```
