package com.vm325.inventory_back.enums;

/**
 * Tipo de evento de negocio que originó una Notification (RF-06). Se espera
 * que crezca a medida que se agreguen más eventos críticos del sistema.
 */
public enum NotificationType {
    STOCK_BAJO,
    DOCUMENTO_GENERADO
}
