package com.gao.product_service.products.exceptions;

import com.gao.product_service.products.dto.ProductErrorResponseDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ProductsExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LogManager.getLogger(ProductsExceptionHandler.class);

    @ExceptionHandler(value = { ProductsException.class })
    protected ResponseEntity<Object> handleProductsError(ProductsException productsException, WebRequest webRequest) {
        ProductErrorResponseDto productErrorResponseDto = new ProductErrorResponseDto(
                productsException.getProductErrors().getMessage(),
                productsException.getProductErrors().getHttpStatus().value(),
                ThreadContext.get("requestId"),
                productsException.getProductId()
        );

        logger.error(productsException.getProductErrors().getMessage());

        return handleExceptionInternal(
                productsException,
                productErrorResponseDto,
                new HttpHeaders(),
                productsException.getProductErrors().getHttpStatus(),
                webRequest
        );
    }
}
