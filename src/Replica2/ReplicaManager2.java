package Replica2;

import Utils.*;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.IOException;
import java.net.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class ReplicaManager2 {
    static Log rmLogger;
    static int seqCounter;
    private static Service serviceAPI;
    static BookingInterface clientObj;
    static boolean isUrlChanged = false;
    static boolean hasCrashOccurred = false;
//    private static String reqMsg;
    public ReplicaManager2() throws IOException {
        rmLogger = new Log("RM2");
    }

    private static ArrayList<String> totalOrderList = new ArrayList<>();
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

    public static String convertArrayToString(ArrayList<String> responseArray) {
        // seqCounter-RM2-Item1 20_Item2 30
        System.out.println("response array - " + responseArray);
        String response = String.join("_", responseArray);
        return response;
    }

    private static void testRMFunctionality() throws NumberFormatException, ParseException, IOException {
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
        rmLogger = new Log("RM2");
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
        listenToFE();
    }

    public static void listenToFE(){
        new Thread(
                () -> {
                    try{
                        getRequestFromFE();
                    }
                    catch(Exception ex){
                        ex.printStackTrace();
                    }
                }
        ).start();
    }

    public static void getRequestFromFE(){ //detecting crash
        DatagramSocket ds = null;
        try {
            while(true){
                System.out.println("Thread started from RM1 to detect crash");
                rmLogger.logger.info("Thread started from RM1 to detect crash");
                ds = new DatagramSocket(Constants.RM2Port);
                rmLogger.logger.info("Datagram socket opened on port: " + Constants.RM1Port);
                byte[] req = new byte[1024];

                DatagramPacket dp = new DatagramPacket(req,req.length);
                ds.receive(dp);
                rmLogger.logger.info("Received datagram packet");
                String reqMsg = new String(dp.getData()).trim();
                System.out.println("in thread reqMsg: " + reqMsg);

                if(reqMsg.equals("CrashFailure")){
                    restartReplica();
                }
                else if(reqMsg.equals("SoftwareFailure")){
                    switchReplica();
                    ds.close();
                }
                ds.close();

                /*
                * InetAddress ina = InetAddress.getLocalHost();
                DatagramPacket dpresp = new DatagramPacket(response,response.length,ina,dp.getPort());

                ds.send(dpresp);
                rmLogger.logger.info("Datagram packet response sent.");
                System.out.println("Thread response sent from " + serverInstance);
                ds.close();
                rmLogger.logger.info("Datagram Socket closed.");
                * */
            }
        }
        catch(Exception ex) {
            System.out.println("Exception occurred!!!123");
            ex.printStackTrace();
            rmLogger.logger.warning("Exception occurred: " + ex);
            System.out.println(ex);
        }
    }

    public static String getNewPortNum(String server){
        String portNum = "";
        if(server.equals("ATWATER")){
            portNum = "8093";
        }
        else if(server.equals("OUTREMONT")){
            portNum = "8091";
        }
        else{
            portNum = "8092";
        }
        return portNum;
    }

    public static void switchReplica() throws MalformedURLException {
        try{
            System.out.println("Switching replica");
            isUrlChanged = true;
            runPreviousRequests();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static void restartReplica() throws Exception {
        try{
            System.out.println("Restarting replica due to fault");
            String []args = {"",""};
            AtwaterServer.main(args);
            OutrementServer.main(args);
            VerdunServer.main(args);

            hasCrashOccurred = true;

            runPreviousRequests();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }

    }

    public static void runPreviousRequests() throws NumberFormatException, IOException, ParseException {
        System.out.println("Running Previous Requests");
        System.out.println(totalOrderList);
        for(String request: totalOrderList){
            String res = processRequest(request);
            System.out.println("Response for " + request + ":" + res);
            seqCounter = Integer.parseInt(request.substring(0,1));
        }
    }

    public static void getRequestFromSequencer(){
        MulticastSocket ms = null;
        try {

            // Thread.sleep(6000);
            ms = new MulticastSocket(Constants.multicastSocket);
            //comment below 3 lines to test on own system
           NetworkInterface networkInterface = NetworkInterface.getByName("en0");
           Enumeration<NetworkInterface> list = NetworkInterface.getNetworkInterfaces();
           ms.setNetworkInterface(networkInterface);

            InetAddress group = InetAddress.getByName(Constants.NetworkIP);
            ms.joinGroup(group);
            while(true){
                try{
                    System.out.println("Thread started from RM2");
                    byte[] req = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(req, req.length);
                    ms.receive(dp);
                    rmLogger.logger.info("Received datagram packet");
                    String reqMsg = (new String(dp.getData())).trim();
                    System.out.println("in thread reqMsg: " + reqMsg);
                    String response = processRequest(reqMsg);
                    CountDownLatch latch = new CountDownLatch(1);
                    boolean timeoutReached = latch.await(6000, TimeUnit.MILLISECONDS);
                    sendResponseToFE(response);
                }
                catch(Exception ex){
                    System.out.println("Exception occurred");
                    ex.printStackTrace();
                    rmLogger.logger.warning("Exception occurred: " + ex);
                }

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

    public static String processRequest(String reqMsg) throws NumberFormatException, IOException, ParseException {
        System.out.println("Entered process Request");
        if(reqMsg.isEmpty()) {
            return seqCounter + "-RM2-Failure";
        }
        String[] request = reqMsg.split("_");
        //seqCounter
        int seqID = Integer.parseInt(request[0].trim());
//        if(seqID - seqCounter != 1){
//            ////ToDo ask other RM's if incorrect
//        }
//        else{
            System.out.println("Split Request: ");
            for(String s: request){
                System.out.println(s);
            }
            String replicaResponse = sendRequestToReplica(reqMsg);
            return seqID + "-RM2-" + replicaResponse;
//        }
    }

    public static String sendRequestToReplica(String reqMsg) throws ParseException, NumberFormatException, IOException {
        System.out.println("Entered sendRequestToReplica");
        URL url;
        QName qName;
        String[] params = reqMsg.split("_");
        String server = identifyClientServer(params[2].trim().toString());
        String portNum = getPortNum(server);

        url = new URL("http://localhost:" + portNum +"/" + server +"?wsdl");
        qName = new QName("http://Replica2/", "BookingImplementationService");
        Service service = Service.create(url,qName);
        clientObj = service.getPort(BookingInterface.class);

        String response;
        switch (params[1].trim()){
            case "addMovieSlots":
                if(!hasCrashOccurred){
                    totalOrderList.add(reqMsg);
                }
                //1_addMovieSlots_customerID_movieID_movieName_bookingCapacity
                response = clientObj.addMovieSlots(params[3].trim(),params[4].trim(),Integer.parseInt(params[5].trim()));
                System.out.println("Response: " + response);
                return response;

            case "removeMovieSlots":
                if(!hasCrashOccurred){
                    totalOrderList.add(reqMsg);
                }
                //2_removeMovieSlots_customerID_movieID_movieName
                response = clientObj.removeMovieSlots(params[3].trim(),params[4].trim(), true);
                System.out.println("Response: " + response);
                return response;

            case "listMovieShowsAvailability":
                //3_listMovieShowsAvailability_customerID_movieName_isClientCall 
                
                ArrayList<String> myList = new ArrayList<String>(Arrays.asList(clientObj.listMovieShowsAvailability(params[3].trim(),Boolean.parseBoolean(params[4].trim()))));
                Collections.sort(myList);
                response = convertArrayToString(myList);
                System.out.println("Response: " + response);
                return response;

            case "bookMovieTickets":
                if(!hasCrashOccurred){
                    totalOrderList.add(reqMsg);
                }
                //4_bookMovieTickets_customerID_movieID_movieName_numberOfTickets
                response = clientObj.bookMovieTickets(params[2].trim(),params[3].trim(),params[4].trim(),Integer.parseInt(params[5].trim()), true);
                System.out.println("Response: " + response);
                return response;

            case "getBookingSchedule":
                //5_getBookingSchedule_customerID_isClientCall
                ArrayList<String> bookingList = new ArrayList<String>(Arrays.asList(clientObj.getBookingSchedule(params[2].trim(),Boolean.parseBoolean(params[3].trim()))));
                Collections.sort(bookingList);
                response = convertArrayToString(bookingList);
                return response;

            case "cancelMovieTickets":
                if(!hasCrashOccurred){
                    totalOrderList.add(reqMsg);
                }
                //6_cancelMovieTickets_customerID_movieID_movieName_numberOfTickets
                response = clientObj.cancelMovieTickets(params[2].trim(),params[3].trim(),params[4].trim(),Integer.parseInt(params[5].trim()), true);
                System.out.println("Response: " + response);
                return response;

            case "exchangeTickets":
                if(!hasCrashOccurred){
                    totalOrderList.add(reqMsg);
                }
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
            // ds = new DatagramSocket(Constants.RM2Port);
            ds = new DatagramSocket();
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
            ex.printStackTrace();
            System.out.println("Exception occurred!");
            rmLogger.logger.warning("Exception occurred: " + ex);
        }
    }
}
