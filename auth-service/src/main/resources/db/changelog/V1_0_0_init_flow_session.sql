--changelog Schuyweiz:init_flow_state

create table if not exists public.flow_state
(
    id         bigint primary key,
    session_id varchar(40) not null unique,
    data      jsonb     not null,
    created_at timestamp   not null default current_timestamp,
    updated_at timestamp   not null default current_timestamp,

    revision   bigint      not null default 0
);
