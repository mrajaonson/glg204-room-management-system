-- Room Management System - Database Init Script (PostgreSQL 18)
-- This script runs automatically on database creation

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create a general schema for the application
CREATE SCHEMA IF NOT EXISTS app;

-- Grant necessary privileges
GRANT CREATE ON SCHEMA app TO "rms-dev";
GRANT USAGE ON SCHEMA app TO "rms-dev";
