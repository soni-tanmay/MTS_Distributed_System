package Replica1;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import Utils.Log;
import javax.xml.ws.Endpoint;

public class VerdunServer {
    public static void main(String[] args) throws Exception {
        configureRMIConnection();
    }

    static void configureRMIConnection() throws Exception {

        Log verdunLogger = new Log("VERDUN-RM-1.txt");
        verdunLogger.logger.setLevel(Level.ALL);
        verdunLogger.logger.setUseParentHandlers(false);
        verdunLogger.logger.info("VERDUN Server started.");
        try{
//            Customer verClient = new Customer("VERDUN", verdunLogger);
            CustomerImpl verClient = new CustomerImpl("VERDUN",verdunLogger);
            Endpoint ep = Endpoint.publish("http://localhost:8082/VERDUN",verClient);
//            Naming.rebind("VerInstance",verClient);
            System.out.println("Verdun Server Started!");

//            String []newArgs = {"-ORBInitialPort","1050"};
//            ORB orb = ORB.init(newArgs,null);
//            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
//            rootPOA.the_POAManager().activate();
//
////            ICustomerServant servant = new ICustomerServant();
////            servant.setORB(orb);
//            verClient.setORB(orb);
//
//            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(verClient);
//            webmts.ICustomer href = ICustomerHelper.narrow(ref);
//
//            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
//            NamingContextExt ncref = NamingContextExtHelper.narrow(objRef);
//
//            NameComponent path[] = ncref.to_name("VERDUN");
//            ncref.rebind(path,href);

            System.out.println("Server is running now ... ");

//            webmts.Log verdunLogger = new webmts.Log("VERDUN.txt");
//            verdunLogger.logger.setLevel(Level.ALL);
//            verdunLogger.logger.info("VERDUN Server started.");


            int atwVerPortNum = 4200;
//        int atwOutPortNum = 4201;
//        int verAtwPortNum = 4204;
//        int verOutPortNum = 4205;
            int outVerPortNum = 4208;
//        int outAtwPortNum = 4209;


            //make static hashmap
            verClient.movies.put("AVATAR",new ConcurrentHashMap<String, MovieData>(){
                {
                    put("VERM240323", new MovieData(4));
                }
            });
            verClient.movies.get("AVATAR").put("VERM230323",new MovieData(10));
            verClient.movies.get("AVATAR").put("VERM250323",new MovieData(6));
            verClient.movies.get("AVATAR").put("VERE240323",new MovieData(8));
            ArrayList<String> c = new ArrayList<>();
            c.add("ATWC7777");
            c.add("VERC7777");
            c.add("OUTC7777");
            verClient.movies.get("AVATAR").get("VERE240323").customers = c;


            verClient.movies.put("AVENGERS",new ConcurrentHashMap<String, MovieData>(){
                {
                    put("VERA250323", new MovieData(4));
                }
            });
            verClient.movies.get("AVENGERS").put("VERM230323",new MovieData(5));
            verClient.movies.get("AVENGERS").put("VERM250323",new MovieData(3));
            ArrayList<String> customers = new ArrayList<>();
            customers.add("VERC7777");
            verClient.movies.get("AVENGERS").get("VERA250323").customers = customers;


            verClient.movies.put("TITANIC",new ConcurrentHashMap<String, MovieData>(){
                {
                    put("VERE240323", new MovieData(6));
                }
            });
            verClient.movies.get("TITANIC").put("VERM250323",new MovieData(8));

            //populate data for ATWC7777
            ArrayList<CustomerData> shows = new ArrayList<>();
            shows.add(new CustomerData("AVATAR","VERE240323",1));
            verClient.customerBookings.put("ATWC7777", shows);

            //populate data for VERC7777
            ArrayList<CustomerData> shows1 = new ArrayList<>();
            shows1.add(new CustomerData("AVENGERS","VERA250323",1));
            shows1.add(new CustomerData("AVATAR","VERE240323",3));
            verClient.customerBookings.put("VERC7777", shows1);

            //populate data for OUTC7777
            ArrayList<CustomerData> shows2 = new ArrayList<>();
            shows2.add(new CustomerData("AVATAR","VERE240323",1));
            verClient.customerBookings.put("OUTC7777", shows2);

            System.out.println("static hashmap initialized in Verdun Server");

            Runnable thread1 = new ThreadCall(atwVerPortNum, verClient, verdunLogger);
            Runnable thread2 = new ThreadCall(outVerPortNum, verClient, verdunLogger);
            System.out.println("Threads created");

            Executor executor = Executors.newFixedThreadPool(2);
            executor.execute(thread1);
            executor.execute(thread2);

//            orb.run();
        }
        catch(Exception ex){
            System.out.println("Exception occurred in Verdun Server: " + ex);
            verdunLogger.logger.warning("Exception occurred in server: " + ex);
        }
    }
}

