CREATE TABLE contact
(
    id SERIAL PRIMARY KEY NOT NULL,
    user_id INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(35) NOT NULL,
    CONSTRAINT contact_user_id_fk FOREIGN KEY (user_id) REFERENCES "user" (id)
);
CREATE UNIQUE INDEX contact_user_id_address_uindex ON contact (user_id, address);