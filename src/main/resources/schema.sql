-- Wallets: balance in minor units (integer)
CREATE TABLE IF NOT EXISTS wallets (
    id           BIGSERIAL    PRIMARY KEY,
    balance      BIGINT       NOT NULL DEFAULT 0,
    description  VARCHAR(255),
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Migration scripts for existing databases
-- Uncomment and run these if updating an existing database:

-- Add description and created_at columns to existing wallets table
-- ALTER TABLE wallets ADD COLUMN IF NOT EXISTS description VARCHAR(255);
-- ALTER TABLE wallets ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add created_at column to existing wallet_transactions table
-- ALTER TABLE wallet_transactions ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Credit/debit transactions; idempotency via unique idempotency_key
CREATE TABLE IF NOT EXISTS wallet_transactions (
    id              BIGSERIAL    PRIMARY KEY,
    wallet_id       BIGINT       NOT NULL,
    amount          BIGINT       NOT NULL,
    type            VARCHAR(20)  NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (wallet_id) REFERENCES wallets(id)
);

CREATE INDEX IF NOT EXISTS idx_wallet_transactions_wallet ON wallet_transactions(wallet_id);
CREATE INDEX IF NOT EXISTS idx_wallet_transactions_idempotency ON wallet_transactions(idempotency_key);

-- Transfer transactions; idempotency via unique idempotency_key
CREATE TABLE IF NOT EXISTS transfer_transactions (
    id              BIGSERIAL    PRIMARY KEY,
    from_wallet_id  BIGINT       NOT NULL,
    to_wallet_id    BIGINT       NOT NULL,
    amount          BIGINT       NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_wallet_id) REFERENCES wallets(id),
    FOREIGN KEY (to_wallet_id) REFERENCES wallets(id)
);

CREATE INDEX IF NOT EXISTS idx_transfer_transactions_from_wallet ON transfer_transactions(from_wallet_id);
CREATE INDEX IF NOT EXISTS idx_transfer_transactions_to_wallet ON transfer_transactions(to_wallet_id);
CREATE INDEX IF NOT EXISTS idx_transfer_transactions_idempotency ON transfer_transactions(idempotency_key);

-- Cached responses for idempotent transaction requests
CREATE TABLE IF NOT EXISTS transaction_idempotency_responses (
    id               BIGSERIAL    PRIMARY KEY,
    idempotency_key  VARCHAR(255) NOT NULL UNIQUE,
    transaction_id   BIGINT       NOT NULL,
    wallet_id        BIGINT       NOT NULL,
    balance          BIGINT       NOT NULL,
    timestamp        TIMESTAMP    NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Cached responses for idempotent transfer requests
CREATE TABLE IF NOT EXISTS transfer_idempotency_responses (
    id               BIGSERIAL    PRIMARY KEY,
    idempotency_key  VARCHAR(255) NOT NULL UNIQUE,
    transfer_id      BIGINT       NOT NULL,
    from_wallet_id   BIGINT       NOT NULL,
    from_balance     BIGINT       NOT NULL,
    to_wallet_id     BIGINT       NOT NULL,
    to_balance       BIGINT       NOT NULL,
    timestamp        TIMESTAMP    NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
