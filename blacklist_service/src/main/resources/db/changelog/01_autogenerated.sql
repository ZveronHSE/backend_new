-- liquibase formatted sql

-- changeset Dmitry.Kochik:1671826241870-1
CREATE TABLE public.blacklist_record
(
    owner_user_id    BIGINT NOT NULL,
    reported_user_id BIGINT NOT NULL,
    CONSTRAINT "blacklist_recordPK" PRIMARY KEY (owner_user_id, reported_user_id)
);

-- changeset Dmitry.Kochik:1671826241870-2
CREATE INDEX embedded_key_index ON public.blacklist_record (owner_user_id, reported_user_id);
