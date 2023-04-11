package Replica3;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class ServerThread implements Runnable{
    int portNumber;
    ServerImplementation serverImplementation;

    public ServerThread(int portNumber, ServerImplementation serverImplementation) {
        this.portNumber = portNumber;
        this.serverImplementation = serverImplementation;
    }

    public void run() {
        try {
            while (true) {
                System.out.println(portNumber);
                DatagramSocket datagramSocket = new DatagramSocket(portNumber);

                byte[] b1 = new byte[1024];
                DatagramPacket datagramPacket1 = new DatagramPacket(b1, b1.length);
                datagramSocket.receive(datagramPacket1);
                String str = new String(datagramPacket1.getData());

                byte[] b2 = serverImplementation.mapMethods(str);
                InetAddress ia = InetAddress.getLocalHost();
                DatagramPacket datagramPacket2 = new DatagramPacket(b2, b2.length, ia, datagramPacket1.getPort());
                datagramSocket.send(datagramPacket2);
                datagramSocket.close();
            }
        } catch (Exception e) {
            System.out.println("MTS.ServerThread: " + serverImplementation.serverName + e);
        }
    }
}
