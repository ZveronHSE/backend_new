--changeset dakochik:chat_aliases

insert into public.method_metadata (alias, service_name, grpc_service_name, grpc_method_name, access_scope)
values ('chatPing', 'chat-service', 'PingServiceExternal', 'Ping', 'ANY'),
       ('bidiChatRoute', 'chat-service', 'ChatServiceExternal', 'BidiChatRoute', 'BUYER');
