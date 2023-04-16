package Replica1;

import javax.xml.ws.Endpoint;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import Utils.Log;

public class AtwaterServer {

    public static void main(String[] args) throws Exception {

        Log atwaterLogger = new Log("ATWATER-RM-1.txt");
        atwaterLogger.logger.setLevel(Level.ALL);
        atwaterLogger.logger.setUseParentHandlers(false);
        atwaterLogger.logger.info("ATWATER Server started.");

        try{
            CustomerImpl atwClient = new CustomerImpl("ATWATER",atwaterLogger);
            System.out.println("Atwater Server Started!");
            Endpoint ep = Endpoint.publish("http://localhost:8083/ATWATER",atwClient);


//        int atwVerPortNum = 4200;
//        int atwOutPortNum = 4201;
            int verAtwPortNum = 4204;
//        int verOutPortNum = 4205;
//        int outVerPortNum = 4208;
            int outAtwPortNum = 4209;

            //make static hashmap
            atwClient.movies.put("AVATAR",new ConcurrentHashMap<String, MovieData>(){
                {
                    put("ATWM130423", new MovieData(48)); //8 is correct
                }
            });
            atwClient.movies.get("AVATAR").put("ATWM140423",new MovieData(15));
            atwClient.movies.get("AVATAR").put("ATWM150423",new MovieData(13));
            atwClient.movies.get("AVATAR").put("ATWE150423",new MovieData(18));
            ArrayList<String> c = new ArrayList<>();
            c.add("ATWC7777");
            c.add("VERC7777");
            c.add("OUTC7777");
            atwClient.movies.get("AVATAR").get("ATWM130423").customers = c;


            atwClient.movies.put("AVENGERS",new ConcurrentHashMap<String, MovieData>(){
                {
                    put("ATWA130423", new MovieData(4));
                }
            });
            atwClient.movies.get("AVENGERS").put("ATWM140423",new MovieData(7));
            atwClient.movies.get("AVENGERS").put("ATWM150423",new MovieData(5));
            ArrayList<String> customers = new ArrayList<>();
            customers.add("ATWC7777");
            atwClient.movies.get("AVENGERS").get("ATWM140423").customers = customers;

            atwClient.movies.put("TITANIC",new ConcurrentHashMap<String, MovieData>(){
                {
                    put("ATWE130423", new MovieData(6));
                }
            });
            atwClient.movies.get("TITANIC").put("ATWA150423",new MovieData(8));

            //populate data for ATWC7777
            ArrayList<CustomerData> shows = new ArrayList<>();
            shows.add(new CustomerData("AVATAR","ATWM130423",3));
            shows.add(new CustomerData("AVENGERS","ATWM140423",2));
            atwClient.customerBookings.put("ATWC7777", shows);

            //populate data for VERC7777
            ArrayList<CustomerData> shows1 = new ArrayList<>();
            shows1.add(new CustomerData("AVATAR","ATWM130423",1));
            atwClient.customerBookings.put("VERC7777", shows1);

            //populate data for OUTC7777
            ArrayList<CustomerData> shows2 = new ArrayList<>();
            shows2.add(new CustomerData("AVATAR","ATWM130423",1));
            atwClient.customerBookings.put("OUTC7777", shows2);

            System.out.println("customerBookings: " + atwClient.customerBookings);

            Runnable thread1 = new ThreadCall(verAtwPortNum, atwClient, atwaterLogger);
            Runnable thread2 = new ThreadCall(outAtwPortNum, atwClient, atwaterLogger);
            System.out.println("threads created in Atw server");

            Executor executor = Executors.newFixedThreadPool(2);
            executor.execute(thread1);
            executor.execute(thread2);

        }
        catch(Exception ex){
            System.out.println("Exception occurred in Atwater Server: " + ex);
            atwaterLogger.logger.warning("Exception occurred in server: " + ex);
        }
    }
}