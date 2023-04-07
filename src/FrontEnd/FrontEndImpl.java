package FrontEnd;
import Utils.Log;
import Utils.Models.Response;

import javax.jws.WebService;

@WebService(endpointInterface="Frontend.IFrontEnd")
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
    public Response removeMovieSlots(String movieID, String movieName, boolean isClientCall) {
        return null;
    }

    @Override
    public Response listMovieShowsAvailability(String movieName, boolean isClientCall) {
        return null;
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
}
