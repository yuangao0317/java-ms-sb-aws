package com.gao.product_service.products.controllers;

import com.gao.product_service.products.dto.ProductDto;
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
public class ProductsController {
    private static final Logger log = LogManager.getLogger(ProductsController.class);
    private final ProductsRepository productsRepository;

    @Autowired
    public ProductsController(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        log.info("Get all products");
        return new ResponseEntity<>(productsRepository.getAll(), HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getProductById(@PathVariable("id") String id) {
        Product product = productsRepository.getById(id).join();

        if (product != null) {
            return new ResponseEntity<>(new ProductDto(product), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        Product newProduct = ProductDto.toProduct(productDto);

        newProduct.setId(UUID.randomUUID().toString());
        productsRepository.create(newProduct).join();

        log.info("Product created - ID: {}", newProduct.getId());
        return new ResponseEntity<>(new ProductDto(newProduct), HttpStatus.CREATED);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteProductById(@PathVariable String id) {
        Product productDeleted = productsRepository.deleteById(id).join();

        if (productDeleted != null) {
            log.info("Product created - ID: {}", productDeleted.getId());
            return new ResponseEntity<>(new ProductDto(productDeleted), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updateProductById(@PathVariable String id, @RequestBody ProductDto productDto) {
        try {
            Product productUpdated = productsRepository.update(id, ProductDto.toProduct(productDto)).join();
            log.info("Product updated - ID: {}", productUpdated.getId());
            return new ResponseEntity<>(new ProductDto(productUpdated), HttpStatus.OK);
        } catch (CompletionException e) {
            log.error("Product updated - ID: {}", id, e.getMessage(), e);
            return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
        }
    }
}
