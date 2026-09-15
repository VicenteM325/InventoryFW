package com.vm325.inventory_back.services;

import com.vm325.inventory_back.dtos.NotificationResponseDto;
import com.vm325.inventory_back.enums.NotificationType;
import com.vm325.inventory_back.enums.RoleList;

import java.util.List;

public interface NotificationService {

    /**
     * Registra una notificación de un evento de negocio. No lanza excepción
     * ante fallos que no sean de datos inválidos: es una operación de
     * trazabilidad secundaria y nunca debe tumbar el flujo principal
     * (registrar una venta, generar un documento DPI) que la origina.
     *
     * @param type              tipo de evento.
     * @param message           mensaje legible para mostrar en el dashboard.
     * @param relatedEntityType nombre de la entidad relacionada (opcional, p.ej. "StockAlert").
     * @param relatedEntityId   id de la entidad relacionada (opcional).
     * @param recipientRole     rol destinatario, o null si aplica a todos los roles.
     */
    NotificationResponseDto notify(NotificationType type,
                                    String message,
                                    String relatedEntityType,
                                    Long relatedEntityId,
                                    RoleList recipientRole);

    List<NotificationResponseDto> findAll(Boolean read);

    NotificationResponseDto markAsRead(Long id);
}
