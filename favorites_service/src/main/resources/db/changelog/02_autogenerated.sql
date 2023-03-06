-- liquibase formatted sql

-- changeset Dmitry.Kochik:1678474408972-1
ALTER TABLE public.lots_favorites_record
    ADD category_id INTEGER NOT NULL;
