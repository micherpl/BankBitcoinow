CREATE TABLE "user"
(
    id SERIAL PRIMARY KEY NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    otp_keyid VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT now() NOT NULL
);
CREATE UNIQUE INDEX user_email_uindex ON "user" (email);
