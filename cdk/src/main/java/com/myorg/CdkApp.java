package com.myorg;
import io.github.cdimascio.dotenv.Dotenv;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CdkApp {
    public static void main(final String[] args) {
        Dotenv dotenv = Dotenv.load();

        App app = new App();

        Environment environment = Environment.builder()
                .account(dotenv.get("AWS_ACCOUNT"))
                .build();

        Map<String, String> infraTags = new HashMap<>();
        infraTags.put("team", "java-ms-sb-aws");
        infraTags.put("cost", "java-ms-sb-aws-ecr-infrastructure");

        new ECRStack(app, "ECR-Stack", StackProps.builder()
                .env(environment)
                .tags(infraTags)
                .build());

        // VPC Stack


        // Cluster Stack


        // NLB Stack


        // Product Service Stack


        // API Stack


        app.synth();
    }
}

