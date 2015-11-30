import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartupInit {
//public class StartupInit implements ServletContextListener {

	public static void main(String[] args) {
    //public void contextInitialized(ServletContextEvent event) {
        // Webapp startup.
    	/*System.out.println("Starting webapp init");
    	AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
    	if(sqs == null){
	    	sqs = new AmazonSQSClient(credentialsProvider);
	    	Region usEast1 = Region.getRegion(Regions.US_EAST_1);
	    	sqs.setRegion(usEast1);
	    }
    	try{
			ListQueuesResult queue = sqs.listQueues("TweetQueue");
			Url = queue.getQueueUrls().get(0);
		} catch (IndexOutOfBoundsException ase) {
			CreateQueueRequest createQueueRequest = new CreateQueueRequest("TweetQueue");
	        Url = sqs.createQueue(createQueueRequest).getQueueUrl();
		}*/
    	System.out.println("Stating 5 threads");
    	Runnable pool = new ThreadPool(5);
    	ExecutorService executor = Executors.newFixedThreadPool(2);
//    	Runnable GetTweets = new GetTweets();
//    	executor.execute(GetTweets);
    	executor.execute(pool);
    	//System.out.println("Starting servlet index");
    }

//    public void contextDestroyed(ServletContextEvent event) {
        // Webapp shutdown.
    	//pool.shutdown();
//    	System.out.println("Stopping webapp");
//    }


}