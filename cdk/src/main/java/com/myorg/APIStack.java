package com.myorg;

import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.elasticloadbalancingv2.NetworkLoadBalancer;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.LogGroupProps;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class APIStack extends Stack {
    public APIStack(@Nullable final Construct scope,
                    @Nullable final String id,
                    @Nullable final StackProps props,
                    final APIStackDependency dependency) {
        super(scope, id, props);

        LogGroup logGroup = new LogGroup(this, "API-Logs",
                LogGroupProps.builder()
                        .logGroupName("api-logs")
                        .removalPolicy(RemovalPolicy.DESTROY)
                        .retention(RetentionDays.ONE_MONTH)
                        .build());

        RestApi restApi = new RestApi(this, "App-RestApi",
                RestApiProps.builder()
                        .restApiName("app-api-gateway")
                        .cloudWatchRole(true)
                        .deployOptions(StageOptions.builder()
                                .loggingLevel(MethodLoggingLevel.INFO)
                                .accessLogDestination(new LogGroupLogDestination((logGroup)))
                                .accessLogFormat(AccessLogFormat.jsonWithStandardFields(
                                        JsonWithStandardFieldProps.builder()
                                                .caller(true)
                                                .httpMethod(true)
                                                .ip(true)
                                                .protocol(true)
                                                .requestTime(true)
                                                .resourcePath(true)
                                                .responseLength(true)
                                                .status(true)
                                                .user(true)
                                                .build()
                                ))
                                .build())
                        .build());

        this.createProductsResource(restApi, dependency);
    }

    private void createProductsResource(RestApi restApi, APIStackDependency apiStackDependency) {
        Resource productsResource = restApi.getRoot().addResource("products");

        Map<String, String> productsIntegrationParameters = new HashMap<>();
        productsIntegrationParameters.put("integration.request.header.requestId", "context.requestId");

        Map<String, Boolean> productsMethodParameters = new HashMap<>();
        productsMethodParameters.put("method.request.header.requestId", false);

        // API Gateway forward requests to NetworkLoadBalancer through VPC Link
        // GET /products
        productsResource.addMethod("GET", new Integration(
                IntegrationProps.builder()
                        .type(IntegrationType.HTTP_PROXY)
                        .uri("http://" + apiStackDependency.nlb().getLoadBalancerDnsName() + ":8080/api/products")
                        .options(
                                IntegrationOptions.builder()
                                        .vpcLink(apiStackDependency.vpcLink())
                                        .connectionType(ConnectionType.VPC_LINK)
                                        .requestParameters(productsIntegrationParameters)
                                        .build()
                        )
                        .build()),
                MethodOptions.builder().requestParameters(productsMethodParameters).build()
        );

        // POST /products
        productsResource.addMethod("POST", new Integration(
                IntegrationProps.builder()
                        .type(IntegrationType.HTTP_PROXY)
                        .integrationHttpMethod("POST")
                        .uri("http://" + apiStackDependency.nlb().getLoadBalancerDnsName() + ":8080/api/products")
                        .options(
                                IntegrationOptions.builder()
                                        .vpcLink(apiStackDependency.vpcLink())
                                        .connectionType(ConnectionType.VPC_LINK)
                                        .requestParameters(productsIntegrationParameters)
                                        .build()
                        )
                        .build()),
                MethodOptions.builder().requestParameters(productsMethodParameters).build()
        );

        // GET /products/{id}
        String productId = "{id}";

        Map<String, String> productIdIntegrationParameters = new HashMap<>();
        productIdIntegrationParameters.put("integration.request.path.id", "method.request.path.id");
        productIdIntegrationParameters.put("integration.request.header.requestId", "context.requestId");

        Map<String, Boolean> productIdMethodParameters = new HashMap<>();
        productIdMethodParameters.put("method.request.path.id", true);
        productIdMethodParameters.put("method.request.header.requestId", false);

        Resource productsIdResource = productsResource.addResource(productId);

        productsIdResource.addMethod("GET", new Integration(
                IntegrationProps.builder()
                        .type(IntegrationType.HTTP_PROXY)
                        .uri("http://" + apiStackDependency.nlb().getLoadBalancerDnsName() + ":8080/api/products" + String.format("/%s", productId))
                        .options(
                                IntegrationOptions.builder()
                                        .vpcLink(apiStackDependency.vpcLink())
                                        .connectionType(ConnectionType.VPC_LINK)
                                        .requestParameters(productIdIntegrationParameters)
                                        .build())
                        .build()),
                MethodOptions.builder().requestParameters(productIdMethodParameters).build()
        );


        // PUT /products/{id}
        productsIdResource.addMethod("PUT", new Integration(
                        IntegrationProps.builder()
                                .type(IntegrationType.HTTP_PROXY)
                                .uri("http://" + apiStackDependency.nlb().getLoadBalancerDnsName() + ":8080/api/products" + String.format("/%s", productId))
                                .options(
                                        IntegrationOptions.builder()
                                                .vpcLink(apiStackDependency.vpcLink())
                                                .connectionType(ConnectionType.VPC_LINK)
                                                .requestParameters(productIdIntegrationParameters)
                                                .build())
                                .build()),
                MethodOptions.builder().requestParameters(productIdMethodParameters).build()
        );

        // DELETE /products/{id}
        productsIdResource.addMethod("DELETE", new Integration(
                        IntegrationProps.builder()
                                .type(IntegrationType.HTTP_PROXY)
                                .uri("http://" + apiStackDependency.nlb().getLoadBalancerDnsName() + ":8080/api/products" + String.format("/%s", productId))
                                .options(
                                        IntegrationOptions.builder()
                                                .vpcLink(apiStackDependency.vpcLink())
                                                .connectionType(ConnectionType.VPC_LINK)
                                                .requestParameters(productIdIntegrationParameters)
                                                .build())
                                .build()),
                MethodOptions.builder().requestParameters(productIdMethodParameters).build()
        );

    }

}

record APIStackDependency(
        NetworkLoadBalancer nlb,
        VpcLink vpcLink
) {}
