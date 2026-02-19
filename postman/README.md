# CMS API Postman Collection

This directory contains Postman collection and environment files for testing the Card Management System (CMS) API.

## Files

- **CMS_API_Collection.json** - Complete API collection with all endpoints
- **CMS_API_Environment.json** - Environment variables configuration
- **README.md** - This documentation file

## Quick Start

### 1. Import into Postman

1. Open Postman
2. Click **Import** button
3. Select both files:
   - `CMS_API_Collection.json`
   - `CMS_API_Environment.json`
4. Click **Import**

### 2. Select Environment

1. In Postman, click the environment dropdown (top-right)
2. Select **CMS API Environment**

### 3. Start Testing

1. Ensure the Spring Boot application is running on `http://localhost:8080`
2. Select any request from the collection
3. Click **Send**

## API Overview

### Authentication
**No authentication required** - All endpoints are publicly accessible for development/testing.

### Architecture
- **Loosely Coupled Design** - Service layer depends on repository interfaces
- **No Transaction Management** - Each database operation commits immediately
- **Comprehensive Logging** - All requests are logged with detailed information

### Base URL
```
http://localhost:8080
```

## Available Endpoints

### Card Management (11 endpoints)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/cards` | Get all cards |
| GET | `/api/v1/cards/{cardNumber}` | Get card by card number |
| GET | `/api/v1/cards/status/{status}` | Get cards by status |
| GET | `/api/v1/cards/expired` | Get expired cards |
| GET | `/api/v1/cards/expiring?days={days}` | Get cards expiring soon |
| GET | `/api/v1/cards/count?status={status}` | Get card count by status |
| POST | `/api/v1/cards` | Create new card |
| PUT | `/api/v1/cards/{cardNumber}` | Update card |
| PATCH | `/api/v1/cards/{cardNumber}/status?status={status}` | Update card status only |
| DELETE | `/api/v1/cards/{cardNumber}` | Delete card |

### Card Request Management (12 endpoints)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/requests` | Get all requests |
| GET | `/api/v1/requests/{requestId}` | Get request by ID |
| GET | `/api/v1/requests/card/{cardNumber}` | Get requests by card number |
| GET | `/api/v1/requests/status/{status}` | Get requests by status |
| GET | `/api/v1/requests/type/{type}` | Get requests by type |
| GET | `/api/v1/requests/pending` | Get pending requests |
| GET | `/api/v1/requests/count?status={status}` | Get request count by status |
| POST | `/api/v1/requests` | Create new request |
| PUT | `/api/v1/requests/{requestId}` | Update request |
| POST | `/api/v1/requests/{requestId}/process` | Approve/reject request |
| DELETE | `/api/v1/requests/{requestId}` | Delete request |

## Status Codes

### Card Status
- **IACT** - Inactive (newly created cards)
- **CACT** - Active (currently usable cards)
- **DACT** - Deactivated (permanently disabled)

### Request Status
- **PEND** - Pending (awaiting approval)
- **APPR** - Approved (accepted requests)
- **RJCT** - Rejected (denied requests)

### Request Types
- **ACTI** - Card Activation Request
- **CDCL** - Card Close Request

## Sample Requests

### Create a Card
```json
POST /api/v1/cards
Content-Type: application/json

{
  "cardNumber": "4532015112830366",
  "expiryDate": "2027-12-31",
  "cardStatus": "IACT",
  "creditLimit": 100000.00,
  "cashLimit": 50000.00,
  "availableCreditLimit": 100000.00,
  "availableCashLimit": 50000.00
}
```

### Create a Card Request
```json
POST /api/v1/requests
Content-Type: application/json

{
  "cardNumber": "4532015112830366",
  "requestReasonCode": "ACTI",
  "remark": "Customer requested card activation"
}
```

### Approve a Request
```json
POST /api/v1/requests/1/process
Content-Type: application/json

{
  "requestStatusCode": "APPR",
  "remark": "Request approved by admin"
}
```

## Environment Variables

The environment file includes:
- **base_url** - `http://localhost:8080` (can be changed for different environments)

## Troubleshooting

### Connection Refused
- Ensure the Spring Boot application is running
- Check if PostgreSQL database is accessible
- Verify the port is 8080

### 404 Not Found
- Verify the endpoint URL is correct
- Check the API path starts with `/api/v1/`

### 500 Internal Server Error
- Check application logs in the console
- Verify database connection
- Ensure all required fields are provided in request body

### Database Errors
- Ensure PostgreSQL is running on `localhost:5432`
- Verify database `cms` exists
- Check Flyway migrations have executed successfully

## Logging

The application provides comprehensive logging:
- All incoming requests with headers and authentication info
- Response status and duration
- Service layer operations
- Database queries (SQL)
- Error details with stack traces

Check the application console for detailed logs.

## Support

For issues or questions:
1. Check application logs
2. Verify database connectivity
3. Review API documentation in Swagger UI: `http://localhost:8080/swagger-ui.html`

## Version
- Collection Version: 1.0
- API Version: v1
- Last Updated: 2026-02-19
