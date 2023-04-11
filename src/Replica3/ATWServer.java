package Replica3;

import Utils.Log;

import javax.xml.ws.Endpoint;
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

            //static data
            serverImplementation.serverDataList.put("AVATAR",new ConcurrentHashMap<String, ServerData>(){
                {
                    put("ATWM220323", new ServerData(40));
                    put("ATWA220323",new ServerData(50));
                    put("ATWE220323",new ServerData(30));
                }
            });
            serverImplementation.serverDataList.put("TITANIC",new ConcurrentHashMap<String, ServerData>(){
                {
                    put("ATWM220323", new ServerData(40));
                    put("ATWA220323",new ServerData(50));
                    put("ATWE220323",new ServerData(30));
                }
            });
            serverImplementation.serverDataList.put("AVENGERS",new ConcurrentHashMap<String, ServerData>(){
                {
                    put("ATWM220323", new ServerData(40));
                    put("ATWA220323",new ServerData(50));
                    put("ATWE220323",new ServerData(30));
                }
            });
            //



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

