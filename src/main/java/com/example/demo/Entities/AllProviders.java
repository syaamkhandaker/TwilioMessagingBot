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
@DynamoDBTable(tableName = "AllProviders")
public class AllProviders {

	@DynamoDBHashKey
	private String providerId;

	@DynamoDBAttribute
	public String type;

	@DynamoDBAttribute
	public String phoneNumber;

	@DynamoDBAttribute
	public String providerName;

	public AllProviders(String type, String providerName, String phoneNumber) throws NoSuchAlgorithmException {
		this.type = type;
		this.providerName = providerName;
		this.phoneNumber = phoneNumber;
		MessageDigest mes = MessageDigest.getInstance("SHA-256");
		String str = "" + this.phoneNumber;
		mes.update(str.getBytes());
		this.providerId = DatatypeConverter.printHexBinary(mes.digest());

	}

	public String getProviderId() {
		return this.providerId;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPhoneNumber() {
		return this.phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getProviderName() {
		return this.providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof AllProviders)) {
			return false;
		}
		AllProviders provider = (AllProviders) o;
		return Objects.equals(providerId, provider.providerId) && type == provider.type
				&& Objects.equals(providerName, provider.providerName)
				&& Objects.equals(phoneNumber, provider.phoneNumber);
	}

	@Override
	public int hashCode() {
		return Objects.hash(providerId, type, providerName, phoneNumber);
	}

	@Override
	public String toString() {
		return "{" + " userId='" + getProviderId() + "'" + ", type='" + getType() + "'" + ", userName='"
				+ getProviderName() + "'" + ", phoneNumber='" + getPhoneNumber() + "'" + "}";
	}

}
