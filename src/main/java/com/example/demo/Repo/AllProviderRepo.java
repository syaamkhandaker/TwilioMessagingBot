package com.example.demo.Repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.example.demo.Entities.AllProviders;




@Repository
public class AllProviderRepo{
    
    @Autowired
    private DynamoDBMapper dynamoDBMapper;


    public AllProviders save(AllProviders allProviders){
        dynamoDBMapper.save(allProviders);
        return allProviders;
    }


    public AllProviders getUserById(String AllProvidersId){
        return dynamoDBMapper.load(AllProviders.class, AllProvidersId);
    }
    
    public String delete(String AllProvidersId){
        AllProviders AllProviders = dynamoDBMapper.load(AllProviders.class, AllProvidersId);
        dynamoDBMapper.delete(AllProviders);
        return "AllProviders deleted";
    }


    public String update(String allProvidersId, AllProviders allProviders) {
        dynamoDBMapper.save(allProviders, new DynamoDBSaveExpression()
        .withExpectedEntry("providerId", new ExpectedAttributeValue(
            new AttributeValue().withS(allProvidersId)
        )));
        return allProvidersId;
    }
}
