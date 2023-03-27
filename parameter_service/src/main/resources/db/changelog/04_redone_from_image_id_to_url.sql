-- changeset WolfAlm:1672868866023-3

ALTER TABLE public.category
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(300) DEFAULT 'https://storage.yandexcloud.net/zveron-profile/random.jpeg';