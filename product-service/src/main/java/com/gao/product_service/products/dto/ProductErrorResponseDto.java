package com.gao.product_service.products.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

public record ProductErrorResponseDto(
        String message,
        int statusCode,
        String requestId,
        @JsonInclude(JsonInclude.Include.NON_NULL) String productId
) {
}
