-- liquibase formatted sql

-- changeset Wolfalm:InitService
create table achievement
(
    id            BIGINT  NOT NULL,
    title         varchar(200),
    year          INTEGER not null,
    document_url  varchar(500),
    show_photo    bool,
    specialist_id BIGINT  NOT NULL,
    CONSTRAINT "achievementPK" PRIMARY KEY (id)
);

create table document
(
    id            BIGINT NOT NULL,
    url           varchar(500),
    specialist_id BIGINT NOT NULL,
    CONSTRAINT "documentPK" PRIMARY KEY (id)
);

create table education
(
    id                      BIGINT  NOT NULL,
    educational_institution varchar(200),
    faculty                 varchar(200),
    specialization          varchar(200),
    start_year              INTEGER not null,
    end_year                INTEGER not null,
    diploma_url             varchar(500),
    show_photo              bool,
    specialist_id           BIGINT  NOT NULL,
    CONSTRAINT "educationPK" PRIMARY KEY (id)
);

create table other_info
(
    id            BIGINT NOT NULL,
    title         varchar(300),
    document_url  varchar(500),
    show_photo    bool,
    specialist_id BIGINT NOT NULL,
    CONSTRAINT "otherInfoPK" PRIMARY KEY (id)
);

create table service
(
    id            BIGINT NOT NULL,
    title         varchar(200),
    start_price   INTEGER,
    end_price     INTEGER,
    is_remotely   bool,
    at_home       bool,
    is_home_visit bool,
    specialist_id BIGINT NOT NULL,
    CONSTRAINT "servicePK" PRIMARY KEY (id)
);

create table work_experience
(
    id            BIGINT  NOT NULL,
    organization  varchar(200),
    work_title    varchar(200),
    start_year    INTEGER not null,
    end_year      INTEGER,
    document_url  varchar(500),
    specialist_id BIGINT  NOT NULL,
    CONSTRAINT "work_experiencePK" PRIMARY KEY (id)
);

create table public.specialist
(
    id            BIGINT NOT NULL,
    name          varchar(30),
    surname       varchar(30),
    patronymic    varchar(30),
    img_url       varchar(500),
    description   varchar(1000),
    CONSTRAINT "specialistPK" PRIMARY KEY (id)
);

ALTER TABLE achievement
    ADD CONSTRAINT "achievement_fk" FOREIGN KEY (specialist_id) REFERENCES public.specialist (id);


ALTER TABLE document
    ADD CONSTRAINT "document_fk" FOREIGN KEY (specialist_id) REFERENCES public.specialist (id);


ALTER TABLE other_info
    ADD CONSTRAINT "other_info_fk" FOREIGN KEY (specialist_id) REFERENCES public.specialist (id);


ALTER TABLE service
    ADD CONSTRAINT "service_fk" FOREIGN KEY (specialist_id) REFERENCES public.specialist (id);

ALTER TABLE work_experience
    ADD CONSTRAINT "work_experience_fk" FOREIGN KEY (specialist_id) REFERENCES public.specialist (id);

ALTER TABLE education
    ADD CONSTRAINT "education_fk" FOREIGN KEY (specialist_id) REFERENCES public.specialist (id);

CREATE SEQUENCE IF NOT EXISTS achievement_id_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS document_id_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS education_id_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS other_id_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS service_id_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS specialist_id_seq START WITH 100 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS work_experience_id_seq START WITH 100 INCREMENT BY 1;