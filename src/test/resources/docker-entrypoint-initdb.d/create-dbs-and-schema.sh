#!/bin/bash
set -e

for n in $(seq -w 1 10); do
DBNAME=hibernate_db$n

createdb --username "$POSTGRES_USER" "$DBNAME"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$DBNAME" <<EOSQL
CREATE TABLE Gift(id bigint primary key, name text);
CREATE sequence gift_id_seq;
EOSQL

done
