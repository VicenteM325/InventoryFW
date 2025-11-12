package com.vm325.inventory_back.repositories;

import com.vm325.inventory_back.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUserName(String userName);
    boolean existsByUserName(String userName);
}