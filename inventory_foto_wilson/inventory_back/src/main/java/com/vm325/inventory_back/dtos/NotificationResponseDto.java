package com.vm325.inventory_back.dtos;

import com.vm325.inventory_back.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class NotificationResponseDto {
    private Long notificationId;
    private NotificationType type;
    private String message;
    private String relatedEntityType;
    private Long relatedEntityId;
    private boolean read;
    private LocalDateTime createdAt;
}
