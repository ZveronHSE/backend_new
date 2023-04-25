INSERT INTO public.contact (additional_email, gmail, gmail_id, phone, vk_id, vk_ref, profile_id)
VALUES ('', '', '', '79993332211', '', '', '1'),
       ('', '', '', '79998882211', '', '', '2'),
       ('', '', '', '79997779977', '', '', '3')
ON CONFLICT DO NOTHING;