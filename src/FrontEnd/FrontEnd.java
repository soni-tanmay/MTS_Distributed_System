package FrontEnd;
import Utils.Log;

import javax.xml.ws.Endpoint;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class FrontEnd {

    public static void main(String[] args){
        try {
            Log log = new Log("FrontEnd.txt");
            log.logger.setLevel(Level.ALL);
            log.logger.info("FrontEnd Started");

            FrontEndImpl implementation = new FrontEndImpl(log);
            Endpoint endpoint = Endpoint.publish("http://localhost:8080/"+ "frontend", implementation);
            System.out.println("frontend is published: " + endpoint.isPublished());

            Runnable thread = new FEThread(implementation);
            Executor executor = Executors.newFixedThreadPool(1);
            executor.execute(thread);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEnd: " + e);
        }
    }
}
