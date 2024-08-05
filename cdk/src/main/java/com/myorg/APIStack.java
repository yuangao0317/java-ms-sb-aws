package com.myorg;

import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigateway.RestApiProps;
import software.amazon.awscdk.services.apigateway.VpcLink;
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

    }
}

record APIStackDependency(
        NetworkLoadBalancer nlb,
        VpcLink vpcLink
) {}
