package com.vm325.inventory_back.controllers;

import com.vm325.inventory_back.dtos.SupplierRequestDto;
import com.vm325.inventory_back.dtos.SupplierResponseDto;
import com.vm325.inventory_back.services.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<SupplierResponseDto> create(@Valid @RequestBody SupplierRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponseDto> update(@PathVariable Long id,
                                                       @Valid @RequestBody SupplierRequestDto dto) {
        return ResponseEntity.ok(supplierService.update(id, dto));
    }

    // Igual que en Productos: solo Administrador puede eliminar.
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponseDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponseDto>> findAll() {
        return ResponseEntity.ok(supplierService.findAll());
    }
}
