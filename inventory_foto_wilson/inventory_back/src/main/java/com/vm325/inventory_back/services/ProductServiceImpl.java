package com.vm325.inventory_back.services;

import com.vm325.inventory_back.dtos.ProductRequestDto;
import com.vm325.inventory_back.dtos.ProductResponseDto;
import com.vm325.inventory_back.entities.Product;
import com.vm325.inventory_back.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final StockStatusCalculator stockStatusCalculator;

    @Override
    public ProductResponseDto create(ProductRequestDto dto) {
        if (productRepository.findByBarcode(dto.getBarcode()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un producto con el código de barras " + dto.getBarcode());
        }

        Product product = Product.builder()
                .name(dto.getName())
                .barcode(dto.getBarcode())
                .description(dto.getDescription())
                .stock(dto.getStock())
                .build();

        return toResponseDto(productRepository.save(product));
    }

    @Override
    public ProductResponseDto update(Long id, ProductRequestDto dto) {
        Product product = getProductOrThrow(id);

        product.setName(dto.getName());
        product.setBarcode(dto.getBarcode());
        product.setDescription(dto.getDescription());
        product.setStock(dto.getStock());

        return toResponseDto(productRepository.save(product));
    }

    @Override
    public void delete(Long id) {
        productRepository.delete(getProductOrThrow(id));
    }

    @Override
    public ProductResponseDto findById(Long id) {
        return toResponseDto(getProductOrThrow(id));
    }

    @Override
    public List<ProductResponseDto> findAll() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private Product getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado con id " + id));
    }

    private ProductResponseDto toResponseDto(Product product) {
        String stockStatus = stockStatusCalculator.resolve(product.getStock());

        return ProductResponseDto.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .barcode(product.getBarcode())
                .description(product.getDescription())
                .stock(product.getStock())
                .stockStatus(stockStatus)
                .build();
    }
}
