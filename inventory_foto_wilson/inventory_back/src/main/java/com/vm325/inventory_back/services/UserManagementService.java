package com.vm325.inventory_back.services;

import com.vm325.inventory_back.dtos.UserCreateDto;
import com.vm325.inventory_back.dtos.UserResponseDto;
import com.vm325.inventory_back.dtos.UserUpdateDto;

import java.util.List;
import java.util.UUID;

/**
 * Administración de cuentas de usuario por un Admin (crear, editar,
 * activar/desactivar). Deliberadamente separado de {@link UserService},
 * que implementa UserDetailsService y solo resuelve autenticación.
 */
public interface UserManagementService {
    List<UserResponseDto> findAll();
    UserResponseDto create(UserCreateDto dto);
    UserResponseDto update(UUID id, UserUpdateDto dto);
    UserResponseDto setActive(UUID id, boolean active);
}
