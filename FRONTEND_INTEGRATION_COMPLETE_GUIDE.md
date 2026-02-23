# Frontend Integration Guide - Card Encryption System

## Overview

This guide provides complete instructions for integrating the encrypted card creation endpoint with your frontend application.

## Important Changes - Expiry Date Format

**The backend now accepts expiry dates in `MM-YYYY` format instead of full ISO dates.**

### ✅ Correct Format
```json
{
  "expiryDate": "02-2026"
}
```

### ❌ Incorrect Format
```json
{
  "expiryDate": "2026-02-28"
}
```

## Backend Processing

The backend will:
1. Validate the `MM-YYYY` format using regex: `(0[1-9]|1[0-2])-(20[2-9][0-9]|2[1-9][0-9]{2})`
2. Convert to LocalDate by setting the day to the **last day of the month**
3. Validate that the expiry date is in the future
4. Store in database as LocalDate

### Example Conversion
- `"02-2026"` → `2026-02-28` (last day of February 2026)
- `"12-2029"` → `2029-12-31` (last day of December 2029)

---

## Step 1: Install CryptoJS

```bash
npm install crypto-js
```

---

## Step 2: Set Environment Variable

Add the transport encryption key to your `.env` file:

```env
VITE_ENCRYPTION_KEY=bPRw3dR0xfnHIn3Vr0mFS4BLjEvzDpWPW/0k2+P+72c=
```

**⚠️ SECURITY WARNING:** 
- This key is for **DEVELOPMENT ONLY**
- For production, use a different key provided by your DevOps team
- Never commit the `.env` file to version control

---

## Step 3: Create Encryption Utility

Create a new file `src/utils/encryptionUtil.js`:

```javascript
import CryptoJS from 'crypto-js';

/**
 * Encrypts data using AES-256-GCM (simulated with AES-256-CTR in browser)
 * 
 * @param {string} plainText - The data to encrypt (JSON string)
 * @param {string} base64Key - The base64-encoded encryption key
 * @returns {string} Encrypted data in format: "base64IV.base64EncryptedData"
 */
export function encryptData(plainText, base64Key) {
  try {
    // Decode base64 key
    const keyBytes = CryptoJS.enc.Base64.parse(base64Key);
    
    // Generate random IV (16 bytes for AES)
    const iv = CryptoJS.lib.WordArray.random(16);
    
    // Encrypt using AES-256-CTR (browser-compatible alternative to GCM)
    const encrypted = CryptoJS.AES.encrypt(plainText, keyBytes, {
      iv: iv,
      mode: CryptoJS.mode.CTR,
      padding: CryptoJS.pad.NoPadding
    });
    
    // Convert IV and encrypted data to base64
    const ivBase64 = CryptoJS.enc.Base64.stringify(iv);
    const encryptedBase64 = encrypted.ciphertext.toString(CryptoJS.enc.Base64);
    
    // Return in format expected by backend: "IV.EncryptedData"
    return `${ivBase64}.${encryptedBase64}`;
    
  } catch (error) {
    console.error('Encryption failed:', error);
    throw new Error('Failed to encrypt data');
  }
}

/**
 * Formats a date to MM-YYYY format for backend
 * 
 * @param {Date} date - JavaScript Date object
 * @returns {string} Date in MM-YYYY format (e.g., "02-2026")
 */
export function formatExpiryDate(date) {
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();
  return `${month}-${year}`;
}

/**
 * Formats expiry date from separate month and year values
 * 
 * @param {number|string} month - Month (1-12)
 * @param {number|string} year - Full year (e.g., 2026)
 * @returns {string} Date in MM-YYYY format (e.g., "02-2026")
 */
export function formatExpiryFromParts(month, year) {
  const monthStr = String(month).padStart(2, '0');
  return `${monthStr}-${year}`;
}
```

---

## Step 4: Update Your Card Form Component

### Example with React:

```javascript
import React, { useState } from 'react';
import { encryptData, formatExpiryFromParts } from '../utils/encryptionUtil';

function CreateCardForm() {
  const [formData, setFormData] = useState({
    cardNumber: '',
    expiryMonth: '',
    expiryYear: '',
    cardStatus: 'IACT',
    creditLimit: '',
    cashLimit: '',
    availableCreditLimit: '',
    availableCashLimit: ''
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      // STEP 1: Clean card number (remove spaces, dashes, etc.)
      const cleanCardNumber = formData.cardNumber.replace(/\D/g, '');
      
      // STEP 2: Format expiry date as MM-YYYY
      const expiryDate = formatExpiryFromParts(
        formData.expiryMonth,
        formData.expiryYear
      );
      
      // STEP 3: Validate expiry date is in the future
      const [month, year] = expiryDate.split('-').map(Number);
      const expiryDateObj = new Date(year, month - 1, 1);
      if (expiryDateObj < new Date()) {
        throw new Error('Expiry date must be in the future');
      }
      
      // STEP 4: Create card payload
      const cardPayload = {
        cardNumber: cleanCardNumber,
        expiryDate: expiryDate,  // MM-YYYY format
        cardStatus: formData.cardStatus,
        creditLimit: parseFloat(formData.creditLimit),
        cashLimit: parseFloat(formData.cashLimit),
        availableCreditLimit: parseFloat(formData.availableCreditLimit),
        availableCashLimit: parseFloat(formData.availableCashLimit)
      };

      console.log('Card Payload (before encryption):', cardPayload);
      
      // STEP 5: Convert to JSON string
      const jsonPayload = JSON.stringify(cardPayload);
      
      // STEP 6: Encrypt the payload
      const transportKey = import.meta.env.VITE_ENCRYPTION_KEY;
      const encryptedData = encryptData(jsonPayload, transportKey);
      
      // STEP 7: Create encrypted payload with timestamp
      const encryptedPayload = {
        encryptedData: encryptedData,
        timestamp: new Date().toISOString()
      };

      console.log('Encrypted Payload:', encryptedPayload);
      
      // STEP 8: Send to backend
      const response = await fetch('http://localhost:8080/api/v1/cards/encrypted', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(encryptedPayload)
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to create card');
      }

      const createdCard = await response.json();
      console.log('Card created successfully:', createdCard);
      
      alert('Card created successfully!');
      
    } catch (err) {
      console.error('Error creating card:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <label>Card Number (13-16 digits):</label>
        <input
          type="text"
          value={formData.cardNumber}
          onChange={(e) => setFormData({ ...formData, cardNumber: e.target.value })}
          placeholder="4532 0151 1283 0366"
          required
        />
        <small>Spaces and dashes will be automatically removed</small>
      </div>

      <div>
        <label>Expiry Month (01-12):</label>
        <input
          type="number"
          min="1"
          max="12"
          value={formData.expiryMonth}
          onChange={(e) => setFormData({ ...formData, expiryMonth: e.target.value })}
          required
        />
      </div>

      <div>
        <label>Expiry Year:</label>
        <input
          type="number"
          min={new Date().getFullYear()}
          value={formData.expiryYear}
          onChange={(e) => setFormData({ ...formData, expiryYear: e.target.value })}
          required
        />
      </div>

      <div>
        <label>Card Status:</label>
        <select 
          value={formData.cardStatus}
          onChange={(e) => setFormData({ ...formData, cardStatus: e.target.value })}
        >
          <option value="IACT">Inactive (IACT)</option>
          <option value="CACT">Active (CACT)</option>
          <option value="DACT">Deactivated (DACT)</option>
        </select>
      </div>

      <div>
        <label>Credit Limit:</label>
        <input
          type="number"
          step="0.01"
          min="0"
          value={formData.creditLimit}
          onChange={(e) => setFormData({ ...formData, creditLimit: e.target.value })}
          required
        />
      </div>

      <div>
        <label>Cash Limit:</label>
        <input
          type="number"
          step="0.01"
          min="0"
          value={formData.cashLimit}
          onChange={(e) => setFormData({ ...formData, cashLimit: e.target.value })}
          required
        />
      </div>

      <div>
        <label>Available Credit Limit:</label>
        <input
          type="number"
          step="0.01"
          min="0"
          value={formData.availableCreditLimit}
          onChange={(e) => setFormData({ ...formData, availableCreditLimit: e.target.value })}
          required
        />
      </div>

      <div>
        <label>Available Cash Limit:</label>
        <input
          type="number"
          step="0.01"
          min="0"
          value={formData.availableCashLimit}
          onChange={(e) => setFormData({ ...formData, availableCashLimit: e.target.value })}
          required
        />
      </div>

      {error && <div style={{ color: 'red' }}>{error}</div>}

      <button type="submit" disabled={loading}>
        {loading ? 'Creating...' : 'Create Card'}
      </button>
    </form>
  );
}

export default CreateCardForm;
```

---

## Step 5: Validation Rules

### Card Number
- **Format**: 13-16 consecutive digits
- **Validation**: `^\d{13,16}$`
- **Examples**:
  - ✅ Valid: `4532015112830366`
  - ❌ Invalid: `4532-0151-1283-0366` (contains dashes)
  - ❌ Invalid: `4532 0151 1283 0366` (contains spaces)
  - ❌ Invalid: `123` (too short)

**Frontend Action**: Strip all non-digit characters before encryption:
```javascript
const cleanCardNumber = cardNumber.replace(/\D/g, '');
```

### Expiry Date
- **Format**: `MM-YYYY` where MM is 01-12 and YYYY is year 2020-9999
- **Validation**: `^(0[1-9]|1[0-2])-(20[2-9][0-9]|2[1-9][0-9]{2})$`
- **Must be in the future**
- **Examples**:
  - ✅ Valid: `02-2026`, `12-2029`
  - ❌ Invalid: `2-2026` (month not zero-padded)
  - ❌ Invalid: `2026-02` (wrong order)
  - ❌ Invalid: `02/2026` (wrong separator)
  - ❌ Invalid: `01-2020` (past date)

### Card Status
- **Allowed Values**: `IACT`, `CACT`, `DACT`
- **Examples**:
  - ✅ Valid: `IACT`
  - ❌ Invalid: `Active`, `inactive`, `ACTIVE`

### Numeric Limits
- **All limits must be non-negative numbers**
- **Examples**:
  - ✅ Valid: `50000.00`, `0`, `10000.50`
  - ❌ Invalid: `-100`, `"not a number"`

---

## Step 6: Testing

### Test Payload Example

Here's what your encrypted payload should look like when sent to the backend:

```json
{
  "encryptedData": "u3Rl5GeOR4VIZg5Y.5Pbh5L08J3/FFi4uuLUePFJFExT0xorPCwQMFBWNI848A4H8pxoe046OCzqPccxr8LzzGHl7LJzSS0PNMHqaRcYP1j7RMg9EX/Ssdumpv7eUtwe65rHw0lg+bwclZvOf4sJt/v0ZR1PP7D073kUfQcLaubzjDfjtfG7LvfSJW4DZx/G9IeUXj1PtJhjZ4sgJDRKNKrkCUH3NffvG8XERP1Ako10w+FEJUY1zchZWXANzdVrQZZ13QgwL7z/XqczSG+25llB8",
  "timestamp": "2026-02-23T12:52:43.747759500"
}
```

### Test with curl

```bash
curl -X POST http://localhost:8080/api/v1/cards/encrypted \
  -H "Content-Type: application/json" \
  -d '{
    "encryptedData": "YOUR_ENCRYPTED_DATA_HERE",
    "timestamp": "2026-02-23T12:52:43.747"
  }'
```

### Expected Success Response (201 Created)

```json
{
  "cardNumber": "4532015112830366",
  "expiryDate": "2029-02-28",
  "cardStatus": "IACT",
  "creditLimit": 50000.00,
  "cashLimit": 10000.00,
  "availableCreditLimit": 50000.00,
  "availableCashLimit": 10000.00,
  "lastUpdateTime": "2026-02-23T12:52:43.123"
}
```

### Expected Error Responses

#### 400 Bad Request - Invalid Card Number
```json
{
  "timestamp": "2026-02-23T12:52:43.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid card data: cardNumber: Card number must be 13-16 digits",
  "path": "/api/v1/cards/encrypted"
}
```

#### 400 Bad Request - Invalid Expiry Date Format
```json
{
  "timestamp": "2026-02-23T12:52:43.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid card data: expiryDate: Expiry date must be in MM-YYYY format (e.g., 02-2026)",
  "path": "/api/v1/cards/encrypted"
}
```

#### 400 Bad Request - Expiry Date in Past
```json
{
  "timestamp": "2026-02-23T12:52:43.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Expiry date must be in the future",
  "path": "/api/v1/cards/encrypted"
}
```

#### 400 Bad Request - Decryption Failed
```json
{
  "timestamp": "2026-02-23T12:52:43.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid encrypted payload or card data: Decryption failed",
  "path": "/api/v1/cards/encrypted"
}
```

---

## Step 7: Debugging

### Enable Console Logging

Add these console.log statements to debug issues:

```javascript
// Before encryption
console.log('Original card number:', formData.cardNumber);
console.log('Cleaned card number:', cleanCardNumber);
console.log('Formatted expiry date:', expiryDate);
console.log('Card payload:', cardPayload);

// After encryption
console.log('Encrypted data length:', encryptedData.length);
console.log('Encrypted payload:', encryptedPayload);

// After response
console.log('Response status:', response.status);
console.log('Response data:', await response.json());
```

### Common Issues and Solutions

#### Issue 1: "Card number must be 13-16 digits"
- **Cause**: Card number contains spaces, dashes, or other non-digit characters
- **Solution**: Use `cardNumber.replace(/\D/g, '')` to strip all non-digits

#### Issue 2: "Expiry date must be in MM-YYYY format"
- **Cause**: Wrong date format (e.g., `2026-02` instead of `02-2026`)
- **Solution**: Use `formatExpiryFromParts(month, year)` helper function

#### Issue 3: "Expiry date must be in the future"
- **Cause**: Date is in the past or current month
- **Solution**: Validate date on frontend before submitting

#### Issue 4: "Decryption failed"
- **Cause**: Wrong encryption key or encryption algorithm mismatch
- **Solution**: 
  - Verify `VITE_ENCRYPTION_KEY` matches backend transport key
  - Check encryption util implementation

---

## Step 8: Security Best Practices

### ✅ DO:
- Always clean card numbers before encryption
- Validate all data on frontend before encryption
- Use HTTPS in production
- Store encryption key in environment variables
- Use different keys for development and production
- Clear sensitive data from memory after use

### ❌ DON'T:
- Never log decrypted card numbers
- Never commit encryption keys to version control
- Never store card numbers in browser local storage
- Never send card numbers in URL parameters
- Never reuse the same encryption key across environments

---

## Complete Example - Vanilla JavaScript

```javascript
// Import crypto-js (via CDN or npm)
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

// Create card function
async function createCard() {
  const transportKey = 'bPRw3dR0xfnHIn3Vr0mFS4BLjEvzDpWPW/0k2+P+72c=';
  
  // Clean card number
  const cardNumber = document.getElementById('cardNumber').value.replace(/\D/g, '');
  
  // Format expiry date
  const month = document.getElementById('expiryMonth').value.padStart(2, '0');
  const year = document.getElementById('expiryYear').value;
  const expiryDate = `${month}-${year}`;
  
  // Create payload
  const cardPayload = {
    cardNumber: cardNumber,
    expiryDate: expiryDate,
    cardStatus: 'IACT',
    creditLimit: parseFloat(document.getElementById('creditLimit').value),
    cashLimit: parseFloat(document.getElementById('cashLimit').value),
    availableCreditLimit: parseFloat(document.getElementById('availableCreditLimit').value),
    availableCashLimit: parseFloat(document.getElementById('availableCashLimit').value)
  };
  
  // Encrypt
  const jsonPayload = JSON.stringify(cardPayload);
  const encryptedData = encryptData(jsonPayload, transportKey);
  
  const encryptedPayload = {
    encryptedData: encryptedData,
    timestamp: new Date().toISOString()
  };
  
  // Send to backend
  const response = await fetch('http://localhost:8080/api/v1/cards/encrypted', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(encryptedPayload)
  });
  
  if (response.ok) {
    const card = await response.json();
    console.log('Card created:', card);
  } else {
    const error = await response.json();
    console.error('Error:', error);
  }
}
```

---

## Migration Guide (For Existing Implementations)

If you already have code that sends dates in a different format, here's what to change:

### Before (Old Format - ISO Date)
```javascript
const cardPayload = {
  expiryDate: "2026-02-28"  // ❌ No longer supported
};
```

### After (New Format - MM-YYYY)
```javascript
const cardPayload = {
  expiryDate: "02-2026"  // ✅ Correct format
};
```

### Migration Function
```javascript
// Helper to convert from Date object to MM-YYYY
function migrateDateFormat(date) {
  if (date instanceof Date) {
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${month}-${year}`;
  }
  // If already a string in ISO format (YYYY-MM-DD)
  if (typeof date === 'string' && date.match(/^\d{4}-\d{2}-\d{2}$/)) {
    const [year, month, _] = date.split('-');
    return `${month}-${year}`;
  }
  return date; // Already in correct format
}
```

---

## API Reference

### Endpoint
```
POST /api/v1/cards/encrypted
```

### Request Headers
```
Content-Type: application/json
```

### Request Body
```typescript
interface EncryptedPayload {
  encryptedData: string;  // Format: "base64IV.base64EncryptedData"
  timestamp: string;       // ISO 8601 format
}
```

### Decrypted Payload Structure (What's inside encrypted)
```typescript
interface CreateCardDTO {
  cardNumber: string;              // 13-16 digits (no spaces/dashes)
  expiryDate: string;              // MM-YYYY format
  cardStatus: "IACT" | "CACT" | "DACT";
  creditLimit: number;             // Non-negative
  cashLimit: number;               // Non-negative
  availableCreditLimit: number;    // Non-negative
  availableCashLimit: number;      // Non-negative
}
```

---

## Support

If you encounter issues:

1. Check browser console for errors
2. Verify encryption key matches backend
3. Validate payload format before encryption
4. Check backend logs at `logs/system.log`
5. Look for "Decrypted JSON:" entries in backend logs to see what data was received

For additional help, contact the backend team or refer to:
- `ENCRYPTION_VERIFICATION_REPORT.md` - Complete verification and test results
- `ENCRYPTION_DEPLOYMENT_GUIDE.md` - Production deployment guide
