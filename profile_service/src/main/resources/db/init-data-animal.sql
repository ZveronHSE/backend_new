INSERT INTO public.animal (name, breed, species, age, image_urls, document_urls, profile_id)
VALUES ('Кошка', 'Сиамская', 'Кошка', 3, ARRAY ['https://storage.yandexcloud.net/zveron-animal/cat.jpeg'],
        ARRAY ['document1|https://storage.yandexcloud.net/zveron-document/SSRN-id908444.pdf'], 1),
       ('Собака', 'Лабрадор', 'Собака', 2, ARRAY ['https://storage.yandexcloud.net/zveron-animal/cat.jpeg'],
        ARRAY ['document2|https://storage.yandexcloud.net/zveron-document/SSRN-id908444.pdf'], 2),
       ('Попугай', 'Ара', 'Птица', 1, ARRAY ['https://storage.yandexcloud.net/zveron-animal/cat.jpeg'],
        ARRAY ['document3|https://storage.yandexcloud.net/zveron-document/SSRN-id908444.pdf'], 3),
       ('Змея', 'Питон', 'Змея', 4, ARRAY ['https://storage.yandexcloud.net/zveron-animal/cat.jpeg'],
        ARRAY ['document4|https://storage.yandexcloud.net/zveron-document/SSRN-id908444.pdf'], 1),
       ('Кролик', 'Бельгийский великан', 'Кролик', 2, ARRAY ['https://storage.yandexcloud.net/zveron-animal/cat.jpeg'],
        ARRAY ['document5|https://storage.yandexcloud.net/zveron-document/SSRN-id908444.pdf'], 2),
       ('Хомяк', 'Джунгарский', 'Хомяк', 1, ARRAY ['https://storage.yandexcloud.net/zveron-animal/cat.jpeg'],
        ARRAY ['document6|https://storage.yandexcloud.net/zveron-document/SSRN-id908444.pdf'], 3),
       ('Черепаха', 'Сухопутная', 'Черепаха', 5, ARRAY ['https://storage.yandexcloud.net/zveron-animal/cat.jpeg'],
        ARRAY ['document7|https://storage.yandexcloud.net/zveron-document/SSRN-id908444.pdf'], 1),
       ('Кошка', 'Персидская', 'Кошка', 6, ARRAY ['https://storage.yandexcloud.net/zveron-animal/cat.jpeg'],
        ARRAY ['document8|https://storage.yandexcloud.net/zveron-document/SSRN-id908444.pdf'], 2),
       ('Собака', 'Овчарка', 'Собака', 3, ARRAY ['https://storage.yandexcloud.net/zveron-animal/cat.jpeg'],
        ARRAY ['document9|https://storage.yandexcloud.net/zveron-document/SSRN-id908444.pdf'], 3),
       ('Попугай', 'Грач', 'Птица', 2, ARRAY ['https://storage.yandexcloud.net/zveron-animal/cat.jpeg'],
        ARRAY ['document10|https://storage.yandexcloud.net/zveron-document/SSRN-id908444.pdf'], 1)

ON CONFLICT DO NOTHING;

