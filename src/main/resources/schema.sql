CREATE TABLE IF NOT EXISTS price_ticks (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(32) NOT NULL,
    price DECIMAL(20, 8) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    source VARCHAR(64),
    volume DOUBLE PRECISION
);

CREATE INDEX IF NOT EXISTS idx_price_ticks_symbol_timestamp
    ON price_ticks (symbol, timestamp DESC);
