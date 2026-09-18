package com.vm325.inventory_back.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SupplierResponseDto {
    private Long supplierId;
    private String name;
    private String contactName;
    private String phone;
}
