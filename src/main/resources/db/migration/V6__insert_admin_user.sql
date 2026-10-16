UPDATE users SET username = 'yasir_customer' WHERE username = 'yasir' AND email != 'mail@mail.com';

INSERT INTO users (id, username, email, password, first_name, last_name, role, status, created_at)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'yasir',
    'mail@mail.com',
    '$2a$10$TUQ7CkWLf2v5apLqzdpBB.z9CcVcI9UU87CeobmhypizCjHtHtfiO',
    'yasir',
    'serttas',
    'ADMIN',
    'ACTIVE',
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO UPDATE
SET username = EXCLUDED.username,
    password = EXCLUDED.password,
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    role = EXCLUDED.role,
    status = EXCLUDED.status;
