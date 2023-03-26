-- changeset Schuyweiz:image_id_to_image_url
ALTER TABLE public.lot_photo
    DROP COLUMN image_id;

ALTER TABLE public.lot_photo
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(255) DEFAULT 'https://storage.yandexcloud.net/zveron-profile/random.jpeg';
