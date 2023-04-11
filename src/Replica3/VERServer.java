package Replica3;

import Utils.Log;

import javax.xml.ws.Endpoint;
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

            //static data
            serverImplementation.serverDataList.put("AVATAR",new ConcurrentHashMap<String, ServerData>(){
                {
                    put("VERM220323", new ServerData(40));
                    put("VERA220323",new ServerData(50));
                    put("VERE220323",new ServerData(30));
                }
            });
            serverImplementation.serverDataList.put("TITANIC",new ConcurrentHashMap<String, ServerData>(){
                {
                    put("VERM220323", new ServerData(40));
                    put("VERA220323",new ServerData(50));
                    put("VERE220323",new ServerData(30));
                }
            });
            serverImplementation.serverDataList.put("AVENGERS",new ConcurrentHashMap<String, ServerData>(){
                {
                    put("VERM220323", new ServerData(40));
                    put("VERA220323",new ServerData(50));
                    put("VERE220323",new ServerData(30));
                }
            });
            //

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
