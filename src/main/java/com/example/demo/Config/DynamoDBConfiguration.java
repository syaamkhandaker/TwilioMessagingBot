package com.example.demo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

@Configuration
public class DynamoDBConfiguration {
        private static final String endpoint="endpoint";
        private static final String accessKey = "accessKey;
        private static final String privateKey = "privateKey";

        @Bean
        public DynamoDBMapper dynamoDBMapper(){
        return new DynamoDBMapper(AmazonDynamoDBClientBuilder.standard()
        .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                        endpoint,"sample-region")
        )
        .withCredentials(new AWSStaticCredentialsProvider(
        new BasicAWSCredentials(accessKey,privateKey))).build());
        }      
    }

