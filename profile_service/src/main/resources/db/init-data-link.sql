INSERT INTO public.communication_link (id, communication_link_id, data, profile_id, type)
VALUES ('1', '79993332211', '{"type": "phone-communication-link"}', '1', 'PHONE'),
       ('2', '79998882211', '{"type": "phone-communication-link"}', '2', 'PHONE'),
       ('3', '79997779977', '{"type": "phone-communication-link"}', '3', 'PHONE')
ON CONFLICT DO NOTHING;