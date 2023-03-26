-- changeset Schuyweiz:image_url

ALTER TABLE public.lot_photo
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(255) DEFAULT 'https://storage.yandexcloud.net/zveron-profile/random.jpeg';
