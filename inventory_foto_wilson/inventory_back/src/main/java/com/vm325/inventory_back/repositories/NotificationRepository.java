package com.vm325.inventory_back.repositories;

import com.vm325.inventory_back.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReadOrderByCreatedAtDesc(boolean read);
    List<Notification> findAllByOrderByCreatedAtDesc();
}
