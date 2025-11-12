package com.vm325.inventory_back.repositories;

import com.vm325.inventory_back.entities.Role;
import com.vm325.inventory_back.enums.RoleList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByName(RoleList name);
}