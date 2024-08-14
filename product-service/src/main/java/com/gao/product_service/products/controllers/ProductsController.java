package com.gao.product_service.products.controllers;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.gao.product_service.products.dto.ProductDto;
import com.gao.product_service.products.enums.ProductErrors;
import com.gao.product_service.products.exceptions.ProductsException;
import com.gao.product_service.products.models.Product;
import com.gao.product_service.products.repositories.ProductsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionException;

@RestController
@RequestMapping("/api/products")
@XRayEnabled
public class ProductsController {
    private static final Logger logger = LogManager.getLogger(ProductsController.class);
    private final ProductsRepository productsRepository;

    @Autowired
    public ProductsController(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        logger.info("Get all products");
        return new ResponseEntity<>(productsRepository.getAll(), HttpStatus.OK);
    }
    // @RequestParam(name = "page", defaultValue = 1)
    @GetMapping("{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable("id") String id) throws ProductsException {
        Product product = productsRepository.getById(id).join();

        if (product != null) {
            logger.info("Get product by ID: {}", product.getId());
            return new ResponseEntity<>(new ProductDto(product), HttpStatus.OK);
        } else {
            throw new ProductsException(ProductErrors.PRODUCT_NOT_FOUND, id);
        }
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        Product newProduct = ProductDto.toProduct(productDto);

        newProduct.setId(UUID.randomUUID().toString());
        productsRepository.create(newProduct).join();

        logger.info("Product created - ID: {}", newProduct.getId());
        return new ResponseEntity<>(new ProductDto(newProduct), HttpStatus.CREATED);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ProductDto> deleteProductById(@PathVariable String id) throws ProductsException {
        Product productDeleted = productsRepository.deleteById(id).join();

        if (productDeleted != null) {
            logger.info("Product created - ID: {}", productDeleted.getId());
            return new ResponseEntity<>(new ProductDto(productDeleted), HttpStatus.OK);
        } else {
            throw new ProductsException(ProductErrors.PRODUCT_NOT_FOUND, id);
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<ProductDto> updateProductById(@PathVariable String id, @RequestBody ProductDto productDto) throws ProductsException {
        try {
            Product productUpdated = productsRepository.update(id, ProductDto.toProduct(productDto)).join();
            logger.info("Product updated - ID: {}", productUpdated.getId());
            return new ResponseEntity<>(new ProductDto(productUpdated), HttpStatus.OK);
        } catch (CompletionException e) {
            logger.error("Product updated - ID: {}", id, e.getMessage(), e);
            throw new ProductsException(ProductErrors.PRODUCT_NOT_FOUND, id);
        }
    }
}
