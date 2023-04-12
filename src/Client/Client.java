package Client;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;

import FrontEnd.IFrontEnd;
import Utils.Log;
import Utils.Models.Response;

public class Client {
    private static URL url;
    private static Service serviceAPI;
    static IFrontEnd clientObj;
    private static String getUserType(String userid){
        char userType = userid.charAt(3);
        System.out.println("userType: " + userType);
        if(userType == 'A'){
            return "admin";
        }
        else if(userType == 'C'){
            return "customer";
        }
        else
            return null;
    }
    private static String validateUser(String userid){
        try{
            String res;
            String server = "";
            if(userid.length() == 8){
                server = userid.substring(0,3);
            }
            if(userid.length() != 8){
                res = "Invalid UserID - Incorrect length";
            }
            else if(!(server.equals("VER") || server.equals("ATW") || server.equals("OUT"))){
                res = "Invalid UserID - Location not supported";
            }
            else if(userid.charAt(3) != 'A' && userid.charAt(3) != 'C'){
                res = "Invalid UserID - User type not supported";
            }
            else{
                res = "Valid";
            }
            return res;
        }
        catch(Exception ex){
            ex.printStackTrace();
            return "Exception occurred";
        }

    }
    private static boolean validateCustomer(String customerID){
        if(customerID.charAt(3) == 'C'){
            return true;
        }
        else
            return false;
    }
    private static boolean validateMovieName(String movieName){
        String movie = movieName.toUpperCase();
        if(movie.equals("AVATAR") || movie.equals("AVENGERS") || movie.equals("TITANIC")){
            return true;
        }
        else
            return false;
    }
    private static boolean validateMovieID(String movieID){
        if(movieID.length() != 10){
            return false;
        }
        String movieUpper = movieID.toUpperCase();
        String sub = movieUpper.substring(0,3);
        if(sub.equals("OUT") || sub.equals("ATW") || sub.equals("VER")){
            char c = movieID.charAt(3);
            if(c == 'A' || c == 'M' || c == 'E'){
                //check last 6 chars are number
                String date = movieUpper.substring(4);
                try {
                    Double.parseDouble(date);
                    boolean isMovieInComingWeek = checkMovieIDIsInComingWeek(movieID);
                    return isMovieInComingWeek;
//                    return true;
                } catch(NumberFormatException e){
                    return false;
                }
            }
        }
        return false;
    }
    private static boolean validateSlotAddition(String adminID, String movieID){
        if(adminID!=null && movieID!=null){
//            System.out.println("Entered if1");
            if(adminID.substring(0,3).equals(movieID.substring(0,3))){
//                System.out.println("Entered if2");
                try{
//                    System.out.println("Entered try");
                    String movieDate = movieID.substring(4);

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
                    LocalDate currentDate = LocalDate.now();
                    LocalDate showDate = LocalDate.parse(movieDate, formatter);
                    String formattedDate = currentDate.format(formatter);

                    LocalDate datePlus8 = currentDate.plusDays(8);
                    Integer todaysDate = Integer.parseInt(LocalDate.now().format(formatter));
                    Integer dateAfter8 = Integer.parseInt(datePlus8.format(formatter));
//                    System.out.println("currentDate: " + currentDate);
//                    System.out.println("datePlus8: " + datePlus8);
//                    System.out.println("todaysDate: " + todaysDate);
//                    System.out.println("dateAfter8: " + dateAfter8);
                    if(showDate.isBefore(currentDate)){
//                        System.out.println("showDate.isBefore(currentDate)");
                        return false;
                    }
                    else if(showDate.isBefore(datePlus8)){
//                        System.out.println("showDate.isBefore(datePlus8)");
                        return true;
                    }
                    else{
//                        System.out.println("Entered final else");
                        return false;
                    }
//                    if(movieDate > myObj){
//                        return false;
//                    }
//                    if(movieDate < dateAfter8){
//                        System.out.println("Entered if L121");
//                        return true;
//                    }
//                    else{
//                        System.out.println("Entered else L125");
//                        return false;
//                    }
                }
                catch(Exception ex){
                    System.out.println("Exception");
                    System.out.println(ex);
                    return false;
                }
            }
        }
        return false;
    }
    private static boolean validateSlotRemoval(String adminID, String movieID){
        if(adminID!=null && movieID!=null) {
            if (adminID.substring(0, 3).equals(movieID.substring(0, 3))) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
                LocalDate today = LocalDate.now();

                int todaysDate = Integer.parseInt(today.format(formatter));
                String movieDate = movieID.substring(4);
                LocalDate showDate = LocalDate.parse(movieDate, formatter);
                System.out.println("Show date: " + showDate);
                System.out.println("todaysDate: " + todaysDate);
                if(showDate.isBefore(today)){
                    return false;
                }
                return true;
            }
        }
        return false;
    }
    private static boolean checkMovieIDIsInComingWeek(String movieID){
        String movieDate = movieID.toUpperCase().substring(4);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
        LocalDate showDate = LocalDate.parse(movieDate,formatter);
        LocalDate todayPlus8 = LocalDate.now().plusDays(8);

        if(showDate.isBefore(todayPlus8) && showDate.isAfter(LocalDate.now())){
            return true;
        }
        return false;
    }
    private static String identifyClientServer(String userid){
        String server = userid.substring(0,3).toUpperCase();
        if(server.equals("ATW")){
//            return "AtwInstance";
            return "ATWATER";
        }
        else if(server.equals("OUT")){
            return "OUTREMONT";
        }
        else {
            return "VERDUN";
        }
    }
    private static String getCustomerID(){
        Scanner sc = new Scanner(System.in);
        String customerID;
        while(true){
            customerID = sc.nextLine();
            if(validateUser(customerID).equals("Valid") && validateCustomer(customerID)){
                return customerID.toUpperCase();
            }
            else{
                System.out.println("Incorrect customer ID. Please enter again.");
            }
        }
    }
    private static String getMovieName(){
        Scanner sc = new Scanner(System.in);
        String movieName;
        while(true){
            movieName = sc.nextLine();
            if(validateMovieName(movieName)){
                return movieName.toUpperCase();
            }
            else{
                System.out.println("Incorrect movie name. Please enter again.");
            }
        }
    }
    private static String getMovieID(){
        Scanner sc = new Scanner(System.in);
        String movieid;
        while(true){
            movieid = sc.nextLine();
            if(validateMovieID(movieid)){
                return movieid;
            }
            else{
                System.out.println("Incorrect movie id. Please try again.");
            }
        }
    }
    private static int getNumTickets(){
        Scanner sc = new Scanner(System.in);
        while(true){
            try{
                int tickets = sc.nextInt();
                return tickets;
            }
            catch(Exception ex){
                System.out.println("Invalid number. Please enter again");
            }
        }


    }
    public static void getUrlRef() {
        try {
            url = new URL("http://localhost:8080/"+"frontend"+"?wsdl");
            QName qName = new QName("http://FrontEnd/", "FrontEndImplService");
            serviceAPI = Service.create(url, qName);
            clientObj = serviceAPI.getPort(IFrontEnd.class); //Reference of Interface at which Implementation is running
        } catch (MalformedURLException ex) {
            ex.getStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Log clientLogger = new Log("client.txt");
        clientLogger.logger.setLevel(Level.ALL);
        clientLogger.logger.setUseParentHandlers(false);
        clientLogger.logger.info("webmts.Client Started!");

//        String []newArgs = {"-ORBInitialPort","1050"};
//        ORB orb = ORB.init(newArgs,null);
//        org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
//        NamingContextExt ncref = NamingContextExtHelper.narrow(objRef);

//        webmts.ICustomer customer = (webmts.ICustomer) ICustomerHelper.narrow(ncref.resolve_str("ABC"));

        System.out.println("Welcome to Movie Ticketing System");
        Scanner sc = new Scanner(System.in);
        String userid, isValid, userType, movieName, movieId, custID;
        int action,bookingCapacity;
        while(true){
            System.out.println("Please enter your UserID");
            userid = sc.nextLine();
            Log customerLogger = new Log(userid + ".txt");
            customerLogger.logger.setLevel(Level.ALL);
            customerLogger.logger.setUseParentHandlers(false);
            clientLogger.logger.info("User entered: " + userid);
            isValid = validateUser(userid);
            if(!isValid.equals("Valid")){
                System.out.println("Error! " + isValid);
            }
            else {
//                System.out.println("Valid user!");
                userType = getUserType(userid);
                String server = identifyClientServer(userid);
                Log serverLogger = new Log(server + ".txt");
//                ICustomerApp.webmts.ICustomer client = ICustomerHelper.narrow(ncref.resolve_str(server));
                getUrlRef();
//                webmts.ICustomer client = (webmts.ICustomer) Naming.lookup(server);
//                System.out.println("User Type = " + userType);
//                clientLogger.logger.info("User type: " + userType);
                if(userType != null && userType.equals("admin")){
                    while(true){
                        System.out.println("Enter the function number you want to perform:");
                        System.out.println("1. bookMovieTicketsForCustomer");
                        System.out.println("2. getBookingScheduleForCustomer");
                        System.out.println("3. cancelMovieTicketsForCustomer");
                        System.out.println("4. addMovieSlots");
                        System.out.println("5. removeMovieSlots");
                        System.out.println("6. listMovieShowsAvailability");
                        System.out.println("7. exchangeTicketsForCustomer");
                        System.out.println("0. change user");

                        action = sc.nextInt();
//                        if(action == 9){
//                            String res = client.sampleFunc("Vibhor");
//                            System.out.println(res);
//                            break;
//                        }
                        if(action < 0 || action > 7){
                            System.out.println("Invalid input. Please try again!");
                            clientLogger.logger.info("User entered invalid action number");
                        }
                        else if(action == 0){
                            clientLogger.logger.info("User logged out.");
                            break;
                        }
                        else if(action == 1){
                            System.out.println("You selected: 1 bookMovieTicketsForCustomer");
                            clientLogger.logger.info("Admin selected: 1 bookMovieTicketsForCustomer");
                            serverLogger.logger.info("Admin selected: 1 bookMovieTicketsForCustomer");
                            System.out.println("Please enter customer ID for whom you want to book ticket");
                            String customerId = getCustomerID();
                            System.out.println("Please enter movie name");
                            String movie = getMovieName();
                            System.out.println("Please enter movie ID");
                            String movieID = getMovieID();
                            System.out.println("Please enter number of tickets");
                            int numTickets = getNumTickets();
                            clientLogger.logger.info("Details entered for booking: " + customerId + " " + movie + " " + movieID + " " + numTickets);
                            try{
                                Response res = clientObj.bookMovieTickets(customerId,movieID,movie,numTickets);
                                if(res.StatusCode == 200){
                                    System.out.println("Booking Successful!");
                                    clientLogger.logger.info("Booking was successful");
                                    serverLogger.logger.info("Booking was successful");
                                }
                                else {
                                    System.out.println("Could not book ticket!");
                                    clientLogger.logger.info("Could not book ticket!");
                                    serverLogger.logger.info("Could not book ticket!");
                                }
                            }
                            catch(Exception ex){
                                System.out.println("Error occurred. Exception: " + ex.getMessage());
                                clientLogger.logger.warning("Error occurred. Exception: " + ex);
                                serverLogger.logger.warning("Error occurred. Exception: " + ex);
                            }
                        }
                        else if(action == 2){
                            System.out.println("You selected: 2 getBookingScheduleForCustomer");
                            System.out.println("Please enter customer ID for whom you want to get Booking Schedule");
                            String customerId = getCustomerID();
                            clientLogger.logger.info("Admin selected: 2 getBookingScheduleForCustomer for : " + customerId);
                            serverLogger.logger.info("Admin selected: 2 getBookingScheduleForCustomer for : " + customerId);

                            Response bookingScheduleRes = clientObj.getBookingSchedule(customerId, true);
                            if(bookingScheduleRes.StatusCode == 200) {
                                ArrayList<String> shows = bookingScheduleRes.body;
                                if(bookingScheduleRes.body.isEmpty()){
                                    System.out.println("No bookings for given customer.");
                                    clientLogger.logger.info("No bookings for given customer.");
                                    serverLogger.logger.info("No bookings for given customer.");
                                }
                                else{
                                    System.out.println("Booking schedule for " + customerId + " are: ");
                                    for(String s: shows){
                                        System.out.println(s);
                                    }
                                    clientLogger.logger.info("Bookings returned successfully");
                                    serverLogger.logger.info("Bookings returned successfully");
                                    serverLogger.logger.info("Server Response: ");
                                }
                                System.out.println("Call done!");
                                clientLogger.logger.info("Call done successfully");
                            }
                            
                        }
                        else if(action == 3){
                            System.out.println("You selected: 3 cancelMovieTicketsForCustomer");
                            System.out.println("Please enter customer ID for whom you want to cancel ticket");
                            clientLogger.logger.info("Admin selected: 3 cancelMovieTicketsForCustomer");
                            serverLogger.logger.info("Admin selected: 3 cancelMovieTicketsForCustomer");

                            String customerId = getCustomerID();
                            System.out.println("Please enter movie name");
                            String movie = getMovieName();
                            System.out.println("Please enter movie ID");
                            String movieID = getMovieID();
                            System.out.println("Please enter number of tickets");
                            int numTickets = getNumTickets();
                            clientLogger.logger.info("Params: " + customerId + " " + movie + " " + movieID + " " + numTickets);
                            try{
                                Response res = clientObj.cancelMovieTickets(customerId, movieID, movie, numTickets);
                                clientLogger.logger.info("webmts.Client call made to server");
                                if(res.StatusCode == 200){
                                    System.out.println("Cancellation successful");
                                }
                                else {
                                    System.out.println("Something went wrong.");
                                }
                                clientLogger.logger.info("Response from server: " + res);
                            }
                            catch(Exception ex){
                                System.out.println("Error occurred. Exception: " + ex.getMessage());
                                clientLogger.logger.warning("Error occurred. Exception: " + ex.getMessage());
                            }
                        }
                        else if(action == 4){
                            System.out.println("You selected: 4 addMovieSlots");
                            clientLogger.logger.info("Admin selected: 4 addMovieSlots");
                            serverLogger.logger.info("Admin selected: 4 addMovieSlots");

                            System.out.println("Please enter movie name you want to add slots for");
                            movieName = getMovieName();
                            System.out.println("Please enter movie id");
                            movieId = getMovieID();
                            if(!validateSlotAddition(userid, movieId)){
                                System.out.println("Cannot add slot.");
                                clientLogger.logger.warning("Admin slot booking failed");
                                break;
                            }
                            System.out.println("Please enter booking capacity");
                            bookingCapacity = getNumTickets();
                            Response res = clientObj.addMovieSlots(userid,movieId,movieName,bookingCapacity);
                            clientLogger.logger.info("Request sent to server");
                            clientLogger.logger.info("Params: " + movieName + " " + movieId + " " + bookingCapacity);
                            if(res.StatusCode == 200){
                                System.out.println("Slots added successfully");
                            }
                            else {
                                System.out.println("Something went wrong!");
                            }
                            clientLogger.logger.info("Response received from server: " + res);
                            System.out.println("Call done!");
                        }
                        else if(action == 5){
                            System.out.println("You selected: 5 removeMovieSlots");
                            clientLogger.logger.info("Admin selected: 5 removeMovieSlots");
                            serverLogger.logger.info("Admin selected: 5 removeMovieSlots");

                            System.out.println("Enter movie name you want to remove slot of");
                            movieName = getMovieName();
                            System.out.println("Enter movie ID you want to remove slot of");
                            movieId = getMovieID();
                            if(!validateSlotRemoval(userid, movieId)){
                                System.out.println("Cannot remove slot for given movie ID");
                                clientLogger.logger.warning("Admin slot removal failed");
                                break;
                            }
                            clientLogger.logger.info("Request sent to server with params: " + movieName + " " + movieId);

                            Response res = clientObj.removeMovieSlots(userid,movieId, movieName);
                            if(res.StatusCode == 200){
                                System.out.println("Slots removed successfully");
                            }
                            else {
                                System.out.println("Slot could not be removed");
                            }
                            // else{
                            //     System.out.println("Something went wrong! Please try again later.");
                            // }
                            clientLogger.logger.info("Response received from server: " + res);
                        }
                        else if (action == 6){
                            System.out.println("You selected: 6 listMovieShowsAvailability");
                            clientLogger.logger.info("Admin selected: 6 listMovieShowsAvailability");
                            serverLogger.logger.info("Admin selected: 6 listMovieShowsAvailability");

                            System.out.println("Please enter the movie name");
                            movieName = getMovieName();
                            clientLogger.logger.info("Request sent to server");
                            Response res = clientObj.listMovieShowsAvailability(userid,movieName, true);
                            if(res.StatusCode == 200) {
                                // String[] shows = res.split("~");
                                if(res.body.isEmpty() && res.body == null){
                                    System.out.println("There are no shows for this movie");
                                }
                                else {
                                    System.out.println("Available shows for " + movieName + " are: ");
                                    for(String s: res.body){
                                        System.out.println(s);
                                    }
                                }
                                clientLogger.logger.info("Response received from server successfully.");
                                serverLogger.logger.info("Response received from server successfully.");
                                serverLogger.logger.info("Server Response: " + res.body);

                            } else {
                                System.out.println("Something went wrong!");
                            }
                        }
                        else if(action == 7){
                            System.out.println("You selected: 7 exchangeTicketsForCustomer");
                            System.out.println("Please enter customer ID for whom you want to exchange ticket");
                            clientLogger.logger.info("Admin selected: 7 exchangeTicketsForCustomer");
                            serverLogger.logger.info("Admin selected: 7 exchangeTicketsForCustomer");

                            String customerId = getCustomerID();
                            System.out.println("Please enter previous movie name");
                            String oldMovieName = getMovieName();
                            System.out.println("Please enter movie ID");
                            String oldMovieID = getMovieID();
                            System.out.println("Please enter new movie name");
                            String newMovieName = getMovieName();
                            System.out.println("Please enter new movie ID");
                            String newMovieID = getMovieID();
                            System.out.println("Please enter number of tickets");
                            int numTickets = getNumTickets();
                            clientLogger.logger.info("Params: " + customerId + " " + oldMovieName + " " + oldMovieID + " " + newMovieName + " " + newMovieID + " " + numTickets);
                            try{
                                Response res = clientObj.exchangeTickets(customerId, oldMovieName, oldMovieID, newMovieID, newMovieName, numTickets);
                                clientLogger.logger.info("Response from server: " + res);
                                if(res.StatusCode == 200){
                                    System.out.println("Exchange successful");
                                    clientLogger.logger.info("Exchange successful");
                                }
                                else{
                                    System.out.println("Could not exchange tickets.");
                                    clientLogger.logger.info("Could not exchange tickets.");
                                }
                            }
                            catch(Exception ex){
                                ex.printStackTrace();
                            }
                        }
                    }
                }
                else if(userType != null && userType.equals("customer")){
                    while(true){
                        System.out.println("Enter the function number you want to perform:");
                        System.out.println("1. bookMovieTickets");
                        System.out.println("2. getBookingSchedule");
                        System.out.println("3. cancelMovieTicketsForCustomer");
                        System.out.println("4. exchangeTicketsForCustomer");
                        System.out.println("0. change user");

                        String actionStr = sc.nextLine();
                        action = Integer.parseInt(actionStr);
                        if(action < 0 || action > 4){
                            System.out.println("Invalid input. Please try again!");
                            clientLogger.logger.info("User entered invalid input");
                        }
                        else if(action == 0){
                            clientLogger.logger.info("User logged out");
                            break;
                        }
                        else if(action == 1){
                            try{
                                System.out.println("You selected: 1 bookMovieTickets");
                                clientLogger.logger.info("User selected: 1 bookMovieTicketsForCustomer");
                                serverLogger.logger.info("User selected: 1 bookMovieTicketsForCustomer");

                                String customerId = userid;
                                System.out.println("Please enter movie name");
                                String movie = getMovieName();
                                System.out.println("Please enter movie ID");
                                String movieID = getMovieID();
                                System.out.println("Please enter number of tickets");
                                int numTickets = getNumTickets();
                                clientLogger.logger.info("Details entered for booking: " + customerId + " " + movie + " " + movieID + " " + numTickets);
                                clientLogger.logger.info("Request sent to server");
                                Response res = clientObj.bookMovieTickets(customerId,movieID,movie,numTickets);
                                if(res.StatusCode == 200){
                                    System.out.println("Booking Successful!");
                                }
                                else {
                                    System.out.println("Could not book ticket!");
                                }
                                clientLogger.logger.info("Response from server: " + res);
                            }
                            catch(Exception ex){
                                System.out.println("Error occurred. Exception: " + ex.getMessage());
                                clientLogger.logger.warning("Error occurred. Exception: " + ex);
                            }
                        }
                        else if(action == 2){

                            try{
                                System.out.println("You selected 2. getBookingSchedule");
                                clientLogger.logger.info("User selected: 2. getBookingSchedule");
                                serverLogger.logger.info("User selected: 2. getBookingSchedule");

                                String customerId = userid;
                                clientLogger.logger.info("Request sent to server with params: " + customerId);
                                Response bookingScheduleRes = clientObj.getBookingSchedule(customerId, true);
                                if(bookingScheduleRes.StatusCode == 200) {
                                    if(bookingScheduleRes.body.equals("")){
                                        System.out.println("No bookings for given customer.");
                                        clientLogger.logger.info("No bookings for given customer.");
                                    }
                                    else if(bookingScheduleRes.body == null || bookingScheduleRes.body.equals("")){
                                        System.out.println("No bookings for given customer.");
                                        clientLogger.logger.info("No bookings for given customer.");
                                    }
                                    else{
                                        for(String show: bookingScheduleRes.body){
                                            System.out.println(show.trim());
                                        }
                                        clientLogger.logger.info("Bookings for given customer: " + bookingScheduleRes.body);
                                    }
                                    System.out.println("Call done!");
                                    clientLogger.logger.info("Response received from server successfully.");
                                }

                                
                            }
                            catch(Exception ex){
                                System.out.println("Exception occurred: " + ex);
                                clientLogger.logger.warning("Exception occurred: " + ex);
                            }
                        }
                        else if(action == 3) {
                            try{
                                System.out.println("3. cancelMovieTickets");
                                clientLogger.logger.info("User selected: 3. cancelMovieTickets");
                                serverLogger.logger.info("User selected: 3. cancelMovieTickets");

                                String customerId = userid;
                                System.out.println("Please enter movie name");
                                String movie = getMovieName();
                                System.out.println("Please enter movie ID");
                                String movieID = getMovieID();
                                System.out.println("Please enter number of tickets");
                                int numTickets = getNumTickets();
                                clientLogger.logger.info("Request sent to server with params: " + customerId + " " + movie + " " + movieID + " " + numTickets);
                                Response res = clientObj.cancelMovieTickets(customerId, movieID, movie, numTickets);
                                if(res.StatusCode == 200){
                                    System.out.println("Cancellation successful");
                                }
                                else{
                                    System.out.println("Something went wrong.");
                                }
                                clientLogger.logger.info("Response from server: " + res);
                            }
                            catch(Exception ex){
                                System.out.println("Error occurred. Exception: " + ex.getMessage());
                                clientLogger.logger.warning("Error occurred. Exception: " + ex.getMessage());
                            }
                        }
                        else{
                            System.out.println("You selected: 4 exchangeTicketsForCustomer");
                            clientLogger.logger.info("User selected: 4 exchangeTicketsForCustomer");
                            serverLogger.logger.info("User selected: 4 exchangeTicketsForCustomer");

                            String customerId = userid;
                            System.out.println("Please enter previous movie name");
                            String oldMovieName = getMovieName();
                            System.out.println("Please enter movie ID");
                            String oldMovieID = getMovieID();
                            System.out.println("Please enter new movie name");
                            String newMovieName = getMovieName();
                            System.out.println("Please enter new movie ID");
                            String newMovieID = getMovieID();
                            System.out.println("Please enter number of tickets");
                            int numTickets = getNumTickets();
                            clientLogger.logger.info("Params: " + customerId + " " + oldMovieName + " " + oldMovieID + " " + newMovieName + " " + newMovieID + " " + numTickets);
                            try{
                                Response res = clientObj.exchangeTickets(customerId, oldMovieName, oldMovieID, newMovieID, newMovieName, numTickets);
                                clientLogger.logger.info("Response from server: " + res);
                                if(res.StatusCode == 200){
                                    System.out.println("Exchange successful");
                                }
                                else{
                                    System.out.println("Could not exchange tickets.");
                                }
                            }
                            catch(Exception ex){
                                ex.printStackTrace();
                            }
                        }

                    }
                }
            }
        }
    }
}
