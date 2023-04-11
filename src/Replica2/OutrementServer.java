package Replica2;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.xml.ws.Endpoint;

import Utils.Log;

public class OutrementServer {
	public static void main(String[] args) throws SecurityException, IOException {

        Log outrementLogger = new Log("OUTREMENT.txt");
        outrementLogger.logger.setLevel(Level.ALL);
        outrementLogger.logger.setUseParentHandlers(false);
        outrementLogger.logger.info("OUTREMENT Server started.");

		// LoggerClass logInfo = new LoggerClass("Outrement" + ".txt");

        // logInfo.logger.setLevel(Level.ALL);

        BookingImplementation out = new BookingImplementation("OUT", outrementLogger);
		Endpoint endpoint = Endpoint.publish("http://localhost:8081/OUTREMONT", out);

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
