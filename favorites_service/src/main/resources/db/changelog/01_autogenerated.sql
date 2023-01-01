-- liquibase formatted sql

-- changeset Dmitry.Kochik:1672391500791-1
CREATE TABLE public.lots_favorites_counter
(
    lot_id BIGINT  NOT NULL,
    shard_id      INTEGER NOT NULL,
    counter       BIGINT  NOT NULL,
    CONSTRAINT "lots_favorites_counterPK" PRIMARY KEY (lot_id, shard_id)
);

-- changeset Dmitry.Kochik:1672391500791-2
CREATE TABLE public.lots_favorites_record
(
    favorite_lot_id BIGINT NOT NULL,
    owner_user_id   BIGINT NOT NULL,
    CONSTRAINT "lots_favorites_recordPK" PRIMARY KEY (favorite_lot_id, owner_user_id)
);

-- changeset Dmitry.Kochik:1672391500791-3
CREATE TABLE public.profiles_favorites_record
(
    favorite_user_id BIGINT NOT NULL,
    owner_user_id    BIGINT NOT NULL,
    CONSTRAINT "profiles_favorites_recordPK" PRIMARY KEY (favorite_user_id, owner_user_id)
);

-- changeset Dmitry.Kochik:1672391500791-4
CREATE INDEX lots_favorites_index ON public.lots_favorites_record (owner_user_id, favorite_lot_id);

-- changeset Dmitry.Kochik:1672391500791-5
CREATE INDEX profiles_favorites_index ON public.profiles_favorites_record (owner_user_id, favorite_user_id);

-- changeset Dmitry.Kochik:1672391500791-7
CREATE INDEX lots_statistics_index ON public.lots_favorites_counter (lot_id, shard_id);
