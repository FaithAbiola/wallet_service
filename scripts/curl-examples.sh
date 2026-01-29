#!/bin/bash
# cURL examples for the Wallet Service API.
# Start the app first: ./mvnw spring-boot:run (with H2) or setup PostgreSQL first
# Usage: ./scripts/curl-examples.sh [BASE_URL]

BASE_URL="${1:-http://localhost:8080}"

echo "=== 1. Create two wallets ==="
A=$(curl -s -X POST "$BASE_URL/wallets" -H "Content-Type: application/json" \
  -d '{"initialBalance":1000,"description":"Wallet A"}')
echo "Wallet A: $A"
A_ID=$(echo "$A" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')

B=$(curl -s -X POST "$BASE_URL/wallets" -H "Content-Type: application/json" \
  -d '{"initialBalance":500,"description":"Wallet B"}')
echo "Wallet B: $B"
B_ID=$(echo "$B" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')

echo ""
echo "=== 2. Get wallet A ==="
curl -s "$BASE_URL/wallets/$A_ID"
echo ""

echo ""
echo "=== 3. Credit wallet A (500) ==="
curl -s -X POST "$BASE_URL/transactions" -H "Content-Type: application/json" \
  -d "{\"walletId\":$A_ID,\"amount\":500,\"type\":\"CREDIT\",\"idempotencyKey\":\"b9baf0d1-4c87-4f65-9f9e-4e3b9fd2a5e2\"}"
echo ""

echo ""
echo "=== 4. Debit wallet A (200) ==="
curl -s -X POST "$BASE_URL/transactions" -H "Content-Type: application/json" \
  -d "{\"walletId\":$A_ID,\"amount\":200,\"type\":\"DEBIT\",\"idempotencyKey\":\"7e2c1f44-8f0a-4c6f-9c3a-92c7c7d8f4a1\"}"
echo ""

echo ""
echo "=== 5. Idempotency: same key again (400) ==="
curl -s -X POST "$BASE_URL/transactions" -H "Content-Type: application/json" \
  -d "{\"walletId\":$A_ID,\"amount\":200,\"type\":\"DEBIT\",\"idempotencyKey\":\"7e2c1f44-8f0a-4c6f-9c3a-92c7c7d8f4a1\"}"
echo ""

echo ""
echo "=== 6. Transfer 300 from B to A ==="
curl -s -X POST "$BASE_URL/transactions/transfer" -H "Content-Type: application/json" \
  -d "{\"fromWalletId\":$B_ID,\"toWalletId\":$A_ID,\"amount\":300,\"idempotencyKey\":\"e4f8a2d1-3b6c-4e7a-9f5d-2c8b1a7e6d90\"}"
echo ""

echo ""
echo "=== 7. Reject debit (insufficient balance) ==="
curl -s -X POST "$BASE_URL/transactions" -H "Content-Type: application/json" \
  -d "{\"walletId\":$A_ID,\"amount\":99999,\"type\":\"DEBIT\",\"idempotencyKey\":\"1a2f9c4b-8d6e-4a3f-9b7c-5e1d0c2a8f44\"}"
echo ""

echo ""
echo "=== Final balances ==="
echo "Expected: Wallet A = 1600 (1000 + 500 credit + 300 transfer - 200 debit)"
echo "Expected: Wallet B = 200 (500 - 300 transfer)"
echo "Wallet A: $(curl -s "$BASE_URL/wallets/$A_ID")"
echo "Wallet B: $(curl -s "$BASE_URL/wallets/$B_ID")"

