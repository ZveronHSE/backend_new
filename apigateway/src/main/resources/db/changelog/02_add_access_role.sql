--changeset Schuyweiz:access_role_init

drop type if exists access_role cascade;

create type access_role as enum (
    'ANY',
    'BUYER'
    );

alter table public.method_metadata
    add if not exists access_role access_role;