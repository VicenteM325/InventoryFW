package com.vm325.inventory_back.dtos;

import com.vm325.inventory_back.enums.StockAlertStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class StockAlertResponseDto {
    private Long stockAlertId;
    private Long productId;
    private String productName;
    private Integer stockAtAlert;
    private StockAlertStatus status;
    private LocalDateTime createdAt;
}
