#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
export DB_USER="${DB_USER:-postgres}"
export DB_PASSWORD="${DB_PASSWORD:-postgres}"
export DB_URL="${DB_URL:-jdbc:postgresql://localhost:5432/students_db}"
export DEMO_DATA_ENABLED="${DEMO_DATA_ENABLED:-true}"
export JWT_SECRET="${JWT_SECRET:-local-development-secret-replace-before-deployment-1234567890}"
if ! command -v psql >/dev/null 2>&1; then
  echo "PostgreSQL не найден. Установите и запустите локальный PostgreSQL."
  exit 1
fi
if ! command -v pg_isready >/dev/null 2>&1; then
  echo "Не найдена утилита pg_isready. Проверьте установку PostgreSQL."
  exit 1
fi
if ! pg_isready -h localhost -p 5432 -q; then
  echo "PostgreSQL не отвечает на localhost:5432."
  exit 1
fi
export PGPASSWORD="$DB_PASSWORD"
if ! psql -h localhost -p 5432 -U "$DB_USER" -d postgres -tAc "SELECT 1" >/dev/null; then
  echo "Нет подключения к PostgreSQL под пользователем $DB_USER."
  exit 1
fi
if ! psql -h localhost -p 5432 -U "$DB_USER" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = 'students_db'" | grep -qx 1; then
  createdb -h localhost -p 5432 -U "$DB_USER" students_db
fi
unset PGPASSWORD
./mvnw spring-boot:run
