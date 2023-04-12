package Replica2;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import javax.xml.ws.Endpoint;
import Utils.Log;

public class AtwaterServer {
	public static void main(String[] args) throws SecurityException, IOException {
        // LoggerClass logInfo = new LoggerClass("Atwater" + ".txt");

        Log atwaterLogger = new Log("ATWATER-RM-2.txt");
        atwaterLogger.logger.setLevel(Level.ALL);
        atwaterLogger.logger.setUseParentHandlers(false);
        atwaterLogger.logger.info("ATWATER Server started.");
        

        BookingImplementation atw = new BookingImplementation("ATW", atwaterLogger);
		Endpoint endpoint = Endpoint.publish("http://localhost:8083/ATWATER", atw);

        atw.movies.put("AVATAR",new ConcurrentHashMap<String, Booking>(){
            {
                put("ATWM130423", new Booking(8));
            }
        });
        atw.movies.get("AVATAR").put("ATWM140423",new Booking(15));
        atw.movies.get("AVATAR").put("ATWM150423",new Booking(13));
        atw.movies.get("AVATAR").put("ATWE150423",new Booking(18));
        ArrayList<String> c = new ArrayList<>();
        c.add("ATWC7777");
        c.add("VERC7777");
        c.add("OUTC7777");
        atw.movies.get("AVATAR").get("ATWM130423").cust_ids = c;


        atw.movies.put("AVENGERS",new ConcurrentHashMap<String, Booking>(){
            {
                put("ATWA130423", new Booking(4));
            }
        });
        atw.movies.get("AVENGERS").put("ATWM140423",new Booking(7));
        atw.movies.get("AVENGERS").put("ATWM150423",new Booking(5));
        ArrayList<String> customers = new ArrayList<>();
        customers.add("ATWC7777");
        atw.movies.get("AVENGERS").get("ATWM140423").cust_ids = customers;

        atw.movies.put("TITANIC",new ConcurrentHashMap<String, Booking>(){
            {
                put("ATWE130423", new Booking(6));
            }
        });
        atw.movies.get("TITANIC").put("ATWA150423",new Booking(8));

        //populate data for ATWC7777
        ArrayList<Customer> shows = new ArrayList<>();
        shows.add(new Customer("AVATAR",3, "ATWM130423"));
        shows.add(new Customer("AVENGERS",2, "ATWM140423"));
        atw.customers.put("ATWC7777", shows);

        //populate data for VERC7777
        ArrayList<Customer> shows1 = new ArrayList<>();
        shows1.add(new Customer("AVATAR",1,"ATWM130423"));
        atw.customers.put("VERC7777", shows1);

        //populate data for OUTC7777
        ArrayList<Customer> shows2 = new ArrayList<>();
        shows2.add(new Customer("AVATAR",1, "ATWM130423"));
        atw.customers.put("OUTC7777", shows2);

        Thread UDPCommunicator = new Thread(){
            public void run() {           
                try {
                while(true) {
                    // int port = serverName.substring(0, 3).equals("ATW") ? 7600 : serverName.substring(0, 3).equals("VER") ? 7800 :   serverName.substring(0, 3).equals("OUT") ? 7700 : 7600;

                    DatagramSocket ds = new DatagramSocket(7600);
    
                    byte[]b1=new byte[1024];
    
                    DatagramPacket dp=new DatagramPacket(b1,b1.length);
    
                    ds.receive(dp);
    
                    String str = new String(dp.getData());
    
                    ArrayList<String> result = new ArrayList<>();
    
                    result = atw.mapFunction(str, atwaterLogger);
    
                    // System.out.println("Result in UDP server " + serverName + " " + result);
    
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    
                    DataOutputStream out = new DataOutputStream(baos);
    
                    for (String element : result) {
                        out.writeUTF(element);
                    }
    
                    byte[] bytes = baos.toByteArray();
    
                    InetAddress ia=InetAddress.getLocalHost();
    
                    DatagramPacket dp1=new DatagramPacket(bytes, bytes.length, ia, dp.getPort());
    
                    ds.send(dp1);
    
                    ds.close();
                }
            } catch(Exception err) {
                System.out.println("Error in Atwater thread " + err);
                err.printStackTrace();
            }
        }
    };

    UDPCommunicator.start();
    atwaterLogger.logger.info("Atwater service is published " + endpoint.isPublished());
	// System.out.println("Atwater service is published: " + endpoint.isPublished());
	}

}