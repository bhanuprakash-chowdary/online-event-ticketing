#!/bin/bash
set -e

# Step 1: Create the oetp database if it doesn't exist
psql -U postgres -d postgres -tc "SELECT 1 FROM pg_database WHERE datname = 'oetp'" | grep -q 1 || \
psql -U postgres -d postgres -c "CREATE DATABASE oetp;"

# Step 2: Enable citus extension in oetp database
psql -U postgres -d oetp -c "CREATE EXTENSION IF NOT EXISTS citus;"

# Step 3: Wait until all workers are ready
until pg_isready -h citus-worker1 -p 5432 -U postgres && \
      pg_isready -h citus-worker2 -p 5432 -U postgres && \
      pg_isready -h citus-worker3 -p 5432 -U postgres && \
      pg_isready -h citus-worker4 -p 5432 -U postgres; do
    echo "Waiting for Citus workers to be ready..."
    sleep 2
done

# Step 4: Run schema and data setup in oetp
psql -U postgres -d oetp <<-'EOSQL'
  -- Register coordinator and workers
  SELECT citus_set_coordinator_host('citus-coordinator', 5432);
  SELECT citus_add_node('citus-worker1', 5432);
  SELECT citus_add_node('citus-worker2', 5432);
  SELECT citus_add_node('citus-worker3', 5432);
  SELECT citus_add_node('citus-worker4', 5432);

  -- Replication config
  ALTER SYSTEM SET citus.shard_replication_factor = 2;
  SELECT pg_reload_conf();

  -- Debug: view nodes
  SELECT * FROM pg_dist_node;

  -- Sequences
  CREATE SEQUENCE IF NOT EXISTS user_seq INCREMENT BY 1;
  CREATE SEQUENCE IF NOT EXISTS event_seq INCREMENT BY 1;
  CREATE SEQUENCE IF NOT EXISTS booking_seq INCREMENT BY 1;

  -- Tables
  CREATE TABLE IF NOT EXISTS app_user (
      id BIGINT PRIMARY KEY DEFAULT nextval('user_seq'),
      email VARCHAR(255) NOT NULL UNIQUE,
      password VARCHAR(255) NOT NULL,
      role VARCHAR(50) NOT NULL
  );
  SELECT create_distributed_table('app_user', 'id');

  CREATE TABLE IF NOT EXISTS event (
      id INTEGER PRIMARY KEY DEFAULT nextval('event_seq'),
      name VARCHAR(255) NOT NULL,
      available_tickets INTEGER NOT NULL CHECK (available_tickets >= 0),
      event_date TIMESTAMP NOT NULL,
      location VARCHAR(255),
      category VARCHAR(100)
  );
  SELECT create_distributed_table('event', 'id');

  CREATE TABLE IF NOT EXISTS booking (
      id BIGINT PRIMARY KEY DEFAULT nextval('booking_seq'),
      user_email VARCHAR(255) NOT NULL,
      event_id INTEGER NOT NULL REFERENCES event(id),
      quantity INTEGER NOT NULL CHECK (quantity > 0),
      booking_time TIMESTAMP NOT NULL,
      CONSTRAINT unique_booking UNIQUE (user_email, event_id)
  );
  SELECT create_distributed_table('booking', 'event_id', colocate_with => 'event');

  -- Sample users (bcrypt password: "password")
  DO $$
  BEGIN
      IF NOT EXISTS (SELECT 1 FROM app_user) THEN
          INSERT INTO app_user (email, password, role)
          VALUES
              ('user@example.com', '$2a$10$WJIt2tGozZH2f6lrxjkzQuUMxyZc.MFv4SnBaA9KWe6W7MP6X3ZQ6', 'USER'),
              ('admin@example.com', '$2a$10$WJIt2tGozZH2f6lrxjkzQuUMxyZc.MFv4SnBaA9KWe6W7MP6X3ZQ6', 'ADMIN');
      END IF;
  END;
  $$;

  -- Sample events
  DO $$
  BEGIN
      IF NOT EXISTS (SELECT 1 FROM event) THEN
          INSERT INTO event (name, available_tickets, event_date, location, category)
          VALUES
              ('Rock Concert', 1000, '2025-11-15 14:30:00', 'New York', 'Music'),
              ('Jazz Night', 500, '2025-12-01 09:00:00', 'Chicago', 'Music');
      END IF;
  END;
  $$;

  -- Indexes
  CREATE INDEX IF NOT EXISTS idx_event_name ON event (name);
  CREATE INDEX IF NOT EXISTS idx_event_category ON event (category);
  CREATE INDEX IF NOT EXISTS idx_event_location ON event (location);
  CREATE INDEX IF NOT EXISTS idx_event_date ON event (event_date);
  CREATE INDEX IF NOT EXISTS idx_booking_user_email ON booking (user_email);
EOSQL