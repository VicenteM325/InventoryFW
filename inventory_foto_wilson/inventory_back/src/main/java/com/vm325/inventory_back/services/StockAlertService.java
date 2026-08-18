package com.vm325.inventory_back.services;

import com.vm325.inventory_back.dtos.StockAlertResponseDto;

import java.util.List;

public interface StockAlertService {
    List<StockAlertResponseDto> findPending();
    StockAlertResponseDto attend(Long id);
}
