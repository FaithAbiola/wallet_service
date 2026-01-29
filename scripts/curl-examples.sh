#!/bin/bash
# cURL examples for the Wallet Service API.
# Start the app first: ./mvnw spring-boot:run
# Usage: ./scripts/curl-examples.sh [BASE_URL]

BASE_URL="${1:-http://localhost:8080}"

echo "=== 1. Create two wallets ==="
A=$(curl -s -X POST "$BASE_URL/wallets")
echo "Wallet A: $A"
A_ID=$(echo "$A" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')

B=$(curl -s -X POST "$BASE_URL/wallets")
echo "Wallet B: $B"
B_ID=$(echo "$B" | sed -n 's/.*"id":\([0-9]*\).*/\1/p')

echo ""
echo "=== 2. Get wallet A ==="
curl -s "$BASE_URL/wallets/$A_ID"
echo ""

echo ""
echo "=== 3. Credit wallet A (500) ==="
curl -s -X POST "$BASE_URL/transactions" -H "Content-Type: application/json" \
  -d "{\"walletId\":$A_ID,\"amount\":500,\"type\":\"CREDIT\",\"idempotencyKey\":\"credit-a-001\"}"
echo ""

echo ""
echo "=== 4. Debit wallet A (200) ==="
curl -s -X POST "$BASE_URL/transactions" -H "Content-Type: application/json" \
  -d "{\"walletId\":$A_ID,\"amount\":200,\"type\":\"DEBIT\",\"idempotencyKey\":\"debit-a-001\"}"
echo ""

echo ""
echo "=== 5. Idempotency: same key again (400) ==="
curl -s -X POST "$BASE_URL/transactions" -H "Content-Type: application/json" \
  -d "{\"walletId\":$A_ID,\"amount\":200,\"type\":\"DEBIT\",\"idempotencyKey\":\"debit-a-001\"}"
echo ""

echo ""
echo "=== 6. Credit B (1000), then transfer 300 from B to A ==="
curl -s -X POST "$BASE_URL/transactions" -H "Content-Type: application/json" \
  -d "{\"walletId\":$B_ID,\"amount\":1000,\"type\":\"CREDIT\",\"idempotencyKey\":\"credit-b-001\"}"
echo ""
curl -s -X POST "$BASE_URL/transactions/transfer" -H "Content-Type: application/json" \
  -d "{\"fromWalletId\":$B_ID,\"toWalletId\":$A_ID,\"amount\":300}"
echo ""

echo ""
echo "=== 7. Reject debit (insufficient balance) ==="
curl -s -X POST "$BASE_URL/transactions" -H "Content-Type: application/json" \
  -d "{\"walletId\":$A_ID,\"amount\":99999,\"type\":\"DEBIT\",\"idempotencyKey\":\"debit-over-001\"}"
echo ""

echo ""
echo "=== Final balances ==="
echo "Wallet A: $(curl -s "$BASE_URL/wallets/$A_ID")"
echo "Wallet B: $(curl -s "$BASE_URL/wallets/$B_ID")"
