package Sequencer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
// import java.net.InetAddress;
import java.net.SocketException;

import Utils.Constants;

public class Sequencer {
    // private InetAddress multicastAddr;
    // private int multicastPort;
    private DatagramSocket socket;

    private int sequenceNo;

    public Sequencer() throws SocketException {
        this.socket = new DatagramSocket(7000);
        // this.multicastAddr = multicastAddr;
        // this.multicastPort = multicastPort;
    }

    private void listen() throws IOException {
        byte[] bufferData = new byte[1024];
        DatagramPacket dp = new DatagramPacket(bufferData, bufferData.length);

        while(true) {
            System.out.println("Sequencer listening to FE at port ");
            socket.receive(dp);

            // String sentenceData = new String(dp.getData(), 0, dp.getLength());
            // String[] splitSentenceData  = sentenceData.split(";");

            int seqNo = generateSequenceNo();

            sendSeqNoToFE(dp, seqNo);
        }
    }

    private int generateSequenceNo() {
        return ++this.sequenceNo;
    }

    private void sendSeqNoToFE(DatagramPacket dp, int seqNo) {
        InetAddress client = null;
        try {
            client = InetAddress.getLocalHost();
        } catch (Exception e) {
            // TODO: handle exception
            throw new RuntimeException(e);
        }

        int clientPort = Constants.FEPort;

        String resp = String.valueOf(seqNo);
        byte[] data = resp.getBytes();
            DatagramPacket resPacket = new DatagramPacket(data, data.length, client, clientPort);
            try {
                socket.send(resPacket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }

    public static void main(String[] args) throws IOException {
        Sequencer seq = new Sequencer();
        seq.listen();
    }
}
