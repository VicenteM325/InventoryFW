package com.vm325.inventory_back.dtos;

import com.vm325.inventory_back.enums.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class ProductResponseDto {
    private Long productId;
    private String name;
    private String barcode;
    private String description;
    private Integer stock;
    private String stockStatus; // DISPONIBLE | STOCK_BAJO | AGOTADO
    private BigDecimal price;
    private ProductCategory category;
    private Integer minStock;
    private boolean active;
    private Long supplierId;
    private String supplierName;
}
