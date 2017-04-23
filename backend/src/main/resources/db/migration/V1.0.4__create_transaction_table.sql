CREATE TABLE transaction
(
    id SERIAL PRIMARY KEY NOT NULL,
    address_id INTEGER NOT NULL,
    source_address VARCHAR(35) NOT NULL,
    destination_address VARCHAR(35) NOT NULL,
    amount BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT now() NOT NULL,
    status SMALLINT NOT NULL,
    confirmations INTEGER DEFAULT 0 NOT NULL,
    blockchain_data BYTEA,
    CONSTRAINT transaction_address_id_fk FOREIGN KEY (address_id) REFERENCES address (id)
);