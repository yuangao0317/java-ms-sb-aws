package com.gao.product_service.products.repositories;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.gao.product_service.products.models.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;


@Repository
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
        PagePublisher<Product> publisher = this.productsTable.scan();
        publisher.subscribe(page -> page.items().forEach(product -> {
                    logger.info("Processing page: {}, product: {}", page, product);
                }))
                .exceptionally(failure -> {
                    logger.error("Processing was interrupted: {}", failure.getMessage(), failure);
                    return null;
                });
    }

}
