package com.gao.product_service.products.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductsController {
    private static final Logger log = LogManager.getLogger(ProductsController.class);

    @GetMapping
    public String getAllProducts() {
        log.info("Get all products");
        return "All Products";
    }
}
