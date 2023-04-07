package FrontEnd;

public interface IFrontEnd {
    String addMovieSlots (String movieID, String movieName, int bookingCapacity);
    String removeMovieSlots (String movieID, String movieName, boolean isClientCall);
    String listMovieShowsAvailability (String movieName, boolean isClientCall);
    String bookMovieTickets (String customerID, String movieID, String movieName, int numberOfTickets);
    String getBookingSchedule (String customerID, boolean isClientCall);
    String cancelMovieTickets (String customerID, String movieID, String movieName, int numberOfTickets);
    String exchangeTickets (String customerID, String movieID, String old_movieName, String new_movieID, String new_movieName, int numberOfTickets);

}
