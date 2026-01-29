package com.faith.wallet_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleWalletNotFound(WalletNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorDetails.builder()
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .message(e.getMessage())
                        .time(LocalDateTime.now())
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .build());
    }

    @ExceptionHandler(DuplicateTransactionException.class)
    public ResponseEntity<ErrorDetails> handleDuplicateTransaction(DuplicateTransactionException e) {
        return ResponseEntity.badRequest().body(
                ErrorDetails.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(e.getMessage())
                        .time(LocalDateTime.now())
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .build());
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorDetails> handleInsufficientBalance(InsufficientBalanceException e) {
        return ResponseEntity.badRequest().body(
                ErrorDetails.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(e.getMessage())
                        .time(LocalDateTime.now())
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetails> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(
                ErrorDetails.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(e.getMessage())
                        .time(LocalDateTime.now())
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(
                ErrorDetails.builder()
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .message(message)
                        .time(LocalDateTime.now())
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGeneral(Exception e, WebRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorDetails.builder()
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message(e.getMessage())
                        .time(LocalDateTime.now())
                        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
    }
}
