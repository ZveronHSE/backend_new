-- liquibase formatted sql

--changeset Schuyweiz:init_order_table

CREATE TYPE status AS ENUM ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'UPDATING');

CREATE TYPE service_type AS ENUM ('WALK', 'SITTING', 'BOARDING', 'TRAINING', 'GROOMING', 'OTHER');
CREATE TYPE service_delivery_type AS ENUM ('REMOTE', 'IN_PERSON');

CREATE TABLE IF NOT EXISTS public.order_lot
(
    id                    BIGINT                NOT NULL,
    profile_id            BIGINT                NOT NULL,
    animal_id             BIGINT                NOT NULL,
    price                 VARCHAR(20)           NOT NULL,
    subway_id             INTEGER               NOT NULL,
    title                 VARCHAR(255)          NOT NULL,
    description           VARCHAR(255)          NOT NULL,
    service_date_from     DATE                  NOT NULL,
    service_date_to       DATE,
    service_time          TIME,
    status                status                NOT NULL,
    type                  service_type          NOT NULL,
    service_delivery_type service_delivery_type NOT NULL,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE CAST (character varying AS status) WITH INOUT AS ASSIGNMENT;
CREATE CAST (character varying AS service_type) WITH INOUT AS ASSIGNMENT;
CREATE CAST (character varying AS service_delivery_type) WITH INOUT AS ASSIGNMENT;

