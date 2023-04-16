package Sequencer;

import java.io.IOException;
import java.net.*;
// import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import Utils.Constants;

public class Sequencer {
    private InetAddress multicastAddr;
    private int multicastPort;
    private DatagramSocket socket;

    private int sequenceNo;

    public Sequencer(InetAddress multicastAddr, int multicastPort) throws SocketException {
        this.socket = new DatagramSocket(7002);
        this.multicastAddr = multicastAddr;
        this.multicastPort = multicastPort;
    }

    private void listen() throws IOException {
        System.out.println("inside listen");
        byte[] bufferData = new byte[1024];
        DatagramPacket dp = new DatagramPacket(bufferData, bufferData.length);

        while(true) {
            System.out.println("Sequencer listening to FE at port ");
            socket.receive(dp);

            System.out.println("dp " + dp.getData());
            String sentenceData = new String(dp.getData(), 0, dp.getLength());
            System.out.println("sentenceData " + sentenceData);
            String[] splitSentenceData  = sentenceData.split("_");

            System.out.println("splitSentenceData " + splitSentenceData);

            int seqNo = generateSequenceNo();
            String seqData = addSequenceNoToData(seqNo, sentenceData);

            System.out.println("seqData" + seqData);

            System.out.println("seqNo " + seqNo);
            byte[] message = seqData.getBytes();

            List<InetAddress> listAllBroadcastAddresses = listAllBroadcastAddresses();
            broadcastToRm(listAllBroadcastAddresses,message,multicastPort);
            //broadcastToRm(message);
            
            sendSeqNoToFE(dp, seqData);
        }
    }

    private int generateSequenceNo() {
        return ++this.sequenceNo;
    }

    private String addSequenceNoToData(int seqNum, String data) {
        String SeqWithData = seqNum + "_" + data;
        System.out.println("data " + data);
        return SeqWithData;

    }

    private void sendSeqNoToFE(DatagramPacket dp, String seqData) throws UnknownHostException {
        System.out.println("###" + dp + " " + seqData);

        // String resp = String.valueOf(seqNo);
        byte[] data = seqData.getBytes();
        DatagramPacket resPacket = new DatagramPacket(data, data.length, InetAddress.getByName(Constants.FE_IP),  Constants.FEPort);
        //uncomment below line to run in own env
        //DatagramPacket resPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(),  Constants.FEPort);
            System.out.println("resPacket " + resPacket);
            try {
                socket.send(resPacket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }

//    private void broadcastToRm(byte[] data) {
//        DatagramSocket socket1;
//                try {
//                    socket1 = new DatagramSocket();
//                } catch (SocketException e) {
//                    throw new RuntimeException(e);
//                }
//                try {
//                    socket1.setBroadcast(true);
//                } catch (SocketException e) {
//                    throw new RuntimeException(e);
//                }
//                DatagramPacket dp
//                        = new DatagramPacket(data, data.length, multicastAddr, multicastPort);
//                try {
//                    socket1.send(dp);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                socket1.close();
//
//    }

    private void broadcastToRm(List<InetAddress> listAllBroadcastAddresses, byte[] sequencedData, int multicastPort) {
        for(InetAddress addr: listAllBroadcastAddresses) {
            DatagramSocket socketInner;
            try {
                socketInner = new DatagramSocket();
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
            try {
                socketInner.setBroadcast(true);
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }

            //System.out.println("Sequencer sending request to RM : MulticastAddress - "+multicastAddress);
            System.out.println("Sequencer sending request to RM : MulticastPort - "+multicastPort);

            DatagramPacket packet
                    = new DatagramPacket(sequencedData, sequencedData.length, addr, multicastPort);
            try {
                socketInner.send(packet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            socketInner.close();
        }
    }

    private List<InetAddress> listAllBroadcastAddresses() {
        List<InetAddress> broadcastList = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces
                = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            try {
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }

            networkInterface.getInterfaceAddresses().stream()
                    .map(a -> a.getBroadcast())
                    .filter(Objects::nonNull)
                    .forEach(broadcastList::add);
        }
        return broadcastList;
    }

    public static void main(String[] args) throws IOException {
        Sequencer seq = new Sequencer(InetAddress.getByName(Constants.NetworkIP), Constants.multicastSocket);
        seq.listen();
    }
}
