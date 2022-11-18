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
        private static final String endpoint="dynamodb.us-east-2.amazonaws.com";
        private static final String accessKey = "AKIAUFHDV5GBNYWFDPO7";
        private static final String privateKey = "2vHUqJhPDduAt5QdKAdiLTY3AGlFUEpkDyuJezPH";

        @Bean
        public DynamoDBMapper dynamoDBMapper(){
        return new DynamoDBMapper(AmazonDynamoDBClientBuilder.standard()
        .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                        endpoint,"us-east-2")
        )
        .withCredentials(new AWSStaticCredentialsProvider(
        new BasicAWSCredentials(accessKey,privateKey))).build());
        }      
    }

