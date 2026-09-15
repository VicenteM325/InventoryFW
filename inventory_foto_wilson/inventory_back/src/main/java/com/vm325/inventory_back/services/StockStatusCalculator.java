package com.vm325.inventory_back.services;

import com.vm325.inventory_back.config.InventoryProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Único lugar que decide el estado de stock de un producto (DISPONIBLE /
 * STOCK_BAJO / AGOTADO), usado tanto por ProductServiceImpl (respuesta de
 * la API de productos) como por InventoryReportService (reporte de
 * inventario), para no duplicar el umbral de stock mínimo en dos sitios.
 */
@Component
@RequiredArgsConstructor
public class StockStatusCalculator {

    private final InventoryProperties inventoryProperties;

    public String resolve(int stock) {
        if (stock <= 0) {
            return "AGOTADO";
        }
        if (stock < inventoryProperties.getStockMinimo()) {
            return "STOCK_BAJO";
        }
        return "DISPONIBLE";
    }
}
