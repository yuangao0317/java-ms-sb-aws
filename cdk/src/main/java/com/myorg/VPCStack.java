package com.myorg;

import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcProps;
import software.constructs.Construct;

public class VPCStack extends Stack {
    private final Vpc vpc;

    public VPCStack(@Nullable final Construct scope,
                    @Nullable final String id,
                    @Nullable final StackProps props) {
        super(scope, id, props);

        this.vpc = new Vpc(this, "VPC-Stack", VpcProps.builder()
                .vpcName("vpc-stack")
                .maxAzs(2)
                //DO NOT DO THIS IN PRODUCTION!!!
                //.natGateways(0) // for non NAT Gateway like Internet Gateway(free)
                .build());
    }

    public Vpc getVpc() {
        return vpc;
    }
}
