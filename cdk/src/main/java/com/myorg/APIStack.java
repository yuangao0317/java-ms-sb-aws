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

import java.util.Arrays;
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

    private Map<String, JsonSchema> productRequestValidatorSchema(RestApi restApi) {
        Map<String, JsonSchema> productModelProperties = new HashMap<>();
        productModelProperties.put("name",
                JsonSchema.builder()
                        .type(JsonSchemaType.STRING)
                        .minLength(5)
                        .maxLength(50)
                        .build()
        );
        productModelProperties.put("code",
                JsonSchema.builder()
                        .type(JsonSchemaType.STRING)
                        .minLength(5)
                        .maxLength(15)
                        .build()
        );
        productModelProperties.put("model",
                JsonSchema.builder()
                        .type(JsonSchemaType.STRING)
                        .minLength(5)
                        .maxLength(50)
                        .build()
        );
        productModelProperties.put("price",
                JsonSchema.builder()
                        .type(JsonSchemaType.NUMBER)
                        .minimum(10.0)
                        .maximum(1000.0)
                        .build()
        );

        return productModelProperties;
    }

    private void createProductsResource(RestApi restApi, APIStackDependency apiStackDependency) {
        Resource productsResource = restApi.getRoot().addResource("products");

        Map<String, String> productsIntegrationParameters = new HashMap<>();
        productsIntegrationParameters.put("integration.request.header.requestId", "context.requestId");

        Map<String, Boolean> productsMethodParameters = new HashMap<>();
        productsMethodParameters.put("method.request.header.requestId", false);
        productsMethodParameters.put("method.request.querystring.code", false);

        // API Gateway forward requests to NetworkLoadBalancer through VPC Link
        // GET /products?code=
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

        RequestValidator productRequestValidator = new RequestValidator(this, "Product-Request-Validator",
                RequestValidatorProps.builder()
                        .restApi(restApi)
                        .requestValidatorName("Product-request-validator")
                        .validateRequestBody(true)
                        .build()
        );

        Model productModel = new Model(this, "Product-Model",
                ModelProps.builder()
                        .modelName("product-model")
                        .restApi(restApi)
                        .contentType("application/json")
                        .schema(JsonSchema.builder()
                                .type(JsonSchemaType.OBJECT)
                                .properties(this.productRequestValidatorSchema(restApi))
                                .required(Arrays.asList("name", "code"))
                                .build())
                        .build()
        );
        Map<String, Model> productRequestModels = new HashMap<>();
        productRequestModels.put("application/json", productModel);

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
                MethodOptions.builder()
                        .requestParameters(productsMethodParameters)
                        .requestValidator(productRequestValidator)
                        .requestModels(productRequestModels)
                        .build()
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
