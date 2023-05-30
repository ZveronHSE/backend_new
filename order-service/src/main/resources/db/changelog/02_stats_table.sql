--changeset Schuyweiz:init_stats_table

CREATE TABLE IF NOT EXISTS public.statistics
(
    id           BIGSERIAL PRIMARY KEY,
    order_lot_id BIGINT NOT NULL,
    view_count   BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS statistics_order_lot_id ON public.statistics (order_lot_id);
