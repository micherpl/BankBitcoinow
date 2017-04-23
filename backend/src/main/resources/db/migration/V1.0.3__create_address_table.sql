CREATE TABLE address
(
    id SERIAL PRIMARY KEY NOT NULL,
    user_id INTEGER NOT NULL,
    address VARCHAR(35) NOT NULL,
    private_key BYTEA NOT NULL,
    balance BIGINT DEFAULT 0 NOT NULL,
    created_at TIMESTAMP DEFAULT now() NOT NULL,
    CONSTRAINT address_user_id_fk FOREIGN KEY (user_id) REFERENCES "user" (id)
);
CREATE UNIQUE INDEX address_address_uindex ON address (address);