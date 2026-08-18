package com.vm325.inventory_back.repositories;

import com.vm325.inventory_back.entities.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findTop10ByOrderByDateDesc();
}