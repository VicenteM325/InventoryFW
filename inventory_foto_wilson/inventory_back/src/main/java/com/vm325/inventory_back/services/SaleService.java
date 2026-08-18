package com.vm325.inventory_back.services;

import com.vm325.inventory_back.dtos.SaleRequestDto;
import com.vm325.inventory_back.dtos.SaleResponseDto;

import java.util.List;

public interface SaleService {
    SaleResponseDto registerSale(SaleRequestDto dto, String username);
    List<SaleResponseDto> findRecent();
}
