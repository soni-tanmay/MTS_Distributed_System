package Replica1;

import Utils.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ReplicaManager1 {
    static Log rmLogger;
//    private static String reqMsg;
    public ReplicaManager1() throws IOException {
        rmLogger = new Log("RM1");
    }

    public static void main(String[] args) {
        new Thread(
            () -> {
                try{
                    getRequestFromSequencer();
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        ).start();
    }

    public static void getRequestFromSequencer(){
        MulticastSocket ms = null;
        try {
            ms = new MulticastSocket(Constants.multicastSocket);
            InetAddress group = InetAddress.getByName(Constants.NetworkIP);
            ms.joinGroup(group);
            while(true){
                System.out.println("Thread started from RM1");
                byte[] req = new byte[1024];
                DatagramPacket dp = new DatagramPacket(req, req.length);
                ms.receive(dp);
                rmLogger.logger.info("Received datagram packet");
                String reqMsg = (new String(dp.getData())).trim();
                System.out.println("in thread reqMsg: " + reqMsg);
                String response = processRequest(reqMsg);
                sendResponseToFE(response);
//                rmLogger.logger.info("Datagram packet response sent.");
//                ms.close();
//                rmLogger.logger.info("Multicast Socket closed.");
            }
        }
        catch(Exception ex) {
            System.out.println("Exception occurred!!!");
            rmLogger.logger.warning("Exception occurred: " + ex);
            System.out.println(ex);
        }
        finally {
            System.out.println("Exit Server");
        }
    }

    public static String processRequest(String reqMsg){
        return "Response from Server";
    }

    public static void sendResponseToFE(String response){
        DatagramSocket ds = null;
        try{
            ds = new DatagramSocket();
            byte[] req = response.getBytes();
            InetAddress ia = InetAddress.getByName(Constants.FE_IP);
            DatagramPacket dp = new DatagramPacket(req,req.length,ia,Constants.FEPort);
            ds.send(dp);
            rmLogger.logger.info("Datagram packet sent to port " + Constants.FEPort);

//            byte[] resp = new byte[1024];
//            DatagramPacket dpreply = new DatagramPacket(resp,resp.length);
//
//            //receive data
//            ds.receive(dpreply);
//            rmLogger.logger.info("response received of sent packet");
//            byte[] res = dpreply.getData();
//            rmLogger.logger.info("Function call completed successfully");
        }
        catch(Exception ex){
            System.out.println("Exception occurred!");
            rmLogger.logger.warning("Exception occurred: " + ex);
        }
    }
}
