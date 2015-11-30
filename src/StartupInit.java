import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartupInit {

	public static void main(String[] args) {
    	System.out.println("Stating 5 threads");
    	Runnable pool = new ThreadPool(5);
    	ExecutorService executor = Executors.newFixedThreadPool(2);
    	executor.execute(pool);
    }

}