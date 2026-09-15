package com.vm325.inventory_back.controllers;

import com.vm325.inventory_back.dtos.UserCreateDto;
import com.vm325.inventory_back.dtos.UserResponseDto;
import com.vm325.inventory_back.dtos.UserUpdateDto;
import com.vm325.inventory_back.services.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Gestión de usuarios (Encargados) por un Administrador. No hay registro
// público: RF-01 no lo pide y este es un sistema interno de acceso
// restringido, así que dar de alta cuentas es exclusivamente tarea de un
// Admin.
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserManagementService userManagementService;

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> findAll() {
        return ResponseEntity.ok(userManagementService.findAll());
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> create(@Valid @RequestBody UserCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userManagementService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> update(@PathVariable UUID id, @Valid @RequestBody UserUpdateDto dto) {
        return ResponseEntity.ok(userManagementService.update(id, dto));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<UserResponseDto> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(userManagementService.setActive(id, false));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<UserResponseDto> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(userManagementService.setActive(id, true));
    }
}
