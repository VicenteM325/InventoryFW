package com.vm325.inventory_back.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierRequestDto {
    @NotBlank(message = "El nombre del proveedor es obligatorio")
    private String name;

    private String contactName;

    private String phone;
}
