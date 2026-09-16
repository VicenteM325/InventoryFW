package com.vm325.inventory_back.services;

import org.springframework.stereotype.Component;

/**
 * Único lugar que decide el estado de stock de un producto (DISPONIBLE /
 * STOCK_BAJO / AGOTADO), usado tanto por ProductServiceImpl (respuesta de
 * la API de productos) como por InventoryReportService (reporte de
 * inventario), para no duplicar esta lógica en dos sitios.
 * <p>
 * El umbral es por producto ({@code minStock}), no un único valor global:
 * la regla de negocio del documento de análisis dice explícitamente "stock
 * mínimo por modelo". {@link com.vm325.inventory_back.config.InventoryProperties}
 * solo se usa como valor por defecto al crear un producto que no especifica
 * el suyo propio.
 */
@Component
public class StockStatusCalculator {

    public String resolve(int stock, int minStock) {
        if (stock <= 0) {
            return "AGOTADO";
        }
        if (stock < minStock) {
            return "STOCK_BAJO";
        }
        return "DISPONIBLE";
    }
}
