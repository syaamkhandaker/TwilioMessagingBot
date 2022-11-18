package com.example.demo.Entities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "PastUserRequest")
public class PastUserRequest {

	@DynamoDBHashKey
	private String requestId;

	@DynamoDBAttribute(attributeName = "User")
	@Autowired
	private UserCopy user;

	@DynamoDBAttribute
	private String details;

	@DynamoDBAttribute
	private String price;

	@DynamoDBAttribute
	private String timeline;

	public PastUserRequest(UserCopy user, String details, String price, String timeline)
			throws NoSuchAlgorithmException {
		super();
		this.user = user;
		this.details = details;
		this.price = price;
		this.timeline = timeline;
		MessageDigest mes = MessageDigest.getInstance("SHA-256");
		String str = "" + user.toString() + details + price;
		mes.update(str.getBytes());
		this.requestId = DatatypeConverter.printHexBinary(mes.digest());
	}

	public String getRequestId() {
		return requestId;
	}

	public UserCopy getUser() {
		return this.user;
	}

	public void setUser(UserCopy user) {
		this.user = user;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getTimeline() {
		return this.timeline;
	}

	public void setTimeline(String timeline) {
		this.timeline = timeline;
	}

}
