package com.gao.product_service.products.repositories;
// https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/ddb-en-client-use-multirecord.html
// https://github.com/aws/aws-sdk-java-v2/blob/master/services-custom/dynamodb-enhanced/README.md

// https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-dynamodb.html
// https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-dynamodb.html

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.gao.product_service.products.dto.ProductDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.gao.product_service.products.models.Product;

import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;



@Repository
@XRayEnabled
public class ProductsRepository {
    private static final Logger logger = LogManager.getLogger(ProductsRepository.class);

    private final DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    private final DynamoDbAsyncTable<Product> productsTable;

    @Autowired
    public ProductsRepository(
            DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
            @Value("${aws.dynamodb.products.name}") String productsDdbName
    ) {
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
        this.productsTable = dynamoDbEnhancedAsyncClient.table(productsDdbName, TableSchema.fromBean(Product.class));

    }

    // Optional method to handle iteration over paginated results
    // https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/enhanced/dynamodb/model/PagePublisher.html
    public void processAllProducts() {
        PagePublisher<Product> publisher = productsTable.scan();
        publisher.subscribe(page -> page.items().forEach(product -> {
                    logger.info("Processing page: {}, product: {}", page, product);
                }))
                .exceptionally(failure -> {
                    logger.error("Processing was interrupted: {}", failure.getMessage(), failure);
                    return null;
                });
    }

    // !!! WARNING: ONLY FOR TESTING, will change later
    public List<ProductDto> getAll() {
        List<ProductDto> productsDto = new ArrayList<>();
        productsTable.scan().items().subscribe(product -> {
            productsDto.add(new ProductDto(product));
        }).join();
        return productsDto;
    }

    public CompletableFuture<Product> getById(String productId) {
        logger.info("GET BY ProductId: {}", productId);
        return productsTable.getItem(Key.builder().partitionValue(productId).build());
    }

    public CompletableFuture<Void> create(Product product) {
        logger.info("CREATE product: {}", product);
        return productsTable.putItem(product);
    }

    public CompletableFuture<Product> deleteById(String productId) {
        logger.info("DELETE BY ProductId: {}", productId);
        return productsTable.deleteItem(Key.builder()
                .partitionValue(productId)
                .build());
    }

    public CompletableFuture<Product> update(String productId, Product product) {
        logger.info("UPDATE product: {}, WITH: {}", product, productId);
        product.setId(productId);
        return productsTable.updateItem(
                UpdateItemEnhancedRequest.<Product>builder(Product.class)
                        .item(product)
                        .conditionExpression(
                                Expression.builder()
                                    .expression("attribute_exists(id)")
                                    .build())
                        .build()
        );
    }

}
