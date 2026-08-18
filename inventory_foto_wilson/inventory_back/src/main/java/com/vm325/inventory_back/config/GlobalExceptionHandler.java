package com.vm325.inventory_back.config;

import com.vm325.inventory_back.dtos.ApiMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiMessage> handValidationException(MethodArgumentNotValidException ex){
        String errorMessage = Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
        return ResponseEntity.badRequest().body(new ApiMessage(errorMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiMessage> handleGeneralException(Exception ex){
        String errorMessage = ex.getMessage();
        return ResponseEntity.internalServerError().body(new ApiMessage(errorMessage));
    }
}