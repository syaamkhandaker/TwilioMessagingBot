package com.example.demo.Repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.example.demo.Entities.Provider;




@Repository
public class ProviderRepo{
    
    @Autowired
    private DynamoDBMapper dynamoDBMapper;


    public Provider save(Provider provider){
        dynamoDBMapper.save(provider);
        return provider;
    }


    public Provider getUserById(String providerId){
        return dynamoDBMapper.load(Provider.class, providerId);
    }
    
    public String delete(String providerId){
        Provider provider = dynamoDBMapper.load(Provider.class, providerId);
        dynamoDBMapper.delete(provider);
        return "Provider deleted";
    }


    public String update(String providerId, Provider provider) {
        dynamoDBMapper.save(provider, new DynamoDBSaveExpression()
        .withExpectedEntry("providerId", new ExpectedAttributeValue(
            new AttributeValue().withS(providerId)
        )));
        return providerId;
    }
}
