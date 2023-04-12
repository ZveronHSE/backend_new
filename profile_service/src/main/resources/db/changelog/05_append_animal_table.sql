-- changeset Schuyweiz:init_animal_seq
CREATE SEQUENCE IF NOT EXISTS public.animal_id_seq START WITH 1 INCREMENT BY 1;

-- changeset Schuyweiz:init_animal_table
CREATE TABLE IF NOT EXISTS public.animal
(
    id            BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name          VARCHAR(50) NOT NULL,
    breed         VARCHAR(50) NOT NULL,
    species       VARCHAR(50) NOT NULL,
    age           INT NOT NULL,
    image_urls    text[] NOT NULL,
    document_urls text[] NOT NULL,
    profile_id    BIGINT NOT NULL,
    CONSTRAINT "animalPK" PRIMARY KEY (id)
);

ALTER TABLE public.animal
    ADD CONSTRAINT "animal_refer_profile_constraint" FOREIGN KEY (profile_id) REFERENCES public.profile (id);

CREATE INDEX IF NOT EXISTS animal_profile_id_index ON public.animal (profile_id);


