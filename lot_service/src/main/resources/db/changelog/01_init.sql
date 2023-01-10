-- liquibase formatted sql

-- changeset WolfAlm:1674856710886-1
CREATE SEQUENCE IF NOT EXISTS public.lot_id_seq START WITH 1 INCREMENT BY 1;

-- changeset WolfAlm:1674856710886-2
CREATE SEQUENCE IF NOT EXISTS public.lot_photo_id_seq START WITH 1 INCREMENT BY 1;

-- changeset WolfAlm:1674856710886-3
CREATE TABLE public.lot
(
    id            BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    address_id    BIGINT,
    category_id   INTEGER,
    channel_type  JSONB,
    date_creation TIMESTAMP WITHOUT TIME ZONE,
    description   VARCHAR(255),
    gender        INTEGER,
    lot_form_id   INTEGER,
    price         INTEGER                                 NOT NULL,
    seller_id     BIGINT,
    status        VARCHAR(255),
    title         VARCHAR(255),
    CONSTRAINT "lotPK" PRIMARY KEY (id)
);

-- changeset WolfAlm:1674856710886-4
CREATE TABLE public.lot_parameter
(
    lot_id       BIGINT  NOT NULL,
    parameter_id INTEGER NOT NULL,
    value        VARCHAR(255),
    CONSTRAINT "lot_parameterPK" PRIMARY KEY (lot_id, parameter_id)
);

-- changeset WolfAlm:1674856710886-5
CREATE TABLE public.lot_photo
(
    id       BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    image_id BIGINT                                  NOT NULL,
    "order"  INTEGER,
    lot_id   BIGINT                                  NOT NULL,
    CONSTRAINT "lot_photoPK" PRIMARY KEY (id)
);

-- changeset WolfAlm:1674856710886-6
CREATE TABLE public.lot_statistics
(
    quantity_view INTEGER,
    lot_id        BIGINT NOT NULL,
    CONSTRAINT "lot_statisticsPK" PRIMARY KEY (lot_id)
);

-- changeset WolfAlm:1674856710886-7
ALTER TABLE public.lot_photo
    ADD CONSTRAINT "lot_photo_fk" FOREIGN KEY (lot_id) REFERENCES public.lot (id);

-- changeset WolfAlm:1674856710886-8
ALTER TABLE public.lot_parameter
    ADD CONSTRAINT "lot_parameter_fk" FOREIGN KEY (lot_id) REFERENCES public.lot (id);

-- changeset WolfAlm:1674856710886-9
ALTER TABLE public.lot_statistics
    ADD CONSTRAINT "lot_statistics_fk" FOREIGN KEY (lot_id) REFERENCES public.lot (id);

-- changeset WolfAlm:1674856710886-10
CREATE INDEX lot_seller_id_index ON public.lot (seller_id);

-- changeset WolfAlm:1674856710886-11
create index lot_id_date_creation_index
    on public.lot (id, date_creation);

-- changeset WolfAlm:1674856710886-12
create index lot_id_price_index
    on public.lot (id, price);