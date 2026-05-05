package com.example.shared_shopping_list_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ApiException extends RuntimeException {

    private final String code;
    private final HttpStatus status;
    private final Map<String, String> errors;

    public ApiException(String code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
        this.errors = null;
    }

    public ApiException(String code, HttpStatus status, String message, Map<String, String> errors) {
        super(message);
        this.code = code;
        this.status = status;
        this.errors = errors;
    }
}
