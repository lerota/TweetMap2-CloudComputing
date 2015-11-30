import java.sql.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;

//import com.amazonaws.services.simpleemail.model.Message;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>This is a code example of Twitter4J Streaming API - sample method support.<br>
 * Usage: java twitter4j.examples.PrintSampleStream<br>
 * </p>
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class TweetGet {
    /**
     * Main entry of this application.
     *
     * @param args
     */
	public static int count = 0;
	public static KeyWordManagement kwmng = new KeyWordManagement();
	public static AmazonSQS sqs;
	public static String Url;
	public String lat;
	public String lon;
	
    public static void main(String[] args) throws TwitterException, SQLException, NamingException, FileNotFoundException, IOException {
    	AWSCredentials credentials = null;
		 // Load the AWS credentials file
		 try {
		 	credentials = new ProfileCredentialsProvider("default").getCredentials();
		 } catch (Exception e) {
		 	throw new AmazonClientException(
                     "Cannot load the credentials from the credential profiles file. " +
                     "Please make sure that your credentials file is at the correct, and is in valid format.", e);
		 }
    	
    	 ConfigurationBuilder cb = new ConfigurationBuilder();
         cb.setDebugEnabled(true)
           .setOAuthConsumerKey("cZREF9Mq4d5BOfc7SSdl39ljQ")
           .setOAuthConsumerSecret("DMnWwh7h2rKun05qoxjQwvjMClI4LANFJRiNOB0eoGYPp6s1vt")
           .setOAuthAccessToken("1253902718-vHf7e6OEFVI9coNtFQh2JvhSO5pkfeC4Q8FmJnq")
           .setOAuthAccessTokenSecret("ix0ug0WHm4lrQThFRKTkAtppcHjFDVqP8P4ksLoId18SP");
         
        if(sqs == null){
  	    	sqs = new AmazonSQSClient(credentials);
  	    	Region usEast1 = Region.getRegion(Regions.US_EAST_1);
  	    	sqs.setRegion(usEast1);
  	    }
  	    try{
  			ListQueuesResult queue = sqs.listQueues("TweetQueue");
  			Url = queue.getQueueUrls().get(0);
  		} catch (IndexOutOfBoundsException ase) {
  			CreateQueueRequest createQueueRequest = new CreateQueueRequest("TweetQueue");
  	        Url = sqs.createQueue(createQueueRequest).getQueueUrl();
  		}
         
         try {
	         final TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
	         final Connection conn = TweetToDB.createConnection();
	         StatusListener listener = new StatusListener() {
	             @Override
	             public void onStatus(Status status) {	            
               	 if (count < 200) {
	             		if (status.getGeoLocation() != null && status.getLang().equals("en")){
		            		 String text = status.getText();
		            		 String display = new String(text);
		            		 
		            		 String topic = kwmng.getTopic(status);		            		 
		            		 
		            		 if (display.length() > 100) {
		            			 display = display.substring(0, 101);
		            		 }
	             			PreparedStatement preparedStatement = null;
	             			try {
	             				preparedStatement = conn.prepareStatement("insert into TwitterMap1 values(?, ?, ?, ?, ?, ?, ?, ?)");
	             				preparedStatement.setString(1, Long.toString(status.getId()));
								preparedStatement.setString(2, status.getUser().getScreenName() );
								preparedStatement.setString(3, Double.toString(status.getGeoLocation().getLatitude()));
								preparedStatement.setString(4, Double.toString(status.getGeoLocation().getLongitude()));
								preparedStatement.setString(5, status.getText());
								preparedStatement.setString(6, "" + status.getCreatedAt());
								preparedStatement.setString(7, topic);
								preparedStatement.setString(8, null);
								preparedStatement.executeUpdate();	
								
								JSONObject jsonObject = new JSONObject();
			                      try {
			                    	    jsonObject.put("flag", "sentiment");
			                    	    jsonObject.put("id", Long.toString(status.getId()));
										jsonObject.put("latitude", Double.toString(status.getGeoLocation().getLatitude()));
										jsonObject.put("longitude",Double.toString(status.getGeoLocation().getLongitude()));
										jsonObject.put("text", status.getText());
			                      } catch (JSONException e) {
									e.printStackTrace();
			                      }
			                      sqs.sendMessage(new SendMessageRequest( Url, jsonObject.toString() ));
								
							} catch (SQLException e) {
								e.printStackTrace();
							}
	                      count++;
	             		}
	             	} 
	            	 else{
	             		twitterStream.clearListeners();
	             		twitterStream.shutdown();
	             	}
	             }
	
	             @Override
	             public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
	             }
	
	             @Override
	             public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
	             }
	
	             @Override
	             public void onScrubGeo(long userId, long upToStatusId) {
	             }
	
	             @Override
	             public void onStallWarning(StallWarning warning) {
	             }
	
	             @Override
	             public void onException(Exception ex) {
	                 ex.printStackTrace();
	             }
	         };
	         twitterStream.addListener(listener);
	         //Filter
	         FilterQuery filter = new FilterQuery();
	         KeyWordManagement kw = new KeyWordManagement();
	         String[] keywordsArray = kw.keyWords;
	         filter.track(keywordsArray);
	         twitterStream.filter(filter);
	          
         } catch (Exception e) {
        	 System.out.println(e);
         }
     }
 }