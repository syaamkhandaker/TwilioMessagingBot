package com.example.demo.WebhookHandlers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.example.demo.Entities.AllProviders;
import com.example.demo.Entities.PastUserRequest;
import com.example.demo.Entities.Provider;
import com.example.demo.Entities.UserCopy;
import com.example.demo.Entities.UserRequest;
import com.example.demo.Repo.AllProviderRepo;
import com.example.demo.Repo.PastUserRequestRepo;
import com.example.demo.Repo.ProviderRepo;
import com.example.demo.Repo.UserRepo;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@RestController
public class ProviderSMSWebhookHandler {

	@Autowired
	private ProviderRepo repo;

	@Autowired
	private AllProviderRepo allRepo;

	@Autowired
	private PastUserRequestRepo pastRepo;

	@Autowired
	private UserRepo userRepo;

	private static final String endpoint = "dynamodb.us-east-2.amazonaws.com";
	private static final String accessKey = "AKIAUFHDV5GBNYWFDPO7";
	private static final String privateKey = "2vHUqJhPDduAt5QdKAdiLTY3AGlFUEpkDyuJezPH";
	public static final String accountSid = "ACb80a5699bbcf32c554a17698071dc8c1";
	public static final String authToken = "5da29f9724c483857a32e1cbecbff52e";
	public static final String myNumber = "+19134122893";

	private static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
			.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, "us-east-2"))
			.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, privateKey))).build();
	private static DynamoDB dynamo = new DynamoDB(client);

	/**
	 * This method adds each of the Student users into the database. The database is
	 * formatted as following: userId | userName | type | phoneNumber
	 * ______________________________________
	 * 
	 * The userId is auto generated from the sha256 hash function that I implemented
	 * in. The userName and type are received from twilio.
	 * 
	 * @param name   receives the name from the twilio name_request block
	 * 
	 * @param body   receives the type that the user wants to request for
	 * 
	 * @param number receives the users phone number in case we want to message them
	 *               in the future
	 */
	@RequestMapping(value = "/addProvider", method = RequestMethod.POST)
	public void handleType(@RequestParam("Name") String name, @RequestParam("Body") String body,
			@RequestParam("Number") String number) throws NoSuchAlgorithmException {
		Provider provider = new Provider(body.trim(), name.trim(), number.trim());
		AllProviders allProvider = new AllProviders(body.trim(), name.trim(), number.trim());
		Table table = dynamo.getTable("Providers");
		GetItemSpec item = new GetItemSpec().withPrimaryKey("providerId", provider.getProviderId());
		Item t = table.getItem(item);
		if (!Objects.nonNull(t)) {
			repo.save(provider);
			allRepo.save(allProvider);
		} else {
			repo.update(provider.getProviderId(), provider);
			allRepo.update(allProvider.getProviderId(), allProvider);
		}
	}

	/**
	 * This method sends out all the current unfinished requests to new students who
	 * sign up, in order to still be notified of all the opportunities. It reads
	 * through the AWS DynamoDB database for past user requests and outputs each of
	 * them in a readable format.
	 * 
	 * @param type        the categories the current provider/student has signed up
	 *                    for
	 * @param phoneNumber the phone number of the student who had just signed up
	 * @throws NoSuchAlgorithmException
	 */
	@RequestMapping(value = "/sendOutActiveTasks", method = RequestMethod.POST)
	private static void sendOutActiveTasks(@RequestParam("Type") String type,
			@RequestParam("Number") String phoneNumber) throws NoSuchAlgorithmException {

		Twilio.init(accountSid, authToken);

		MessageDigest mes = MessageDigest.getInstance("SHA-256");
		String str = "" + phoneNumber;
		mes.update(str.getBytes());
		String providerId = DatatypeConverter.printHexBinary(mes.digest());

		Table table = dynamo.getTable("Providers");
		GetItemSpec item = new GetItemSpec().withPrimaryKey("providerId", providerId);
		Item t = table.getItem(item);

		if ((!Objects.nonNull(t))) {
			ScanRequest pastUserRequestScanRequest = new ScanRequest().withTableName("PastUserRequest");
			ScanResult pastUserRequestScan = client.scan(pastUserRequestScanRequest);
			end: for (Map<String, AttributeValue> dbValues : pastUserRequestScan.getItems()) {
				String pastUserRequestTypes = dbValues.get("User").getM().get("type").getN() == null
						? dbValues.get("User").getM().get("type").getS()
						: dbValues.get("User").getM().get("type").getN();
				String hirerTypes = type;
				for (String s : hirerTypes.split("")) {
					if (!s.equals("") && pastUserRequestTypes.contains(s)) {
						if (!dbValues.get("User").getM().get("phoneNumber").getS().equals(phoneNumber)) {
							String text = dbValues.get("User").getM().get("userName").getS() + " has an opportunity"
									+ ".\nDetails: " + dbValues.get("details").getS() + "\nPrice: "
									+ dbValues.get("price").getS() + "\nTimeline: " + dbValues.get("timeline").getS();
							Message message = Message
									.creator(new PhoneNumber(phoneNumber), new PhoneNumber(myNumber), text).create();
							continue end;
						}

					}
				}

			}
		}

	}

	/**
	 * Method used to send each user with the same type the request. It searches
	 * through the DynamoDB database reading everyone's types. If it matches the
	 * original type then it sends them a message. In twilio, we check if the
	 * providers response matches the users name in the request.
	 * 
	 * UserRequest DB is supposed to only hold one value at a time. After each
	 * iteration it deletes the value from UserRequest and puts it into the
	 * PastUserRequest DB.
	 */
	@RequestMapping(value = "/providerRequests", method = RequestMethod.GET)
	public void sendRequests() throws NoSuchAlgorithmException {
		ScanRequest userScan = new ScanRequest().withTableName("UserRequest");
		ScanResult res = client.scan(userScan);
		int index = 0;
		if (res.getItems().get(0).get("details").equals("")) {
			index = 1;
		}
		UserCopy user = new UserCopy(res.getItems().get(index).get("User").getM().get("type").getS().trim(),
				res.getItems().get(index).get("User").getM().get("userName").getS().trim(),
				res.getItems().get(index).get("User").getM().get("phoneNumber").getS().trim());
		UserRequest userRequest = new UserRequest(user, res.getItems().get(index).get("details").getS().trim(),
				res.getItems().get(index).get("price").getS().trim(),
				res.getItems().get(index).get("timeline").getS().trim(), "true");
		PastUserRequest pastUserRequest = new PastUserRequest(user, userRequest.getDetails(), userRequest.getPrice(),
				userRequest.getTimeline());

		Twilio.init(accountSid, authToken);

		Table table = dynamo.getTable("UserRequest");
		table.deleteItem("requestId", userRequest.getRequestId());
		pastRepo.save(pastUserRequest);

		ScanRequest providerScan = new ScanRequest().withTableName("Providers");
		ScanResult providers = client.scan(providerScan);

		end: for (Map<String, AttributeValue> dbValues : providers.getItems()) {
			String providerTypes = dbValues.get("type").getN() == null ? dbValues.get("type").getS()
					: dbValues.get("type").getN();
			String hirerTypes = userRequest.getUser().getType();

			for (String s : hirerTypes.split("")) {
				if (!s.equals("") && providerTypes.contains(s)) {
					if (!dbValues.get("phoneNumber").getS().equals(user.getPhoneNumber())) {
						String text = user.userName + " has an opportunity" + ".\nDetails: " + userRequest.getDetails()
								+ "\nPrice: " + userRequest.getPrice() + "\nTimeline: " + userRequest.getTimeline();
						String number = dbValues.get("phoneNumber").getS();
						Message message = Message.creator(new PhoneNumber(number), new PhoneNumber(myNumber), text)
								.create();
						break end;
					}

				}
			}
		}
	}

	/**
	 * Checks who is approved for their request. If the name the provider replies
	 * with is in the DB, then test becomes true and it automatically sends them a
	 * message saying their offer has been sent to the creator. If it isn't in the
	 * DB, then it sends a 500 error, which makes the HTTP Request fail and send a
	 * message back saying that they did'nt properly put the name in. Makes the user
	 * retry to put the correct name in.
	 * 
	 * @param body                is the name the provider puts in response to each
	 *                            request they get
	 * 
	 * @param providerName        is the provider's name, so we can send it to the
	 *                            user who asked for the requests
	 * 
	 * @param providerPhoneNumber is the provider's phoneNumber, so the user who
	 *                            made the request can message them.
	 */
	@RequestMapping(value = "/handleIncomingMessage", method = RequestMethod.GET)
	public void handleIncomingMessage(@RequestParam("Body") String body,
			@RequestParam("ProviderName") String providerName,
			@RequestParam("ProviderNumber") String providerPhoneNumber) throws NoSuchAlgorithmException {
		String userPhoneNumber = "";
		ScanRequest userScan = new ScanRequest().withTableName("Users");
		ScanResult users = client.scan(userScan);
		boolean test = false;
		end: for (Map<String, AttributeValue> dbValues : users.getItems()) {// whole for loop just looks through the
																			// entire DynamoDB Users DB to check if the
																			// user's name is in there
			if (dbValues.get("userName").getS().trim().equals(body.trim())) {
				userPhoneNumber = dbValues.get("phoneNumber").getS();
				test = true;
				break end;
			}
		}
		if (test) {
			Twilio.init(accountSid, authToken);
			Message message = Message.creator(new PhoneNumber(providerPhoneNumber), new PhoneNumber(myNumber),
					"Your offer has been sent to " + body + ".\nThey will message you if they choose to accept.")
					.create();
			String provName = providerName.split(" ").length > 1 ? providerName.split(" ")[0] : providerName;
			Message m = Message.creator(new PhoneNumber(userPhoneNumber), new PhoneNumber(myNumber),
					provName + " wants to fulfill " + body + "'s gig. Their number is " + providerPhoneNumber
							+ "\nSend them a message to begin the gig.")
					.create();

			return;
		}
		throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "");
	}

	/**
	 * Used as a placeholder method in case we do plan to make updates to our twilio
	 * service. This can notify everyone to type STOP and START to restart twilio
	 * and to be updated to the most newest version.
	 * 
	 * 
	 */
	@RequestMapping(value = "/sendUpdates")
	public void sendOut() {
		ScanRequest providerScan = new ScanRequest().withTableName("Providers");
		ScanResult providers = client.scan(providerScan);
		Twilio.init(accountSid, authToken);
		for (Map<String, AttributeValue> dbValues : providers.getItems()) {
			String text = "Hey! We just changed our sign up process, please type STOP and START to get notified on micro internship opportunities.";
			String number = "" + dbValues.get("phoneNumber").getS();
			Message message = Message.creator(new PhoneNumber(number), new PhoneNumber(myNumber), text).create();
		}

	}
}
