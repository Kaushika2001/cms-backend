# Card Management System - Frontend API Documentation

## Table of Contents
1. [Overview](#overview)
2. [Base URL](#base-url)
3. [Authentication](#authentication)
4. [Card Management API](#card-management-api)
5. [Card Request Management API](#card-request-management-api)
6. [Pagination](#pagination)
7. [Data Models](#data-models)
8. [Error Handling](#error-handling)
9. [Integration Examples](#integration-examples)
10. [Best Practices](#best-practices)

---

## Overview

This document provides comprehensive API documentation for the Card Management System backend. The API allows you to manage credit/debit cards and their related requests (activation, closure, etc.).

### Key Features
- RESTful API design
- JSON request/response format
- Card number masking for security (GET requests show masked numbers)
- Comprehensive validation
- Detailed error messages
- OpenAPI/Swagger documentation available

### API Version
Current version: **v1**

---

## Base URL

```
http://localhost:8080/api/v1
```

**Note:** Replace with your actual server URL in production.

---

## Authentication

**Security Configuration:** The API uses Spring Security with role-based access control.

**Note:** Authentication details depend on your security configuration. Check `SecurityConfig.java` for specific requirements.

---

## Card Management API

Base Path: `/api/v1/cards`

### 1. Get All Cards

Retrieve a list of all cards with masked card numbers.

**Endpoint:** `GET /api/v1/cards`

**Response:** `200 OK`

```json
[
  {
    "cardNumber": "123456******3456",
    "expiryDate": "2027-12-31",
    "cardStatus": "CACT",
    "cardStatusDescription": "Currently Active",
    "creditLimit": 50000.00,
    "cashLimit": 10000.00,
    "availableCreditLimit": 45000.00,
    "availableCashLimit": 8000.00,
    "lastUpdateTime": "2026-02-19T10:30:00"
  }
]
```

**JavaScript Example:**
```javascript
fetch('http://localhost:8080/api/v1/cards', {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json'
  }
})
  .then(response => response.json())
  .then(data => console.log(data))
  .catch(error => console.error('Error:', error));
```

---

### 2. Get Card by Card Number

Retrieve a specific card by its card number (masked response).

**Endpoint:** `GET /api/v1/cards/{cardNumber}`

**Path Parameters:**
- `cardNumber` (string, required) - The full card number

**Response:** `200 OK`

```json
{
  "cardNumber": "123456******3456",
  "expiryDate": "2027-12-31",
  "cardStatus": "CACT",
  "cardStatusDescription": "Currently Active",
  "creditLimit": 50000.00,
  "cashLimit": 10000.00,
  "availableCreditLimit": 45000.00,
  "availableCashLimit": 8000.00,
  "lastUpdateTime": "2026-02-19T10:30:00"
}
```

**React Example:**
```javascript
const getCardByNumber = async (cardNumber) => {
  try {
    const response = await fetch(`http://localhost:8080/api/v1/cards/${cardNumber}`);
    if (!response.ok) throw new Error('Card not found');
    const card = await response.json();
    return card;
  } catch (error) {
    console.error('Error fetching card:', error);
    throw error;
  }
};
```

**Error Response:** `404 NOT FOUND`
```json
{
  "timestamp": "2026-02-19T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Card not found with cardNumber: 1234567890123456",
  "path": "/api/v1/cards/1234567890123456",
  "details": []
}
```

---

### 3. Get Cards by Status

Retrieve all cards with a specific status.

**Endpoint:** `GET /api/v1/cards/status/{status}`

**Path Parameters:**
- `status` (string, required) - Status code: `IACT`, `CACT`, or `DACT`

**Status Codes:**
| Code | Description |
|------|-------------|
| IACT | Inactive |
| CACT | Currently Active |
| DACT | Deactivated |

**Response:** `200 OK`

```json
[
  {
    "cardNumber": "123456******3456",
    "expiryDate": "2027-12-31",
    "cardStatus": "CACT",
    "cardStatusDescription": "Currently Active",
    "creditLimit": 50000.00,
    "cashLimit": 10000.00,
    "availableCreditLimit": 45000.00,
    "availableCashLimit": 8000.00,
    "lastUpdateTime": "2026-02-19T10:30:00"
  }
]
```

**Vue.js Example:**
```javascript
const fetchActiveCards = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/v1/cards/status/CACT');
    const activeCards = await response.json();
    return activeCards;
  } catch (error) {
    console.error('Error:', error);
  }
};
```

---

### 4. Get Expired Cards

Retrieve all cards that have expired.

**Endpoint:** `GET /api/v1/cards/expired`

**Response:** `200 OK`

```json
[
  {
    "cardNumber": "123456******7890",
    "expiryDate": "2025-12-31",
    "cardStatus": "DACT",
    "cardStatusDescription": "Deactivated",
    "creditLimit": 30000.00,
    "cashLimit": 5000.00,
    "availableCreditLimit": 0.00,
    "availableCashLimit": 0.00,
    "lastUpdateTime": "2026-01-01T00:00:00"
  }
]
```

---

### 5. Get Cards Expiring Soon

Retrieve cards expiring within a specified number of days.

**Endpoint:** `GET /api/v1/cards/expiring?days={days}`

**Query Parameters:**
- `days` (integer, optional, default: 30) - Number of days to look ahead

**Response:** `200 OK`

```json
[
  {
    "cardNumber": "123456******1111",
    "expiryDate": "2026-03-15",
    "cardStatus": "CACT",
    "cardStatusDescription": "Currently Active",
    "creditLimit": 40000.00,
    "cashLimit": 8000.00,
    "availableCreditLimit": 35000.00,
    "availableCashLimit": 7000.00,
    "lastUpdateTime": "2026-02-18T15:20:00"
  }
]
```

**Angular Example:**
```typescript
import { HttpClient } from '@angular/common/http';

getExpiringCards(days: number = 30): Observable<Card[]> {
  return this.http.get<Card[]>(
    `http://localhost:8080/api/v1/cards/expiring?days=${days}`
  );
}
```

---

### 6. Get Card Count by Status

Get the count of cards with a specific status.

**Endpoint:** `GET /api/v1/cards/count?status={status}`

**Query Parameters:**
- `status` (string, required) - Status code: `IACT`, `CACT`, or `DACT`

**Response:** `200 OK`

```json
42
```

**Example:**
```javascript
const getCardCount = async (status) => {
  const response = await fetch(`http://localhost:8080/api/v1/cards/count?status=${status}`);
  const count = await response.json();
  return count;
};
```

---

### 7. Create New Card

Create a new card in the system.

**Endpoint:** `POST /api/v1/cards`

**Request Body:** `CreateCardDTO`

```json
{
  "cardNumber": "1234567890123456",
  "expiryDate": "2027-12-31",
  "cardStatus": "IACT",
  "creditLimit": 50000.00,
  "cashLimit": 10000.00,
  "availableCreditLimit": 50000.00,
  "availableCashLimit": 10000.00
}
```

**Validation Rules:**
- `cardNumber`: Required, 13-16 digits, must be unique
- `expiryDate`: Required, must be a future date, format: YYYY-MM-DD
- `cardStatus`: Required, must be one of: IACT, CACT, DACT
- `creditLimit`: Required, must be >= 0
- `cashLimit`: Required, must be >= 0
- `availableCreditLimit`: Required, must be >= 0
- `availableCashLimit`: Required, must be >= 0

**Response:** `201 CREATED`

```json
{
  "cardNumber": "1234567890123456",
  "expiryDate": "2027-12-31",
  "cardStatus": "IACT",
  "cardStatusDescription": "Inactive",
  "creditLimit": 50000.00,
  "cashLimit": 10000.00,
  "availableCreditLimit": 50000.00,
  "availableCashLimit": 10000.00,
  "lastUpdateTime": "2026-02-19T10:30:00"
}
```

**React Example:**
```javascript
const createCard = async (cardData) => {
  try {
    const response = await fetch('http://localhost:8080/api/v1/cards', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(cardData)
    });
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message);
    }
    
    const newCard = await response.json();
    return newCard;
  } catch (error) {
    console.error('Error creating card:', error);
    throw error;
  }
};

// Usage
const cardData = {
  cardNumber: "1234567890123456",
  expiryDate: "2027-12-31",
  cardStatus: "IACT",
  creditLimit: 50000.00,
  cashLimit: 10000.00,
  availableCreditLimit: 50000.00,
  availableCashLimit: 10000.00
};

createCard(cardData);
```

**Error Response:** `400 BAD REQUEST` (Validation Failure)
```json
{
  "timestamp": "2026-02-19T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/cards",
  "details": [
    "cardNumber: must match \"\\d{13,16}\"",
    "expiryDate: must be a future date",
    "creditLimit: must be greater than or equal to 0"
  ]
}
```

**Error Response:** `409 CONFLICT` (Duplicate Card)
```json
{
  "timestamp": "2026-02-19T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Card already exists with cardNumber: 1234567890123456",
  "path": "/api/v1/cards",
  "details": []
}
```

---

### 8. Update Card

Update an existing card.

**Endpoint:** `PUT /api/v1/cards/{cardNumber}`

**Path Parameters:**
- `cardNumber` (string, required) - The card number to update

**Request Body:** `CreateCardDTO`

```json
{
  "cardNumber": "1234567890123456",
  "expiryDate": "2028-12-31",
  "cardStatus": "CACT",
  "creditLimit": 60000.00,
  "cashLimit": 12000.00,
  "availableCreditLimit": 55000.00,
  "availableCashLimit": 11000.00
}
```

**Response:** `200 OK`

```json
{
  "cardNumber": "1234567890123456",
  "expiryDate": "2028-12-31",
  "cardStatus": "CACT",
  "cardStatusDescription": "Currently Active",
  "creditLimit": 60000.00,
  "cashLimit": 12000.00,
  "availableCreditLimit": 55000.00,
  "availableCashLimit": 11000.00,
  "lastUpdateTime": "2026-02-19T11:45:00"
}
```

**Axios Example:**
```javascript
import axios from 'axios';

const updateCard = async (cardNumber, updatedData) => {
  try {
    const response = await axios.put(
      `http://localhost:8080/api/v1/cards/${cardNumber}`,
      updatedData
    );
    return response.data;
  } catch (error) {
    if (error.response?.status === 404) {
      console.error('Card not found');
    }
    throw error;
  }
};
```

---

### 9. Update Card Status

Update only the status of a card.

**Endpoint:** `PATCH /api/v1/cards/{cardNumber}/status?status={status}`

**Path Parameters:**
- `cardNumber` (string, required) - The card number

**Query Parameters:**
- `status` (string, required) - New status: `IACT`, `CACT`, or `DACT`

**Response:** `200 OK`

```json
{
  "cardNumber": "1234567890123456",
  "expiryDate": "2028-12-31",
  "cardStatus": "DACT",
  "cardStatusDescription": "Deactivated",
  "creditLimit": 60000.00,
  "cashLimit": 12000.00,
  "availableCreditLimit": 55000.00,
  "availableCashLimit": 11000.00,
  "lastUpdateTime": "2026-02-19T12:00:00"
}
```

**Example:**
```javascript
const deactivateCard = async (cardNumber) => {
  const response = await fetch(
    `http://localhost:8080/api/v1/cards/${cardNumber}/status?status=DACT`,
    { method: 'PATCH' }
  );
  return response.json();
};
```

---

### 10. Delete Card

Delete a card from the system.

**Endpoint:** `DELETE /api/v1/cards/{cardNumber}`

**Path Parameters:**
- `cardNumber` (string, required) - The card number to delete

**Response:** `204 NO CONTENT`

**Example:**
```javascript
const deleteCard = async (cardNumber) => {
  try {
    const response = await fetch(
      `http://localhost:8080/api/v1/cards/${cardNumber}`,
      { method: 'DELETE' }
    );
    
    if (response.status === 204) {
      console.log('Card deleted successfully');
      return true;
    }
  } catch (error) {
    console.error('Error deleting card:', error);
    return false;
  }
};
```

---

## Card Request Management API

Base Path: `/api/v1/requests`

### 1. Get All Requests

Retrieve a list of all card requests with masked card numbers.

**Endpoint:** `GET /api/v1/requests`

**Response:** `200 OK`

```json
[
  {
    "requestId": 1,
    "cardNumber": "123456******3456",
    "requestReasonCode": "ACTI",
    "requestReasonDescription": "Activation",
    "requestStatusCode": "PEND",
    "requestStatusDescription": "Pending",
    "remark": "Customer requested card activation",
    "createdTime": "2026-02-19T10:30:00"
  }
]
```

---

### 2. Get Request by ID

Retrieve a specific request by its ID.

**Endpoint:** `GET /api/v1/requests/{requestId}`

**Path Parameters:**
- `requestId` (integer, required) - The request ID

**Response:** `200 OK`

```json
{
  "requestId": 1,
  "cardNumber": "123456******3456",
  "requestReasonCode": "ACTI",
  "requestReasonDescription": "Activation",
  "requestStatusCode": "PEND",
  "requestStatusDescription": "Pending",
  "remark": "Customer requested card activation",
  "createdTime": "2026-02-19T10:30:00"
}
```

---

### 3. Get Requests by Card Number

Retrieve all requests for a specific card.

**Endpoint:** `GET /api/v1/requests/card/{cardNumber}`

**Path Parameters:**
- `cardNumber` (string, required) - The card number

**Response:** `200 OK`

```json
[
  {
    "requestId": 1,
    "cardNumber": "123456******3456",
    "requestReasonCode": "ACTI",
    "requestReasonDescription": "Activation",
    "requestStatusCode": "APPR",
    "requestStatusDescription": "Approved",
    "remark": "Card activated successfully",
    "createdTime": "2026-02-18T09:00:00"
  },
  {
    "requestId": 5,
    "cardNumber": "123456******3456",
    "requestReasonCode": "CDCL",
    "requestReasonDescription": "Card Closure",
    "requestStatusCode": "PEND",
    "requestStatusDescription": "Pending",
    "remark": "Customer requested card closure",
    "createdTime": "2026-02-19T10:30:00"
  }
]
```

---

### 4. Get Requests by Status

Retrieve all requests with a specific status.

**Endpoint:** `GET /api/v1/requests/status/{status}`

**Path Parameters:**
- `status` (string, required) - Status code: `PEND`, `APPR`, or `RJCT`

**Status Codes:**
| Code | Description |
|------|-------------|
| PEND | Pending |
| APPR | Approved |
| RJCT | Rejected |

**Response:** `200 OK`

```json
[
  {
    "requestId": 1,
    "cardNumber": "123456******3456",
    "requestReasonCode": "ACTI",
    "requestReasonDescription": "Activation",
    "requestStatusCode": "PEND",
    "requestStatusDescription": "Pending",
    "remark": "Customer requested card activation",
    "createdTime": "2026-02-19T10:30:00"
  }
]
```

**Example:**
```javascript
const getPendingRequests = async () => {
  const response = await fetch('http://localhost:8080/api/v1/requests/status/PEND');
  return response.json();
};
```

---

### 5. Get Requests by Type

Retrieve all requests of a specific type.

**Endpoint:** `GET /api/v1/requests/type/{type}`

**Path Parameters:**
- `type` (string, required) - Request type: `ACTI` or `CDCL`

**Request Types:**
| Code | Description |
|------|-------------|
| ACTI | Activation |
| CDCL | Card Closure |

**Response:** `200 OK`

```json
[
  {
    "requestId": 1,
    "cardNumber": "123456******3456",
    "requestReasonCode": "ACTI",
    "requestReasonDescription": "Activation",
    "requestStatusCode": "PEND",
    "requestStatusDescription": "Pending",
    "remark": "Customer requested card activation",
    "createdTime": "2026-02-19T10:30:00"
  }
]
```

---

### 6. Get Pending Requests

Retrieve all pending requests (convenience endpoint).

**Endpoint:** `GET /api/v1/requests/pending`

**Response:** `200 OK`

```json
[
  {
    "requestId": 1,
    "cardNumber": "123456******3456",
    "requestReasonCode": "ACTI",
    "requestReasonDescription": "Activation",
    "requestStatusCode": "PEND",
    "requestStatusDescription": "Pending",
    "remark": "Customer requested card activation",
    "createdTime": "2026-02-19T10:30:00"
  }
]
```

---

### 7. Get Request Count by Status

Get the count of requests with a specific status.

**Endpoint:** `GET /api/v1/requests/count?status={status}`

**Query Parameters:**
- `status` (string, required) - Status code: `PEND`, `APPR`, or `RJCT`

**Response:** `200 OK`

```json
15
```

---

### 8. Create New Request

Create a new card request (activation or closure).

**Endpoint:** `POST /api/v1/requests`

**Request Body:** `CreateCardRequestDTO`

```json
{
  "cardNumber": "1234567890123456",
  "requestReasonCode": "ACTI",
  "remark": "Customer requested card activation via phone"
}
```

**Validation Rules:**
- `cardNumber`: Required, 13-16 digits, must exist in system
- `requestReasonCode`: Required, must be `ACTI` or `CDCL`
- `remark`: Optional, max 500 characters

**Response:** `201 CREATED`

```json
{
  "requestId": 10,
  "cardNumber": "1234567890123456",
  "requestReasonCode": "ACTI",
  "requestReasonDescription": "Activation",
  "requestStatusCode": "PEND",
  "requestStatusDescription": "Pending",
  "remark": "Customer requested card activation via phone",
  "createdTime": "2026-02-19T10:30:00"
}
```

**React Hook Example:**
```javascript
import { useState } from 'react';

const useCreateRequest = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const createRequest = async (requestData) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch('http://localhost:8080/api/v1/requests', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestData)
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message);
      }

      const newRequest = await response.json();
      return newRequest;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return { createRequest, loading, error };
};

// Usage in component
const { createRequest, loading, error } = useCreateRequest();

const handleSubmit = async () => {
  try {
    const request = await createRequest({
      cardNumber: "1234567890123456",
      requestReasonCode: "ACTI",
      remark: "Customer requested activation"
    });
    console.log('Request created:', request);
  } catch (err) {
    console.error('Failed to create request:', err);
  }
};
```

---

### 9. Update Request

Update an existing request (only pending requests can be updated).

**Endpoint:** `PUT /api/v1/requests/{requestId}`

**Path Parameters:**
- `requestId` (integer, required) - The request ID

**Request Body:** `CreateCardRequestDTO`

```json
{
  "cardNumber": "1234567890123456",
  "requestReasonCode": "CDCL",
  "remark": "Changed to closure request per customer update"
}
```

**Response:** `200 OK`

```json
{
  "requestId": 10,
  "cardNumber": "1234567890123456",
  "requestReasonCode": "CDCL",
  "requestReasonDescription": "Card Closure",
  "requestStatusCode": "PEND",
  "requestStatusDescription": "Pending",
  "remark": "Changed to closure request per customer update",
  "createdTime": "2026-02-19T10:30:00"
}
```

**Error Response:** `400 BAD REQUEST` (Request not pending)
```json
{
  "timestamp": "2026-02-19T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Only pending requests can be updated",
  "path": "/api/v1/requests/10",
  "details": []
}
```

---

### 10. Process Request (Approve/Reject)

Approve or reject a pending request.

**Endpoint:** `POST /api/v1/requests/{requestId}/process`

**Path Parameters:**
- `requestId` (integer, required) - The request ID

**Request Body:** `ApproveRequestDTO`

```json
{
  "requestStatusCode": "APPR",
  "remark": "Request approved by manager"
}
```

**Validation Rules:**
- `requestStatusCode`: Required, must be `APPR` or `RJCT`
- `remark`: Optional, max 500 characters

**Response:** `200 OK`

```json
{
  "requestId": 10,
  "cardNumber": "1234567890123456",
  "requestReasonCode": "ACTI",
  "requestReasonDescription": "Activation",
  "requestStatusCode": "APPR",
  "requestStatusDescription": "Approved",
  "remark": "Request approved by manager",
  "createdTime": "2026-02-19T10:30:00"
}
```

**Vue.js Example:**
```javascript
const approveRequest = async (requestId, remark) => {
  try {
    const response = await fetch(
      `http://localhost:8080/api/v1/requests/${requestId}/process`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          requestStatusCode: 'APPR',
          remark: remark
        })
      }
    );
    
    if (!response.ok) throw new Error('Failed to approve request');
    return await response.json();
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
};

const rejectRequest = async (requestId, remark) => {
  try {
    const response = await fetch(
      `http://localhost:8080/api/v1/requests/${requestId}/process`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          requestStatusCode: 'RJCT',
          remark: remark
        })
      }
    );
    
    if (!response.ok) throw new Error('Failed to reject request');
    return await response.json();
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
};
```

---

### 11. Delete Request

Delete a request (only pending or rejected requests can be deleted).

**Endpoint:** `DELETE /api/v1/requests/{requestId}`

**Path Parameters:**
- `requestId` (integer, required) - The request ID

**Response:** `204 NO CONTENT`

**Error Response:** `400 BAD REQUEST` (Request approved)
```json
{
  "timestamp": "2026-02-19T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Only pending or rejected requests can be deleted",
  "path": "/api/v1/requests/10",
  "details": []
}
```

---

## Pagination

The API supports pagination for all list endpoints to improve performance with large datasets. Pagination endpoints use query parameters to control page size and page number.

### Pagination Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | integer | 0 | Page number (0-based) |
| `size` | integer | 10 | Number of items per page |

### Pagination Response Format

All paginated endpoints return a consistent response structure:

```json
{
  "content": [
    {
      // Array of items (cards or requests)
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 45,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

**Response Fields:**
- `content`: Array of items for the current page
- `page`: Current page number (0-based)
- `size`: Number of items per page
- `totalElements`: Total number of items across all pages
- `totalPages`: Total number of pages
- `first`: `true` if this is the first page
- `last`: `true` if this is the last page

---

### Card Pagination Endpoints

#### 1. Get All Cards (Paginated)

**Endpoint:** `GET /api/v1/cards/paginated`

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Page size

**Example Request:**
```
GET /api/v1/cards/paginated?page=0&size=20
```

**Response:** `200 OK`
```json
{
  "content": [
    {
      "maskedCardId": "CRD-M-943E49CC",
      "cardNumber": "589925******0233",
      "expiryDate": "2027-12-31",
      "cardStatus": "CACT",
      "cardStatusDescription": "Currently Active",
      "creditLimit": 50000.00,
      "cashLimit": 10000.00,
      "availableCreditLimit": 45000.00,
      "availableCashLimit": 8000.00,
      "lastUpdateTime": "2026-02-19T10:30:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 45,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

**JavaScript Example:**
```javascript
const fetchCardsPaginated = async (page = 0, size = 10) => {
  try {
    const response = await fetch(
      `http://localhost:8080/api/v1/cards/paginated?page=${page}&size=${size}`,
      {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
      }
    );
    
    if (!response.ok) throw new Error('Failed to fetch cards');
    return await response.json();
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
};

// Usage
const result = await fetchCardsPaginated(0, 20);
console.log(`Showing ${result.content.length} of ${result.totalElements} cards`);
console.log(`Page ${result.page + 1} of ${result.totalPages}`);
```

---

#### 2. Get Cards by Status (Paginated)

**Endpoint:** `GET /api/v1/cards/status/{status}/paginated`

**Path Parameters:**
- `status` (required) - Card status code (IACT, CACT, DACT)

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Page size

**Example Request:**
```
GET /api/v1/cards/status/CACT/paginated?page=0&size=15
```

---

#### 3. Get Expired Cards (Paginated)

**Endpoint:** `GET /api/v1/cards/expired/paginated`

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Page size

**Example Request:**
```
GET /api/v1/cards/expired/paginated?page=1&size=10
```

---

#### 4. Get Cards Expiring Soon (Paginated)

**Endpoint:** `GET /api/v1/cards/expiring/paginated`

**Query Parameters:**
- `days` (optional, default: 30) - Number of days to check
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Page size

**Example Request:**
```
GET /api/v1/cards/expiring/paginated?days=60&page=0&size=10
```

---

### Card Request Pagination Endpoints

#### 1. Get All Requests (Paginated)

**Endpoint:** `GET /api/v1/requests/paginated`

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Page size

**Example Request:**
```
GET /api/v1/requests/paginated?page=0&size=25
```

**Response:** `200 OK`
```json
{
  "content": [
    {
      "requestId": 10,
      "cardNumber": "1234567890123456",
      "requestReasonCode": "ACTI",
      "requestReasonDescription": "Activation",
      "requestStatusCode": "PEND",
      "requestStatusDescription": "Pending",
      "remark": "Customer requested activation",
      "createdTime": "2026-02-19T10:30:00"
    }
  ],
  "page": 0,
  "size": 25,
  "totalElements": 100,
  "totalPages": 4,
  "first": true,
  "last": false
}
```

---

#### 2. Get Requests by Card Number (Paginated)

**Endpoint:** `GET /api/v1/requests/card/{cardNumber}/paginated`

**Path Parameters:**
- `cardNumber` (required) - Card number

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Page size

**Example Request:**
```
GET /api/v1/requests/card/1234567890123456/paginated?page=0&size=10
```

---

#### 3. Get Requests by Status (Paginated)

**Endpoint:** `GET /api/v1/requests/status/{status}/paginated`

**Path Parameters:**
- `status` (required) - Request status code (PEND, APPR, RJCT)

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Page size

**Example Request:**
```
GET /api/v1/requests/status/PEND/paginated?page=0&size=50
```

---

#### 4. Get Requests by Type (Paginated)

**Endpoint:** `GET /api/v1/requests/type/{type}/paginated`

**Path Parameters:**
- `type` (required) - Request type code (ACTI, CDCL)

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Page size

**Example Request:**
```
GET /api/v1/requests/type/ACTI/paginated?page=1&size=20
```

---

#### 5. Get Pending Requests (Paginated)

**Endpoint:** `GET /api/v1/requests/pending/paginated`

**Query Parameters:**
- `page` (optional, default: 0) - Page number
- `size` (optional, default: 10) - Page size

**Example Request:**
```
GET /api/v1/requests/pending/paginated?page=0&size=10
```

---

### React Pagination Example

Here's a complete example of implementing pagination in React:

```javascript
import React, { useState, useEffect } from 'react';

const CardListPaginated = () => {
  const [paginatedData, setPaginatedData] = useState(null);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchCards();
  }, [page, size]);

  const fetchCards = async () => {
    setLoading(true);
    try {
      const response = await fetch(
        `http://localhost:8080/api/v1/cards/paginated?page=${page}&size=${size}`
      );
      const data = await response.json();
      setPaginatedData(data);
    } catch (error) {
      console.error('Error fetching cards:', error);
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (newPage) => {
    setPage(newPage);
  };

  const handleSizeChange = (newSize) => {
    setSize(newSize);
    setPage(0); // Reset to first page when size changes
  };

  if (loading) return <div>Loading...</div>;
  if (!paginatedData) return null;

  return (
    <div>
      <h2>Cards</h2>
      
      {/* Page size selector */}
      <select value={size} onChange={(e) => handleSizeChange(Number(e.target.value))}>
        <option value={10}>10 per page</option>
        <option value={25}>25 per page</option>
        <option value={50}>50 per page</option>
        <option value={100}>100 per page</option>
      </select>

      {/* Cards list */}
      <div>
        {paginatedData.content.map((card) => (
          <div key={card.maskedCardId}>
            <p>Card: {card.cardNumber}</p>
            <p>Status: {card.cardStatusDescription}</p>
          </div>
        ))}
      </div>

      {/* Pagination controls */}
      <div>
        <p>
          Showing {paginatedData.content.length} of {paginatedData.totalElements} cards
          (Page {paginatedData.page + 1} of {paginatedData.totalPages})
        </p>
        
        <button 
          onClick={() => handlePageChange(page - 1)} 
          disabled={paginatedData.first}
        >
          Previous
        </button>
        
        <span> Page {page + 1} </span>
        
        <button 
          onClick={() => handlePageChange(page + 1)} 
          disabled={paginatedData.last}
        >
          Next
        </button>
      </div>
    </div>
  );
};

export default CardListPaginated;
```

---

### Vue.js Pagination Example

```javascript
<template>
  <div>
    <h2>Cards</h2>
    
    <!-- Page size selector -->
    <select v-model.number="size" @change="fetchCards">
      <option :value="10">10 per page</option>
      <option :value="25">25 per page</option>
      <option :value="50">50 per page</option>
      <option :value="100">100 per page</option>
    </select>

    <!-- Cards list -->
    <div v-if="loading">Loading...</div>
    <div v-else-if="paginatedData">
      <div v-for="card in paginatedData.content" :key="card.maskedCardId">
        <p>Card: {{ card.cardNumber }}</p>
        <p>Status: {{ card.cardStatusDescription }}</p>
      </div>

      <!-- Pagination controls -->
      <div>
        <p>
          Showing {{ paginatedData.content.length }} of {{ paginatedData.totalElements }} cards
          (Page {{ paginatedData.page + 1 }} of {{ paginatedData.totalPages }})
        </p>
        
        <button 
          @click="prevPage" 
          :disabled="paginatedData.first"
        >
          Previous
        </button>
        
        <span> Page {{ page + 1 }} </span>
        
        <button 
          @click="nextPage" 
          :disabled="paginatedData.last"
        >
          Next
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      paginatedData: null,
      page: 0,
      size: 10,
      loading: false
    };
  },
  mounted() {
    this.fetchCards();
  },
  methods: {
    async fetchCards() {
      this.loading = true;
      try {
        const response = await fetch(
          `http://localhost:8080/api/v1/cards/paginated?page=${this.page}&size=${this.size}`
        );
        this.paginatedData = await response.json();
      } catch (error) {
        console.error('Error fetching cards:', error);
      } finally {
        this.loading = false;
      }
    },
    prevPage() {
      if (!this.paginatedData.first) {
        this.page--;
        this.fetchCards();
      }
    },
    nextPage() {
      if (!this.paginatedData.last) {
        this.page++;
        this.fetchCards();
      }
    }
  }
};
</script>
```

---

## Data Models

### Card Status Reference

| Code | Description | Typical Use |
|------|-------------|-------------|
| IACT | Inactive | Card created but not yet activated |
| CACT | Currently Active | Card is active and can be used |
| DACT | Deactivated | Card has been deactivated/blocked |

### Request Status Reference

| Code | Description | Typical Use |
|------|-------------|-------------|
| PEND | Pending | Request awaiting approval |
| APPR | Approved | Request has been approved and processed |
| RJCT | Rejected | Request has been rejected |

### Request Type Reference

| Code | Description | Typical Use |
|------|-------------|-------------|
| ACTI | Activation | Request to activate a card |
| CDCL | Card Closure | Request to close/terminate a card |

---

## Error Handling

### Standard Error Response Format

All errors follow this consistent structure:

```json
{
  "timestamp": "2026-02-19T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/api/v1/cards",
  "details": [
    "field1: validation error message",
    "field2: validation error message"
  ]
}
```

### HTTP Status Codes

| Status Code | Meaning | When It Occurs |
|-------------|---------|----------------|
| 200 OK | Success | Successful GET, PUT, PATCH |
| 201 CREATED | Resource Created | Successful POST |
| 204 NO CONTENT | Success (No Body) | Successful DELETE |
| 400 BAD REQUEST | Invalid Input | Validation errors, invalid data |
| 403 FORBIDDEN | Access Denied | Insufficient permissions |
| 404 NOT FOUND | Resource Not Found | Card/Request doesn't exist |
| 409 CONFLICT | Resource Conflict | Duplicate card number |
| 500 INTERNAL SERVER ERROR | Server Error | Unexpected server error |

### Common Error Scenarios

#### 1. Validation Errors (400)

**Scenario:** Creating a card with invalid data

**Request:**
```json
{
  "cardNumber": "123",
  "expiryDate": "2020-01-01",
  "cardStatus": "INVALID",
  "creditLimit": -1000
}
```

**Response:**
```json
{
  "timestamp": "2026-02-19T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/cards",
  "details": [
    "cardNumber: must match \"\\d{13,16}\"",
    "expiryDate: must be a future date",
    "cardStatus: must be one of: IACT, CACT, DACT",
    "creditLimit: must be greater than or equal to 0"
  ]
}
```

#### 2. Resource Not Found (404)

**Scenario:** Getting a card that doesn't exist

**Response:**
```json
{
  "timestamp": "2026-02-19T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Card not found with cardNumber: 9999999999999999",
  "path": "/api/v1/cards/9999999999999999",
  "details": []
}
```

#### 3. Duplicate Resource (409)

**Scenario:** Creating a card with an existing card number

**Response:**
```json
{
  "timestamp": "2026-02-19T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Card already exists with cardNumber: 1234567890123456",
  "path": "/api/v1/cards",
  "details": []
}
```

#### 4. Invalid Date Format (400)

**Scenario:** Providing an invalid date like February 30

**Response:**
```json
{
  "timestamp": "2026-02-19T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid date format. Expected format: YYYY-MM-DD. Error: Text '2026-02-30' could not be parsed: Invalid date 'FEBRUARY 30'",
  "path": "/api/v1/cards",
  "details": []
}
```

#### 5. Invalid JSON (400)

**Scenario:** Sending malformed JSON

**Response:**
```json
{
  "timestamp": "2026-02-19T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid JSON format in request body",
  "path": "/api/v1/cards",
  "details": []
}
```

### Error Handling Best Practices

**JavaScript/TypeScript:**
```javascript
const handleApiError = (error) => {
  if (error.response) {
    // Server responded with error
    const { status, data } = error.response;
    
    switch (status) {
      case 400:
        // Show validation errors
        if (data.details && data.details.length > 0) {
          data.details.forEach(detail => {
            console.error('Validation error:', detail);
          });
        } else {
          console.error(data.message);
        }
        break;
      
      case 404:
        console.error('Resource not found:', data.message);
        break;
      
      case 409:
        console.error('Duplicate resource:', data.message);
        break;
      
      case 500:
        console.error('Server error. Please try again later.');
        break;
      
      default:
        console.error('An error occurred:', data.message);
    }
  } else if (error.request) {
    // Request made but no response
    console.error('Network error. Please check your connection.');
  } else {
    // Something else happened
    console.error('Error:', error.message);
  }
};
```

---

## Integration Examples

### Complete React Component Example

```javascript
import React, { useState, useEffect } from 'react';
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1';

const CardManagement = () => {
  const [cards, setCards] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Fetch all cards
  useEffect(() => {
    fetchCards();
  }, []);

  const fetchCards = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await axios.get(`${API_BASE_URL}/cards`);
      setCards(response.data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch cards');
    } finally {
      setLoading(false);
    }
  };

  const createCard = async (cardData) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/cards`, cardData);
      setCards([...cards, response.data]);
      return response.data;
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Failed to create card';
      const errorDetails = err.response?.data?.details || [];
      
      if (errorDetails.length > 0) {
        throw new Error(errorDetails.join(', '));
      }
      throw new Error(errorMsg);
    }
  };

  const updateCardStatus = async (cardNumber, status) => {
    try {
      const response = await axios.patch(
        `${API_BASE_URL}/cards/${cardNumber}/status?status=${status}`
      );
      
      // Update local state
      setCards(cards.map(card => 
        card.cardNumber.includes(cardNumber.slice(-4)) 
          ? { ...card, cardStatus: status }
          : card
      ));
      
      return response.data;
    } catch (err) {
      throw new Error(err.response?.data?.message || 'Failed to update status');
    }
  };

  const deleteCard = async (cardNumber) => {
    try {
      await axios.delete(`${API_BASE_URL}/cards/${cardNumber}`);
      setCards(cards.filter(card => !card.cardNumber.includes(cardNumber.slice(-4))));
    } catch (err) {
      throw new Error(err.response?.data?.message || 'Failed to delete card');
    }
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div>
      <h1>Card Management</h1>
      <button onClick={fetchCards}>Refresh</button>
      
      <table>
        <thead>
          <tr>
            <th>Card Number</th>
            <th>Expiry Date</th>
            <th>Status</th>
            <th>Credit Limit</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {cards.map(card => (
            <tr key={card.cardNumber}>
              <td>{card.cardNumber}</td>
              <td>{card.expiryDate}</td>
              <td>{card.cardStatusDescription}</td>
              <td>{card.creditLimit}</td>
              <td>
                <button onClick={() => updateCardStatus(card.cardNumber, 'CACT')}>
                  Activate
                </button>
                <button onClick={() => updateCardStatus(card.cardNumber, 'DACT')}>
                  Deactivate
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default CardManagement;
```

---

### Vue.js Composable Example

```javascript
// useCardService.js
import { ref } from 'vue';
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1';

export function useCardService() {
  const cards = ref([]);
  const loading = ref(false);
  const error = ref(null);

  const fetchCards = async () => {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await axios.get(`${API_BASE_URL}/cards`);
      cards.value = response.data;
    } catch (err) {
      error.value = err.response?.data?.message || 'Failed to fetch cards';
    } finally {
      loading.value = false;
    }
  };

  const fetchCardByNumber = async (cardNumber) => {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await axios.get(`${API_BASE_URL}/cards/${cardNumber}`);
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || 'Card not found';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const fetchCardsByStatus = async (status) => {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await axios.get(`${API_BASE_URL}/cards/status/${status}`);
      cards.value = response.data;
    } catch (err) {
      error.value = err.response?.data?.message || 'Failed to fetch cards';
    } finally {
      loading.value = false;
    }
  };

  const fetchExpiringCards = async (days = 30) => {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await axios.get(`${API_BASE_URL}/cards/expiring?days=${days}`);
      cards.value = response.data;
    } catch (err) {
      error.value = err.response?.data?.message || 'Failed to fetch cards';
    } finally {
      loading.value = false;
    }
  };

  const createCard = async (cardData) => {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await axios.post(`${API_BASE_URL}/cards`, cardData);
      cards.value.push(response.data);
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || 'Failed to create card';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const updateCard = async (cardNumber, cardData) => {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await axios.put(
        `${API_BASE_URL}/cards/${cardNumber}`,
        cardData
      );
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || 'Failed to update card';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const updateCardStatus = async (cardNumber, status) => {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await axios.patch(
        `${API_BASE_URL}/cards/${cardNumber}/status?status=${status}`
      );
      return response.data;
    } catch (err) {
      error.value = err.response?.data?.message || 'Failed to update status';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const deleteCard = async (cardNumber) => {
    loading.value = true;
    error.value = null;
    
    try {
      await axios.delete(`${API_BASE_URL}/cards/${cardNumber}`);
      cards.value = cards.value.filter(
        card => !card.cardNumber.includes(cardNumber.slice(-4))
      );
    } catch (err) {
      error.value = err.response?.data?.message || 'Failed to delete card';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  return {
    cards,
    loading,
    error,
    fetchCards,
    fetchCardByNumber,
    fetchCardsByStatus,
    fetchExpiringCards,
    createCard,
    updateCard,
    updateCardStatus,
    deleteCard
  };
}
```

---

### Angular Service Example

```typescript
// card.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

interface Card {
  cardNumber: string;
  expiryDate: string;
  cardStatus: string;
  cardStatusDescription: string;
  creditLimit: number;
  cashLimit: number;
  availableCreditLimit: number;
  availableCashLimit: number;
  lastUpdateTime: string;
}

interface CardRequest {
  requestId: number;
  cardNumber: string;
  requestReasonCode: string;
  requestReasonDescription: string;
  requestStatusCode: string;
  requestStatusDescription: string;
  remark: string;
  createdTime: string;
}

@Injectable({
  providedIn: 'root'
})
export class CardService {
  private apiUrl = 'http://localhost:8080/api/v1/cards';

  constructor(private http: HttpClient) {}

  // Get all cards
  getAllCards(): Observable<Card[]> {
    return this.http.get<Card[]>(this.apiUrl)
      .pipe(catchError(this.handleError));
  }

  // Get card by card number
  getCardByNumber(cardNumber: string): Observable<Card> {
    return this.http.get<Card>(`${this.apiUrl}/${cardNumber}`)
      .pipe(catchError(this.handleError));
  }

  // Get cards by status
  getCardsByStatus(status: string): Observable<Card[]> {
    return this.http.get<Card[]>(`${this.apiUrl}/status/${status}`)
      .pipe(catchError(this.handleError));
  }

  // Get expired cards
  getExpiredCards(): Observable<Card[]> {
    return this.http.get<Card[]>(`${this.apiUrl}/expired`)
      .pipe(catchError(this.handleError));
  }

  // Get cards expiring soon
  getCardsExpiringSoon(days: number = 30): Observable<Card[]> {
    const params = new HttpParams().set('days', days.toString());
    return this.http.get<Card[]>(`${this.apiUrl}/expiring`, { params })
      .pipe(catchError(this.handleError));
  }

  // Get card count by status
  getCardCountByStatus(status: string): Observable<number> {
    const params = new HttpParams().set('status', status);
    return this.http.get<number>(`${this.apiUrl}/count`, { params })
      .pipe(catchError(this.handleError));
  }

  // Create card
  createCard(cardData: any): Observable<Card> {
    return this.http.post<Card>(this.apiUrl, cardData)
      .pipe(catchError(this.handleError));
  }

  // Update card
  updateCard(cardNumber: string, cardData: any): Observable<Card> {
    return this.http.put<Card>(`${this.apiUrl}/${cardNumber}`, cardData)
      .pipe(catchError(this.handleError));
  }

  // Update card status
  updateCardStatus(cardNumber: string, status: string): Observable<Card> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<Card>(
      `${this.apiUrl}/${cardNumber}/status`,
      null,
      { params }
    ).pipe(catchError(this.handleError));
  }

  // Delete card
  deleteCard(cardNumber: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${cardNumber}`)
      .pipe(catchError(this.handleError));
  }

  // Error handler
  private handleError(error: any): Observable<never> {
    let errorMessage = 'An error occurred';
    
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      errorMessage = error.error?.message || 
                     `Error Code: ${error.status}\nMessage: ${error.message}`;
    }
    
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
```

---

## Best Practices

### 1. Security

- **Never store full card numbers in frontend state longer than necessary**
- **Always use HTTPS in production**
- **Implement proper authentication and authorization**
- **Don't log sensitive data (full card numbers) to console**
- **Use environment variables for API URLs**

```javascript
// Good
const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api/v1';

// Bad
const API_URL = 'http://my-production-server.com/api/v1'; // Hardcoded
```

### 2. Error Handling

- **Always handle errors gracefully**
- **Show user-friendly error messages**
- **Log detailed errors for debugging**

```javascript
try {
  const card = await createCard(cardData);
  showSuccessMessage('Card created successfully');
} catch (error) {
  console.error('Error creating card:', error);
  
  if (error.response?.status === 409) {
    showErrorMessage('Card already exists');
  } else if (error.response?.status === 400) {
    showErrorMessage('Invalid card data. Please check your input.');
  } else {
    showErrorMessage('Failed to create card. Please try again.');
  }
}
```

### 3. Data Validation

- **Validate data on frontend before sending to backend**
- **Use form validation libraries (Formik, React Hook Form, etc.)**
- **Provide real-time validation feedback**

```javascript
const validateCardNumber = (cardNumber) => {
  const regex = /^\d{13,16}$/;
  return regex.test(cardNumber) ? null : 'Card number must be 13-16 digits';
};

const validateExpiryDate = (date) => {
  const expiry = new Date(date);
  const today = new Date();
  
  if (expiry <= today) {
    return 'Expiry date must be in the future';
  }
  return null;
};
```

### 4. State Management

- **Use proper state management (Redux, Zustand, Context)**
- **Cache frequently accessed data**
- **Implement loading states**
- **Handle stale data appropriately**

```javascript
// Example with React Query
import { useQuery, useMutation, useQueryClient } from 'react-query';

const useCards = () => {
  return useQuery('cards', fetchCards, {
    staleTime: 5 * 60 * 1000, // 5 minutes
    cacheTime: 10 * 60 * 1000, // 10 minutes
  });
};

const useCreateCard = () => {
  const queryClient = useQueryClient();
  
  return useMutation(createCard, {
    onSuccess: () => {
      queryClient.invalidateQueries('cards');
    },
  });
};
```

### 5. Performance

- **Use pagination for large datasets** - The API now supports pagination endpoints (see [Pagination](#pagination) section)
- **Use debouncing for search/filter operations**
- **Lazy load components when possible**
- **Memoize expensive computations**

```javascript
import { useMemo, useCallback } from 'react';
import debounce from 'lodash/debounce';

const CardList = ({ cards }) => {
  // Memoize filtered results
  const activeCards = useMemo(() => {
    return cards.filter(card => card.cardStatus === 'CACT');
  }, [cards]);

  // Debounce search
  const handleSearch = useCallback(
    debounce((searchTerm) => {
      // Perform search
    }, 300),
    []
  );

  return <div>{/* Render cards */}</div>;
};
```

### 6. Testing

- **Write unit tests for API service functions**
- **Mock API responses in tests**
- **Test error handling**

```javascript
// Example with Jest
import { createCard } from './cardService';
import axios from 'axios';

jest.mock('axios');

describe('CardService', () => {
  it('should create a card successfully', async () => {
    const mockCard = {
      cardNumber: '1234567890123456',
      cardStatus: 'IACT',
      // ...
    };
    
    axios.post.mockResolvedValue({ data: mockCard });
    
    const result = await createCard(mockCard);
    
    expect(result).toEqual(mockCard);
    expect(axios.post).toHaveBeenCalledWith(
      expect.stringContaining('/cards'),
      mockCard
    );
  });
  
  it('should handle errors when creating a card', async () => {
    const errorResponse = {
      response: {
        status: 409,
        data: { message: 'Card already exists' }
      }
    };
    
    axios.post.mockRejectedValue(errorResponse);
    
    await expect(createCard({})).rejects.toThrow('Card already exists');
  });
});
```

---

## Additional Resources

### Swagger/OpenAPI Documentation

Once the backend server is running, you can access interactive API documentation at:

```
http://localhost:8080/swagger-ui.html
```

This provides a web interface to:
- View all endpoints
- Test API calls directly
- See request/response schemas
- View validation rules

### Postman Collection

Consider creating a Postman collection for easy API testing and sharing with team members.

### TypeScript Interfaces

For TypeScript projects, define interfaces matching the API models:

```typescript
// types.ts
export interface Card {
  cardNumber: string;
  expiryDate: string;
  cardStatus: 'IACT' | 'CACT' | 'DACT';
  cardStatusDescription: string;
  creditLimit: number;
  cashLimit: number;
  availableCreditLimit: number;
  availableCashLimit: number;
  lastUpdateTime: string;
}

export interface CardRequest {
  requestId: number;
  cardNumber: string;
  requestReasonCode: 'ACTI' | 'CDCL';
  requestReasonDescription: string;
  requestStatusCode: 'PEND' | 'APPR' | 'RJCT';
  requestStatusDescription: string;
  remark?: string;
  createdTime: string;
}

export interface CreateCardDTO {
  cardNumber: string;
  expiryDate: string;
  cardStatus: 'IACT' | 'CACT' | 'DACT';
  creditLimit: number;
  cashLimit: number;
  availableCreditLimit: number;
  availableCashLimit: number;
}

export interface CreateCardRequestDTO {
  cardNumber: string;
  requestReasonCode: 'ACTI' | 'CDCL';
  remark?: string;
}

export interface ApproveRequestDTO {
  requestStatusCode: 'APPR' | 'RJCT';
  remark?: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details: string[];
}
```

---

## Contact & Support

For questions or issues regarding the API, please contact the backend development team or refer to the project repository.

**Last Updated:** February 19, 2026
**API Version:** v1
**Document Version:** 1.0.0
