package com.vm325.inventory_back.config;

import com.vm325.inventory_back.dtos.ApiMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiMessage> handleGeneralException(Exception ex){
        String errorMessage = ex.getMessage();
        return ResponseEntity.internalServerError().body(new ApiMessage(errorMessage));
    }
}