package com.vm325.inventory_back.dtos;

import com.vm325.inventory_back.enums.ProductCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequestDto {
    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;

    @NotBlank(message = "El código de barras es obligatorio")
    private String barcode;

    private String description;

    @NotNull(message = "El stock inicial es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    private BigDecimal price;

    @NotNull(message = "La categoría es obligatoria")
    private ProductCategory category;

    /** Opcional: si se omite, se usa el mínimo configurado por defecto. */
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer minStock;

    /** Opcional: producto sin proveedor asignado. */
    private Long supplierId;
}
