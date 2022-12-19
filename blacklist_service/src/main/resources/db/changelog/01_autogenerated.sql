-- liquibase formatted sql

-- changeset Dmitry.Kochik:1671642820807-1
CREATE TABLE public.blacklist_record
(
    reported_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    CONSTRAINT "blacklist_recordPK" PRIMARY KEY (reported_id, reporter_id)
);

