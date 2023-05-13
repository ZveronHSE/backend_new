INSERT INTO public.statistics (id, order_lot_id, view_count)
VALUES (1, 1, 10),
       (2, 2, 20),
       (3, 3, 130),
       (4, 4, 120),
       (5, 5, 150),
       (6, 6, 0),
       (7, 7, 0),
       (8, 8, 1),
       (9, 9, 0),
       (10, 10, 0)

ON CONFLICT (id) DO NOTHING;