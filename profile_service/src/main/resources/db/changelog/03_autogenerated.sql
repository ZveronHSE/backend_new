-- liquibase formatted sql

-- changeset dakochik:1676061781084-1
CREATE SEQUENCE IF NOT EXISTS public.profile_id_seq START WITH 10 INCREMENT BY 1;

-- changeset dakochik:1676061781084-2
DROP SEQUENCE public.communication_link_id_seq;

-- changeset dakochik:1676061781084-3
CREATE SEQUENCE IF NOT EXISTS public.communication_link_id_seq START WITH 10 INCREMENT BY 1;

