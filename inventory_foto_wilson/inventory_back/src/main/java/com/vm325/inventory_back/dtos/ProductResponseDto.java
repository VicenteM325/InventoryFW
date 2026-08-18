package com.vm325.inventory_back.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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
}
