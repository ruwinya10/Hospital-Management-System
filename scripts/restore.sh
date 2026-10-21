#!/usr/bin/env bash
set -euo pipefail
[ "$#" -eq 1 ] || { echo "Usage: ./restore.sh backups/file.sql.gz"; exit 1; }
gunzip -c "$1" | mysql -h "${DB_HOST:-localhost}" -u "${DB_USER:-hms}" -p"${DB_PASSWORD:?Set DB_PASSWORD}" hms
