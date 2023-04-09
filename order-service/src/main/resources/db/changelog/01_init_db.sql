-- liquibase formatted sql

--changeset Schuyweiz:init_order_table

CREATE TYPE status AS ENUM ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'UPDATING');

CREATE TYPE service_type AS ENUM ('WALK', 'SITTING', 'BOARDING', 'TRAINING', 'GROOMING', 'OTHER');
CREATE TYPE service_delivery_type AS ENUM ('REMOTE', 'IN_PERSON');

CREATE TABLE IF NOT EXISTS "order"
(
    id                    INT                   NOT NULL,
    profile_id            INT                   NOT NULL,
    pet_id                INT                   NOT NULL,
    price                 DECIMAL(10, 2)        NOT NULL,
    address_id            INT,
    title                 VARCHAR(255)          NOT NULL,
    service_date_from     DATE                  NOT NULL,
    service_date_to       DATE,
    service_time          TIME,
    status                status                NOT NULL,
    type                  service_type          NOT NULL,
    service_delivery_type service_delivery_type NOT NULL,
    PRIMARY KEY (id)
);

