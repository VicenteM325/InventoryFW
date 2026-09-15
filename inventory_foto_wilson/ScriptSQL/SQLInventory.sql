-- ========================================
--   BASE DE DATOS: Sistema de Ventas Internas Foto Wilson
--   Motor: PostgreSQL
--   Autor: Rocael
-- ========================================
--
-- NOTA (referencia histórica, no ejecutado por la aplicación):
-- Este script fue el diseño original de base de datos y se conserva aquí
-- solo como documentación de esa etapa temprana del proyecto. El backend
-- real NUNCA lo ejecuta: el esquema efectivo vive versionado en
-- inventory_back/src/main/resources/db/migration/ (Flyway), que es la
-- única fuente de verdad del esquema. En particular, los triggers
-- trg_update_product_stock/trg_restore_product_stock de este archivo NO
-- deben aplicarse contra la base real, porque duplicarían el descuento de
-- stock que SaleServiceImpl ya realiza en código Java; y las tablas
-- service/service_detail aquí definidas no tienen entidades JPA
-- equivalentes (quedaron pensadas para un alcance que no se implementó).
-- ========================================

-- Extensión para UUIDs
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ========================================
-- TABLA DE ROLES
-- ========================================
CREATE TABLE role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

INSERT INTO role (name) VALUES
('ROLE_ADMIN'),
('ROLE_EMPLOYEE');

-- ========================================
-- TABLA DE USUARIOS
-- ========================================
CREATE TABLE users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role_id INT REFERENCES role(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========================================
-- TABLA DE PRODUCTOS
-- ========================================
CREATE TABLE product (
    product_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    barcode VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    stock INT DEFAULT 0 CHECK (stock >= 0)
);

-- ========================================
-- TABLA DE SERVICIOS
-- ========================================
CREATE TABLE service (
    service_id SERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0)
);

-- ========================================
-- TABLA DE VENTAS
-- ========================================
CREATE TABLE sales (
    sales_id SERIAL PRIMARY KEY,
    employee_id UUID NOT NULL REFERENCES users(user_id) ON DELETE SET NULL,
    customer_name VARCHAR(100),
    date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10,2) NOT NULL CHECK (total >= 0)
);

-- ========================================
-- DETALLES DE VENTA DE PRODUCTOS
-- ========================================
CREATE TABLE sale_detail (
    sale_detail_id SERIAL PRIMARY KEY,
    sales_id INT NOT NULL REFERENCES sales(sales_id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES product(product_id) ON DELETE SET NULL,
    amount INT NOT NULL CHECK (amount > 0),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0)
);

-- ========================================
-- DETALLES DE VENTA DE SERVICIOS
-- ========================================
CREATE TABLE service_detail (
    service_detail_id SERIAL PRIMARY KEY,
    sales_id INT NOT NULL REFERENCES sales(sales_id) ON DELETE CASCADE,
    service_id INT NOT NULL REFERENCES service(service_id) ON DELETE SET NULL,
    amount INT NOT NULL CHECK (amount > 0),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0)
);

-- ========================================
-- TRIGGER PARA ACTUALIZAR STOCK (al insertar venta de producto)
-- ========================================
CREATE OR REPLACE FUNCTION update_product_stock()
RETURNS TRIGGER AS $$
BEGIN
    -- Verificar stock suficiente
    IF (SELECT stock FROM product WHERE product_id = NEW.product_id) < NEW.amount THEN
        RAISE EXCEPTION 'No hay suficiente stock para el producto con ID %', NEW.product_id;
    END IF;

    -- Actualizar stock
    UPDATE product
    SET stock = stock - NEW.amount
    WHERE product_id = NEW.product_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_product_stock
AFTER INSERT ON sale_detail
FOR EACH ROW
EXECUTE FUNCTION update_product_stock();

-- ========================================
-- TRIGGER PARA RESTAURAR STOCK (al eliminar detalle de venta)
-- ========================================
CREATE OR REPLACE FUNCTION restore_product_stock()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE product
    SET stock = stock + OLD.amount
    WHERE product_id = OLD.product_id;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_restore_product_stock
AFTER DELETE ON sale_detail
FOR EACH ROW
EXECUTE FUNCTION restore_product_stock();

-- ========================================
-- FIN DEL SCRIPT
-- ========================================

--VALORES DE INSERT username: admin contraseña: rocaeladmin
INSERT INTO users (user_id, name, username, password, role_id)
VALUES (gen_random_uuid(), 'Rocael Osorio', 'admin',
        '$2a$12$WophxhfuXZNuixcQYrk/deymuewDiUw1LCYysJpv3E.kgG0z1ryJm',1);
