-- liquibase formatted sql

-- changeset Dmitry.Kochik:1674239282871-1
CREATE TABLE public.contact
(
    additional_email VARCHAR(255),
    gmail            VARCHAR(255),
    gmail_id         VARCHAR(255),
    phone            VARCHAR(255),
    vk_id            VARCHAR(255),
    vk_ref           VARCHAR(255),
    profile_id       BIGINT NOT NULL,
    CONSTRAINT "contactPK" PRIMARY KEY (profile_id)
);

-- changeset Dmitry.Kochik:1674239282871-2
CREATE TABLE public.profile
(
    id         BIGINT       NOT NULL,
    address_id BIGINT       NOT NULL,
    image_id   BIGINT       NOT NULL,
    last_seen  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    name       VARCHAR(255) NOT NULL,
    surname    VARCHAR(255) NOT NULL,
    CONSTRAINT "profilePK" PRIMARY KEY (id)
);

-- changeset Dmitry.Kochik:1674239282871-3
CREATE TABLE public.settings
(
    channels          JSONB,
    search_address_id BIGINT NOT NULL,
    profile_id        BIGINT NOT NULL,
    CONSTRAINT "settingsPK" PRIMARY KEY (profile_id)
);

-- changeset Dmitry.Kochik:1674239282871-4
ALTER TABLE public.settings
    ADD CONSTRAINT "settings_refer_profile_constraint" FOREIGN KEY (profile_id) REFERENCES public.profile (id);

-- changeset Dmitry.Kochik:1674239282871-5
ALTER TABLE public.contact
    ADD CONSTRAINT "contact_refer_profile_constraint" FOREIGN KEY (profile_id) REFERENCES public.profile (id);

-- changeset Dmitry.Kochik:1674239282871-6
CREATE INDEX phone_contact_index ON public.contact (phone);

-- changeset Dmitry.Kochik:1674239282871-7
CREATE INDEX vk_id_contact_index ON public.contact (vk_id);

-- changeset Dmitry.Kochik:1674239282871-8
CREATE INDEX gmail_id_contact_index ON public.contact (gmail_id);

