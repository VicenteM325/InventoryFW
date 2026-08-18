package com.vm325.inventory_back.repositories;

import com.vm325.inventory_back.entities.StockAlert;
import com.vm325.inventory_back.enums.StockAlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {
    List<StockAlert> findByStatusOrderByCreatedAtDesc(StockAlertStatus status);
    List<StockAlert> findByProduct_ProductIdAndStatus(Long productId, StockAlertStatus status);
}
