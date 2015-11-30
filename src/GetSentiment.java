import java.io.IOException;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import api.AlchemyAPI;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class GetSentiment implements Runnable{
	
//	private AmazonDynamoDBClient client;

    private AmazonSNSClient snsClient;
	 
    private String TweetId;
    
    public GetSentiment(String pTweetId){
    	TweetId = pTweetId;
//    	AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
//    	if (client == null) {
//            client = new AmazonDynamoDBClient(credentialsProvider);
//        }
    	AWSCredentials credentials = new ProfileCredentialsProvider("default").getCredentials();
        
	    snsClient = new AmazonSNSClient(credentials);		                           
	    snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
	    
	    
    }
    
    public void run() {
    	
    	JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(TweetId);
		} catch (JSONException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		}
    	
//    	Map<String,AttributeValue> key = new HashMap<String,AttributeValue>();
//    	key.put("TweetId", new AttributeValue().withS(TweetId));
//    	GetItemRequest getItemRequest = new GetItemRequest().withKey(key).withTableName("Tweet2");
//    	Map<String,AttributeValue> Tweet = client.getItem(getItemRequest).getItem();
    	String MessageText = null;
		try {
			MessageText = jsonObject.getString("text");
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
    	String Id = null;
		try {
			Id = jsonObject.getString("id");
		} catch (JSONException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
    	String Longitude = null;
		try {
			Longitude = jsonObject.getString("longitude");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String Latitude = null;
		try {
			Latitude = jsonObject.getString("latitude");
		} catch (JSONException e5) {
			// TODO Auto-generated catch block
			e5.printStackTrace();
		}
            	
        AlchemyAPI alchemy = null;
		try {
			alchemy = AlchemyAPI.GetInstanceFromFile("/home/ec2-user/api_key.txt");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        Document doc;
		try {
			doc = alchemy.TextGetTextSentiment(MessageText);
			String score = getStringFromDocument(doc);
			
//			Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
//    		expressionAttributeValues.put(":val1", new AttributeValue().withS(score)); 
    		
			System.out.println("score: " +  score);
			
			//store score into db
			
			PreparedStatement preparedStatement = null;
 			final Connection conn = TweetToDB.createConnection();
 			String updateSQL = "UPDATE TwitterMap1 SET Score = ? WHERE Id = ?";
 			preparedStatement = conn.prepareStatement(updateSQL);
 			preparedStatement.setString(1, score);
 			preparedStatement.setLong(2, Long.parseLong(Id));
 			preparedStatement.executeUpdate();
 		//	preparedStatement = conn.prepareStatement("update TwitterMap1 set Score = " + score + "where Id = " + Long.parseLong(Id) + ";");
 		//	System.out.println("0");
			
			String topicArn = "arn:aws:sns:us-east-1:994295696375:MyNewTopic";
			JSONObject json = new JSONObject();
			JSONObject locOBJ = new JSONObject();
            try {
          	    json.put("flag", "sentiment");
          	    json.put("id", Id);
          	    json.put("longitude", Longitude);
          	    json.put("latitude", Latitude);
          	    json.put("score", score);
          	    locOBJ.put("loc", json);
            } catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
            }
//			PublishRequest publishRequest = new PublishRequest(topicArn, Id + "," + score);
            PublishRequest publishRequest = new PublishRequest(topicArn, locOBJ.toString());
			PublishResult publishResult = snsClient.publish(publishRequest);
		} catch (XPathExpressionException | IOException | SAXException
				| ParserConfigurationException e) {
			// TODO Auto-generated catch block
			System.out.println("Alchemy parsing error: " + e.toString());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
 // utility method
 	private static String getStringFromDocument(Document doc) {
 	//try {
 	//DOMSource domSource = new DOMSource(doc);
 	
 		NodeList nodeList = doc.getDocumentElement().getChildNodes();
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			Node node = nodeList.item(i);
 			if (node.getNodeName().equals("docSentiment")) {
 				NodeList nodes = node.getChildNodes();
 				for (int j = 0; j < nodes.getLength(); j++) {
 					Node tag = nodes.item(j);
 					if (tag.getNodeName().equals("score")) {
 						return tag.getTextContent();
 					}
 				}
 			}
 		}
 		return "0";
 	}
}