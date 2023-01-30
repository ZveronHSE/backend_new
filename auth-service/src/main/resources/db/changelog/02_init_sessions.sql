--changeset Schuyweiz:init_sessions

create table if not exists public.session
(
    id               uuid primary key default  gen_random_uuid(),

    token_identifier uuid      not null unique,
    fingerprint      varchar   not null,
    profile_id       bigint    not null,

    expires_at       timestamp not null,

    created_at       timestamp not null default current_timestamp,
    updated_at       timestamp not null default current_timestamp,

    version          bigint    not null default 0
);
