INSERT INTO public.profile (id, last_seen, name, surname, address_id, image_url)
VALUES (1, '2022-08-21T14:09:00.000000', 'Петя', 'Иванов', '1',
        'https://storage.yandexcloud.net/zveron-profile/random.jpeg'),
       (2, '2022-08-21T14:10:00.000000', 'Иван', 'Петров', '2',
        'https://storage.yandexcloud.net/zveron-profile/random.jpeg'),
       (3, '2022-08-21T14:11:00.000000', 'Кот', 'Собачков', '3',
        'https://storage.yandexcloud.net/zveron-profile/random.jpeg')
ON CONFLICT DO NOTHING;