INSERT INTO public.order_lot (id, profile_id, animal_id, price, subway_id, title, description, service_date_from, service_date_to, service_time, status, type, service_delivery_type)
VALUES
    (1, 1, 1, '30.00', 1, 'Dog walking', 'Need someone to walk my dog for 30 minutes', '2023-05-01', NULL, '15:00:00', 'PENDING', 'WALK', 'IN_PERSON'),
    (2, 2, 2, '50.00', 2, 'Cat sitting', 'Looking for someone to feed my cat while I am away', '2023-06-15', '2023-06-20', NULL, 'CONFIRMED', 'SITTING', 'IN_PERSON'),
    (3, 3, 3, '25.00', 3, 'Dog grooming', 'Need someone to give my dog a bath and trim his nails', '2023-07-01', NULL, '10:00:00', 'PENDING', 'GROOMING', 'IN_PERSON'),
    (4, 4, 4, '100.00', 4, 'Dog training', 'Want to teach my dog some basic commands like sit and stay', '2023-08-10', '2023-08-20', '13:00:00', 'COMPLETED', 'TRAINING', 'IN_PERSON'),
    (5, 5, 5, '20.00', 5, 'Dog walking', 'Need someone to walk my dog for 20 minutes', '2023-09-01', NULL, '16:30:00', 'CANCELLED', 'WALK', 'IN_PERSON'),
    (6, 6, 6, '75.00', 6, 'Dog boarding', 'Going out of town for a few days and need someone to take care of my dog', '2023-10-05', '2023-10-10', NULL, 'UPDATING', 'BOARDING', 'IN_PERSON'),
    (7, 7, 7, '40.00', 7, 'Cat sitting', 'Looking for someone to check on my cat twice a day while I am away', '2023-11-01', '2023-11-05', NULL, 'PENDING', 'SITTING', 'IN_PERSON'),
    (8, 8, 8, '60.00', 8, 'Dog training', 'Want to teach my dog some advanced commands like roll over and play dead', '2023-12-10', '2023-12-20', '10:00:00', 'CONFIRMED', 'TRAINING', 'IN_PERSON'),
    (9, 9, 9, '35.00', 9, 'Dog grooming', 'Need someone to give my dog a haircut and brush his fur', '2024-01-05', NULL, '14:00:00', 'PENDING', 'GROOMING', 'IN_PERSON'),
    (10, 10, 10, '80.00', 10, 'Other', 'Looking for someone to take care of my pet rabbit while I am away', '2024-02-01', '2024-02-05', NULL, 'UPDATING', 'OTHER', 'IN_PERSON')

ON CONFLICT (id) DO NOTHING;