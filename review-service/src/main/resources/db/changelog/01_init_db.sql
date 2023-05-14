-- liquibase formatted sql

--changeset Schuyweiz:init_reviews

CREATE TABLE IF NOT EXISTS public.lot_review
(
    id                  BIGSERIAL PRIMARY KEY,
    reviewer_profile_id BIGINT    NOT NULL,
    lot_id              BIGINT    NOT NULL,
    score               INTEGER   NOT NULL,
    text                TEXT      NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.profile_review
(
    id                  BIGSERIAL PRIMARY KEY,
    reviewer_profile_id BIGINT    NOT NULL,
    score               INTEGER   NOT NULL,
    text             TEXT      NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS public.review_image
(
    id          BIGSERIAL PRIMARY KEY,
    review_id   BIGINT    NOT NULL,
    image_url   TEXT      NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),

    constraint fk_review_image_review foreign key (review_id) references public.lot_review (id)
);

