package com.vm325.inventory_back.services;

import com.vm325.inventory_back.dtos.SupplierRequestDto;
import com.vm325.inventory_back.dtos.SupplierResponseDto;

import java.util.List;

public interface SupplierService {
    SupplierResponseDto create(SupplierRequestDto dto);
    SupplierResponseDto update(Long id, SupplierRequestDto dto);
    void delete(Long id);
    SupplierResponseDto findById(Long id);
    List<SupplierResponseDto> findAll();
}
