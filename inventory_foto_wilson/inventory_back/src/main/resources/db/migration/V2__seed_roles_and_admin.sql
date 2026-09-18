-- Seed data migrated from the old src/main/resources/data.sql, fixing its
-- lack of idempotency for the role inserts (the admin user insert already
-- guarded itself with ON CONFLICT; role did not, and would fail on any
-- restart against a database where roles already exist since role.name has
-- no unique constraint to key an ON CONFLICT off of).

INSERT INTO role (name)
SELECT 'ROLE_ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE name = 'ROLE_ADMIN');

INSERT INTO role (name)
SELECT 'ROLE_EMPLOYEE'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE name = 'ROLE_EMPLOYEE');

-- Usuario admin por defecto. username: admin / contraseña: rocaeladmin
INSERT INTO users (user_id, name, username, password, role_id)
SELECT gen_random_uuid(),
       'Rocael Osorio',
       'admin',
       '$2a$12$WophxhfuXZNuixcQYrk/deymuewDiUw1LCYysJpv3E.kgG0z1ryJm',
       (SELECT id FROM role WHERE name = 'ROLE_ADMIN')
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');
