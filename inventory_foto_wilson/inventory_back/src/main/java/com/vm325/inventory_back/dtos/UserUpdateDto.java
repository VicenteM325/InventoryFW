package com.vm325.inventory_back.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDto {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El rol es obligatorio")
    private String roleName;
}
