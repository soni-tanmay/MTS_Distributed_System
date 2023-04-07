package FrontEnd;

public class FrontEndImpl implements  IFrontEnd{
    @Override
    public String addMovieSlots(String movieID, String movieName, int bookingCapacity) {
        return null;
    }

    @Override
    public String removeMovieSlots(String movieID, String movieName, boolean isClientCall) {
        return null;
    }

    @Override
    public String listMovieShowsAvailability(String movieName, boolean isClientCall) {
        return null;
    }

    @Override
    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        return null;
    }

    @Override
    public String getBookingSchedule(String customerID, boolean isClientCall) {
        return null;
    }

    @Override
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        return null;
    }

    @Override
    public String exchangeTickets(String customerID, String movieID, String old_movieName, String new_movieID, String new_movieName, int numberOfTickets) {
        return null;
    }
}
