package com.vm325.inventory_back.entities;

import com.vm325.inventory_back.enums.NotificationType;
import com.vm325.inventory_back.enums.RoleList;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Notificación genérica de eventos críticos del sistema (RF-06). Alimentada
 * por eventos ya existentes (StockAlert al generarse) y por eventos nuevos
 * que no tienen una entidad propia de "estado" (por ejemplo, la generación
 * de un documento DPI, que no se persiste). No reemplaza a StockAlert, que
 * sigue siendo la fuente de la tarjeta de reposición de inventario; esta
 * entidad complementa con una vista agregada de eventos para el dashboard.
 */
@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(nullable = false)
    @Builder.Default
    private boolean read = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * A qué rol va dirigida la notificación. Nulo = visible para todos los
     * roles con acceso al sistema (Admin y Encargado).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_role", length = 20)
    private RoleList recipientRole;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
