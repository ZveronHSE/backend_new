insert into specialist (id, name, surname, patronymic, img_url, description)
values (1, 'Екатерина', 'Смирнова', 'Александровна', 'https://example.com/image2.jpg',
        'Профессиональный грумер с 5-летним стажем работы в зоосалонах.'),
       (2, 'Александр', 'Иванов', 'Петрович', 'https://example.com/image1.jpg',
        'Опытный ветеринар с 10-летним стажем работы в клиниках малых и крупных животных.'),
       (3, 'Андрей', 'Петров', 'Иванович', 'https://example.com/image3.jpg',
        'Владелец собственной клиники малых животных. Ветеринар с 8-летним стажем.')
on conflict (id) do nothing;

insert into achievement (id, title, year, document_url, show_photo, specialist_id)
values (1, 'Приз лучшего ветеринара года', 2019, 'https://example.com/document1.pdf', true, 2),
       (2, 'Диплом грумера-стилиста', 2021, 'https://example.com/document2.pdf', true, 1),
       (3, 'Сертификат о прохождении курсов повышения квалификации для ветеринаров', 2020,
        'https://example.com/document3.pdf', true, 2),
       (4, 'Сертификат о прохождении курсов по стрижке и уходу за шерстью животных', 2022,
        'https://example.com/document4.pdf', true, 1)
on conflict (id) do nothing;

insert into document (id, url, specialist_id)
values (1, 'https://example.com/document5.pdf', 3),
       (2, 'https://example.com/document6.pdf', 3),
       (3, 'https://example.com/document7.pdf', 2),
       (4, 'https://example.com/document8.pdf', 2)
on conflict (id) do nothing;

INSERT INTO education (id, educational_institution, faculty, specialization, start_year, end_year, diploma_url,
                       show_photo, specialist_id)
VALUES (1, 'Ветеринарный институт', 'Ветеринарная медицина', 'Ветеринарный врач', 2015, 2020,
        'https://example.com/docs/diploma1.pdf', true, 1),
       (2, 'Школа груминга "Забота"', 'Груминг', 'Грумер', 2017, 2018, 'https://example.com/docs/diploma2.pdf', true,
        1),
       (3, 'Медицинский институт', 'Лечебное дело', 'Ветеринарный врач', 2016, 2021,
        'https://example.com/docs/diploma3.pdf', true, 2),
       (4, 'Школа груминга "Пушистик"', 'Груминг', 'Грумер', 2019, 2020, 'https://example.com/docs/diploma4.pdf', true,
        3)
on conflict (id) do nothing;

INSERT INTO service (id, title, start_price, end_price, is_remotely, at_home, is_home_visit, specialist_id)
VALUES (1, 'Стрижка собаки', 1000, 3000, false, true, true, 1),
       (2, 'Стрижка кота', 800, 2000, false, true, true, 1),
       (3, 'Уход за грызуном', 300, 500, true, true, false, 1),
       (4, 'Лечение птиц', 500, null, false, false, true, 2),
       (5, 'Лечение рыбок', 1000, 2500, false, false, true, 2),
       (6, 'Уход за морской свинкой', null, null, true, true, false, 3),
       (7, 'Кормление черепахи', 200, 500, true, true, false, 3),
       (8, 'Лечение грызунов', 300, 800, true, false, true, 3),
       (9, 'Стрижка собаки дома', 2000, 5000, true, true, true, 1)
on conflict (id) do nothing;
