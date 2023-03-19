CREATE TABLE IF NOT EXISTS zveron_chat.chat (
    user_id bigint,
    folder_type int,
    chat_id uuid,
    another_user_id bigint,
    last_update timestamp,
    lots_ids list<bigint>,
    review_id bigint,
    service_id bigint,
    status text,
    unread_messages int,
    PRIMARY KEY ((user_id, folder_type), chat_id)
) WITH CLUSTERING ORDER BY (chat_id ASC);

CREATE TABLE IF NOT EXISTS zveron_chat.message (
    chat_id uuid,
    received_at timestamp,
    images_ids list<bigint>,
    is_read boolean,
    sender_id bigint,
    text text,
    type text,
    PRIMARY KEY (chat_id, received_at)
) WITH CLUSTERING ORDER BY (received_at DESC);

CREATE TABLE IF NOT EXISTS zveron_chat.connection (
    user_id bigint,
    node_address text,
    is_closed boolean,
    PRIMARY KEY ((user_id, node_address))
);