package com.example.demo.Entities;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import javax.xml.bind.DatatypeConverter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "Users")
public class User {

	@DynamoDBHashKey
	private String userId;

	@DynamoDBAttribute
	public String type;

	@DynamoDBAttribute
	public String phoneNumber;

	@DynamoDBAttribute
	public String userName;

	public User(String type, String userName, String phoneNumber) throws NoSuchAlgorithmException {
		this.type = type;
		this.userName = userName;
		this.phoneNumber = phoneNumber;
		MessageDigest mes = MessageDigest.getInstance("SHA-256");
		String str = "" + this.phoneNumber + this.userName + this.type;
		mes.update(str.getBytes());
		this.userId = DatatypeConverter.printHexBinary(mes.digest());
	}

	public String getUserId() {
		return this.userId;
	}

	public String getPhoneNumber() {
		return this.phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof User)) {
			return false;
		}
		User user = (User) o;
		return Objects.equals(userId, user.userId) && type == user.type && Objects.equals(userName, user.userName)
				&& Objects.equals(phoneNumber, user.phoneNumber);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, type, userName, phoneNumber);
	}

	@Override
	public String toString() {
		return "{" + " userId='" + getUserId() + "'" + ", type='" + getType() + "'" + ", userName='" + getUserName()
				+ "'" + ", phoneNumber='" + getPhoneNumber() + "'" + "}";
	}

}
