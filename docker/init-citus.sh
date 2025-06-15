#!/bin/bash
set -e

# Enable citus extension
psql -U postgres -d postgres -c "CREATE EXTENSION IF NOT EXISTS citus;"

# Wait until both workers are ready
until pg_isready -h citus-worker1 -p 5432 -U postgres && pg_isready -h citus-worker2 -p 5432 -U postgres; do
  echo "Waiting for Citus workers to be ready..."
  sleep 2
done

# Run all SQL setup in one go
psql -U postgres -d postgres <<-'EOSQL'
  -- Set coordinator and add workers
  SELECT citus_set_coordinator_host('citus-coordinator', 5432);
  SELECT citus_add_node('citus-worker1', 5432);
  SELECT citus_add_node('citus-worker2', 5432);

  -- Debug: verify nodes
  SELECT * FROM pg_dist_node;

  -- Create user sequence
  CREATE SEQUENCE IF NOT EXISTS user_seq INCREMENT BY 1;

  -- Create user table
  CREATE TABLE IF NOT EXISTS app_user (
      id BIGINT PRIMARY KEY DEFAULT nextval('user_seq'),
      email VARCHAR(255) NOT NULL UNIQUE,
      password VARCHAR(255) NOT NULL,
      role VARCHAR(50) NOT NULL
  );
  SELECT create_distributed_table('app_user', 'id');

  -- Insert sample user (password: "password" hashed with bcrypt)
  DO $$
  BEGIN
      IF NOT EXISTS (SELECT 1 FROM "app_user") THEN
          INSERT INTO "app_user" (email, password, role)
          VALUES
          ('user@example.com', '$2a$10$WJIt2tGozZH2f6lrxjkzQuUMxyZc.MFv4SnBaA9KWe6W7MP6X3ZQ6', 'USER'),
          ('admin@example.com', '$2a$10$WJIt2tGozZH2f6lrxjkzQuUMxyZc.MFv4SnBaA9KWe6W7MP6X3ZQ6', 'ADMIN');
      END IF;
  END;
  $$;

  -- Create event sequence
  CREATE SEQUENCE IF NOT EXISTS event_seq INCREMENT BY 1;

  -- Create event table with new columns
  CREATE TABLE IF NOT EXISTS event (
      id INTEGER PRIMARY KEY DEFAULT nextval('event_seq'),
      name VARCHAR(255) NOT NULL,
      available_tickets INTEGER NOT NULL CHECK (available_tickets >= 0),
      event_date TIMESTAMP NOT NULL,
      location VARCHAR(255),
      category VARCHAR(100)
  );
  SELECT create_distributed_table('event', 'id');

  -- Create booking sequence
  CREATE SEQUENCE IF NOT EXISTS booking_seq INCREMENT BY 1;

  -- Create booking table
  CREATE TABLE IF NOT EXISTS booking (
      id BIGINT PRIMARY KEY DEFAULT nextval('booking_seq'),
      user_email VARCHAR(255) NOT NULL,
      event_id INTEGER NOT NULL REFERENCES event(id),
      quantity INTEGER NOT NULL CHECK (quantity > 0),
      booking_time TIMESTAMP NOT NULL,
      CONSTRAINT unique_booking UNIQUE (user_email, event_id)
  );
  SELECT create_distributed_table('booking', 'event_id');

  -- Insert sample events
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
EOSQL