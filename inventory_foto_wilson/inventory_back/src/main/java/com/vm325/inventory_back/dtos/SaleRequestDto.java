package com.vm325.inventory_back.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SaleRequestDto {
    private String customerName;

    @NotEmpty(message = "La venta debe incluir al menos un producto")
    @Valid
    private List<SaleItemRequestDto> items;
}
