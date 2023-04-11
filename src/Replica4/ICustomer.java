package Replica4;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style= SOAPBinding.Style.RPC)
public interface ICustomer {
    @WebMethod()
    String addMovieSlots (String movieID, String movieName, int bookingCapacity);
    @WebMethod()
    String removeMovieSlots (String movieID, String movieName);
    @WebMethod()
    String listMovieShowsAvailability (String movieName, boolean isClientCall);
    @WebMethod()
    String bookMovieTickets (String customerID, String movieID, String movieName, int numberOfTickets);
    @WebMethod()
    String getBookingSchedule (String customerID, boolean isClientCall);
    @WebMethod()
    String cancelMovieTickets (String customerID, String movieID, String movieName, int numberOfTickets);
    @WebMethod()
    String exchangeTickets (String customerID, String movieID, String old_movieName, String new_movieID, String new_movieName, int numberOfTickets);
}
