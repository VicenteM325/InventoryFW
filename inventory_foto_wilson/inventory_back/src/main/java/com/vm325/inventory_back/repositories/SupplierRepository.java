package com.vm325.inventory_back.repositories;

import com.vm325.inventory_back.entities.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
