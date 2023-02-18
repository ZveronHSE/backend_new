-- liquibase formatted sql

-- changeset dakochik:1676061781084-1
CREATE SEQUENCE IF NOT EXISTS public.profile_id_seq START WITH 10 INCREMENT BY 1;

-- changeset dakochik:1676061781084-2
DROP SEQUENCE public.communication_link_id_seq;

-- changeset dakochik:1676061781084-3
CREATE SEQUENCE IF NOT EXISTS public.communication_link_id_seq START WITH 10 INCREMENT BY 1;

-- changeset dakochik:1676061781084-4
ALTER TABLE public.communication_link
    ADD type VARCHAR(50) NOT NULL;

-- changeset dakochik:1676061781084-5
ALTER TABLE public.communication_link
    ADD CONSTRAINT communication_link_id_type_constraint UNIQUE (communication_link_id, type);

-- changeset Dmitry.Kochik:1676061781084-6
DROP INDEX communication_link_context_id_index;

-- changeset Dmitry.Kochik:1676061781084-7
CREATE INDEX communication_link_context_id_type_index ON public.communication_link (communication_link_id, type);

-- changeset Dmitry.Kochik:1676061781084-8
ALTER TABLE public.profile
    ADD password_hash VARCHAR(32);
