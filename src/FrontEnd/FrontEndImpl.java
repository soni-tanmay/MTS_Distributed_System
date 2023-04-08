package FrontEnd;
import Utils.Constants;
import Utils.Log;
import Utils.Models.Response;

import javax.jws.WebService;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

@WebService(endpointInterface="FrontEnd.IFrontEnd")
public class FrontEndImpl implements  IFrontEnd{
    Log log;
    public FrontEndImpl(Log log) {
        super();
        this.log=log;
    }
    @Override
    public Response addMovieSlots(String movieID, String movieName, int bookingCapacity) {
        return null;
    }

    @Override
    public Response removeMovieSlots(String movieID, String movieName) {
        return null;
    }

    @Override
    public Response listMovieShowsAvailability(String movieName, boolean isClientCall) {
        System.out.println("Entered listMovieShowsAvailability");
        try {
            String request = "listMovieShowsAvailability_" + movieName + "_" + isClientCall;
            requestFEtoSQ(request);
            return null;
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEndImpl_listMovieShowsAvailability: " + e);
            return null;
        }
    }

    @Override
    public Response bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        return null;
    }

    @Override
    public Response getBookingSchedule(String customerID, boolean isClientCall) {
        return null;
    }

    @Override
    public Response cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        return null;
    }

    @Override
    public Response exchangeTickets(String customerID, String movieID, String old_movieName, String new_movieID, String new_movieName, int numberOfTickets) {
        return null;
    }

    void requestFEtoSQ(String request){
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            byte[] requestData = request.getBytes();
            DatagramPacket requestDp = new DatagramPacket(requestData, requestData.length, InetAddress.getByName(Constants.SQ_IP), Constants.SQPort);
            System.out.println("requestDp: "+requestDp);
            datagramSocket.send(requestDp);

//            byte[] responseData = new byte[1024];
//            DatagramPacket responseDp = new DatagramPacket(responseData, responseData.length);
//            String response = new String(responseDp.getData(), responseDp.getOffset(), responseDp.getLength());
//            datagramSocket.receive(responseDp);
//            System.out.println("Received msg from Sequencer: " + response);

//            datagramSocket.close();
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEndImpl_requestFEtoSQ: " + e);
        }
    }

}
