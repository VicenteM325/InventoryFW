-- Completa el modelo de Producto frente al documento de análisis original:
-- precio (estaba en el diagrama de clases pero nunca se implementó),
-- categoría y proveedor (aparecen en las maquetas), stock mínimo por
-- producto (la regla de negocio dice "por modelo", no un único valor
-- global), y baja lógica (RF-02 pide "baja", no solo borrado físico).
ALTER TABLE product
    ADD COLUMN price NUMERIC(10, 2) NOT NULL DEFAULT 0,
    ADD COLUMN category VARCHAR(20),
    ADD COLUMN min_stock INTEGER NOT NULL DEFAULT 2,
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN supplier_id BIGINT REFERENCES supplier(supplier_id);
