package com.gao.product_service.config;

import com.amazonaws.xray.interceptors.TracingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@Configuration
public class DynamoDBConfig {
    @Value("${aws.region}")
    private String awsRegion;

    @Bean
    @Primary
    public DynamoDbAsyncClient dynamoDbAsyncClient() {
        return DynamoDbAsyncClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.of(awsRegion))
                .overrideConfiguration(
                        ClientOverrideConfiguration.builder()
                                .addExecutionInterceptor(new TracingInterceptor())
                                .build())
                .build();
    }

    @Bean
    @Primary
    public DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient() {
        return DynamoDbEnhancedAsyncClient.builder()
                .dynamoDbClient(dynamoDbAsyncClient())
                .build();
    }
}
