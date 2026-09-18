package com.vm325.inventory_back.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UserResponseDto {
    private UUID userId;
    private String name;
    private String userName;
    private String roleName;
    private boolean active;
}
