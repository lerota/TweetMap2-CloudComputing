import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.naming.NamingException;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class TrendGet{
	
	
	//public static List<String> getTrends() throws TwitterException{
	public static void main(String[] args) throws TwitterException, SQLException, NamingException, FileNotFoundException, IOException {
		ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
          .setOAuthConsumerKey("SomQhEqKHwE3NUMVXbQuUpiAC")
          .setOAuthConsumerSecret("2MHLApfmuqB5lVMkVxTr9UCuhcd5gLowJZjYHFTxvEaScyFH3A")
          .setOAuthAccessToken("1253902718-NmquT94d9X2eMG1jY6dsU5CoLbqEcrUqnwwenSq")
          .setOAuthAccessTokenSecret("tCCVrN3TxhzFh3gkttLdgFp1DZonrCj3negMlrIrpNaZX");
	    try {
			Connection conn = TweetToDB.createConnection();
		
		    Twitter twitter = new TwitterFactory(cb.build()).getInstance();
		    Trends trends = twitter.getPlaceTrends(23424977); //US
		    String name = trends.getTrends()[0].getName();   
		    Query query = new Query(name);
		    query.setResultType(Query.RECENT);
		    QueryResult result = twitter.search(query);
	        int count = 0;
	        do{
	        	List<Status> tweets = result.getTweets();
	        	for(Status tweet: tweets){
	        		if(tweet.getGeoLocation()!=null){
	        	 		System.out.println(tweet.getGeoLocation() + ":" + tweet.getText());
	        	 		PreparedStatement preparedStatement = null;
	    	            preparedStatement = conn.prepareStatement("insert into TrendMap1 values(?, ?, ?, ?, ?)");
						preparedStatement.setString(1, Double.toString(tweet.getGeoLocation().getLatitude()));
						preparedStatement.setString(2, Double.toString(tweet.getGeoLocation().getLongitude()));
						preparedStatement.setString(3, "" + tweet.getCreatedAt());
						preparedStatement.setString(4, name);
						preparedStatement.setString(5, "23424977");
						preparedStatement.executeUpdate();
	        			count++;
	        		}
	        		//System.out.println("Tweet: "+tweet.getText());
	        	}
	        	query=result.nextQuery();
	        	if(query!=null)
	        		result=twitter.search(query);
	        }while(count < 20); 
	        
	        //France
/*	        trends = twitter.getPlaceTrends(23424819); //France
		    name = trends.getTrends()[0].getName();   
		    query = new Query(name);
		    query.setResultType(Query.RECENT);
		    result = twitter.search(query);
		    count = 0;
		    do{
		        List<Status> tweets = result.getTweets();
		        for(Status tweet: tweets){
		        	if(tweet.getGeoLocation()!=null){
		        	 	System.out.println(tweet.getGeoLocation() + ":" + tweet.getText());
		        	 	PreparedStatement preparedStatement = null;
		    	        preparedStatement = conn.prepareStatement("insert into TrendMap1 values(?, ?, ?, ?, ?)");
						preparedStatement.setString(1, Double.toString(tweet.getGeoLocation().getLatitude()));
						preparedStatement.setString(2, Double.toString(tweet.getGeoLocation().getLongitude()));
						preparedStatement.setString(3, "" + tweet.getCreatedAt());
						preparedStatement.setString(4, name);
						preparedStatement.setString(5, "23424819");
						preparedStatement.executeUpdate();
		        		count++;
		        	}
		        		//System.out.println("Tweet: "+tweet.getText());
		        }
		        query=result.nextQuery();
		        if(query!=null)
		        	result=twitter.search(query);
		     }while(count < 20); */
	        
		     //UK
/*		     trends = twitter.getPlaceTrends(23424975); //UK
			 name = trends.getTrends()[0].getName();   
			 query = new Query(name);
			 query.setResultType(Query.RECENT);
			 result = twitter.search(query);
			 count = 0;
			 do{
			     List<Status> tweets = result.getTweets();
			     for(Status tweet: tweets){
			        if(tweet.getGeoLocation()!=null){
			        	System.out.println(tweet.getGeoLocation() + ":" + tweet.getText());
			        	PreparedStatement preparedStatement = null;
			    	    preparedStatement = conn.prepareStatement("insert into TrendMap1 values(?, ?, ?, ?, ?)");
						preparedStatement.setString(1, Double.toString(tweet.getGeoLocation().getLatitude()));
						preparedStatement.setString(2, Double.toString(tweet.getGeoLocation().getLongitude()));
						preparedStatement.setString(3, "" + tweet.getCreatedAt());
						preparedStatement.setString(4, name);
						preparedStatement.setString(5, "23424975");
						preparedStatement.executeUpdate();
			        	count++;
			        }
			        //System.out.println("Tweet: "+tweet.getText());
			     }
			     query=result.nextQuery();
			     if(query!=null)
			        result=twitter.search(query);
			 }while(count < 20);   */
	    } catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
	}
//	public static void main(String[] args) throws TwitterException {
		//List<String> getTrends = getTrends();
//		JSONObject getTrends = getTrends();
//		ServerDispatch.broadcastData(getTrends.toString());
		//for(String getTrend:getTrends){
			//System.out.println(getTrend);
		//}
//	}
}