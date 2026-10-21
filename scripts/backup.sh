#!/usr/bin/env bash
set -euo pipefail
mkdir -p backups
mysqldump --single-transaction -h "${DB_HOST:-localhost}" -u "${DB_USER:-hms}" -p"${DB_PASSWORD:?Set DB_PASSWORD}" hms | gzip > "backups/hms-$(date +%Y%m%d-%H%M%S).sql.gz"
find backups -name 'hms-*.sql.gz' -mtime +30 -delete
