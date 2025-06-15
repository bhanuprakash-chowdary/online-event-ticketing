#!/bin/bash
set -e

# Enable citus extension
psql -U postgres -c "CREATE EXTENSION IF NOT EXISTS citus;"

# Wait until both workers are ready
until pg_isready -h citus-worker1 -p 5432 -U postgres && pg_isready -h citus-worker2 -p 5432 -U postgres; do
  echo "Waiting for Citus workers to be ready..."
  sleep 2
done

# Run all SQL setup in one go
psql -U postgres <<-'EOSQL'
  -- Set coordinator and add workers
  SELECT citus_set_coordinator_host('citus-coordinator', 5432);
  SELECT citus_add_node('citus-worker1', 5432);
  SELECT citus_add_node('citus-worker2', 5432);

  -- Debug: verify nodes
  SELECT * FROM pg_dist_node;

  -- Create event sequence
  CREATE SEQUENCE IF NOT EXISTS event_seq INCREMENT BY 1;
  SELECT create_distributed_table('event_seq', 'id') ON CONFLICT DO NOTHING;

  -- Create event table
  CREATE TABLE IF NOT EXISTS event (
      id INTEGER PRIMARY KEY DEFAULT nextval('event_seq'),
      name VARCHAR(255) NOT NULL,
      available_tickets INTEGER NOT NULL CHECK (available_tickets >= 0)
  );
  SELECT create_distributed_table('event', 'id') ON CONFLICT DO NOTHING;

  -- Insert sample data if empty
  DO $$
  BEGIN
      IF NOT EXISTS (SELECT 1 FROM event) THEN
          INSERT INTO event (name, available_tickets) VALUES 
              ('Rock Concert', 1000),
              ('Jazz Night', 500);
      END IF;
  END;
  $$;
EOSQL
