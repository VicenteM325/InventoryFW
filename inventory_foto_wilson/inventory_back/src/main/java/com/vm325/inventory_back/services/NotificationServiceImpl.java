package com.vm325.inventory_back.services;

import com.vm325.inventory_back.dtos.NotificationResponseDto;
import com.vm325.inventory_back.entities.Notification;
import com.vm325.inventory_back.enums.NotificationType;
import com.vm325.inventory_back.enums.RoleList;
import com.vm325.inventory_back.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public NotificationResponseDto notify(NotificationType type,
                                           String message,
                                           String relatedEntityType,
                                           Long relatedEntityId,
                                           RoleList recipientRole) {
        Notification notification = Notification.builder()
                .type(type)
                .message(message)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .recipientRole(recipientRole)
                .build();

        return toDto(notificationRepository.save(notification));
    }

    @Override
    public List<NotificationResponseDto> findAll(Boolean read) {
        List<Notification> notifications = (read == null)
                ? notificationRepository.findAllByOrderByCreatedAtDesc()
                : notificationRepository.findByReadOrderByCreatedAtDesc(read);

        return notifications.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationResponseDto markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Notificación no encontrada con id " + id));

        notification.setRead(true);
        return toDto(notificationRepository.save(notification));
    }

    private NotificationResponseDto toDto(Notification notification) {
        return NotificationResponseDto.builder()
                .notificationId(notification.getNotificationId())
                .type(notification.getType())
                .message(notification.getMessage())
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
