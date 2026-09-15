package com.vm325.inventory_back.config;

import com.vm325.inventory_back.dtos.ApiMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiMessage> handValidationException(MethodArgumentNotValidException ex){
        String errorMessage = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
        return ResponseEntity.badRequest().body(new ApiMessage(errorMessage));
    }

    // Sin este handler específico, ResponseStatusException (usada en casi
    // todos los servicios para 400/404/409, y en el nuevo módulo de
    // documentos DPI) caía en el @ExceptionHandler(Exception.class) genérico
    // de abajo y siempre se devolvía como 500, perdiendo el código de
    // estado real que el propio servicio ya había decidido.
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiMessage> handleResponseStatusException(ResponseStatusException ex){
        return ResponseEntity.status(ex.getStatusCode()).body(new ApiMessage(ex.getReason()));
    }

    // Un archivo (p.ej. una foto de DPI) que exceda spring.servlet.multipart.*
    // debe verse como un 400 claro para el cliente, no como el 500 genérico.
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiMessage> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex){
        return ResponseEntity.badRequest().body(new ApiMessage("El archivo adjunto supera el tamaño máximo permitido"));
    }

    // Falta un campo multipart requerido (p.ej. no se adjuntó "front"/"back"
    // al generar un documento DPI): 400 con mensaje claro, no 500.
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiMessage> handleMissingPart(MissingServletRequestPartException ex){
        return ResponseEntity.badRequest().body(new ApiMessage("Falta el campo requerido: " + ex.getRequestPartName()));
    }

    // Un usuario autenticado sin el rol requerido por @PreAuthorize debe
    // recibir 403, no el 500 genérico (AccessDeniedException también caía
    // en el handler de abajo).
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiMessage> handleAccessDenied(AccessDeniedException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiMessage("No tiene permisos para realizar esta acción"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiMessage> handleGeneralException(Exception ex){
        String errorMessage = ex.getMessage();
        return ResponseEntity.internalServerError().body(new ApiMessage(errorMessage));
    }
}