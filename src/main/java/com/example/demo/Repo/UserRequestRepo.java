package com.example.demo.Repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.example.demo.Entities.UserRequest;

@Repository
public class UserRequestRepo {

	@Autowired
	private DynamoDBMapper dynamoDBMapper;

	public UserRequest save(UserRequest userRequest) {
		dynamoDBMapper.save(userRequest);
		return userRequest;
	}

	public UserRequest getUserById(String userRequestId) {
		return dynamoDBMapper.load(UserRequest.class, userRequestId);
	}

	public String delete(String userRequestId) {
		UserRequest user = dynamoDBMapper.load(UserRequest.class, userRequestId);
		dynamoDBMapper.delete(user);
		return "User deleted";
	}

	public String update(String userRequestId, UserRequest userRequest) {
		dynamoDBMapper.save(userRequest, new DynamoDBSaveExpression().withExpectedEntry("requestId",
				new ExpectedAttributeValue(new AttributeValue().withS(userRequestId))));

		return userRequestId;
	}
}
