INSERT INTO public.profile (id, last_seen, name, surname, address_id, image_url)
VALUES (1, '2022-08-21T14:09:00.000000', 'Петя', 'Иванов', '1',
        'https://storage.yandexcloud.net/zveron-profile/random.jpeg'),
       (2, '2022-08-21T14:10:00.000000', 'Иван', 'Петров', '2',
        'https://storage.yandexcloud.net/zveron-profile/random.jpeg'),
       (3, '2022-08-21T14:11:00.000000', 'Кот', 'Собачков', '3',
        'https://storage.yandexcloud.net/zveron-profile/random.jpeg')
ON CONFLICT DO NOTHING;

INSERT INTO public.contact (additional_email, gmail, gmail_id, phone, vk_id, vk_ref, profile_id)
VALUES ('', '', '', '79993332211', '', '', '1'),
       ('', '', '', '79998882211', '', '', '2'),
       ('', '', '', '79997779977', '', '', '3')
ON CONFLICT DO NOTHING;

INSERT INTO public.communication_link (id, communication_link_id, data, profile_id, type)
VALUES ('1', '79993332211', '{"type": "phone-communication-link"}', '1', 'PHONE'),
       ('2', '79998882211', '{"type": "phone-communication-link"}', '2', 'PHONE'),
       ('3', '79997779977', '{"type": "phone-communication-link"}', '3', 'PHONE')
ON CONFLICT DO NOTHING;

INSERT INTO public.settings (profile_id, search_address_id, channels)
VALUES ('1', '-1', '{"vk": false, "chat": true, "gmail": false, "phone": true}'),
       ('2', '-1', '{"vk": false, "chat": true, "gmail": false, "phone": true}'),
       ('3', '-1', '{"vk": false, "chat": true, "gmail": false, "phone": true}')
ON CONFLICT DO NOTHING;

