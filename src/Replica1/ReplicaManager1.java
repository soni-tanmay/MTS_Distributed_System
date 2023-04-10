package Replica1;

import Utils.*;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.IOException;
import java.net.*;
import java.util.Enumeration;


public class ReplicaManager1 {
    static Log rmLogger;
    static int seqCounter;
    private static Service serviceAPI;
    static ICustomer clientObj;
//    private static String reqMsg;
    public ReplicaManager1() throws IOException {
        rmLogger = new Log("RM1");
    }

    private static String identifyClientServer(String userid){
        String server = userid.substring(0,3).toUpperCase();
        if(server.equals("ATW")){
            return "ATWATER";
        }
        else if(server.equals("OUT")){
            return "OUTREMONT";
        }
        else {
            return "VERDUN";
        }
    }

    public static void main(String[] args) throws IOException {
        rmLogger = new Log("RM1");
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
            //comment below 3 lines to test on own system
            NetworkInterface networkInterface = NetworkInterface.getByName("en0");
            Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces();
            ms.setNetworkInterface(networkInterface);

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
            System.out.println(ex);
            ex.printStackTrace();
            rmLogger.logger.warning("Exception occurred: " + ex);

        }
        finally {
            System.out.println("Exit Server");
        }
    }


    public static String processRequest(String reqMsg) throws MalformedURLException {
        if(reqMsg.isEmpty()) {
            return seqCounter + "_RM1_Failure";
        }
        String[] request = reqMsg.split("_");
        seqCounter = Integer.parseInt(request[0]);
        //ToDo verify if correct
        System.out.println("Split Request: ");
        for(String s: request){
            System.out.println(s);
        }
        String replicaResponse = sendRequestToReplica(reqMsg); //ToDo send request to implementation
        return seqCounter + "_RM1_" + replicaResponse;
    }

//1_addMovieSlots_movieID_movieName_bookingCapacity
//2_removeMovieSlots_movieID_movieName
//3_listMovieShowsAvailability_movieName_isClientCall
//4_bookMovieTickets_customerID_movieID_movieName_numberOfTickets
//5_getBookingSchedule_customerID_isClientCall
//6_cancelMovieTickets_customerID_movieID_movieName_numberOfTickets
//7_exchangeTickets_customerID_movieID_old_movieName_new_movieID_new_movieName_numberOfTickets
    public static String sendRequestToReplica(String reqMsg) throws MalformedURLException {

        URL url;
        QName qName;
        String[] params = reqMsg.split("_");
        String server = identifyClientServer(params[2].toString());

        url = new URL("http://localhost:8080/" + server +"?wsdl");
        qName = new QName("http://Replica1/", "CustomerService");
        Service service = Service.create(url,qName);
        clientObj = service.getPort(ICustomer.class);

        String response;
//        switch (params[1]){
//            case "addMovieSlots":
//                response = ;
//                System.out.println("Response: " + response);
//                return response;
//
//            case "removeMovieSlots":
//                response = ;
//                System.out.println("Response: " + response);
//                return response;
//
//            case "listMovieShowsAvailability":
//                response = ;
//                System.out.println("Response: " + response);
//                return response;
//
//            case "bookMovieTickets":
//                response = ;
//                System.out.println("Response: " + response);
//                return response;
//
//            case "getBookingSchedule":
//                response = ;
//                System.out.println("Response: " + response);
//                return response;
//
//            case "cancelMovieTickets":
//                response = ;
//                System.out.println("Response: " + response);
//                return response;
//
//            case "exchangeTickets":
//                response = ;
//                System.out.println("Response: " + response);
//                return response;
//        }
        return "abcdResponse";
    }

    public static void sendResponseToFE(String response){
        DatagramSocket ds = null;
        try{
            ds = new DatagramSocket(Constants.RM1Port);
            byte[] req = response.getBytes();
//            InetAddress ia = InetAddress.getByName(Constants.FE_IP);
            //uncomment below line to test on own env
            InetAddress ia = InetAddress.getLocalHost();
            DatagramPacket dp = new DatagramPacket(req,req.length,ia,Constants.FEResPort);
            ds.send(dp);
            rmLogger.logger.info("Datagram packet sent to port " + Constants.FEResPort);

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