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

public class VerdunServer {
	public static void main(String[] args) throws SecurityException, IOException {
        Log verdunLogger = new Log("VERDUN-RM-2.txt");
        verdunLogger.logger.setLevel(Level.ALL);
        verdunLogger.logger.setUseParentHandlers(false);
        verdunLogger.logger.info("VERDUN Server started.");


        BookingImplementation ver = new BookingImplementation("VER", verdunLogger);
		Endpoint endpoint = Endpoint.publish("http://localhost:8082/VERDUN", ver);
        // 7800

        ver.movies.put("AVATAR",new ConcurrentHashMap<String, Booking>(){
            {
                put("VERM240323", new Booking(4));
            }
        });
        ver.movies.get("AVATAR").put("VERM230323",new Booking(10));
        ver.movies.get("AVATAR").put("VERM250323",new Booking(6));
        ver.movies.get("AVATAR").put("VERE240323",new Booking(8));
        ArrayList<String> c = new ArrayList<>();
        c.add("ATWC7777");
        c.add("VERC7777");
        c.add("OUTC7777");
        ver.movies.get("AVATAR").get("VERE240323").cust_ids = c;


        ver.movies.put("AVENGERS",new ConcurrentHashMap<String, Booking>(){
            {
                put("VERA250323", new Booking(4));
            }
        });
        ver.movies.get("AVENGERS").put("VERM230323",new Booking(5));
        ver.movies.get("AVENGERS").put("VERM250323",new Booking(3));
        ArrayList<String> customers = new ArrayList<>();
        customers.add("VERC7777");
        ver.movies.get("AVENGERS").get("VERA250323").cust_ids = customers;


        ver.movies.put("TITANIC",new ConcurrentHashMap<String, Booking>(){
            {
                put("VERE240323", new Booking(6));
            }
        });
        ver.movies.get("TITANIC").put("VERM250323",new Booking(8));

        //populate data for ATWC7777
        ArrayList<Customer> shows = new ArrayList<>();
        shows.add(new Customer("AVATAR", 1, "VERE240323"));
        ver.customers.put("ATWC7777", shows);

        //populate data for VERC7777
        ArrayList<Customer> shows1 = new ArrayList<>();
        shows1.add(new Customer("AVENGERS", 1, "VERA250323"));
        shows1.add(new Customer("AVATAR", 3, "VERE240323"));
        ver.customers.put("VERC7777", shows1);

        //populate data for OUTC7777
        ArrayList<Customer> shows2 = new ArrayList<>();
        shows2.add(new Customer("AVATAR",1, "VERE240323"));
        ver.customers.put("OUTC7777", shows2);

        Thread UDPCommunicator = new Thread(){
            public void run() {           
                try {
                while(true) {
                    // int port = serverName.substring(0, 3).equals("ATW") ? 7600 : serverName.substring(0, 3).equals("VER") ? 7800 :   serverName.substring(0, 3).equals("OUT") ? 7700 : 7600;

                    DatagramSocket ds = new DatagramSocket(7800);
    
                    byte[]b1=new byte[1024];
    
                    DatagramPacket dp=new DatagramPacket(b1,b1.length);
    
                    ds.receive(dp);
    
                    String str = new String(dp.getData());
    
                    ArrayList<String> result = new ArrayList<>();
    
                    result = ver.mapFunction(str, verdunLogger);
    
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
                System.out.println("Error in Verdun thread " + err);
                err.printStackTrace();
            }
        }
    };

    UDPCommunicator.start();
    verdunLogger.logger.info("Verdun service is published: " + endpoint.isPublished());
	}

}

