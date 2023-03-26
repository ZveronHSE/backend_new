ALTER TABLE public.profile
    DROP COLUMN image_id;

ALTER TABLE public.profile
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(255) DEFAULT 'https://storage.yandexcloud.net/zveron-profile/random.jpeg';
