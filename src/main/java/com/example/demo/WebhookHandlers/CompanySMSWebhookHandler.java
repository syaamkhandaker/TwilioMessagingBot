package com.example.demo.WebhookHandlers;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
import com.example.demo.Entities.AllUsers;
import com.example.demo.Entities.User;
import com.example.demo.Entities.UserCopy;
import com.example.demo.Entities.UserRequest;
import com.example.demo.Repo.AllUserRepo;
import com.example.demo.Repo.UserRepo;
import com.example.demo.Repo.UserRequestRepo;

@RestController
public class CompanySMSWebhookHandler {

	@Autowired
	private UserRepo repo;

	@Autowired
	private AllUserRepo allRepo;

	@Autowired
	private UserRequestRepo requestRepo;

	private UserRequest userRequest;

	private static final String endpoint = "dynamodb.us-east-2.amazonaws.com";
	private static final String accessKey = "AKIAUFHDV5GBNYWFDPO7";
	private static final String privateKey = "2vHUqJhPDduAt5QdKAdiLTY3AGlFUEpkDyuJezPH";
	public static final String accountSid = "ACb80a5699bbcf32c554a17698071dc8c1";
	public static final String authToken = "5da29f9724c483857a32e1cbecbff52e";
	public static final String myNumber = "+19134122893";
	public static final String fordNumber = "+14048587064";

	private AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
			.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, "us-east-2"))
			.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, privateKey))).build();
	private DynamoDB dynamo = new DynamoDB(client);

	/**
	 * This method adds each of the Users into the database. The database is
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
	@RequestMapping(value = "/addUser", method = RequestMethod.POST)
	public void handleType(@RequestParam("Name") String name, @RequestParam("Body") String body,
			@RequestParam("Number") String number) throws NoSuchAlgorithmException {
		User user = new User(body.trim(), name.trim(), number.trim());
		AllUsers allUser = new AllUsers(body.trim(), name.trim(), number.trim());
		Table table = dynamo.getTable("Users");
		GetItemSpec item = new GetItemSpec().withPrimaryKey("userId", user.getUserId());
		Item t = table.getItem(item);
		if (!Objects.nonNull(t)) {// makes sure no duplicate values, if not included then we make a new one
			repo.save(user);
			allRepo.save(allUser);
		} else {// if already included, then we update the previous one with the new one
			repo.update(user.getUserId(), user);
			allRepo.update(allUser.getUserId(), allUser);
		}

	}

	/**
	 * Method to add the user request to the "UserRequest" database on AWS DynamoDB.
	 * 
	 * @param name The companies name that we are making a request for
	 * @param type The categories the company wants to sign up for
	 * @param number The number for the current user who is signing up
	 * @param req The string request we receive from the companies we work with talking about the details of the request, price of the 
	 * request, and the timeline of the request. It is received in the following format:
	 * "1. Sample Details\n2. Sample Price\n3. Sample Timeline".
	 * @throws NumberFormatException
	 * @throws NoSuchAlgorithmException
	 * @throws InterruptedException
	 */
	@RequestMapping(value = "/addUserRequest", method = RequestMethod.POST)
	public void addOneUserRequest(@RequestParam("Name") String name, @RequestParam("Type") String type,
			@RequestParam("PhoneNumber") String number, @RequestParam("Request") String req)
			throws NumberFormatException, NoSuchAlgorithmException, InterruptedException {

		String[] arr = req.split(System.lineSeparator());
		String details = arr[0].substring(3).trim();
		String price = arr[1].substring(3).trim();
		String timeline = arr[2].substring(3).trim();

		userRequest = new UserRequest(new UserCopy(type.trim(), name.trim(), number.trim()), details, price, timeline,
				"false");
		Map<String, String> map = new HashMap<>();
		map.put("phoneNumber", userRequest.getUser().getPhoneNumber());
		map.put("type", userRequest.getUser().getType());
		map.put("userId", userRequest.getUser().getUserId());
		map.put("userName", userRequest.getUser().getUserName());
		Table table = dynamo.getTable("UserRequest");
		Item item = new Item().withPrimaryKey("requestId", userRequest.getRequestId())
				.with("details", userRequest.getDetails()).with("price", userRequest.getPrice())
				.with("timeline", userRequest.getTimeline()).with("approved", userRequest.isApproved())
				.withMap("User", map);

		table.putItem(item);
	}

	/**
	 * Method checks the UserRequest database and if the "approved" attribute is
	 * accepted, declined, or hasn't changed. Twilio repeatedly looks through this
	 * to keep check of that. It is changed through the request verification
	 * website, where if you press the accept button the value changes to "approved"
	 * and if you press the decline button it changes to "decline".
	 * 
	 * UserRequest Database looks like the following:
	 * 
	 * userRequestId | details | price | timeline | User (essentially a JSON file
	 * with data)
	 * _________________________________________________________________________________________
	 * "abcdefghijklmn" "Calc" "$15" "Today"
	 * {name:"Syaam",type:"1",number:"+17037084879"}
	 * 
	 * @param name    receives the name from the twilio name_request block
	 * 
	 * @param type    receives the type that the user wants to request for
	 * 
	 * @param number  receives the users phone number in case we want to message
	 *                them in the future
	 * 
	 * @param request takes in the specific request with its details for pricing,
	 *                timeline, the time it would take, and the specific details
	 */
	@RequestMapping(value = "/checkUserRequest", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public String checkUserRequest() throws NumberFormatException, NoSuchAlgorithmException, InterruptedException {
		Thread.sleep(8000);
		Table table = dynamo.getTable("UserRequest");
		GetItemSpec spec = new GetItemSpec().withPrimaryKey("requestId", userRequest.getRequestId());
		Item get = table.getItem(spec);

		if (get.get("approved").equals("decline")) {
			table.deleteItem("requestId", userRequest.getRequestId());
			return "{\"approved\": \"decline\"}";
		} else if (get.get("approved").equals("accept")) {
			return "{\"approved\": \"accept\"}";
		}
		return "{\"approved\":\"" + get.get("approved") + "\"}";
	}
}
