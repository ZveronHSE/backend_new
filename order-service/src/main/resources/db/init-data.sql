INSERT INTO public.order_lot (id, profile_id, animal_id, price, subway_id, title, description, service_date_from, service_date_to, service_time, status, type, service_delivery_type)
VALUES
    (1, 1, 1, '30.00', 1, 'Выгуливание собаки', 'Нужен человек, который выгуляет мою собаку на 30 минут', '2023-05-01', NULL, '15:00:00', 'PENDING', 'WALK', 'IN_PERSON'),
    (2, 2, 2, '50.00', 2, 'Кошачья присмотр', 'Ищу человека, который покормит мою кошку, пока я буду отсутствовать', '2023-06-15', '2023-06-20', NULL, 'CONFIRMED', 'SITTING', 'IN_PERSON'),
    (3, 3, 3, '25.00', 3, 'Стрижка собаки', 'Нужен человек, который помоет мою собаку и подстрижет ей когти', '2023-07-01', NULL, '10:00:00', 'PENDING', 'GROOMING', 'IN_PERSON'),
    (4, 1, 1, '100.00', 4, 'Дрессировка собаки', 'Хочу научить мою собаку базовым командам, таким как "сидеть" и "стоять"', '2023-08-10', '2023-08-20', '13:00:00', 'COMPLETED', 'TRAINING', 'IN_PERSON'),
    (5, 2, 2, '20.00', 5, 'Выгуливание собаки', 'Нужен человек, который выгуляет мою собаку на 20 минут', '2023-09-01', NULL, '16:30:00', 'CANCELLED', 'WALK', 'IN_PERSON'),
    (6, 3, 3, '75.00', 6, 'Постой собаки', 'Уезжаю из города на несколько дней и нужен человек, который позаботится о моей собаке', '2023-10-05', '2023-10-10', NULL, 'UPDATING', 'BOARDING', 'IN_PERSON'),
    (7, 1, 3, '40.00', 7, 'Кошачья присмотр', 'Ищу человека, который проверит мою кошку дважды в день, пока я буду отсутствовать', '2023-11-01', '2023-11-05', NULL, 'PENDING', 'SITTING', 'IN_PERSON'),
    (8, 2, 3, '60.00', 8, 'Дрессировка собаки', 'Хочу научить мою собаку продвинутым командам', '2023-11-04', NULL, '16:30:00', 'CANCELLED', 'WALK', 'IN_PERSON')

ON CONFLICT (id) DO NOTHING;