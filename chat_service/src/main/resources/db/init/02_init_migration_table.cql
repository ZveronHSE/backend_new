CREATE TABLE IF NOT EXISTS zveron_chat.changelog(
    id timeuuid,
    file_name text,
    date_executed timestamp,
    PRIMARY KEY (id)
);