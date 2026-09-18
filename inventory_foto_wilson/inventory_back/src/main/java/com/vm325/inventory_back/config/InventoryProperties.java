package com.vm325.inventory_back.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Business-rule configuration for the inventory domain (Proceso 1 - Gestión
 * de inventario y reposición automática). Centralizes values that were
 * previously duplicated as inconsistent hardcoded constants across
 * ProductServiceImpl (1) and SaleServiceImpl (2).
 */
@Component
@ConfigurationProperties(prefix = "inventory")
@Getter
@Setter
public class InventoryProperties {

    /**
     * Minimum stock threshold per product model. When a sale leaves a
     * product's stock strictly below this value, the system must flag it
     * (StockAlert) and notify the responsible staff. Defaults to 2, per the
     * business rule documented in the Fase 1 BPM analysis.
     */
    private int stockMinimo = 2;
}
