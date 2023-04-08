package FrontEnd;

import Utils.Constants;
import Utils.Log;

import javax.xml.ws.Endpoint;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;

public class FESQThread {
    static String request;
    static FrontEndImpl implementation;
    public FESQThread(String request) {
        super();
        this.request=request;
        this.implementation=implementation;
    }
    public static void main(String[] args){
        try {
            DatagramSocket datagramSocket = new DatagramSocket(Constants.SQPort);
            byte[] requestData = request.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(requestData, requestData.length, InetAddress.getLocalHost(), Constants.SQPort);
            datagramSocket.send(datagramPacket);

            String response = new String(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getLength());
            System.out.println("Received msg from Sequencer: " + response);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FESQThread: " + e);
        }
    }
}
