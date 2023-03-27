-- changeset Schuyweiz:image_url
ALTER TABLE public.lot_photo
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(300) DEFAULT 'https://storage.yandexcloud.net/zveron-profile/random.jpeg';

ALTER TABLE public.lot_photo
    DROP COLUMN image_id;