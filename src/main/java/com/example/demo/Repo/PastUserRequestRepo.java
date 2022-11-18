package com.example.demo.Repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.example.demo.Entities.PastUserRequest;




@Repository
public class PastUserRequestRepo{
    
    @Autowired
    private DynamoDBMapper dynamoDBMapper;


    public PastUserRequest save(PastUserRequest userRequest){
        dynamoDBMapper.save(userRequest);
        return userRequest;
    }


    public PastUserRequest getUserById(String userRequestId){
        return dynamoDBMapper.load(PastUserRequest.class, userRequestId);
    }

    public String delete(String userRequestId){
    	PastUserRequest user = dynamoDBMapper.load(PastUserRequest.class, userRequestId);
        dynamoDBMapper.delete(user);
        return "User deleted";
    }


    public String update(String userRequestId, PastUserRequest userRequest) {
        dynamoDBMapper.save(userRequest, new DynamoDBSaveExpression()
        .withExpectedEntry("userRequestId", new ExpectedAttributeValue(
            new AttributeValue().withS(userRequestId)
        )));
        
        return userRequestId;
    }
}

