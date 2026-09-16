package com.vm325.inventory_back.services;

import com.vm325.inventory_back.dtos.SupplierRequestDto;
import com.vm325.inventory_back.dtos.SupplierResponseDto;
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
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    @Override
    public SupplierResponseDto create(SupplierRequestDto dto) {
        Supplier supplier = Supplier.builder()
                .name(dto.getName())
                .contactName(dto.getContactName())
                .phone(dto.getPhone())
                .build();

        return toResponseDto(supplierRepository.save(supplier));
    }

    @Override
    public SupplierResponseDto update(Long id, SupplierRequestDto dto) {
        Supplier supplier = getSupplierOrThrow(id);
        supplier.setName(dto.getName());
        supplier.setContactName(dto.getContactName());
        supplier.setPhone(dto.getPhone());

        return toResponseDto(supplierRepository.save(supplier));
    }

    @Override
    public void delete(Long id) {
        Supplier supplier = getSupplierOrThrow(id);
        if (productRepository.existsBySupplier_SupplierId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar el proveedor porque tiene productos asociados");
        }
        supplierRepository.delete(supplier);
    }

    @Override
    public SupplierResponseDto findById(Long id) {
        return toResponseDto(getSupplierOrThrow(id));
    }

    @Override
    public List<SupplierResponseDto> findAll() {
        return supplierRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private Supplier getSupplierOrThrow(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Proveedor no encontrado con id " + id));
    }

    private SupplierResponseDto toResponseDto(Supplier supplier) {
        return SupplierResponseDto.builder()
                .supplierId(supplier.getSupplierId())
                .name(supplier.getName())
                .contactName(supplier.getContactName())
                .phone(supplier.getPhone())
                .build();
    }
}
