package com.gao.product_service.products.enums;

import org.springframework.http.HttpStatus;

public enum ProductErrors {
    PRODUCT_NOT_FOUND("Product not found", HttpStatus.NOT_FOUND);

    private final String message;
    private final HttpStatus httpStatus;

    ProductErrors(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
