INSERT INTO public.settings (profile_id, search_address_id, channels)
VALUES ('1', '-1', '{"vk": false, "chat": true, "gmail": false, "phone": true}'),
       ('2', '-1', '{"vk": false, "chat": true, "gmail": false, "phone": true}'),
       ('3', '-1', '{"vk": false, "chat": true, "gmail": false, "phone": true}')
ON CONFLICT DO NOTHING;