package com.myorg;

import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.elasticloadbalancingv2.*;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.LogGroupProps;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProductServiceStack extends Stack {
    private final Number PRODUCT_SERVICE_PORT = 8080;


    public ProductServiceStack(@Nullable final Construct scope,
                               @Nullable final String id,
                               @Nullable final StackProps props,
                               ProductServiceDependency dependency) {
        super(scope, id, props);

        // CloudWatch Logger
        AwsLogDriver logDriver = new AwsLogDriver(AwsLogDriverProps.builder()
                .logGroup(new LogGroup(this, "Log-Group",
                        LogGroupProps.builder()
                                .logGroupName("product-service-log")
                                .removalPolicy(RemovalPolicy.DESTROY)
                                .retention(RetentionDays.ONE_MONTH)
                                .build()))
                .streamPrefix("ProductService")
                .build());

        // Init Fargate compute resources
        FargateTaskDefinition fargateTaskDefinition = new FargateTaskDefinition(this, "Fargate-Task-Definition",
                FargateTaskDefinitionProps.builder()
                        .family("product-service-fargate-tasks")
                        .cpu(512)
                        .memoryLimitMiB(1024)
                        .build());

        // Add ECR container to Fargate
        Map<String, String> containerEnvVariables = new HashMap<>();
        containerEnvVariables.put("SERVER_PORT", String.format("%d", PRODUCT_SERVICE_PORT));
        fargateTaskDefinition.addContainer("Product-Service-Container",
                ContainerDefinitionOptions.builder()
                        .image(ContainerImage.fromEcrRepository(dependency.repository(), "1.0.0"))
                        .containerName("product-service")
                        .logging(logDriver)
                        .portMappings(Collections.singletonList(
                                PortMapping.builder()
                                        .containerPort(PRODUCT_SERVICE_PORT)
                                        .protocol(Protocol.TCP)
                                        .build()
                        ))
                        .environment(containerEnvVariables)
                        .build());

        // Init ECS Fargate Service
        FargateService fargateService = new FargateService(this, "Product-Service-Fargate-Service",
                FargateServiceProps.builder()
                        .serviceName("product-service-fargate-service")
                        .cluster(dependency.cluster())
                        .taskDefinition(fargateTaskDefinition)
                        .desiredCount(2) // how many task instances to hold
                        //DO NOT DO THIS IN PRODUCTION!!!
                        //.assignPublicIp(true) // for non NAT Gateway like Internet Gateway(free)
                        .build());

        // Assign permission to Fargate
        dependency.repository().grantPull(Objects.requireNonNull(fargateTaskDefinition.getExecutionRole()));
        // Assign security group to Fargate
        fargateService.getConnections().getSecurityGroups().get(0).addIngressRule(Peer.anyIpv4(), Port.tcp(PRODUCT_SERVICE_PORT));

        // Listener for connecting to ALB
        ApplicationListener applicationListener = dependency.alb()
                .addListener("Product-Service-ALB-Listener",
                        ApplicationListenerProps.builder()
                                .port(PRODUCT_SERVICE_PORT)
                                .protocol(ApplicationProtocol.HTTP)
                                .loadBalancer(dependency.alb())
                                .build());

        // Connect Fargate to ALB
        applicationListener.addTargets("Product-Service-ALB-Target",
                AddApplicationTargetsProps.builder()
                        .targetGroupName("product-service-alb-target-group")
                        .port(PRODUCT_SERVICE_PORT)
                        .protocol(ApplicationProtocol.HTTP)
                        .targets(Collections.singletonList(fargateService))
                        .deregistrationDelay(Duration.seconds(30))
                        .healthCheck(
                                HealthCheck.builder()
                                        .enabled(true)
                                        .interval(Duration.minutes(1))
                                        .timeout(Duration.seconds(15))
                                        .path("/actuator/health")
                                        .healthyHttpCodes("200")
                                        .port(String.format("%d", PRODUCT_SERVICE_PORT))
                                        .build()
                        )
                        .build());

        // Listener for connecting to NLB
        NetworkListener networkListener = dependency.nlb().addListener("Product-Service-NLB-Listener",
                BaseNetworkListenerProps.builder()
                        .port(PRODUCT_SERVICE_PORT)
                        .protocol(software.amazon.awscdk.services.elasticloadbalancingv2.Protocol.TCP)
                        .build());

        // Connect ALB to NLB
        networkListener.addTargets("Product-Service-NLB-Target",
                AddNetworkTargetsProps.builder()
                        .port(PRODUCT_SERVICE_PORT)
                        .protocol(software.amazon.awscdk.services.elasticloadbalancingv2.Protocol.TCP)
                        .targetGroupName("product-service-nlb-target-group")
                        .targets(Collections.singletonList(
                                fargateService.loadBalancerTarget(
                                        LoadBalancerTargetOptions.builder()
                                                .containerName("product-service")
                                                .containerPort(PRODUCT_SERVICE_PORT)
                                                .protocol(Protocol.TCP)
                                                .build())
                                )
                        )
                        .build());
    }
}

record ProductServiceDependency(
        Vpc vpc,
        Cluster cluster,
        NetworkLoadBalancer nlb,
        ApplicationLoadBalancer alb,
        Repository repository
){}
