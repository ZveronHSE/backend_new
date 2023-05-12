-- liquibase formatted sql

--changeset Schuyweiz:init_order_table

CREATE TABLE IF NOT EXISTS public.order_lot
(
    id                    BIGSERIAL PRIMARY KEY,
    profile_id            BIGINT       NOT NULL,
    animal_id             BIGINT       NOT NULL,
    price                 BIGINT       NOT NULL,
    subway_id             INTEGER,
    title                 VARCHAR(255) NOT NULL,
    description           TEXT         NOT NULL,
    service_date_from     DATE         NOT NULL,
    service_date_to       DATE,
    time_window_from      TIME,
    time_window_to        TIME,
    status                VARCHAR      NOT NULL,
    service_type          VARCHAR      NOT NULL,
    service_delivery_type VARCHAR      NOT NULL,
    created_at            TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS order_lot_profile_id_idx ON public.order_lot (profile_id);
