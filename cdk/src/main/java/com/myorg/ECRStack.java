package com.myorg;

import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ecr.Repository;
import software.amazon.awscdk.services.ecr.RepositoryProps;
import software.amazon.awscdk.services.ecr.TagMutability;
import software.constructs.Construct;

public class ECRStack extends Stack {
    private final Repository productServiceRepo;

    public ECRStack(@Nullable final Construct scope, @Nullable final String id, @Nullable final StackProps props) {
        super(scope, id, props);

        this.productServiceRepo = new Repository(this, "Product-Service", RepositoryProps.builder()
                .repositoryName("product-service")
                .removalPolicy(RemovalPolicy.DESTROY) // delete with ecr stack
                .imageTagMutability(TagMutability.IMMUTABLE) // overwrite version on changes
                .autoDeleteImages(true) // delete image with repo
                .build());
    }

    public Repository getProductServiceRepo() {
        return productServiceRepo;
    }
}
