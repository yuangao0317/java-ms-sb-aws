package com.myorg;

import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ClusterProps;
import software.constructs.Construct;

public class ClusterStack extends Stack {
    private final Cluster cluster;

    public ClusterStack(@Nullable final Construct scope,
                        @Nullable final String id,
                        @Nullable final StackProps props,
                        final ClusterStackDependency dependency) {
        super(scope, id, props);

        this.cluster = new Cluster(this, "App-Cluster", ClusterProps.builder()
                .clusterName("app-cluster")
                .vpc(dependency.vpc())
                .containerInsights(true)
                .build());
    }

    public Cluster getCluster() {
        return cluster;
    }
}

record ClusterStackDependency(
        Vpc vpc
){}