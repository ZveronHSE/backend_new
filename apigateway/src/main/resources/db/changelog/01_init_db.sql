-- liquibase formatted sql

--changeset pikek:alias_table_init

create table if not exists public.method_metadata
(
    "alias"               varchar primary key,
    "service_name"        varchar not null,
    "grpc_service_name"   varchar not null,
    "grpc_method_name"    varchar not null
);
