INSERT INTO public.order_lot (id,
                              profile_id,
                              animal_id,
                              price,
                              subway_id,
                              title,
                              description,
                              service_date_from,
                              service_date_to,
                              time_window_from,
                              time_window_to,
                              status,
                              service_type,
                              service_delivery_type,
                              created_at)

VALUES (1, 1, 1, 30, 1, 'Выгуливание собаки', 'Нужен человек, который выгуляет мою собаку на 30 минут', '2023-05-01',
        '2023-05-04', '15:00:00', '15:00:00', 'PENDING', 'WALK', 'IN_PERSON', NOW()),
       (2, 2, 2, 50, 2, 'Кошачья присмотр', 'Ищу человека, который покормит мою кошку, пока я буду отсутствовать',
        '2023-06-15', '2023-06-20', '15:00:00', '15:00:00', 'CONFIRMED', 'SITTING', 'IN_PERSON', NOW()),
       (3, 3, 3, 20, 3, 'Стрижка собаки', 'Нужен человек, который помоет мою собаку и подстрижет ей когти',
        '2023-07-01', '2023-07-10', '10:00:00', '15:00:00', 'PENDING', 'GROOMING', 'IN_PERSON', NOW()),
       (4, 1, 1, 100, 4, 'Дрессировка собаки',
        'Хочу научить мою собаку базовым командам, таким как "сидеть" и "стоять"', '2023-08-10', '2023-08-20',
        '13:00:00', '15:00:00', 'COMPLETED', 'TRAINING', 'IN_PERSON', NOW()),
       (5, 2, 2, 20, 5, 'Выгуливание собаки', 'Нужен человек, который выгуляет мою собаку на 20 минут', '2023-09-01',
        '2023-09-01', '16:30:00', '18:30:00', 'CANCELLED', 'WALK', 'IN_PERSON', NOW()),
       (6, 3, 3, 75, 6, 'Постой собаки',
        'Уезжаю из города на несколько дней и нужен человек, который позаботится о моей собаке', '2023-10-05',
        '2023-10-10', '16:30:00', '17:30:00', 'UPDATING', 'BOARDING', 'IN_PERSON', NOW()),
       (7, 1, 3, 60, 7, 'Кошачья присмотр',
        'Ищу человека, который проверит мою кошку дважды в день, пока я буду отсутствовать', '2023-11-01', '2023-11-05',
        '16:30:00', '19:30:00', 'PENDING', 'SITTING', 'IN_PERSON', NOW()),
       (8, 2, 3, 65, 8, 'Дрессировка собаки', 'Хочу научить мою собаку продвинутым командам', '2023-11-04',
        '2023-11-04',
        '16:30:00', '18:30:00', 'CANCELLED', 'WALK', 'IN_PERSON', NOW())

ON CONFLICT (id) DO NOTHING;