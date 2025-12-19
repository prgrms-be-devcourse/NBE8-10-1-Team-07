package com.back.global.globalExceptionHandler;

import com.back.global.rsData.RsData;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RsData<Void>> handleConstraintViolation(ConstraintViolationException e) {
        return ResponseEntity
                .badRequest()
                .body(new RsData<>("400-1", e.getMessage(), null));
    }
}
