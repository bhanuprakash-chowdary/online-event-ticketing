#!/bin/bash
psql -U postgres -c "CREATE ROLE replicator WITH REPLICATION LOGIN PASSWORD 'replicator';"
psql -U postgres -c "ALTER SYSTEM SET wal_level = replica;"
psql -U postgres -c "ALTER SYSTEM SET max_wal_senders = 10;"
psql -U postgres -c "ALTER SYSTEM SET wal_keep_size = 64;"
pg_ctl reload