package com.vm325.inventory_back.controllers;

import com.vm325.inventory_back.dtos.StockAlertResponseDto;
import com.vm325.inventory_back.services.StockAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-alerts")
@RequiredArgsConstructor
public class StockAlertController {
    private final StockAlertService stockAlertService;

    @GetMapping("/pending")
    public ResponseEntity<List<StockAlertResponseDto>> findPending() {
        return ResponseEntity.ok(stockAlertService.findPending());
    }

    // Solo Administrador puede marcar una alerta como atendida
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/attend")
    public ResponseEntity<StockAlertResponseDto> attend(@PathVariable Long id) {
        return ResponseEntity.ok(stockAlertService.attend(id));
    }
}
