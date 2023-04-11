package FrontEnd;
import Utils.Constants;
import Utils.Log;
import Utils.Models.Response;
import javax.jws.WebService;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;


@WebService(endpointInterface="FrontEnd.IFrontEnd")
public class FrontEndImpl implements  IFrontEnd{
    Log log;
    public FrontEndImpl(Log log) {
        super();
        this.log=log;
    }

    private static long timeout = 5000;
    CountDownLatch latch;
    ArrayList<String> responses = new ArrayList<>();
    @Override
    public Response addMovieSlots(String customerID,String movieID, String movieName, int bookingCapacity) {
        System.out.println("Entered addMovieSlots");
        try {
            String request = "addMovieSlots_" + customerID + "_" + movieID + "_" + movieName+"_"+bookingCapacity;
            responses.clear();
            requestFEtoSQ(request);
            return responseGenerator();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEndImpl_addMovieSlots: " + e);
            return responseGenerator();
        }
    }

    @Override
    public Response removeMovieSlots(String customerID,String movieID, String movieName) {
        System.out.println("Entered removeMovieSlots");
        try {
            String request = "removeMovieSlots_" + customerID + "_" + movieID + "_" + movieName;
            responses.clear();
            requestFEtoSQ(request);
            return responseGenerator();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEndImpl_removeMovieSlots: " + e);
            return responseGenerator();
        }
    }

    @Override
    public Response listMovieShowsAvailability(String customerID,String movieName, boolean isClientCall) {
        System.out.println("Entered listMovieShowsAvailability");
        try {
            String request = "listMovieShowsAvailability_" + customerID + "_" + movieName + "_" + isClientCall;
            responses.clear();
            requestFEtoSQ(request);
            System.out.println("FrontEndImpl_listMovieShowsAvailability: after requestFEtoSQ" );
            return responseGenerator();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEndImpl_listMovieShowsAvailability: " + e);
            return responseGenerator();
        }
    }

    @Override
    public Response bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        System.out.println("Entered bookMovieTickets");
        try {
            String request = "bookMovieTickets_" + customerID + "_" + movieID+"_"+movieName+"_"+numberOfTickets;
            responses.clear();
            requestFEtoSQ(request);
            return responseGenerator();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEndImpl_bookMovieTickets: " + e);
            return responseGenerator();
        }
    }

    @Override
    public Response getBookingSchedule(String customerID, boolean isClientCall) {
        System.out.println("Entered getBookingSchedule");
        try {
            String request = "getBookingSchedule_" + customerID + "_" + isClientCall;
            responses.clear();
            requestFEtoSQ(request);
            return responseGenerator();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEndImpl_getBookingSchedule: " + e);
            return responseGenerator();
        }
    }

    @Override
    public Response cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        System.out.println("Entered cancelMovieTickets");
        try {
            String request = "cancelMovieTickets_" + customerID + "_" + movieID+"_"+movieName+"_"+numberOfTickets;
            responses.clear();
            requestFEtoSQ(request);
            return responseGenerator();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEndImpl_cancelMovieTickets: " + e);
            return responseGenerator();
        }
    }

    @Override
    public Response exchangeTickets(String customerID, String movieID, String old_movieName, String new_movieID, String new_movieName, int numberOfTickets) {
        System.out.println("Entered exchangeTickets");
        try {
            String request = "exchangeTickets_" + customerID + "_" + movieID+"_"+old_movieName+"_"+new_movieID+"_"+new_movieName+"_"+numberOfTickets;
            responses.clear();
            requestFEtoSQ(request);
            return responseGenerator();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEndImpl_exchangeTickets: " + e);
            return responseGenerator();
        }
    }

    void requestFEtoSQ(String request){
        try {
            DatagramSocket datagramSocket = new DatagramSocket(7001);
            byte[] requestData = request.getBytes();
            DatagramPacket requestDp = new DatagramPacket(requestData, requestData.length, InetAddress.getByName(Constants.SQ_IP), Constants.SQPort);
            //uncomment below line to test in own system
            //DatagramPacket requestDp = new DatagramPacket(requestData, requestData.length, InetAddress.getLocalHost(), Constants.SQPort);
            System.out.println("requestDp: "+requestDp);
            datagramSocket.send(requestDp);

            byte[] responseData = new byte[4096];
            DatagramPacket responseDp = new DatagramPacket(responseData, responseData.length);
            datagramSocket.receive(responseDp);
            String response = new String(responseDp.getData(), responseDp.getOffset(), responseDp.getLength());
            System.out.println("Received msg from Sequencer: " + response);
            datagramSocket.close();
            startTimer();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEndImpl_requestFEtoSQ: " + e);
        }
    }

    void sendFailureMsg(String ip, int port, String msg){
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            byte[] requestData = msg.getBytes();
            DatagramPacket requestDp = new DatagramPacket(requestData, requestData.length, InetAddress.getByName(ip), port);
            System.out.println("requestDp: "+requestDp);
            datagramSocket.send(requestDp);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEndImpl_sendFailureMsg: " + e);
        }
    }

    void startTimer(){
        try {
            latch = new CountDownLatch(1);
            latch.await(timeout, TimeUnit.MILLISECONDS);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEndImpl_startTimer: " + e);
        }
    }

    public void responseRM (String response){
        responses.add(response);
    }

    Response responseGenerator(){
        String rm1 = "";
        String rm2 = "";
        String rm3 = "";
        String rm4 = "";

        for (String s : responses){
            if (s.contains("RM1")){
                rm1 = s.split("-")[2];
            } else if (s.contains("RM2")) {
                rm2 = s.split("-")[2];
            }else if (s.contains("RM3")){
                rm3 = s.split("-")[2];
            }else if (s.contains("RM4")){
                rm4 = s.split("-")[2];
            }
        }
        if(responses.size()==4){
            if (matchResponse(rm1,rm2) && matchResponse(rm2,rm3) && matchResponse(rm3,rm4)) {
                // success send response to client
                return new Response(200, new ArrayList<>(Arrays.asList(rm1.split("_")) ));
            }else{
                if (matchResponse(rm1,rm2)){
                    if(matchResponse(rm2,rm3)){
                        // software failure in rm4
                        return new Response(200, new ArrayList<>(Arrays.asList(rm1.split("_")) ));
                    }else if (matchResponse(rm2,rm4)){
                        // software failure in rm3
                        return new Response(200, new ArrayList<>(Arrays.asList(rm1.split("_")) ));
                    }
                }else if(matchResponse(rm2,rm3)){
                    if (matchResponse(rm3,rm1)){
                        // software failure in rm4
                        return new Response(200, new ArrayList<>(Arrays.asList(rm1.split("_")) ));
                    }else if (matchResponse(rm3,rm4)){
                        // software failure in rm1
                        return new Response(200, new ArrayList<>(Arrays.asList(rm3.split("_")) ));
                    }
                }else if(matchResponse(rm3,rm4)){
                    if (matchResponse(rm4,rm1)){
                        // software failure in rm2
                        return new Response(200, new ArrayList<>(Arrays.asList(rm1.split("_")) ));
                    }else if (matchResponse(rm4,rm2)){
                        // software failure in rm1
                        return new Response(200, new ArrayList<>(Arrays.asList(rm3.split("_")) ));
                    }
                }else if (matchResponse(rm4,rm1)){
                    if (matchResponse(rm1,rm2)){
                        // software failure in rm3
                        return new Response(200, new ArrayList<>(Arrays.asList(rm1.split("_")) ));
                    }else if (matchResponse(rm1,rm3)){
                        // software failure in rm2
                        return new Response(200, new ArrayList<>(Arrays.asList(rm1.split("_")) ));
                    }
                }
            }
        } else if (responses.size()==3) {
            if (rm1.equals("")){
                // crash failure in rm1
                return new Response(200, new ArrayList<>(Arrays.asList(rm3.split("_")) ));
            } else if (rm2.equals("")) {
                // crash failure in rm2
                return new Response(200, new ArrayList<>(Arrays.asList(rm1.split("_")) ));
            }else if (rm3.equals("")) {
                // crash failure in rm3
                return new Response(200, new ArrayList<>(Arrays.asList(rm1.split("_")) ));
            }else if (rm4.equals("")) {
                // crash failure in rm4
                return new Response(200, new ArrayList<>(Arrays.asList(rm1.split("_")) ));
            }
        }
        return new Response(400, new ArrayList<>());
    }

    boolean matchResponse(String r1,String r2){
        return r1.equals(r2);
    }
}

