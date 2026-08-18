package com.vm325.inventory_back.controllers;

import com.vm325.inventory_back.dtos.SaleRequestDto;
import com.vm325.inventory_back.dtos.SaleResponseDto;
import com.vm325.inventory_back.services.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {
    private final SaleService saleService;

    @PostMapping
    public ResponseEntity<SaleResponseDto> registerSale(@Valid @RequestBody SaleRequestDto dto,
                                                        Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(saleService.registerSale(dto, username));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<SaleResponseDto>> findRecent() {
        return ResponseEntity.ok(saleService.findRecent());
    }
}
