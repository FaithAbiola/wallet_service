# Wallet Service

A production-ready REST API for wallet and transaction operations built with Spring Boot. This project implements a comprehensive wallet service that handles money transfers, balance management, and transaction processing with enterprise-grade features.

## Requirements

This implementation fully satisfies the requirements:

- ✅ **Create Wallet**: `POST /wallets` with initial balance
- ✅ **Credit/Debit Operations**: `POST /transactions` with idempotency
- ✅ **Money Transfers**: `POST /transactions/transfer` (atomic)
- ✅ **Balance Validation**: Prevents negative balances
- ✅ **Idempotency**: Duplicate transaction prevention via keys
- ✅ **Minor Units**: All amounts stored as integers (Long/BIGINT)
- ✅ **Java + SQL**: Spring Boot with PostgreSQL/H2 support
- ✅ **Clean Code**: Well-structured, documented, and tested

## Features

- **Wallet Management**: Create wallets with initial balance and optional description, retrieve wallet details with audit timestamps
- **Transaction Processing**: Credit/debit operations with full idempotency support and duplicate transaction prevention
- **Money Transfers**: Atomic transfers between wallets with idempotency and balance validation
- **Data Integrity**: Comprehensive validation, transaction management, and proper error handling
- **API Documentation**: Interactive Swagger UI and OpenAPI specification export
- **Database Support**: PostgreSQL for production, H2 for testing with automatic schema creation

## Key Improvements

- ✅ **Idempotency**: Cached responses prevent duplicate operations
- ✅ **Atomic Transactions**: Database transactions ensure consistency
- ✅ **Input Validation**: Bean validation with meaningful error messages
- ✅ **Audit Trail**: Timestamps and transaction IDs for all operations
- ✅ **Error Handling**: Global exception handler with proper HTTP status codes
- ✅ **API Documentation**: Swagger UI for easy testing and integration
- ✅ **Test Coverage**: Comprehensive integration tests (6/6 passing)

## Requirements

- Java 17+
- Maven (or `./mvnw`)
- PostgreSQL **or** H2 (H2 included for testing, no setup required)

## Setup

### Quick Start (H2 Database - No PostgreSQL Setup Required)
```bash
cd wallet_service
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
```

### Full Setup (PostgreSQL Database)
```bash
# 1. Setup PostgreSQL database
./setup-postgres.sh

# 2. Run the application
./mvnw spring-boot:run
```

### Manual PostgreSQL Setup (Alternative)
```sql
-- Connect to PostgreSQL and run:
CREATE DATABASE wallet_service;

-- Default credentials (update in application.properties if needed):
-- User: postgres
-- Password: password
```

API: `http://localhost:8080`

### API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

### Database Management

- **PostgreSQL**: Connect to `localhost:5432/wallet_service` in pgAdmin
- **H2 Console** (when using test profile): http://localhost:8080/h2-console
- Tables are auto-created on startup

## Hosted Version (For External Testers)

If you don’t want to run the project locally, you can test the live deployment here:

**Base URL:**  
https://walletservice-production-56f5.up.railway.app

**Swagger UI (Live):**  
 https://walletservice-production-56f5.up.railway.app/swagger-ui.html

**OpenAPI JSON (Live):**  
https://walletservice-production-56f5.up.railway.app/v3/api-docs

## API Overview

| Method | Path | Description |
|--------|------|-------------|
| POST | `/wallets` | Create wallet with initial balance (201, returns `Wallet` with id, balance, description, createdAt) |
| GET | `/wallets/:id` | Get wallet details |
| POST | `/transactions` | Credit or debit → returns `TransactionResponse` (`transactionId`, `walletId`, `balance`, `timestamp`) |
| POST | `/transactions/transfer` | Transfer → returns `TransferResponse` (`transferId`, `fromWalletId`, `fromBalance`, `toWalletId`, `toBalance`, `timestamp`) |

## cURL Examples

```bash
# Create wallet with initial balance and optional description
# Returns: {"status":"SUCCESS","message":"Wallet created successfully","data":{"id":1,"balance":100,"description":"My savings wallet","createdAt":"2024-01-28T15:30:00"}}
curl -s -X POST http://localhost:8080/wallets \
  -H "Content-Type: application/json" \
  -d '{"initialBalance":100,"description":"My savings wallet"}'

# Create wallet with just initial balance (description optional)
# Returns: {"status":"SUCCESS","message":"Wallet created successfully","data":{"id":2,"balance":50,"description":null,"createdAt":"2024-01-28T15:31:00"}}
curl -s -X POST http://localhost:8080/wallets \
  -H "Content-Type: application/json" \
  -d '{"initialBalance":50}'

# Get wallet (use id from create)
# Returns: {"status":"SUCCESS","message":"Wallet retrieved successfully","data":{...}}
curl -s http://localhost:8080/wallets/1

# Credit (returns {"status":"SUCCESS","message":"Transaction completed successfully","data":{"transactionId":1,"walletId":1,"balance":500,"timestamp":"..."}})
curl -s -X POST http://localhost:8080/transactions -H "Content-Type: application/json" \
  -d '{"walletId":1,"amount":500,"type":"CREDIT","idempotencyKey":"credit-1"}'

# Debit (returns {"status":"SUCCESS","message":"Transaction completed successfully","data":{"transactionId":2,"walletId":1,"balance":300,"timestamp":"..."}})
curl -s -X POST http://localhost:8080/transactions -H "Content-Type: application/json" \
  -d '{"walletId":1,"amount":200,"type":"DEBIT","idempotencyKey":"debit-1"}'

# Transfer (returns {"status":"SUCCESS","message":"Transfer completed successfully","data":{"transferId":1,"fromWalletId":1,"fromBalance":200,"toWalletId":2,"toBalance":300,"timestamp":"..."}})
curl -s -X POST http://localhost:8080/transactions/transfer -H "Content-Type: application/json" \
  -d '{"fromWalletId":1,"toWalletId":2,"amount":100,"idempotencyKey":"transfer-1"}'
```

## Testing with Postman

1. Start the application: `./mvnw spring-boot:run`
2. Open the **OpenAPI JSON** in your browser: http://localhost:8080/v3/api-docs
3. Press **Command/Ctrl + S** to save it as a `.json` file
4. In Postman: **Import** → **Upload Files** → select the downloaded JSON file
5. Test all endpoints directly in Postman
6. Export individual requests as cURL commands: **Right-click request** → **Copy** → **Copy as cURL**


## Tests

Run comprehensive integration tests:

```bash
./mvnw test
```

**Test Coverage:**
- ✅ Wallet creation with validation
- ✅ Transaction credit/debit operations
- ✅ Idempotency (duplicate prevention)
- ✅ Insufficient balance validation
- ✅ Atomic money transfers
- ✅ Error handling and edge cases

**All 6 tests pass successfully!**

## Troubleshooting

### PostgreSQL Connection Issues
If you get database connection errors:

```bash
# Check if PostgreSQL is running
brew services list | grep postgresql

# Start PostgreSQL if needed
brew services start postgresql

# Reset database
brew services stop postgresql
brew services start postgresql
./setup-postgres.sh
```

### Use H2 for Testing (No PostgreSQL Required)
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
```

### API Returns 500 Errors
- Ensure database is properly set up
- Check application logs for detailed error messages
- Verify PostgreSQL credentials in `application.properties`

### Tests Fail
```bash
# Clean and rerun tests
./mvnw clean test

```

## Project Layout

```
src/main/java/com/faith/wallet_service/
├── WalletServiceApplication.java
├── commons/                     # ResultWrapper (shared utilities)
├── config/                      # WebConfig (CORS configuration)
├── controller/                  # WalletController, TransactionController
├── dto/                         # CreateWalletRequest, TransactionRequest, TransferRequest, Responses
├── entity/                      # Wallet, WalletTransaction, TransferTransaction, Idempotency responses
├── enums/                       # TransactionType (CREDIT, DEBIT)
├── exception/                   # Custom exceptions and GlobalExceptionHandler
├── repository/                  # JPA repositories for all entities
├── service/                     # Service interfaces
└── serviceImpl/                 # Service implementations
```

## Key Technologies

- **Spring Boot 4.0.2**: Modern framework with auto-configuration
- **PostgreSQL/H2**: Production and testing databases
- **Spring Data JPA**: ORM and repository pattern
- **Spring Validation**: Input validation and error handling
- **SpringDoc OpenAPI**: API documentation and Swagger UI
- **Lombok**: Boilerplate code reduction
- **Maven**: Dependency management and build tool
