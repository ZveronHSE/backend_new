--changeset Schuyweiz:access_role_init

drop type if exists access_scope cascade;

create type access_scope as enum (
    'ANY',
    'BUYER'
    );

alter table public.method_metadata
    add if not exists access_scope access_scope;