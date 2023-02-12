--changeset Schuyweiz:add_expiration_to_flow_state

alter table if exists public.state_context
    add column if not exists expires_at timestamp default current_timestamp;