package com.myorg;

import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.VpcLink;
import software.amazon.awscdk.services.apigateway.VpcLinkProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationLoadBalancer;
import software.amazon.awscdk.services.elasticloadbalancingv2.ApplicationLoadBalancerProps;
import software.amazon.awscdk.services.elasticloadbalancingv2.NetworkLoadBalancer;
import software.amazon.awscdk.services.elasticloadbalancingv2.NetworkLoadBalancerProps;
import software.constructs.Construct;

import java.util.Collections;

public class NLBStack extends Stack {
    private final VpcLink vpcLink;
    private final NetworkLoadBalancer networkLoadBalancer;
    private final ApplicationLoadBalancer applicationLoadBalancer;

    public NLBStack(@Nullable final Construct scope,
                    @Nullable final String id,
                    @Nullable final StackProps props,
                    NLPStackDependency nlpStackDependency) {
        super(scope, id, props);

        this.networkLoadBalancer = new NetworkLoadBalancer(this, "NLB", NetworkLoadBalancerProps.builder()
                .loadBalancerName("nlb")
                .internetFacing(false)
                .vpc(nlpStackDependency.vpc())
                .build());

        this.applicationLoadBalancer = new ApplicationLoadBalancer(this, "ALB", ApplicationLoadBalancerProps.builder()
                .loadBalancerName("alb")
                .internetFacing(false)
                .vpc(nlpStackDependency.vpc())
                .build());

        this.vpcLink = new VpcLink(this, "VPC-Link", VpcLinkProps.builder()
                .targets(Collections.singletonList(this.networkLoadBalancer))
                .build());
    }

    public VpcLink getVpcLink() {
        return vpcLink;
    }

    public NetworkLoadBalancer getNetworkLoadBalancer() {
        return networkLoadBalancer;
    }

    public ApplicationLoadBalancer getApplicationLoadBalancer() {
        return applicationLoadBalancer;
    }
}

record NLPStackDependency(
        Vpc vpc
){}
