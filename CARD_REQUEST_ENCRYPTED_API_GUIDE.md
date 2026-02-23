# Card Request Encrypted API Guide

## Overview

This guide explains how to create card requests (activation, closure) using the encrypted endpoint when working with cards that have encrypted card numbers in the database.

## Problem Statement

When cards are created using the `/api/v1/cards/encrypted` endpoint, their card numbers are stored encrypted in the database for PCI DSS compliance. The regular `/api/v1/requests` endpoint cannot find these cards because it performs a direct lookup using the plaintext card number.

**Error Example:**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Card not found with maskedCardId : 'CRD-M-5116DA35'",
  "path": "/api/v1/requests"
}
```

## Solution: Use the Encrypted Endpoint

### Endpoint
```
POST /api/v1/requests/encrypted
```

### How It Works

1. **Frontend**: Encrypts the `CreateCardRequestDTO` with the transport key
2. **Backend**: 
   - Decrypts the payload with the transport key
   - Decrypts stored card numbers to find matching card
   - Creates the card request with the encrypted card number from database

### Request Format

#### Step 1: Create the Card Request DTO

```javascript
const cardRequestDTO = {
  cardNumber: "4532019482960366",      // Full unencrypted card number
  requestReasonCode: "ACTI",            // ACTI = Activation, CDCL = Closure
  remark: "Customer requested card activation"
};
```

#### Step 2: Encrypt the DTO

```javascript
import CryptoJS from 'crypto-js';

// Transport key (shared with backend)
const TRANSPORT_KEY = 'bPRw3dR0xfnHIn3Vr0mFS4BLjEvzDpWPW/0k2+P+72c=';

function encryptPayload(data) {
  const jsonString = JSON.stringify(data);
  
  // Generate random IV (12 bytes for GCM)
  const iv = CryptoJS.lib.WordArray.random(12);
  
  // Convert base64 key to WordArray
  const key = CryptoJS.enc.Base64.parse(TRANSPORT_KEY);
  
  // Encrypt with AES-256-GCM
  const encrypted = CryptoJS.AES.encrypt(jsonString, key, {
    iv: iv,
    mode: CryptoJS.mode.GCM,
    padding: CryptoJS.pad.NoPadding
  });
  
  // Combine IV and ciphertext: {iv}.{ciphertext}
  const ivBase64 = CryptoJS.enc.Base64.stringify(iv);
  const ciphertextBase64 = encrypted.ciphertext.toString(CryptoJS.enc.Base64);
  
  return {
    encryptedData: `${ivBase64}.${ciphertextBase64}`
  };
}

// Encrypt the request
const encryptedPayload = encryptPayload(cardRequestDTO);
```

#### Step 3: Send to Backend

```javascript
const response = await fetch('http://localhost:8080/api/v1/requests/encrypted', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(encryptedPayload)
});

const result = await response.json();
console.log('Card request created:', result);
```

### Response Format

```json
{
  "requestId": 123,
  "cardNumber": "VGhpcyBpcyBlbmNyeXB0ZWQ=...",  // Encrypted card number
  "requestReasonCode": "ACTI",
  "requestReasonDescription": "Card Activation Request",
  "requestStatusCode": "PEND",
  "requestStatusDescription": "Pending",
  "remark": "Customer requested card activation",
  "createdTime": "2026-02-23T14:30:00"
}
```

## Request Types

### 1. Card Activation Request (ACTI)

Used to activate an inactive card.

```javascript
{
  "cardNumber": "4532019482960366",
  "requestReasonCode": "ACTI",
  "remark": "Customer requested card activation"
}
```

### 2. Card Closure Request (CDCL)

Used to close/deactivate an active card.

```javascript
{
  "cardNumber": "4532019482960366",
  "requestReasonCode": "CDCL",
  "remark": "Customer reported card lost"
}
```

## Complete React Example

```typescript
import React, { useState } from 'react';
import CryptoJS from 'crypto-js';

const TRANSPORT_KEY = 'bPRw3dR0xfnHIn3Vr0mFS4BLjEvzDpWPW/0k2+P+72c=';

interface CardRequestDTO {
  cardNumber: string;
  requestReasonCode: 'ACTI' | 'CDCL';
  remark?: string;
}

interface EncryptedPayload {
  encryptedData: string;
}

function encryptPayload(data: CardRequestDTO): EncryptedPayload {
  const jsonString = JSON.stringify(data);
  
  // Generate random IV (12 bytes for GCM)
  const iv = CryptoJS.lib.WordArray.random(12);
  
  // Convert base64 key to WordArray
  const key = CryptoJS.enc.Base64.parse(TRANSPORT_KEY);
  
  // Encrypt with AES-256-GCM
  const encrypted = CryptoJS.AES.encrypt(jsonString, key, {
    iv: iv,
    mode: CryptoJS.mode.GCM,
    padding: CryptoJS.pad.NoPadding
  });
  
  // Combine IV and ciphertext
  const ivBase64 = CryptoJS.enc.Base64.stringify(iv);
  const ciphertextBase64 = encrypted.ciphertext.toString(CryptoJS.enc.Base64);
  
  return {
    encryptedData: `${ivBase64}.${ciphertextBase64}`
  };
}

export const CardRequestForm: React.FC = () => {
  const [cardNumber, setCardNumber] = useState('');
  const [requestType, setRequestType] = useState<'ACTI' | 'CDCL'>('ACTI');
  const [remark, setRemark] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setResult(null);

    try {
      // Create request DTO
      const requestDTO: CardRequestDTO = {
        cardNumber: cardNumber.replace(/\s/g, ''), // Remove spaces
        requestReasonCode: requestType,
        remark: remark || undefined
      };

      // Encrypt payload
      const encryptedPayload = encryptPayload(requestDTO);

      // Send to backend
      const response = await fetch('http://localhost:8080/api/v1/requests/encrypted', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(encryptedPayload)
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      setResult(data);
      
      // Clear form
      setCardNumber('');
      setRemark('');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card-request-form">
      <h2>Create Card Request</h2>
      
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Card Number</label>
          <input
            type="text"
            value={cardNumber}
            onChange={(e) => setCardNumber(e.target.value)}
            placeholder="1234 5678 9012 3456"
            required
            maxLength={19}
          />
        </div>

        <div className="form-group">
          <label>Request Type</label>
          <select value={requestType} onChange={(e) => setRequestType(e.target.value as 'ACTI' | 'CDCL')}>
            <option value="ACTI">Card Activation</option>
            <option value="CDCL">Card Closure</option>
          </select>
        </div>

        <div className="form-group">
          <label>Remark (Optional)</label>
          <textarea
            value={remark}
            onChange={(e) => setRemark(e.target.value)}
            placeholder="Reason for request..."
            maxLength={500}
          />
        </div>

        <button type="submit" disabled={loading}>
          {loading ? 'Submitting...' : 'Submit Request'}
        </button>
      </form>

      {error && (
        <div className="error">
          Error: {error}
        </div>
      )}

      {result && (
        <div className="success">
          <h3>Request Created Successfully!</h3>
          <p>Request ID: {result.requestId}</p>
          <p>Status: {result.requestStatusDescription}</p>
          <p>Type: {result.requestReasonDescription}</p>
        </div>
      )}
    </div>
  );
};
```

## Validation Rules

### CreateCardRequestDTO

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `cardNumber` | String | Yes | Must be 13-16 consecutive digits (no spaces, dashes) |
| `requestReasonCode` | String | Yes | Must be "ACTI" or "CDCL" |
| `remark` | String | No | Maximum 500 characters |

## Error Handling

### Common Errors

#### 1. Card Not Found
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Card not found with cardNumber : '****'"
}
```
**Solution**: Verify the card number is correct and the card exists in the database.

#### 2. Invalid Request Type
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "CardRequestType not found with code : 'INVALID'"
}
```
**Solution**: Use only "ACTI" or "CDCL" as request reason codes.

#### 3. Decryption Failed
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Failed to create encrypted card request: Decryption failed"
}
```
**Solution**: 
- Verify the transport key matches the backend configuration
- Ensure encryption algorithm is AES-256-GCM
- Check IV is 12 bytes and properly encoded

## Security Notes

1. **Transport Key**: Must be kept secure and shared only between frontend and backend
2. **Card Number**: Never log or display unencrypted card numbers in production
3. **HTTPS**: Always use HTTPS in production to prevent man-in-the-middle attacks
4. **Key Rotation**: Plan for periodic rotation of encryption keys

## Testing

### Using cURL

You can test the endpoint using the `RequestEncryptionTester` utility:

1. Open `src/main/java/com/epic/cms/util/RequestEncryptionTester.java`
2. Uncomment the `@Component` annotation
3. Restart the application
4. Copy the generated encrypted payload from logs
5. Use it with cURL:

```bash
curl -X POST http://localhost:8080/api/v1/requests/encrypted \
  -H "Content-Type: application/json" \
  -d '{"encryptedData":"<generated_encrypted_data>"}'
```

## Comparison: Regular vs Encrypted Endpoint

| Feature | `/api/v1/requests` | `/api/v1/requests/encrypted` |
|---------|-------------------|------------------------------|
| Card Lookup | Direct plaintext lookup | Decrypts stored card numbers |
| Works with encrypted cards | ❌ No | ✅ Yes |
| Works with unencrypted cards | ✅ Yes | ✅ Yes (fallback) |
| Requires encryption | ❌ No | ✅ Yes |
| PCI DSS Compliant | ⚠️ Partial | ✅ Full |

## Best Practices

1. **Always use the encrypted endpoint** when working with cards created via `/api/v1/cards/encrypted`
2. **Validate card numbers** on the frontend before encryption (13-16 digits)
3. **Handle errors gracefully** and provide user-friendly messages
4. **Test thoroughly** in development before deploying to production
5. **Monitor logs** for encryption/decryption failures

## Related Documentation

- [Frontend Integration Complete Guide](FRONTEND_INTEGRATION_COMPLETE_GUIDE.md)
- [Card Activation API Guide](CARD_ACTIVATION_API_GUIDE.md)
- [Encryption Deployment Guide](ENCRYPTION_DEPLOYMENT_GUIDE.md)
- [Encryption Verification Report](ENCRYPTION_VERIFICATION_REPORT.md)
