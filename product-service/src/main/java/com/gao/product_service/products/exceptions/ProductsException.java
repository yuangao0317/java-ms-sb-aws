package com.gao.product_service.products.exceptions;

import com.gao.product_service.products.enums.ProductErrors;
import jakarta.annotation.Nullable;

public class ProductsException extends Exception {
    private final ProductErrors productErrors;

    @Nullable
    private final String productId;

    public ProductsException(ProductErrors productErrors, @Nullable String productId) {
        this.productErrors = productErrors;
        this.productId = productId;
    }

    @Nullable
    public String getProductId() {
        return productId;
    }

    public ProductErrors getProductErrors() {
        return productErrors;
    }
}
