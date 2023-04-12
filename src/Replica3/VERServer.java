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

public class VERServer {

    public static void main(String[] args){
        try{
            Log verdunLogger = new Log("VERDUN.txt");
            verdunLogger.logger.setLevel(Level.ALL);
            verdunLogger.logger.setUseParentHandlers(false);
            verdunLogger.logger.info("VERDUN Server started.");

            ServerImplementation serverImplementation = new ServerImplementation("VER",verdunLogger);
            Endpoint endpoint = Endpoint.publish("http://localhost:8082/ver", serverImplementation);
            System.out.println("service is published: " + endpoint.isPublished());

            //make static hashmap
            serverImplementation.serverDataList.put("AVATAR",new ConcurrentHashMap<String, ServerData>(){
                {
                    put("VERM240323", new ServerData(4));
                }
            });
            serverImplementation.serverDataList.get("AVATAR").put("VERM230323",new ServerData(10));
            serverImplementation.serverDataList.get("AVATAR").put("VERM250323",new ServerData(6));
            serverImplementation.serverDataList.get("AVATAR").put("VERE240323",new ServerData(8));
            ArrayList<String> c = new ArrayList<>();
            c.add("ATWC7777");
            c.add("VERC7777");
            c.add("OUTC7777");
            serverImplementation.serverDataList.get("AVATAR").get("VERE240323").clientIDList = c;


            serverImplementation.serverDataList.put("AVENGERS",new ConcurrentHashMap<String, ServerData>(){
                {
                    put("VERA250323", new ServerData(4));
                }
            });
            serverImplementation.serverDataList.get("AVENGERS").put("VERM230323",new ServerData(5));
            serverImplementation.serverDataList.get("AVENGERS").put("VERM250323",new ServerData(3));
            ArrayList<String> customers = new ArrayList<>();
            customers.add("VERC7777");
            serverImplementation.serverDataList.get("AVENGERS").get("VERA250323").clientIDList = customers;


            serverImplementation.serverDataList.put("TITANIC",new ConcurrentHashMap<String, ServerData>(){
                {
                    put("VERE240323", new ServerData(6));
                }
            });
            serverImplementation.serverDataList.get("TITANIC").put("VERM250323",new ServerData(8));

            //populate data for ATWC7777
            ArrayList<ClientData> shows = new ArrayList<>();
            shows.add(new ClientData("AVATAR","VERE240323",1));
            serverImplementation.clientDataList.put("ATWC7777", shows);

            //populate data for VERC7777
            ArrayList<ClientData> shows1 = new ArrayList<>();
            shows1.add(new ClientData("AVENGERS","VERA250323",1));
            shows1.add(new ClientData("AVATAR","VERE240323",3));
            serverImplementation.clientDataList.put("VERC7777", shows1);

            //populate data for OUTC7777
            ArrayList<ClientData> shows2 = new ArrayList<>();
            shows2.add(new ClientData("AVATAR","VERE240323",1));
            serverImplementation.clientDataList.put("OUTC7777", shows2);
            
            
            System.out.println("VER Server started");

            Runnable thread1 = new ServerThread(serverImplementation.atwverPort, serverImplementation);
            Runnable thread2 = new ServerThread(serverImplementation.outverPort, serverImplementation);

            Executor executor = Executors.newFixedThreadPool(2);
            executor.execute(thread1);
            executor.execute(thread2);

        }catch (Exception e){
            e.printStackTrace();
            System.out.println("MTS.VERServer: " + e);
        }
    }

}
