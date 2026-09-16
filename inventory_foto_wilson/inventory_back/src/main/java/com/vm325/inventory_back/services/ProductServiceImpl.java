package com.vm325.inventory_back.services;

import com.vm325.inventory_back.config.InventoryProperties;
import com.vm325.inventory_back.dtos.ProductRequestDto;
import com.vm325.inventory_back.dtos.ProductResponseDto;
import com.vm325.inventory_back.entities.Product;
import com.vm325.inventory_back.entities.Supplier;
import com.vm325.inventory_back.repositories.ProductRepository;
import com.vm325.inventory_back.repositories.SupplierRepository;
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
    private final SupplierRepository supplierRepository;
    private final StockStatusCalculator stockStatusCalculator;
    private final InventoryProperties inventoryProperties;

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
                .price(dto.getPrice())
                .category(dto.getCategory())
                .minStock(dto.getMinStock() != null ? dto.getMinStock() : inventoryProperties.getStockMinimo())
                .supplier(resolveSupplier(dto.getSupplierId()))
                .active(true)
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
        product.setPrice(dto.getPrice());
        product.setCategory(dto.getCategory());
        product.setMinStock(dto.getMinStock() != null ? dto.getMinStock() : inventoryProperties.getStockMinimo());
        product.setSupplier(resolveSupplier(dto.getSupplierId()));

        return toResponseDto(productRepository.save(product));
    }

    @Override
    public void delete(Long id) {
        productRepository.delete(getProductOrThrow(id));
    }

    private Supplier resolveSupplier(Long supplierId) {
        if (supplierId == null) {
            return null;
        }
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Proveedor no encontrado con id " + supplierId));
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
        String stockStatus = stockStatusCalculator.resolve(product.getStock(), product.getMinStock());
        Supplier supplier = product.getSupplier();

        return ProductResponseDto.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .barcode(product.getBarcode())
                .description(product.getDescription())
                .stock(product.getStock())
                .stockStatus(stockStatus)
                .price(product.getPrice())
                .category(product.getCategory())
                .minStock(product.getMinStock())
                .active(product.isActive())
                .supplierId(supplier != null ? supplier.getSupplierId() : null)
                .supplierName(supplier != null ? supplier.getName() : null)
                .build();
    }
}
