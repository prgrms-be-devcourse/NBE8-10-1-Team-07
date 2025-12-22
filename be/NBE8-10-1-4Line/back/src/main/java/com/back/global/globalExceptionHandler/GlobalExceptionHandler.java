package com.back.global.globalExceptionHandler;

import com.back.global.rsData.RsData;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ QueryParam / PathVariable 검증 실패
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RsData<Void>> handleConstraintViolation(ConstraintViolationException e) {

        String msg = e.getConstraintViolations()
                .stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("요청 값이 올바르지 않습니다.");

        return ResponseEntity
                .badRequest()
                .body(new RsData<>("400-1", msg, null));
    }

    // ✅ @Valid @RequestBody 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RsData<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {

        FieldError fe = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .orElse(null);

        String msg = (fe != null && fe.getDefaultMessage() != null && !fe.getDefaultMessage().isBlank())
                ? fe.getDefaultMessage()
                : "요청 값이 올바르지 않습니다.";

        return ResponseEntity
                .badRequest()
                .body(new RsData<>("400-1", msg, null));
    }

    // (선택) IllegalArgumentException 통일
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RsData<Void>> handleIllegalArgument(IllegalArgumentException e) {
        String msg = (e.getMessage() != null && !e.getMessage().isBlank())
                ? e.getMessage()
                : "잘못된 요청입니다.";

        return ResponseEntity
                .badRequest()
                .body(new RsData<>("400-1", msg, null));
    }

    // (선택) IllegalStateException 통일
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<RsData<Void>> handleIllegalState(IllegalStateException e) {
        String msg = (e.getMessage() != null && !e.getMessage().isBlank())
                ? e.getMessage()
                : "처리할 수 없는 상태입니다.";

        return ResponseEntity
                .badRequest()
                .body(new RsData<>("400-1", msg, null));
    }
}
