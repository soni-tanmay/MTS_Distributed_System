package Replica3;

import Utils.Log;

import javax.xml.ws.Endpoint;
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

            //static data
            serverImplementation.serverDataList.put("AVATAR",new ConcurrentHashMap<String, ServerData>(){
                {
                    put("OUTM220323", new ServerData(40));
                    put("OUTA220323",new ServerData(50));
                    put("OUTE220323",new ServerData(30));
                }
            });
            serverImplementation.serverDataList.put("TITANIC",new ConcurrentHashMap<String, ServerData>(){
                {
                    put("OUTM220323", new ServerData(40));
                    put("OUTA220323",new ServerData(50));
                    put("OUTE220323",new ServerData(30));
                }
            });
            serverImplementation.serverDataList.put("AVENGERS",new ConcurrentHashMap<String, ServerData>(){
                {
                    put("OUTM220323", new ServerData(40));
                    put("OUTA220323",new ServerData(50));
                    put("OUTE220323",new ServerData(30));
                }
            });
            //

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
