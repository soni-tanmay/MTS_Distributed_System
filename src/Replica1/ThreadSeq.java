package Replica1;

import Utils.Constants;
import Utils.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ThreadSeq implements Runnable{
    private String sequencerIP = "";
    private Log logger;
    private int portNum;

//    public ThreadSeq(int portNum, Log logger, CustomerImpl serverInstance){
//        this.portNum = portNum;
//        this.logger = logger;
//        this.serverInstance = serverInstance;
//    }

    public ThreadSeq(Log logger) {
        this.logger = logger;
    }

    @Override
    public void run() {
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
                this.logger.logger.info("Received datagram packet");
                String reqMsg = (new String(dp.getData())).trim();
                System.out.println("in thread reqMsg: " + reqMsg);
                this.logger.logger.info("Datagram packet response sent.");
                ms.close();
                this.logger.logger.info("Multicast Socket closed.");
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