-- ========================================
--   BASE DE DATOS: Sistema de Ventas Internas Foto Wilson
--   Motor: PostgreSQL
--   Autor: Rocael
-- ========================================

-- Extensión para UUIDs
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

INSERT INTO role (name) VALUES
('ROLE_ADMIN'),
('ROLE_EMPLOYEE');

--VALORES DE INSERT username: admin contraseña: rocaeladmin
INSERT INTO users (user_id, name, username, password, role_id)
VALUES (gen_random_uuid(), 'Rocael Osorio', 'admin',
        '$2a$12$WophxhfuXZNuixcQYrk/deymuewDiUw1LCYysJpv3E.kgG0z1ryJm',1)
        ON CONFLICT (username) DO NOTHING;
