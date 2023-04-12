package Replica3;

import Replica1.CustomerData;
import Replica1.MovieData;
import Utils.Log;

import javax.xml.ws.Endpoint;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class ATWServer {

    public static void main(String[] args){
        try{
//            MyLogger myLogger = new MyLogger("ATWServer.txt");
//            myLogger.lgr.setLevel(Level.ALL);
//            myLogger.lgr.info("ATW Server Started");
            Log atwaterLogger = new Log("ATWATER-RM-3.txt");
            atwaterLogger.logger.setLevel(Level.ALL);
            atwaterLogger.logger.setUseParentHandlers(false);
            atwaterLogger.logger.info("ATWATER Server started.");

            ServerImplementation serverImplementation = new ServerImplementation("ATW", atwaterLogger);
            Endpoint endpoint = Endpoint.publish("http://localhost:8081/atw", serverImplementation);
            System.out.println("service is published: " + endpoint.isPublished());

            //make static hashmap
            serverImplementation.serverDataList.put("AVATAR",new ConcurrentHashMap<String, ServerData>(){
                {
                    put("ATWM130423", new ServerData(8));
                }
            });
            serverImplementation.serverDataList.get("AVATAR").put("ATWM140423",new ServerData(15));
            serverImplementation.serverDataList.get("AVATAR").put("ATWM150423",new ServerData(13));
            serverImplementation.serverDataList.get("AVATAR").put("ATWE150423",new ServerData(18));
            ArrayList<String> c = new ArrayList<>();
            c.add("ATWC7777");
            c.add("VERC7777");
            c.add("OUTC7777");
            serverImplementation.serverDataList.get("AVATAR").get("ATWM130423").clientIDList = c;


            serverImplementation.serverDataList.put("AVENGERS",new ConcurrentHashMap<String, ServerData>(){
                {
                    put("ATWA130423", new ServerData(4));
                }
            });
            serverImplementation.serverDataList.get("AVENGERS").put("ATWM140423",new ServerData(7));
            serverImplementation.serverDataList.get("AVENGERS").put("ATWM150423",new ServerData(5));
            ArrayList<String> customers = new ArrayList<>();
            customers.add("ATWC7777");
            serverImplementation.serverDataList.get("AVENGERS").get("ATWM140423").clientIDList = customers;

            serverImplementation.serverDataList.put("TITANIC",new ConcurrentHashMap<String, ServerData>(){
                {
                    put("ATWE130423", new ServerData(6));
                }
            });
            serverImplementation.serverDataList.get("TITANIC").put("ATWA150423",new ServerData(8));

            //populate data for ATWC7777
            ArrayList<ClientData> shows = new ArrayList<>();
            shows.add(new ClientData("AVATAR","ATWM130423",3));
            shows.add(new ClientData("AVENGERS","ATWM140423",2));
            serverImplementation.clientDataList.put("ATWC7777", shows);

            //populate data for VERC7777
            ArrayList<ClientData> shows1 = new ArrayList<>();
            shows1.add(new ClientData("AVATAR","ATWM130423",1));
            serverImplementation.clientDataList.put("VERC7777", shows1);

            //populate data for OUTC7777
            ArrayList<ClientData> shows2 = new ArrayList<>();
            shows2.add(new ClientData("AVATAR","ATWM130423",1));
            serverImplementation.clientDataList.put("OUTC7777", shows2);




            System.out.println("ATW Server started");

            Runnable thread1 = new ServerThread(serverImplementation.veratwPort, serverImplementation);
            Runnable thread2 = new ServerThread(serverImplementation.outatwPort, serverImplementation);

            Executor executor = Executors.newFixedThreadPool(2);
            executor.execute(thread1);
            executor.execute(thread2);

        }catch (Exception e){
            e.printStackTrace();
            System.out.println("MTS.ATWServer: " + e);
        }
    }

}

