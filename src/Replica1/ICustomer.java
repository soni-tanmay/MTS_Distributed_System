package Replica1;

import Utils.Models.Response;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style= SOAPBinding.Style.RPC)
public interface ICustomer {
    @WebMethod()
    Response addMovieSlots (String movieID, String movieName, int bookingCapacity);
    @WebMethod()
    Response removeMovieSlots (String movieID, String movieName);
    @WebMethod()
    Response listMovieShowsAvailability (String movieName, boolean isClientCall);
    @WebMethod()
    Response bookMovieTickets (String customerID, String movieID, String movieName, int numberOfTickets);
    @WebMethod()
    Response getBookingSchedule (String customerID, boolean isClientCall);
    @WebMethod()
    Response cancelMovieTickets (String customerID, String movieID, String movieName, int numberOfTickets);
    @WebMethod()
    Response exchangeTickets (String customerID, String movieID, String old_movieName, String new_movieID, String new_movieName, int numberOfTickets);
}
