-- liquibase formatted sql

--changeset Schuyweiz:init_order_table

CREATE TYPE status AS ENUM ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'UPDATING');

CREATE TYPE service_type AS ENUM ('WALK', 'SITTING', 'BOARDING', 'TRAINING', 'GROOMING', 'OTHER');

CREATE TABLE IF NOT EXISTS service
(
    id                INT            NOT NULL,
    profile_id        INT            NOT NULL,
    pet_id            INT            NOT NULL,
    price             DECIMAL(10, 2) NOT NULL,
    address_id        INT,
    title             VARCHAR(255)   NOT NULL,
    service_date_from DATE           NOT NULL,
    service_date_to   DATE,
    service_time      TIME,
    status            status         NOT NULL,
    type              service_type   NOT NULL,
    PRIMARY KEY (id)
);

