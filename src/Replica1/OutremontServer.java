package Replica1;

import Utils.Log;
import javax.xml.ws.Endpoint;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class OutremontServer {
    public static void main(String[] args) throws Exception {
//        configureRMIConnection();
        Log outLogger = new Log("OUTREMONT.txt");
        outLogger.logger.setLevel(Level.ALL);
        outLogger.logger.setUseParentHandlers(false);
        outLogger.logger.info("OUTREMONT Server started.");
        try{
            CustomerImpl outClient = new CustomerImpl("OUTREMONT", outLogger);
            Endpoint ep = Endpoint.publish("http://localhost:8081/OUTREMONT",outClient);
            System.out.println("Outremont Server Started!");


//            String []newArgs = {"-ORBInitialPort","1050"};
//            ORB orb = ORB.init(newArgs,null);
//            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
//            rootPOA.the_POAManager().activate();
//
////            ICustomerServant servant = new ICustomerServant("OUTREMONT", outLogger);
////            servant.setORB(orb);
//            outClient.setORB(orb);
//
//            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(outClient);
////            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(servant);
//            webmts.ICustomer href = ICustomerHelper.narrow(ref);
//
//            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
//            NamingContextExt ncref = NamingContextExtHelper.narrow(objRef);
//
//            NameComponent path[] = ncref.to_name("OUTREMONT");
//            ncref.rebind(path,href);

            System.out.println("Server is running now ... ");

//        int atwVerPortNum = 4200;
            int atwOutPortNum = 4201;
//        int verAtwPortNum = 4204;
            int verOutPortNum = 4205;
//        int outVerPortNum = 4208;
//        int outAtwPortNum = 4209;

            //make static hashmap
            outClient.movies.put("AVATAR",new ConcurrentHashMap<String, MovieData>(){
                {
                    put("OUTM130423", new MovieData(4));
                }
            });
            outClient.movies.get("AVATAR").put("OUTE130423",new MovieData(10));
            outClient.movies.get("AVATAR").put("OUTA140423",new MovieData(6));
            outClient.movies.get("AVATAR").put("OUTE150423",new MovieData(8));


            outClient.movies.put("AVENGERS",new ConcurrentHashMap<String, MovieData>(){
                {
                    put("OUTA130423", new MovieData(4));
                }
            });
            outClient.movies.get("AVENGERS").put("OUTA140423",new MovieData(5));
            outClient.movies.get("AVENGERS").put("OUTA150423",new MovieData(3));
            ArrayList<String> customers = new ArrayList<>();
            customers.add("OUTC7777");
            outClient.movies.get("AVENGERS").get("OUTA150423").customers = customers;


            outClient.movies.put("TITANIC",new ConcurrentHashMap<String, MovieData>(){
                {
                    put("OUTA130423", new MovieData(6));
                }
            });
            outClient.movies.get("TITANIC").put("OUTA150423",new MovieData(8));

//            ArrayList<webmts.CustomerData> shows = new ArrayList<>();
////            shows.add(new webmts.CustomerData("AVENGERS","OUTA280223",3));
//            shows.add(new webmts.CustomerData("AVATAR","OUTA260223",1));
//            outClient.customerBookings.put("ATWC7777", shows);
//            ArrayList<String> catw = new ArrayList<>();
//            customers.add("ATWC7777");
////            customers.add("VERC7777");
////            customers.add("OUTC7777");
//            outClient.movies.get("AVATAR").get("OUTA260223").customers.add("ATWC7777");
//
//            ArrayList<webmts.CustomerData> shows1 = new ArrayList<>();
//            shows1.add(new webmts.CustomerData("AVENGERS","OUTA260223",1));
////            shows1.add(new webmts.CustomerData("AVATAR","OUTA280223",1));
//            outClient.customerBookings.put("VERC7777", shows1);

            ArrayList<CustomerData> shows2 = new ArrayList<>();
            shows2.add(new CustomerData("AVENGERS","OUTA150423",4));
            outClient.customerBookings.put("OUTC7777", shows2);
//            System.out.println("static hashmap initialized in Outremont Server");


            Runnable thread1 = new ThreadCall(atwOutPortNum, outClient, outLogger);
            Runnable thread2 = new ThreadCall(verOutPortNum, outClient, outLogger);

            System.out.println("Threads created in Out Server");

            Executor executor = Executors.newFixedThreadPool(2);
            executor.execute(thread1);
            executor.execute(thread2);

//            orb.run();
        }
        catch(Exception ex){
            System.out.println("Exception occurred in Outremont Server: " + ex);
            outLogger.logger.warning("Exception occurred: " + ex);
        }
    }

    static void configureRMIConnection() throws Exception {
    }
}
