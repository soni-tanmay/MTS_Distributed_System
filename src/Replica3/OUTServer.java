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

public class OUTServer {

    public static void main(String[] args){
        try{
            Log outLogger = new Log("OUTREMONT-RM-3.txt");
            outLogger.logger.setLevel(Level.ALL);
            outLogger.logger.setUseParentHandlers(false);
            outLogger.logger.info("OUTREMONT Server started.");

            ServerImplementation serverImplementation = new ServerImplementation("OUT",outLogger);
            Endpoint endpoint = Endpoint.publish("http://localhost:8083/out", serverImplementation);
            System.out.println("service is published: " + endpoint.isPublished());

            //make static hashmap
            serverImplementation.serverDataList.put("AVATAR",new ConcurrentHashMap<String, ServerData>(){
                {
                    put("OUTM130423", new ServerData(4));
                }
            });
            serverImplementation.serverDataList.get("AVATAR").put("OUTE130423",new ServerData(10));
            serverImplementation.serverDataList.get("AVATAR").put("OUTA140423",new ServerData(6));
            serverImplementation.serverDataList.get("AVATAR").put("OUTE150423",new ServerData(8));


            serverImplementation.serverDataList.put("AVENGERS",new ConcurrentHashMap<String, ServerData>(){
                {
                    put("OUTA130423", new ServerData(4));
                }
            });
            serverImplementation.serverDataList.get("AVENGERS").put("OUTA140423",new ServerData(5));
            serverImplementation.serverDataList.get("AVENGERS").put("OUTA150423",new ServerData(3));
            ArrayList<String> customers = new ArrayList<>();
            customers.add("OUTC7777");
            serverImplementation.serverDataList.get("AVENGERS").get("OUTA150423").clientIDList = customers;


            serverImplementation.serverDataList.put("TITANIC",new ConcurrentHashMap<String, ServerData>(){
                {
                    put("OUTA130423", new ServerData(6));
                }
            });
            serverImplementation.serverDataList.get("TITANIC").put("OUTA150423",new ServerData(8));
            
            ArrayList<ClientData> shows2 = new ArrayList<>();
            shows2.add(new ClientData("AVENGERS","OUTA150423",4));
            serverImplementation.clientDataList.put("OUTC7777", shows2);

            System.out.println("OUT Server started");

            Runnable thread1 = new ServerThread(serverImplementation.atwoutPort, serverImplementation);
            Runnable thread2 = new ServerThread(serverImplementation.veroutPort, serverImplementation);

            Executor executor = Executors.newFixedThreadPool(2);
            executor.execute(thread1);
            executor.execute(thread2);

        }catch (Exception e){
            e.printStackTrace();
            System.out.println("0UTServer: " + e);
        }
    }

}
