# Card Management System (CMS)

A comprehensive Spring Boot JDBC application for managing credit/debit cards and their associated requests.

## Features

- **Card Management**: Create, update, delete, and query cards
- **Request Management**: Handle card-related requests (activation, card close)
- **Status Tracking**: Track card and request statuses
- **Search & Filter**: Search cards by various criteria
- **Security**: Role-based access control (RBAC)
- **API Documentation**: Interactive Swagger UI
- **Database Migrations**: Flyway for version-controlled database schema

## Technology Stack

- **Java 17**
- **Spring Boot 3.5.10**
- **Spring JDBC** (JdbcTemplate)
- **Spring Security**
- **PostgreSQL** database
- **Flyway** migrations
- **Lombok** for reducing boilerplate
- **SpringDoc OpenAPI** for API documentation
- **Maven** for dependency management

## Database Schema

### Lookup Tables
- **CardStatus**: IACT (Inactive), CACT (Active), DACT (Deactivated)
- **CardRequestType**: ACTI (Activation), CDCL (Card Close)
- **RequestStatus**: PEND (Pending), APPR (Approved), RJCT (Rejected)

### Main Tables
- **Card**: Stores card information with credit/cash limits
- **CardRequest**: Stores card-related requests

## Prerequisites

1. **Java 17** or higher
2. **Maven 3.6+**
3. **PostgreSQL 12+**
4. Database: `card_management_system`

## Database Setup

1. Create a PostgreSQL database:
```sql
CREATE DATABASE card_management_system;
```

2. The application will automatically create tables using Flyway migrations on startup.

## Configuration

### Environment Variables

Optionally set:
- `DB_PASSWORD`: PostgreSQL password (default: Malani1963)
- `DB_USERNAME`: PostgreSQL username (default: postgres)

### Application Properties

Main configuration is in `src/main/resources/application.yaml`:
- Database: `jdbc:postgresql://localhost:5432/card_management_system`
- Server port: 8080
- Flyway migration settings
- Logging levels

## Running the Application

### Using Maven

```bash
# Install dependencies
mvn clean install

# Run the application
mvn spring-boot:run
```

### Using JAR

```bash
# Build the JAR
mvn clean package

# Run the JAR
java -jar target/cms-0.0.1-SNAPSHOT.jar
```

## API Documentation

Once running, access:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

## Authentication

HTTP Basic Authentication with two default users:

| Username | Password  | Role  |
|----------|-----------|-------|
| user     | user123   | USER  |
| admin    | admin123  | ADMIN |

### Permissions

- **USER role**: Can view cards and requests, create new requests
- **ADMIN role**: Full access including card creation, request approval/rejection

## API Endpoints

### Card Management (`/api/v1/cards`)

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| GET | `/api/v1/cards` | Get all cards | USER |
| GET | `/api/v1/cards/{cardNumber}` | Get card by number | USER |
| GET | `/api/v1/cards/status/{status}` | Get cards by status | USER |
| GET | `/api/v1/cards/expired` | Get expired cards | USER |
| GET | `/api/v1/cards/expiring?days=30` | Get cards expiring soon | USER |
| POST | `/api/v1/cards` | Create new card | ADMIN |
| PUT | `/api/v1/cards/{cardNumber}` | Update card | ADMIN |
| PATCH | `/api/v1/cards/{cardNumber}/status` | Update card status | ADMIN |
| DELETE | `/api/v1/cards/{cardNumber}` | Delete card | ADMIN |
| GET | `/api/v1/cards/count?status=` | Get card count by status | USER |

### Request Management (`/api/v1/requests`)

| Method | Endpoint | Description | Required Role |
|--------|----------|-------------|---------------|
| GET | `/api/v1/requests` | Get all requests | USER |
| GET | `/api/v1/requests/{requestId}` | Get request by ID | USER |
| GET | `/api/v1/requests/card/{cardNumber}` | Get requests by card | USER |
| GET | `/api/v1/requests/status/{status}` | Get requests by status | USER |
| GET | `/api/v1/requests/type/{type}` | Get requests by type | USER |
| GET | `/api/v1/requests/pending` | Get pending requests | USER |
| POST | `/api/v1/requests` | Create new request | USER |
| PUT | `/api/v1/requests/{requestId}` | Update request | USER |
| POST | `/api/v1/requests/{requestId}/process` | Approve/reject request | ADMIN |
| DELETE | `/api/v1/requests/{requestId}` | Delete request | ADMIN |
| GET | `/api/v1/requests/count?status=` | Get request count | USER |

## Data Model

### Card Status Codes
- `IACT` - Card Inactive (Initial/Pending state)
- `CACT` - Card Active (Normal active state)
- `DACT` - Card Deactivated (Card has been deactivated)

### Request Type Codes
- `ACTI` - Card Activation Request
- `CDCL` - Card Close Request

### Request Status Codes
- `PEND` - Pending (Awaiting approval)
- `APPR` - Approved (Request has been approved)
- `RJCT` - Rejected (Request has been rejected)

## Example API Calls

### Create a Card (Admin only)

```bash
curl -X POST http://localhost:8080/api/v1/cards \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4532015112830366",
    "expiryDate": "2027-12-31",
    "cardStatus": "IACT",
    "creditLimit": 100000.00,
    "cashLimit": 50000.00,
    "availableCreditLimit": 100000.00,
    "availableCashLimit": 50000.00
  }'
```

### Create a Card Request (User or Admin)

```bash
curl -X POST http://localhost:8080/api/v1/requests \
  -u user:user123 \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "4532015112830366",
    "requestReasonCode": "ACTI",
    "remark": "Customer requested card activation"
  }'
```

### Approve a Request (Admin only)

```bash
curl -X POST http://localhost:8080/api/v1/requests/1/process \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "requestStatusCode": "APPR",
    "remark": "Request approved by admin"
  }'
```

### Reject a Request (Admin only)

```bash
curl -X POST http://localhost:8080/api/v1/requests/2/process \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "requestStatusCode": "RJCT",
    "remark": "Insufficient documentation"
  }'
```

## Error Handling

The application provides consistent error responses:

```json
{
  "timestamp": "2026-02-18T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Card not found with cardNumber : '1234567890123456'",
  "path": "/api/v1/cards/1234567890123456"
}
```

## Development

### Building the Project

```bash
mvn clean install
```

### Running Tests

```bash
mvn test
```

### Code Style

The project uses Lombok to reduce boilerplate code. Make sure you have Lombok plugin installed in your IDE.

## Architecture

The project follows a layered architecture:

- **Controller Layer**: REST API endpoints
- **Service Layer**: Business logic
- **Repository Layer**: Database access using JdbcTemplate
- **Model Layer**: Domain objects (POJOs)
- **DTO Layer**: Data transfer objects for API

## Production Considerations

Before deploying to production:

1. **Replace in-memory authentication** with database or OAuth2
2. **Use environment-specific profiles** (dev, test, prod)
3. **Enable HTTPS** and update CSRF settings
4. **Configure proper logging** with log aggregation
5. **Set up monitoring** and health checks
6. **Use connection pooling** (HikariCP is configured)
7. **Enable database backups**
8. **Review security settings** in SecurityConfig
9. **Use strong passwords** and rotate credentials regularly
10. **Configure proper transaction management**

## Database Schema Changes

To add new migrations:
1. Create a new file in `src/main/resources/db/migration/`
2. Follow naming convention: `V{version}__description.sql`
3. Example: `V2__add_customer_info.sql`

## License

This project is for demonstration purposes.

## Support

For issues and questions, please refer to the API documentation at `/swagger-ui.html`.
