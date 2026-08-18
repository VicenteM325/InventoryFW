package com.vm325.inventory_back.services;

import com.vm325.inventory_back.dtos.ProductRequestDto;
import com.vm325.inventory_back.dtos.ProductResponseDto;

import java.util.List;

public interface ProductService {
    ProductResponseDto create(ProductRequestDto dto);
    ProductResponseDto update(Long id, ProductRequestDto dto);
    void delete(Long id);
    ProductResponseDto findById(Long id);
    List<ProductResponseDto> findAll();
}