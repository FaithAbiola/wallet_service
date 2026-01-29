# Wallet Service

REST API for wallet and transaction operations. **Wallet** and **Transaction** are separated into dedicated services and controllers.

## Features

- **Wallet:** Create wallet, get wallet by id.
- **Transaction:** Credit/debit (idempotent via `idempotencyKey`), transfer between wallets. Amounts in **minor units** (integer).

## Requirements

- Java 17+
- Maven (or `./mvnw`)
- PostgreSQL (running on localhost:5432)

## Setup

### Database Setup
Run the automated setup script:
```bash
./setup-postgres.sh
```

Or manually create the database:
```sql
-- Connect to PostgreSQL and run:
CREATE DATABASE wallet_service;

-- Default credentials (update in application.properties if needed):
-- User: postgres
-- Password: password
```

### Run Application
```bash
cd wallet_service
./mvnw clean install
./mvnw spring-boot:run
```

API: `http://localhost:8080`

### API Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

### Database Management
- **pgAdmin**: Connect to PostgreSQL at `localhost:5432/wallet_service`
- Tables are auto-created on startup

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
# Returns: {"id":1,"balance":100,"description":"My savings wallet","createdAt":"2024-01-28T15:30:00"}
curl -s -X POST http://localhost:8080/wallets \
  -H "Content-Type: application/json" \
  -d '{"initialBalance":100,"description":"My savings wallet"}'

# Create wallet with just initial balance (description optional)
# Returns: {"id":2,"balance":50,"description":null,"createdAt":"2024-01-28T15:31:00"}
curl -s -X POST http://localhost:8080/wallets \
  -H "Content-Type: application/json" \
  -d '{"initialBalance":50}'

# Get wallet (use id from create)
curl -s http://localhost:8080/wallets/1

# Credit (returns { "transactionId": 1, "walletId": 1, "balance": 500, "timestamp": "..." })
curl -s -X POST http://localhost:8080/transactions -H "Content-Type: application/json" \
  -d '{"walletId":1,"amount":500,"type":"CREDIT","idempotencyKey":"credit-1"}'

# Debit (returns { "transactionId": 2, "walletId": 1, "balance": 300, "timestamp": "..." })
curl -s -X POST http://localhost:8080/transactions -H "Content-Type: application/json" \
  -d '{"walletId":1,"amount":200,"type":"DEBIT","idempotencyKey":"debit-1"}'

# Transfer (returns { "transferId": 1, "fromWalletId": 1, "fromBalance": 200, "toWalletId": 2, "toBalance": 300, "timestamp": "..." })
curl -s -X POST http://localhost:8080/transactions/transfer -H "Content-Type: application/json" \
  -d '{"fromWalletId":1,"toWalletId":2,"amount":100,"idempotencyKey":"transfer-1"}'
```

## Testing with Postman

1. Start the application: `./mvnw spring-boot:run`
2. Open **Swagger UI**: http://localhost:8080/swagger-ui.html
3. Click **"Export"** → **"Download OpenAPI spec"** (JSON/YAML)
4. In Postman: **Import** → **Upload Files** → select the downloaded spec
5. Test all endpoints directly in Postman
6. Export individual requests as cURL commands: **Right-click request** → **Copy** → **Copy as cURL**

Run `./scripts/curl-examples.sh` (app must be running).

## Tests

```bash
./mvnw test
```

## Project Layout

```
src/main/java/com/faith/wallet_service/
├── WalletServiceApplication.java
├── controller/
│   ├── WalletController.java
│   └── TransactionController.java
├── dto/                         # CreateWalletRequest, TransactionRequest, TransferRequest, TransactionResponse, TransferResponse
├── entity/                      # Wallet, WalletTransaction, TransferTransaction, TransactionIdempotencyResponse, TransferIdempotencyResponse
├── entity/                      # Wallet, WalletTransaction
├── enums/                       # TransactionType (CREDIT, DEBIT)
├── exception/
├── repository/                  # WalletRepository, TransactionRepository
├── service/                     # WalletService, TransactionService (interfaces)
└── serviceImpl/
    ├── WalletServiceImpl.java
    └── TransactionServiceImpl.java
```
