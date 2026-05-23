-- Create table for greetings if it does not exist yet
CREATE TABLE IF NOT EXISTS greetings (
    id SERIAL PRIMARY KEY,
    text VARCHAR(255) NOT NULL UNIQUE
);
