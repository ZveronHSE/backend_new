--changeset Schuyweiz:init_flow_state

create table if not exists public.flow_context
(
    "id"         bigserial primary key,
    "session_id" uuid      not null unique,
    "data"       jsonb     not null,

    created_at   timestamp not null default current_timestamp,
    updated_at   timestamp not null default current_timestamp,

    version     bigint       not null default 0
);
