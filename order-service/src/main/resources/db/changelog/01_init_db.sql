-- liquibase formatted sql

--changeset Schuyweiz:init_order_table

CREATE TYPE status AS ENUM ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'UPDATING');

CREATE TYPE service_type AS ENUM ('WALK', 'SITTING', 'BOARDING', 'TRAINING', 'GROOMING', 'OTHER');
CREATE TYPE service_delivery_type AS ENUM ('REMOTE', 'IN_PERSON');

CREATE TABLE IF NOT EXISTS public."order"
(
    id                    BIGINT                NOT NULL,
    profile_id            BIGINT                NOT NULL,
    pet_id                BIGINT                NOT NULL,
    price                 VARCHAR(20)           NOT NULL,
    address_id            BIGINT,
    title                 VARCHAR(255)          NOT NULL,
    description           VARCHAR(255)          NOT NULL,
    service_date_from     DATE                  NOT NULL,
    service_date_to       DATE,
    service_time          TIME,
    status                status                NOT NULL,
    type                  service_type          NOT NULL,
    service_delivery_type service_delivery_type NOT NULL,
    PRIMARY KEY (id)
);
