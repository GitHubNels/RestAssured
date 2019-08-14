import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class API_Automation extends GetProperties {

	// Setup Proxy settings
	public static void setHttpsProxy() throws FileNotFoundException, IOException {

		GetProperties pro = new GetProperties();

		pro.GetProperty("./Inputs/ProxySettings.properties");
		properties.load(new FileInputStream("./Inputs/ProxySettings.properties"));
		System.setProperty("https.proxyHost", properties.getProperty("Host"));
		System.setProperty("https.proxyPort", properties.getProperty("Port"));
		System.setProperty("https.proxyUser", properties.getProperty("Username"));
		System.setProperty("https.proxyPassword", properties.getProperty("Password"));
	}

	public String[] Generate_IDP_Token() throws IOException {
		GetProperties prop = new GetProperties();
		prop.GetProperty("./Inputs/SHARCK_API_Config.properties");
		properties.load(new FileInputStream("./Inputs/SHARCK_API_Config.properties"));

		// Now let us print the body of the message to see what response
		// we have recieved from the server
		String responseBody[] = new String[5];

		setHttpsProxy();
		// IDP Token (Taken from DB)
		String tokenURL = properties.getProperty("RE7_tokenURL");
		String client_id = properties.getProperty("RE7_client_id");
		String client_secret = properties.getProperty("RE7_client_secret");
		String grant_type = properties.getProperty("RE7_grant_type");
		String username = properties.getProperty("RE7_username");
		String password = properties.getProperty("RE7_password");
		String scope = properties.getProperty("RE7_scope");

		RestAssured.baseURI = tokenURL + "?";

		// Passing the Request here
		RequestSpecification httpRequest = RestAssured.given().queryParam("grant_type", grant_type)
				.queryParam("client_id", client_id).queryParam("username", username).queryParam("password", password)
				.queryParam("client_secret", client_secret).queryParam("scope", scope);

		// As Post Method and Getting Response
		Response response = httpRequest.request(Method.POST);
		responseBody[0] = response.getBody().asString();
		String code = Integer.toString(response.getStatusCode());
		responseBody[1] = code;

		// Reading a Jason File and storing it in Arry
		JsonPath jsonPathEvaluator = response.jsonPath();
		responseBody[2] = jsonPathEvaluator.get("access_token");
		responseBody[3] = jsonPathEvaluator.get("token_type");

		return responseBody;

	}

	public Object Read_Jason() throws IOException

	{
		String[] Get_Jason = Generate_IDP_Token();
		Object Jason = Get_Jason[0];
		Object Jason1 = Get_Jason[2];

		return Jason1;

	}

	/*
	 * READ Jason file
	 */

	public Object Get_Jason_Request() throws Throwable, IOException {
		Object Jason_Request = null;
		JSONObject jsonObject = null;

		JSONParser parser = new JSONParser();

		Jason_Request = parser.parse(new FileReader("./Inputs/Input_Jason.json"));
		jsonObject = (JSONObject) Jason_Request;

		return jsonObject;

	}

	public Map<String, Object> Store_Request_Map() throws IOException, Throwable {
		Object jsonObject = Get_Jason_Request();
		Map<String, Object> Request_map = new HashMap<String, Object>();
		Request_map.put("partReferenceNumber", (String) ((HashMap) jsonObject).get("partReferenceNumber"));
		Request_map.put("requestId", (String) ((HashMap) jsonObject).get("requestId"));
		Request_map.put("VIN", (String) ((HashMap) jsonObject).get("VIN"));
		Request_map.put("NITG", (String) ((HashMap) jsonObject).get("NITG"));

		return Request_map;

	}

	public String[] Get_Reponse() throws Throwable, Throwable {
		String[] Response = new String[5];
		setHttpsProxy();
		Object token = Read_Jason();
		String Jason_File = Get_Jason_Request().toString();
		GetProperties prop = new GetProperties();
		prop.GetProperty("./Inputs/SHARCK_API_Config.properties");
		properties.load(new FileInputStream("./Inputs/SHARCK_API_Config.properties"));

		String API_URL = properties.getProperty("RE7_API_URL");
		RestAssured.baseURI = API_URL;

		// Passing the Request here
		RequestSpecification httpRequest = RestAssured.given().relaxedHTTPSValidation()
				.header("Content-Type", "application/json").header("Authorization", "Bearer " + token)
				.header("Apikey", "cKsSFQqt3fXGrAIcGmVrGESSutSOUCgo").body(Jason_File);

		// As Post Method and Getting Response
		Response response = httpRequest.request(Method.POST);

		Response[0] = Jason_File;
		Response[1] = Integer.toString(response.getStatusCode());
		Response[2] = response.getBody().asString();
		System.out.println("Getting the Token \n" + token);
		System.out.println("****************************\n");
		System.out.println("Passing the Request \n" + Response[0]);
		System.out.println("****************************\n");
		System.out.println("Getting the Response Code \n" + Response[1]);
		System.out.println("****************************\n");
		System.out.println("Getting the Response as JASON \n" + Response[2]);
		return Response;

	}

	/*
	 * New method to Store the Response in a New MAP
	 */
	public Map<String, Object> Store_Response_Map() throws IOException, Throwable {

		Map<String, Object> Response_map = null;
		JSONParser Jparse = new JSONParser();

		Object Receive[] = Get_Reponse();
		Object FinalReceive = Receive[2];
		Object Output = Jparse.parse((String) FinalReceive);
		JSONObject jsonObject_Receive = (JSONObject) Output;
		Response_map = new HashMap<String, Object>();
		Response_map.put("partReferenceNumber",
				(String) ((HashMap) jsonObject_Receive).get("partReferenceNumber"));
		Response_map.put("requestId", (String) ((HashMap) jsonObject_Receive).get("requestId"));
		Response_map.put("VIN", (String) ((HashMap) jsonObject_Receive).get("VIN"));
		Response_map.put("NITG", (String) ((HashMap) jsonObject_Receive).get("NITG"));
		Response_map.put("Return_code",(String) ((HashMap) jsonObject_Receive).get("returnCode"));
		Response_map.put("setNumber",(String) ((HashMap) jsonObject_Receive).get("setNumber"));
	
		return Response_map;
	}
	
	/*
	 * Method to Compare Both the Request /Response JASON 
	 */
	public void Compare_Output() throws IOException, Throwable
	{
		Map<String, Object> Request=Store_Request_Map();
		Map<String, Object> Response=Store_Response_Map();
		
		String code=(String) Response.get("Return_code");
		if(code.equals("02"))
		{
			System.out.println("---------------------------------\n");
			System.out.println("RETUNR CODE=" +code);
			System.out.println("---------------------------------\n");
		}else
		System.out.println("---------------------------------\n");
		System.out.println("RETUNR CODE Error=" +code);
		System.out.println("---------------------------------\n");
		
	}

	public static void main(String[] args) throws Throwable {

		{
			API_Automation obj = new API_Automation();

			obj.Read_Jason();
			obj.Get_Reponse();
			obj.Store_Request_Map();
			obj.Get_Jason_Request();
			obj.Store_Response_Map();
			//obj.Compare_Output();
		}
	}

}
