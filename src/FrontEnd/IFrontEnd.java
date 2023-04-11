package FrontEnd;
import Utils.Models.Response;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style= SOAPBinding.Style.RPC)
public interface IFrontEnd {
    @WebMethod()
    Response addMovieSlots (String customerID, String movieID, String movieName, int bookingCapacity);
    @WebMethod()
    Response removeMovieSlots (String customerID, String movieID, String movieName);
    @WebMethod()
    Response listMovieShowsAvailability (String customerID, String movieName, boolean isClientCall);
    @WebMethod()
    Response bookMovieTickets (String customerID, String movieID, String movieName, int numberOfTickets);
    @WebMethod()
    Response getBookingSchedule (String customerID, boolean isClientCall);
    @WebMethod()
    Response cancelMovieTickets (String customerID, String movieID, String movieName, int numberOfTickets);
    @WebMethod()
    Response exchangeTickets (String customerID, String movieID, String old_movieName, String new_movieID, String new_movieName, int numberOfTickets);

}
