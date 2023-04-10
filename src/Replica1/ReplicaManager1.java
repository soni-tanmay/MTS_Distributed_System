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
    private static String getPortNum(String server){
        String portNum = "";
        if(server.equals("ATWATER")){
            portNum = "8083";
        }
        else if(server.equals("OUTREMONT")){
            portNum = "8081";
        }
        else{
            portNum = "8082";
        }
        return portNum;
    }
    private static void testRMFunctionality() throws MalformedURLException {
        //1_addMovieSlots_ATWC5555_ATWM170423_TITANIC_10
        //2_removeMovieSlots_ATWC5555_ATWM140423_AVATAR
        //3_listMovieShowsAvailability_ATWC5555_TITANIC_true
        //4_bookMovieTickets_ATWC5555_ATWM140423_AVATAR_8
        //5_getBookingSchedule_ATWC5555_true
        //6_cancelMovieTickets_ATWC5555_ATWM140423_AVATAR_5
        //7_exchangeTickets_ATWC5555_ATWM140423_old_movieName_new_movieID_new_movieName_numberOfTickets
        String reqMsg = "1_addMovieSlots_ATWC5555_ATWM140423_AVATAR_5";
        String response = processRequest(reqMsg);
        System.out.println("Test complete for " + reqMsg);
        System.out.println(response);
//        sendResponseToFE(response);
    }

    public static void main(String[] args) throws IOException {
        rmLogger = new Log("RM1");
//        testRMFunctionality();
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
//            NetworkInterface networkInterface = NetworkInterface.getByName("eth0");
//            Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces();
//            ms.setNetworkInterface(networkInterface);

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
        System.out.println("Entered process Request");
        if(reqMsg.isEmpty()) {
            return seqCounter + "-RM1-Failure";
        }
        String[] request = reqMsg.split("_");
        seqCounter = Integer.parseInt(request[0].trim());
        //ToDo verify if correct
        System.out.println("Split Request: ");
        for(String s: request){
            System.out.println(s);
        }
        String replicaResponse = sendRequestToReplica(reqMsg); //ToDo send request to implementation
        return seqCounter + "-RM1-" + replicaResponse;
    }

    public static String sendRequestToReplica(String reqMsg) throws MalformedURLException {
        System.out.println("Entered sendRequestToReplica");
        URL url;
        QName qName;
        String[] params = reqMsg.split("_");
        String server = identifyClientServer(params[2].trim().toString());
        String portNum = getPortNum(server);

        url = new URL("http://localhost:" + portNum +"/" + server +"?wsdl");
        qName = new QName("http://Replica1/", "CustomerImplService");
        Service service = Service.create(url,qName);
        clientObj = service.getPort(ICustomer.class);

        String response;
        switch (params[1].trim()){
            case "addMovieSlots":
                //1_addMovieSlots_customerID_movieID_movieName_bookingCapacity
                response = clientObj.addMovieSlots(params[3].trim(),params[4].trim(),Integer.parseInt(params[5].trim()));
                System.out.println("Response: " + response);
                return response;

            case "removeMovieSlots":
                //2_removeMovieSlots_customerID_movieID_movieName
                response = clientObj.removeMovieSlots(params[3].trim(),params[4].trim());
                System.out.println("Response: " + response);
                return response;

            case "listMovieShowsAvailability":
                //3_listMovieShowsAvailability_customerID_movieName_isClientCall
                response = clientObj.listMovieShowsAvailability(params[3].trim(),Boolean.parseBoolean(params[4].trim()));
                System.out.println("Response: " + response);
                return response;

            case "bookMovieTickets":
                //4_bookMovieTickets_customerID_movieID_movieName_numberOfTickets
                response = clientObj.bookMovieTickets(params[2].trim(),params[3].trim(),params[4].trim(),Integer.parseInt(params[5].trim()));
                System.out.println("Response: " + response);
                return response;

            case "getBookingSchedule":
                //5_getBookingSchedule_customerID_isClientCall
                response = clientObj.getBookingSchedule(params[2].trim(),Boolean.parseBoolean(params[3].trim()));
                System.out.println("Response: " + response);
                return response;

            case "cancelMovieTickets":
                //6_cancelMovieTickets_customerID_movieID_movieName_numberOfTickets
                response = clientObj.cancelMovieTickets(params[2].trim(),params[3].trim(),params[4].trim(),Integer.parseInt(params[5].trim()));
                System.out.println("Response: " + response);
                return response;

            case "exchangeTickets":
                //7_exchangeTickets_customerID_movieID_old_movieName_new_movieID_new_movieName_numberOfTickets
                response = clientObj.exchangeTickets(params[2].trim(),params[3].trim(),params[4].trim(),params[5].trim(),params[6].trim(),Integer.parseInt(params[7].trim()));
                System.out.println("Response: " + response);
                return response;

        }
        return null;
    }

    public static void sendResponseToFE(String response){
        DatagramSocket ds = null;
        try{
            ds = new DatagramSocket(Constants.RM1Port);
            byte[] req = response.getBytes();
            InetAddress ia = InetAddress.getByName(Constants.FE_IP);
            //uncomment below line to test on own env
//            InetAddress ia = InetAddress.getLocalHost();
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