package com.example.demo.Repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.example.demo.Entities.AllUsers;




@Repository
public class AllUserRepo{
    
    @Autowired
    private DynamoDBMapper dynamoDBMapper;


    public AllUsers save(AllUsers AllUsers){
        dynamoDBMapper.save(AllUsers);
        return AllUsers;
    }


    public AllUsers getAllUsersById(String allUsersId){
        return dynamoDBMapper.load(AllUsers.class, allUsersId);
    }

    public String delete(String allUsersId){
        AllUsers allUsers = dynamoDBMapper.load(AllUsers.class, allUsersId);
        dynamoDBMapper.delete(allUsers);
        return "AllUsers deleted";
    }


    public String update(String allUsersId, AllUsers allUsers) {
        dynamoDBMapper.save(allUsers, new DynamoDBSaveExpression()
        .withExpectedEntry("userId", new ExpectedAttributeValue(
            new AttributeValue().withS(allUsersId)
        )));
        
        return allUsersId;
    }
}

