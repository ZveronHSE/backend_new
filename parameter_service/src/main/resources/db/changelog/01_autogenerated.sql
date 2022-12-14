-- liquibase formatted sql

-- changeset WolfAlm:1672868862716-1
CREATE SEQUENCE IF NOT EXISTS public.hibernate_sequence START WITH 1000 INCREMENT BY 1;

-- changeset WolfAlm:1672868862716-2
CREATE SEQUENCE IF NOT EXISTS public.parameter_id_seq START WITH 1 INCREMENT BY 1;

-- changeset WolfAlm:1672868862716-3
CREATE TABLE public.category
(
    id        INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name      VARCHAR(255),
    id_parent INTEGER,
    CONSTRAINT "categoryPK" PRIMARY KEY (id)
);

-- changeset WolfAlm:1672868862716-4
CREATE TABLE public.lot_form
(
    id   INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    form VARCHAR(255),
    type VARCHAR(255),
    CONSTRAINT "lot_formPK" PRIMARY KEY (id)
);

-- changeset WolfAlm:1672868862716-5
CREATE TABLE public.parameter
(
    id          INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    is_required BOOLEAN                                  NOT NULL,
    list_value  JSONB,
    name        VARCHAR(255),
    type        VARCHAR(255),
    CONSTRAINT "parameterPK" PRIMARY KEY (id)
);

-- changeset WolfAlm:1672868862716-6
CREATE TABLE public.parameter_from_type
(
    id_category  INTEGER NOT NULL,
    id_lot_form  INTEGER NOT NULL,
    id_parameter INTEGER NOT NULL,
    CONSTRAINT "parameter_from_typePK" PRIMARY KEY (id_category, id_lot_form, id_parameter)
);

-- changeset WolfAlm:1672868862716-7
ALTER TABLE public.parameter_from_type
    ADD CONSTRAINT "FK2wrru7x7ywx23eumsjxf1vtul" FOREIGN KEY (id_category) REFERENCES public.category (id);

-- changeset WolfAlm:1672868862716-8
ALTER TABLE public.category
    ADD CONSTRAINT "FKd1h3qjr1tnlqkh4o68t82m8am" FOREIGN KEY (id_parent) REFERENCES public.category (id);

-- changeset WolfAlm:1672868862716-9
ALTER TABLE public.parameter_from_type
    ADD CONSTRAINT "FKl2xsffluc46ur1jg9a22hmwbr" FOREIGN KEY (id_lot_form) REFERENCES public.lot_form (id);

-- changeset WolfAlm:1672868862716-10
ALTER TABLE public.parameter_from_type
    ADD CONSTRAINT "FKn3bsn184mb58fsoxjmt6kgjev" FOREIGN KEY (id_parameter) REFERENCES public.parameter (id);

