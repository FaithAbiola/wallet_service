#!/bin/bash

echo "Setting up PostgreSQL database for Wallet Service..."

if ! command -v psql &> /dev/null; then
  echo "❌ PostgreSQL is not installed."
  exit 1
fi

if ! pg_isready -q; then
  echo "❌ PostgreSQL is not running."
  exit 1
fi

echo "Creating database 'wallet_service'..."
createdb -U postgres wallet_service 2>/dev/null || echo "Database 'wallet_service' already exists or creation failed."

echo "Testing database connection..."
psql -U postgres -d wallet_service -c "SELECT 1;" > /dev/null 2>&1

if [ $? -eq 0 ]; then
  echo "✅ PostgreSQL setup complete!"
else
  echo "❌ Failed to connect to PostgreSQL."
  exit 1
fi
