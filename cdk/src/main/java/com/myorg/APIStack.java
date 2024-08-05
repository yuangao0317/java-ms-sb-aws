package com.myorg;

import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.elasticloadbalancingv2.NetworkLoadBalancer;
import software.constructs.Construct;

public class APIStack extends Stack {
    public APIStack(@Nullable final Construct scope,
                    @Nullable final String id,
                    @Nullable final StackProps props,
                    final APIStackDependency dependency) {
        super(scope, id, props);

        RestApi restApi = new RestApi(this, "App-RestApi",
                RestApiProps.builder()
                        .restApiName("app-api-gateway")
                        .build());

        this.createProductsResource(restApi, dependency);
    }

    private void createProductsResource(RestApi restApi, APIStackDependency apiStackDependency) {
        Resource productsResource = restApi.getRoot().addResource("products");

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
                                        .build()
                        )
                        .build())
        );
    }
}

record APIStackDependency(
        NetworkLoadBalancer nlb,
        VpcLink vpcLink
) {}
