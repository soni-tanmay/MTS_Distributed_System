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

public class OutrementServer {
	public static void main(String[] args) throws SecurityException, IOException {

        Log outrementLogger = new Log("OUTREMENT-RM-2.txt");
        outrementLogger.logger.setLevel(Level.ALL);
        outrementLogger.logger.setUseParentHandlers(false);
        outrementLogger.logger.info("OUTREMENT Server started.");

		// LoggerClass logInfo = new LoggerClass("Outrement" + ".txt");

        // logInfo.logger.setLevel(Level.ALL);

        BookingImplementation out = new BookingImplementation("OUT", outrementLogger);
		Endpoint endpoint = Endpoint.publish("http://localhost:8081/OUTREMONT", out);

        out.movies.put("AVATAR",new ConcurrentHashMap<String, Booking>(){
            {
                put("OUTM130423", new Booking(4));
            }
        });
        out.movies.get("AVATAR").put("OUTE130423",new Booking(10));
        out.movies.get("AVATAR").put("OUTA140423",new Booking(6));
        out.movies.get("AVATAR").put("OUTE150423",new Booking(8));


        out.movies.put("AVENGERS",new ConcurrentHashMap<String, Booking>(){
            {
                put("OUTA130423", new Booking(4));
            }
        });
        out.movies.get("AVENGERS").put("OUTA140423",new Booking(5));
        out.movies.get("AVENGERS").put("OUTA150423",new Booking(3));
        ArrayList<String> customers = new ArrayList<>();
        customers.add("OUTC7777");
        out.movies.get("AVENGERS").get("OUTA150423").cust_ids = customers;


        out.movies.put("TITANIC",new ConcurrentHashMap<String, Booking>(){
            {
                put("OUTA130423", new Booking(6));
            }
        });
        out.movies.get("TITANIC").put("OUTA150423",new Booking(8));

        ArrayList<Customer> shows2 = new ArrayList<>();
        shows2.add(new Customer("AVENGERS",4,"OUTA150423"));
        out.customers.put("OUTC7777", shows2);


        //populate data for ATWC7777
        ArrayList<Customer> shows = new ArrayList<>();
        shows.add(new Customer("AVATAR",3, "ATWM130423"));
        shows.add(new Customer("AVENGERS",2, "ATWM140423"));
        out.customers.put("ATWC7777", shows);

        //populate data for VERC7777
        ArrayList<Customer> shows1 = new ArrayList<>();
        shows1.add(new Customer("AVATAR",1,"ATWM130423"));
        out.customers.put("VERC7777", shows1);

        //populate data for OUTC7777
        ArrayList<Customer> shows3 = new ArrayList<>();
        shows2.add(new Customer("AVATAR",1, "ATWM130423"));
        out.customers.put("OUTC7777", shows3);

        Thread UDPCommunicator = new Thread(){
            public void run() {           
                try {
                while(true) {
                    // int port = serverName.substring(0, 3).equals("ATW") ? 7600 : serverName.substring(0, 3).equals("VER") ? 7800 :   serverName.substring(0, 3).equals("OUT") ? 7700 : 7600;

                    DatagramSocket ds = new DatagramSocket(7700);
    
                    byte[]b1=new byte[1024];
    
                    DatagramPacket dp=new DatagramPacket(b1,b1.length);
    
                    ds.receive(dp);
    
                    String str = new String(dp.getData());
    
                    ArrayList<String> result = new ArrayList<>();
    
                    result = out.mapFunction(str, outrementLogger);
    
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
                System.out.println("Error in Outrement thread " + err);
                err.printStackTrace();
            }
        }
    };

    UDPCommunicator.start();
	outrementLogger.logger.info("outrement service is published: " + endpoint.isPublished());
	}

}
