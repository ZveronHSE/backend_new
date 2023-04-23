CREATE TABLE IF NOT EXISTS zveron_chat.chat (
    profile_id bigint,
    chat_id timeuuid,
    another_profile_id bigint,
    last_update timestamp,
    lots_ids set<bigint>,
    review_id bigint,
    service_id bigint,
    chat_status text,
    unread_messages int,
    PRIMARY KEY ((profile_id), chat_id)
) WITH CLUSTERING ORDER BY (chat_id DESC);

CREATE TABLE IF NOT EXISTS zveron_chat.message (
    chat_id uuid,
    id timeuuid,
    received_at timestamp,
    images_urls list<text>,
    is_read boolean,
    sender_id bigint,
    text text,
    type text,
    PRIMARY KEY ((chat_id), id)
) WITH CLUSTERING ORDER BY (id DESC);

CREATE TABLE IF NOT EXISTS zveron_chat.connection (
    profile_id bigint,
    node_address uuid,
    is_closed boolean,
    last_status_change timestamp,
    PRIMARY KEY ((profile_id), node_address)
);