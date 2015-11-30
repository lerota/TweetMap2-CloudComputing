import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

@ServerEndpoint(value = "/aws")
public class ServerDispatch {

	static String dbName = "twitterMap1";
	static String userName = "twitterMap";
	static String password = "cloudcomputing";
	static String hostname = "cs6998cloud.ckv1ixbhxyon.us-east-1.rds.amazonaws.com";
	static String port = "3306";
	static String jdbcUrl = "jdbc:mysql://" + hostname + ":" +
					port + "/" + dbName + "?user=" + userName + "&password=" + password;
	static Connection conn = null;
	static Statement setupStatement = null;
	static ResultSet resultSet = null;
	
	String keyword = null;
	
	private static final Logger LOGGER = 
            Logger.getLogger(ServerDispatch.class.getName());
	private static Set<Session> sessionslist = Collections.synchronizedSet(new HashSet<Session>());
    
    @OnOpen
    public void onOpen(Session session) throws IOException {
      	conn = DBcontrol.getConn(jdbcUrl);
        LOGGER.log(Level.INFO, "New connection with client: {0}", 
                session.getId());
    }
    
    public static void broadcastData(String locOBJ){
    	for (Session s : sessionslist){
                try {
					s.getBasicRemote().sendText(locOBJ);
                } catch (IOException e) {
					e.printStackTrace();
				}
    	}
    }
    
    @OnMessage
    public void onMessage(String message, Session session) throws IOException, InterruptedException, TwitterException {
    	sessionslist.add(session);
    	keyword = message;
    	JSONObject locOBJ = new JSONObject();
    	String selectSQL = null;
		for (Session peer : session.getOpenSessions()) {
                System.out.println("sending ...");
                try {
                	  if (keyword.equals("United States")){
          				selectSQL = "SELECT * FROM TrendMap1 t1 WHERE t1.WOEID = '23424977';";
        				locOBJ = selectTrd(selectSQL);
                        System.out.println("United States");
                        
                	} else if (keyword.equals("France")){
          				selectSQL = "SELECT * FROM TrendMap1 t1 WHERE t1.WOEID = '23424819';";
        				locOBJ = selectTrd(selectSQL);
                        System.out.println("France");
                        
                	} else if (keyword.equals("United Kingdom")){
          				selectSQL = "SELECT * FROM TrendMap1 t1 WHERE t1.WOEID = '23424975';";
        				locOBJ = selectTrd(selectSQL);
                        System.out.println("United Kingdom");
                        
                	} else if (keyword.equals("all")){
        				selectSQL = "SELECT * FROM TwitterMap1;";
        				locOBJ = selectLoc(selectSQL);
        			} else if (keyword.equals("apple") || keyword.equals("football") || keyword.equals("Taylor Swift") || keyword.equals("Accenture")) {
        				selectSQL = "SELECT * FROM TwitterMap1 t1 "
        						+ "WHERE t1.Text LIKE " + "'%" + keyword + "%';";
        				locOBJ = selectLoc(selectSQL);
        			} else {			
        				selectSQL = "SELECT * FROM TwitterMap1 t1 "
        						+ " WHERE t1.Topic = '" + keyword + "';";
        				locOBJ = selectLoc(selectSQL);
        			}
        		} catch (SQLException | JSONException e) {
        			e.printStackTrace();
        		}
                peer.getBasicRemote().sendText(locOBJ.toString());
                System.out.println(locOBJ.toString());
                Thread.sleep(2000);
		}
    }
    
    @OnClose
    public void onClose(Session session) {
    	sessionslist.remove(session);
    	DBcontrol.disConn(conn, setupStatement, resultSet);
        LOGGER.log(Level.INFO, "Close connection for client: {0}", 
                session.getId());
    }
    
    @OnError
    public void onError(Throwable exception, Session session) {
        LOGGER.log(Level.INFO, "Error for client: {0}", session.getId());
    }
    
    public static JSONObject getTrends(int woe) throws TwitterException{
		ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
          .setOAuthConsumerKey("SomQhEqKHwE3NUMVXbQuUpiAC")
          .setOAuthConsumerSecret("2MHLApfmuqB5lVMkVxTr9UCuhcd5gLowJZjYHFTxvEaScyFH3A")
          .setOAuthAccessToken("1253902718-NmquT94d9X2eMG1jY6dsU5CoLbqEcrUqnwwenSq")
          .setOAuthAccessTokenSecret("tCCVrN3TxhzFh3gkttLdgFp1DZonrCj3negMlrIrpNaZX");
		Twitter twitter = new TwitterFactory(cb.build()).getInstance();
	    Trends trends = twitter.getPlaceTrends(woe); //NYC
	    String name = trends.getTrends()[0].getName();  
        Query query = new Query(name);
        query.setResultType(Query.RECENT);
        QueryResult result = twitter.search(query);
        JSONObject json = new JSONObject();
		JSONObject locOBJ = new JSONObject();
        int countTrend = 1;
        do{
        	List<Status> tweets = result.getTweets();
        	for(Status tweet: tweets){
        		if(tweet.getGeoLocation()!=null){
    	            try {
    	          	    json.put("flag", "trends");
    	          	    json.put("longitude", tweet.getGeoLocation().getLongitude());
    	          	    json.put("latitude", tweet.getGeoLocation().getLatitude());
    	          	    json.put("trend", name);
    	          	    locOBJ.put("loc"+countTrend, json);
    	            } catch (JSONException e) {
    					e.printStackTrace();
    	            }
        			countTrend++;
        		}
        	}
        	query=result.nextQuery();
        	if(query!=null)
        		result=twitter.search(query);
        	}while(countTrend<2);
	        
	    return locOBJ;
	}
    
    public static JSONObject selectLoc(String selectSQL) throws SQLException, JSONException{
    	int countLoc = 1;
    	resultSet = DBcontrol.doSelect(conn, selectSQL, setupStatement, resultSet);
    	JSONObject locOBJ = new JSONObject();
		while(resultSet.next()){
			String lng = resultSet.getString("Longitude");
			String lat = resultSet.getString("Latitude");
			JSONObject loc = new JSONObject();
			
			//add flag "location" to JSON object
			loc.put("flag", "location");
			loc.put("longitude", lng);
			loc.put("latitude", lat);
			locOBJ.put("loc"+countLoc, loc);
			countLoc++;
		}
		return locOBJ; 	
    }
    
    public static JSONObject selectTrd(String selectSQL) throws SQLException, JSONException{
    	int countTrd = 1;
    	resultSet = DBcontrol.doSelect(conn, selectSQL, setupStatement, resultSet);
    	JSONObject locOBJ = new JSONObject();
		while(resultSet.next()){
			String lng = resultSet.getString("Longitude");
			String lat = resultSet.getString("Latitude");
			String trend = resultSet.getString("Trend");

			JSONObject loc = new JSONObject();
			
			//add flag "trends" to JSON object
			loc.put("flag", "trends");
			loc.put("longitude", lng);
			loc.put("latitude", lat);
			loc.put("trend", trend);

			locOBJ.put("loc"+countTrd, loc);
			countTrd++;
		}
		return locOBJ; 	
    }
}