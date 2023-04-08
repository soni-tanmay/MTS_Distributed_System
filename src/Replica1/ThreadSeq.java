package Replica1;

import Utils.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ThreadSeq implements Runnable{
    private String sequencerIP = "";
    private CustomerImpl serverInstance;
    private Log logger;
    private int portNum;

    public ThreadSeq(int portNum, Log logger, CustomerImpl serverInstance){
        this.portNum = portNum;
        this.logger = logger;
        this.serverInstance = serverInstance;
    }

    @Override
    public void run() {
        DatagramSocket ds = null;
        try {
            while(true){
                System.out.println("Thread started from server: " + serverInstance);
                logger.logger.info("Thread started from server: " + serverInstance);
                ds = new DatagramSocket(portNum);
                logger.logger.info("Datagram socket opened on IP: " + sequencerIP);
                byte[] req = new byte[1024];

                DatagramPacket dp = new DatagramPacket(req,req.length);

                ds.receive(dp);
                logger.logger.info("Received datagram packet");
                String reqMsg = new String(dp.getData()).trim();
                System.out.println("in thread reqMsg: " + reqMsg);

                byte[] response = "Response from RM".getBytes();
//                        serverInstance.getMyData(reqMsg);
                InetAddress ina = InetAddress.getLocalHost();
                DatagramPacket dpresp = new DatagramPacket(response,response.length,ina,dp.getPort());

                ds.send(dpresp);
                logger.logger.info("Datagram packet response sent.");
                System.out.println("Thread response sent from " + serverInstance);
                ds.close();
                logger.logger.info("Datagram Socket closed.");
            }
        }
        catch(Exception ex) {
            System.out.println("Exception occurred!!!");
            logger.logger.warning("Exception occurred: " + ex);
            System.out.println(ex);
        }
        finally {
            System.out.println("Exit Server");
        }
    }
}
