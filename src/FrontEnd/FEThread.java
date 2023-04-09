package FrontEnd;

import Utils.Constants;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class FEThread implements Runnable{
    @Override
    public void run() {
        try {
            while (true) {
                DatagramSocket datagramSocket = new DatagramSocket(Constants.FEResPort);
                byte[] b1 = new byte[1024];
                DatagramPacket responseDp = new DatagramPacket(b1, b1.length);
                datagramSocket.receive(responseDp);
                String response = new String(responseDp.getData(), responseDp.getOffset(), responseDp.getLength());
                System.out.println("Received msg from Replica Manager: " + response);
                datagramSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("FEThread: "+e);
        }
    }
}
