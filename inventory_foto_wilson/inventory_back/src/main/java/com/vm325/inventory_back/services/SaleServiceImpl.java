package com.vm325.inventory_back.services;

import com.vm325.inventory_back.dtos.*;
import com.vm325.inventory_back.entities.Product;
import com.vm325.inventory_back.entities.Sale;
import com.vm325.inventory_back.entities.SaleDetail;
import com.vm325.inventory_back.entities.StockAlert;
import com.vm325.inventory_back.entities.User;
import com.vm325.inventory_back.enums.StockAlertStatus;
import com.vm325.inventory_back.repositories.ProductRepository;
import com.vm325.inventory_back.repositories.SaleRepository;
import com.vm325.inventory_back.repositories.StockAlertRepository;
import com.vm325.inventory_back.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final StockAlertRepository stockAlertRepository;
    private final UserRepository userRepository;
    private static final int STOCK_MINIMO = 2;

    @Override
    @Transactional
    public SaleResponseDto registerSale(SaleRequestDto dto, String username) {
        // Obtener el empleado
        User employee = userRepository.findByUserName(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Usuario no encontrado: " + username));

        // VALIDAR Y ACTUALIZAR STOCK DE CADA PRODUCTO
        List<Product> productosActualizados = new ArrayList<>();

        for (SaleItemRequestDto item : dto.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Producto no encontrado con id " + item.getProductId()));

            // Validar stock disponible
            if (product.getStock() < item.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Stock insuficiente para el producto: " + product.getName() +
                                ". Stock disponible: " + product.getStock() +
                                ", solicitado: " + item.getQuantity());
            }

            // Actualizar stock (restar la cantidad vendida)
            int nuevoStock = product.getStock() - item.getQuantity();
            product.setStock(nuevoStock);
            productosActualizados.add(product);
        }

        // Guardar todos los productos actualizados
        productRepository.saveAll(productosActualizados);

        // Crear la venta
        Sale sale = Sale.builder()
                .employee(employee)
                .customerName(dto.getCustomerName())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        // Crear los detalles de la venta
        for (SaleItemRequestDto item : dto.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Producto no encontrado con id " + item.getProductId()));

            SaleDetail detail = SaleDetail.builder()
                    .product(product)
                    .amount(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .build();
            sale.addDetail(detail);

            total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        sale.setTotal(total);

        // Guardar la venta
        Sale savedSale = saleRepository.save(sale);

        // Generar alertas de stock si es necesario
        List<Long> alertedProductIds = new ArrayList<>();
        List<SaleItemResponseDto> itemDtos = new ArrayList<>();

        for (SaleDetail detail : savedSale.getDetails()) {
            Product product = detail.getProduct();

            // Verificar si el stock está por debajo del mínimo
            if (product.getStock() < STOCK_MINIMO) {
                boolean alreadyPending = !stockAlertRepository
                        .findByProduct_ProductIdAndStatus(product.getProductId(), StockAlertStatus.PENDIENTE)
                        .isEmpty();

                if (!alreadyPending) {
                    StockAlert alert = StockAlert.builder()
                            .product(product)
                            .stockAtAlert(product.getStock())
                            .build();
                    stockAlertRepository.save(alert);
                    alertedProductIds.add(product.getProductId());
                }
            }

            // Crear DTO de respuesta con el stock actualizado
            itemDtos.add(SaleItemResponseDto.builder()
                    .productId(product.getProductId())
                    .productName(product.getName())
                    .quantity(detail.getAmount())
                    .unitPrice(detail.getUnitPrice())
                    .subtotal(detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getAmount())))
                    .remainingStock(product.getStock())
                    .build());
        }

        // 8. Retornar respuesta
        return SaleResponseDto.builder()
                .salesId(savedSale.getSalesId())
                .employeeUsername(employee.getUserName())
                .customerName(savedSale.getCustomerName())
                .date(savedSale.getDate())
                .total(savedSale.getTotal())
                .items(itemDtos)
                .stockAlertsGeneratedFor(alertedProductIds)
                .build();
    }

    @Override
    public List<SaleResponseDto> findRecent() {
        return saleRepository.findTop10ByOrderByDateDesc()
                .stream()
                .map(sale -> SaleResponseDto.builder()
                        .salesId(sale.getSalesId())
                        .employeeUsername(sale.getEmployee().getUserName())
                        .customerName(sale.getCustomerName())
                        .date(sale.getDate())
                        .total(sale.getTotal())
                        .items(sale.getDetails().stream()
                                .map(d -> SaleItemResponseDto.builder()
                                        .productId(d.getProduct().getProductId())
                                        .productName(d.getProduct().getName())
                                        .quantity(d.getAmount())
                                        .unitPrice(d.getUnitPrice())
                                        .subtotal(d.getUnitPrice().multiply(BigDecimal.valueOf(d.getAmount())))
                                        .remainingStock(d.getProduct().getStock())
                                        .build())
                                .collect(Collectors.toList()))
                        .stockAlertsGeneratedFor(List.of())
                        .build())
                .collect(Collectors.toList());
    }
}