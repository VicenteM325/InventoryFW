-- Gestión de usuarios: permite a un Admin desactivar la cuenta de un
-- Encargado (revocando su acceso) sin borrar su historial de ventas.
ALTER TABLE users ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
