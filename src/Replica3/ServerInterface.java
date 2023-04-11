package Replica3;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService
@SOAPBinding(style=Style.RPC)
public interface ServerInterface {
    @WebMethod
    String addMovieSlots (String movieID, String movieName, int bookingCapacity);
    @WebMethod
    String removeMovieSlots (String movieID, String movieName, boolean isClientCall);
    @WebMethod
    String listMovieShowsAvailability (String movieName, boolean isClientCall);
    @WebMethod
    String bookMovieTickets (String customerID, String movieID, String movieName, int numberOfTickets);
    @WebMethod
    String getBookingSchedule (String customerID, boolean isClientCall);
    @WebMethod
    String cancelMovieTickets (String customerID, String movieID, String movieName, int numberOfTickets);
    @WebMethod
    String exchangeTickets (String customerID, String movieID, String old_movieName, String new_movieID, String new_movieName, int numberOfTickets);
}
