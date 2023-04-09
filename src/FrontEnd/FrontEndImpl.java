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
        System.out.println("Entered addMovieSlots");
        try {
            String request = "addMovieSlots_" + movieID + "_" + movieName+"_"+bookingCapacity;
            requestFEtoSQ(request);
            return null;
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEndImpl_addMovieSlots: " + e);
            return null;
        }
    }

    @Override
    public Response removeMovieSlots(String movieID, String movieName) {
        System.out.println("Entered removeMovieSlots");
        try {
            String request = "removeMovieSlots_" + movieID + "_" + movieName;
            requestFEtoSQ(request);
            return null;
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEndImpl_removeMovieSlots: " + e);
            return null;
        }
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
        System.out.println("Entered bookMovieTickets");
        try {
            String request = "bookMovieTickets_" + customerID + "_" + movieID+"_"+movieName+"_"+numberOfTickets;
            requestFEtoSQ(request);
            return null;
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEndImpl_bookMovieTickets: " + e);
            return null;
        }
    }

    @Override
    public Response getBookingSchedule(String customerID, boolean isClientCall) {
        System.out.println("Entered getBookingSchedule");
        try {
            String request = "getBookingSchedule_" + customerID + "_" + isClientCall;
            requestFEtoSQ(request);
            return null;
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEndImpl_getBookingSchedule: " + e);
            return null;
        }
    }

    @Override
    public Response cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        System.out.println("Entered cancelMovieTickets");
        try {
            String request = "cancelMovieTickets_" + customerID + "_" + movieID+"_"+movieName+"_"+numberOfTickets;
            requestFEtoSQ(request);
            return null;
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEndImpl_cancelMovieTickets: " + e);
            return null;
        }
    }

    @Override
    public Response exchangeTickets(String customerID, String movieID, String old_movieName, String new_movieID, String new_movieName, int numberOfTickets) {
        System.out.println("Entered exchangeTickets");
        try {
            String request = "exchangeTickets_" + customerID + "_" + movieID+"_"+old_movieName+"_"+new_movieID+"_"+new_movieName+"_"+numberOfTickets;
            requestFEtoSQ(request);
            return null;
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEndImpl_exchangeTickets: " + e);
            return null;
        }
    }

    void requestFEtoSQ(String request){
        try {
            DatagramSocket datagramSocket = new DatagramSocket(7001);
            byte[] requestData = request.getBytes();
            DatagramPacket requestDp = new DatagramPacket(requestData, requestData.length, InetAddress.getByName(Constants.SQ_IP), Constants.SQPort);
            System.out.println("requestDp: "+requestDp);
            datagramSocket.send(requestDp);

            byte[] responseData = new byte[4096];
            DatagramPacket responseDp = new DatagramPacket(responseData, responseData.length);
            datagramSocket.receive(responseDp);
            String response = new String(responseDp.getData(), responseDp.getOffset(), responseDp.getLength());
            System.out.println("Received msg from Sequencer: " + response);
            datagramSocket.close();

        }catch (Exception e){
            e.printStackTrace();
            System.out.println("FrontEndImpl_requestFEtoSQ: " + e);
        }
    }

}
