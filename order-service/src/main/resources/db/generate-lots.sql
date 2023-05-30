-- Generate random data for 10 rows
INSERT INTO public.order_lot (profile_id, animal_id, price, subway_id, title, description, service_date_from, service_date_to, time_window_from, time_window_to, status, service_type, service_delivery_type, created_at)
SELECT
        floor(random() * 5) + 1 as profile_id,
        floor(random() * (230 - 91 + 1)) + 91 as animal_id,
        floor(random() * 1000) + 1 as price,
        floor(random() * 10) + 1 as subway_id,
        substr(md5(random()::text), 1, 10) as title,
        substr(md5(random()::text), 1, 20) as description,
        current_date + (random() * 30 || ' days')::interval as service_date_from,
        current_date + (random() * 30 || ' days')::interval as service_date_to,
        time '00:00' + (random() * 86399 || ' seconds')::interval as time_window_from,
        time '00:00' + (random() * 86399 || ' seconds')::interval as time_window_to,
        CASE floor(random() * 5)
            WHEN 0 THEN 'PENDING'
            WHEN 1 THEN 'CONFIRMED'
            WHEN 2 THEN 'COMPLETED'
            WHEN 3 THEN 'CANCELLED'
            WHEN 4 THEN 'UPDATING'
            END as status,
        CASE floor(random() * 6)
            WHEN 0 THEN 'WALK'
            WHEN 1 THEN 'SITTING'
            WHEN 2 THEN 'BOARDING'
            WHEN 3 THEN 'TRAINING'
            WHEN 4 THEN 'GROOMING'
            ELSE 'OTHER'
            END as service_type,
        CASE floor(random() * 2)
            WHEN 0 THEN 'REMOTE'
            ELSE 'IN_PERSON'
            END as service_delivery_type,
        current_timestamp as created_at
FROM generate_series(1, 1000);

