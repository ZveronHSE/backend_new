-- liquibase formatted sql

--changeset Schuyweiz:init_order_table

CREATE TYPE status AS ENUM ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'UPDATING');
CREATE TYPE service_delivery_type AS ENUM ('REMOTE', 'IN_PERSON');
CREATE TYPE service_type AS ENUM ('WALK', 'SITTING', 'BOARDING', 'GROOMING', 'TRAINING' ,'OTHER');

CREATE TABLE IF NOT EXISTS public.order_lot
(
    id                    BIGINT PRIMARY KEY    NOT NULL,
    profile_id            BIGINT                NOT NULL,
    animal_id             BIGINT                NOT NULL,
    price                 BIGINT                NOT NULL,
    subway_id             INTEGER               NOT NULL,
    title                 VARCHAR(255)          NOT NULL,
    description           TEXT                  NOT NULL,
    service_date_from     DATE                  NOT NULL,
    service_date_to       DATE,
    time_window_from      TIME,
    time_window_to        TIME,
    status                status                NOT NULL,
    service_type          service_type          NOT NULL,
    service_delivery_type service_delivery_type NOT NULL,
    created_at            TIMESTAMP             NOT NULL
);
