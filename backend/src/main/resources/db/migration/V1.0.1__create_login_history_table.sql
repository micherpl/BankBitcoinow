CREATE TABLE login_history
(
    id SERIAL PRIMARY KEY NOT NULL,
    user_id INTEGER NOT NULL,
    date TIMESTAMP NOT NULL,
    success BOOLEAN DEFAULT FALSE NOT NULL,
    ip VARCHAR(15) NOT NULL,
    CONSTRAINT login_history_user_id_fk FOREIGN KEY (user_id) REFERENCES "user" (id)
);
