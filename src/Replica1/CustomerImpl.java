package Replica1;

import javax.jws.WebService;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import Utils.*;
import Utils.Models.Response;

class sortAvailableShowListComparator implements Comparator {
    @Override
    public int compare(Object show1, Object show2) {
        Date showDate1, showDate2;
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyy");

        try {
            String date1Sub = show1.toString().substring(4);
            String date2Sub = show2.toString().substring(4);
            showDate1 = dateFormat.parse(date1Sub);
            showDate2 = dateFormat.parse(date2Sub);
            if(showDate1.compareTo(showDate2) == 0){
                List<Character> showTimings = new ArrayList<>(Arrays.asList('M','A','E'));
                char[] time = {'M', 'A', 'E'};
                char day1showTime = show1.toString().charAt(3);
                char day2showTime = show2.toString().charAt(3);
                if(showTimings.indexOf(day1showTime) < showTimings.indexOf(day2showTime)) {
//                    System.out.println("return -1");
                    return -1;
                }
                else {
//                    System.out.println("return 1");
                    return 1;
                }
            }
            else if(showDate1.compareTo(showDate2) < 0) {
                return -1;
            }
            else if(showDate1.compareTo(showDate2) > 0) {
                return 1;
            }
        } catch ( ParseException ex) {
            System.out.println("Exception occurred: " + ex);
        }
        return 0;
    }
}

@WebService(endpointInterface = "Replica1.ICustomer")
public class CustomerImpl implements ICustomer {

    public ConcurrentHashMap<String, ConcurrentHashMap<String, MovieData>> movies = new ConcurrentHashMap<String, ConcurrentHashMap<String, MovieData>>();
    public ConcurrentHashMap<String, ArrayList<CustomerData>> customerBookings = new ConcurrentHashMap<String, ArrayList<CustomerData>>();
    public String serverName;
    Log logger;
    int atwVerPortNum = 4200;
    int atwOutPortNum = 4201;
    int verAtwPortNum = 4204;
    int verOutPortNum = 4205;
    int outVerPortNum = 4208;
    int outAtwPortNum = 4209;

    public CustomerImpl(String server, Log logger) throws Exception {
        super();
        this.serverName = server.toUpperCase();
        this.logger = logger;
    }

    public byte[] getMyData(String req) throws Exception {
        logger.logger.info("UDP call received");
        System.out.println("Entered getMyData for server: " + serverName);
        System.out.println("req = " + req);
        String[] args = req.split("_");
        byte[] response = new byte[1024];
        switch (args[0]){
            case "removeMovieSlots":
                break;

            case "listMovieShowsAvailability":
//                ArrayList<String> respList = new ArrayList<>();
                String dataList = listMovieShowsAvailability(args[1], false);
                System.out.println("dataList in getMyData: " + dataList);
                response = dataList.getBytes();
                return response;

            case "bookMovieTickets":
                String res = bookMovieTickets(args[1],args[2],args[3],Integer.parseInt(args[4]));
                if(res.equals("Success")){
                    System.out.println("Ticket Booking Successful!");
                }
                else if(res.equals("Failure")){
                    System.out.println("Something went wrong!");
                }
                response = res.getBytes();
                return response;

            case "getBookingSchedule":
                String bookingSchedule = getBookingSchedule(args[1], false);
                System.out.println("bookingSchedule in getMyData: " + bookingSchedule);
                response = bookingSchedule.getBytes();
                return response;


            case "cancelMovieTickets":
                String result = cancelMovieTickets(args[1],args[2],args[3],Integer.parseInt(args[4]));
                if(result.equals("Success")){
                    System.out.println("Ticket Cancellation Successful!");
                }
                else if(result.equals("Failure")){
                    System.out.println("Something went wrong!");
                }
//                return result + "";
                response = result.getBytes();
                return response;
        }
        return response;
//        return "EndOfFunction-GetMyData";
    }

    public int populateCustomerBookings(String customerID, String movieID, String movieName, int numberOfTickets){
//        logger.logger.info("Called populateCustomerBookings with details: " + customerID + " " + movieID + " " + movieName + " " + numberOfTickets);
        System.out.println("Entered populateCustomerBookings with details: " + customerID + " " + movieID + " " + movieName + " " + numberOfTickets);
        boolean isSimilarShowBooked = false;
        if(customerBookings == null){
            System.out.println("Entered basic if");
            System.out.println("isSimilarShowBooked: " + isSimilarShowBooked);
            customerBookings = new ConcurrentHashMap<>();
            isSimilarShowBooked = false;
        }
        if(customerBookings.get(customerID) == null){
            isSimilarShowBooked = false;
            System.out.println("Entered 2nd if");
            System.out.println("isSimilarShowBooked: " + isSimilarShowBooked);

            //enter in movies HashMap
//            movies.get(movieName).get(movieID).capacity -= numberOfTickets;
//            movies.get(movieName).get(movieID).customers.add(customerID);
            System.out.println("Capacity: " + movies.get(movieName).get(movieID).capacity);
            System.out.println("Customers: " + movies.get(movieName).get(movieID).customers);

            //enter in customerBookings Hashmap
            customerBookings.put(customerID, new ArrayList<>());
            customerBookings.get(customerID).add(new CustomerData(movieName, movieID, numberOfTickets));
            System.out.println("CustomerBookings: " + customerBookings.get(customerID));
        }
        else if(customerBookings.get(customerID) != null){
            System.out.println("Entered else if");
            System.out.println("isSimilarShowBooked: " + isSimilarShowBooked);
            ArrayList<CustomerData> customerShows = customerBookings.get(customerID);
            for (int i = 0 ; i < customerShows.size() && !isSimilarShowBooked; i++) {
                CustomerData show = customerShows.get(i);
                System.out.println(show.movieID + " :: " + show.movieName + " :: " + show.tickets);
                if(show.movieID.equals(movieID) && show.movieName.equals(movieName)){
                    System.out.println("----");
                    show.tickets += numberOfTickets;
                    isSimilarShowBooked = true;
//                    isSimilarShowBooked = false;
                    break;
                }
            }
            if(!isSimilarShowBooked){
                System.out.println("Entered last if");
                System.out.println("isSimilarShowBooked: " + isSimilarShowBooked);

                //enter in movies HashMap
//                movies.get(movieName).get(movieID).capacity -= numberOfTickets;
//                movies.get(movieName).get(movieID).customers.add(customerID);

                boolean res =  customerBookings.get(customerID).add(new CustomerData(movieName, movieID, numberOfTickets));
                System.out.println("res: " + res);
                if(!res){
                    return -1;
                }
            }
        }
        return 0;
    }

    public ArrayList<Integer> getPortList(){
        ArrayList<Integer> portList = new ArrayList<>();
        switch (serverName) {
            case "ATWATER":
                portList.add(atwVerPortNum);
                portList.add(atwOutPortNum);
                break;
            case "VERDUN":
                portList.add(verOutPortNum);
                portList.add(verAtwPortNum);
                break;
            case "OUTREMONT":
                portList.add(outAtwPortNum);
                portList.add(outVerPortNum);
                break;
        }
        return portList;
    }

    public int makeUDPCallForCancellingTicket(String customerID, String movieID, String movieName, int numberOfTickets, int portNum){
        logger.logger.info("Entered function call of makeUDPCallForCancellingTickets");
        DatagramSocket ds = null;
        try{
            ds = new DatagramSocket();
            String msg = "cancelMovieTickets_" + customerID + "_" + movieID + "_" + movieName + "_" + numberOfTickets;
            byte[] req = msg.getBytes();
            InetAddress ia = InetAddress.getLocalHost();
            DatagramPacket dp = new DatagramPacket(req,req.length,ia,portNum);
            ds.send(dp);
            logger.logger.info("Datagram packet sent to port " + portNum);

            byte[] response = new byte[1024];
            DatagramPacket dpreply = new DatagramPacket(response,response.length);

            //receive data
            ds.receive(dpreply);
            logger.logger.info("Response received for sent packet");
            byte[] res = dpreply.getData();
            logger.logger.info("Function call completed successfully");
            return Integer.parseInt(new String(res).trim());
        }
        catch(Exception ex){
            System.out.println("Exception occurred!");
            logger.logger.warning("Exception occurred: " + ex);
            return -1;
        }
    }

    public int makeUDPCallForBookingTicket(String customerID, String movieID, String movieName, int numberOfTickets, int portNum){
        logger.logger.info("Entered function makeUDPCallForBookingTicket");
        DatagramSocket ds = null;
        try{
            ds = new DatagramSocket();
            String msg = "bookMovieTickets_" + customerID + "_" + movieID + "_" + movieName + "_" + numberOfTickets;
            byte[] req = msg.getBytes();
            InetAddress ia = InetAddress.getLocalHost();
            DatagramPacket dp = new DatagramPacket(req,req.length,ia,portNum);
            ds.send(dp);
            logger.logger.info("Datagram packet sent to port " + portNum);

            byte[] response = new byte[1024];
            DatagramPacket dpreply = new DatagramPacket(response,response.length);

            //receive data
            ds.receive(dpreply);
            logger.logger.info("response received of sent packet");
            byte[] res = dpreply.getData();
            logger.logger.info("Function call completed successfully");
            return Integer.parseInt(new String(res).trim());
        }
        catch(Exception ex){
            System.out.println("Exception occurred!");
            logger.logger.warning("Exception occurred: " + ex);
            return -1;
        }
    }

    public String isCancelled(String customerID, String movieID, String movieName, int numOfTickets){
        try{
            String res = cancelMovieTickets(customerID, movieID, movieName, numOfTickets);
            return res;
        }
        catch(Exception ex){
            ex.printStackTrace();
            return "Failure";
        }
    }

    public String isBooked(String customerID, String movieID, String movieName, int numOfTickets){
        try{
            String res = bookMovieTickets(customerID,movieID,movieName,numOfTickets);
            return res;
        }
        catch(Exception ex){
            ex.printStackTrace();
            return "Failure";
        }
    }

    public Response convertIntToResponse(int result){
        if(result == 0){
            return new Response(200, null);
        }
        else{
            return new Response(400, null);
        }
    }

    public Response convertStringToResponse(String result){
        return new Response(200,null);
    }
    public String convertIntToString(int result){
        if (result == 0){
            return "Success";
        }
        else {
            return "Failure";
        }
    }
    public int convertStringToInt(String result){
        if(result.equals("Success")){
            return 0;
        }
        return -1;
    }

    @Override
    public String addMovieSlots(String movieId, String movieName, int bookingCapacity) {

        try{
            logger.logger.info("Called addMovieSlots with details: " + movieId + " " + movieName + " " + bookingCapacity);
            System.out.println("Entered addMovieSlots()");
            if(movies.get(movieName)==null){
                movies.put(movieName,new ConcurrentHashMap<String, MovieData>(){
                    {
                        put(movieId, new MovieData(bookingCapacity));
                    }
                });
                logger.logger.info("New Slot Added!");
            }
            else if(movies.get(movieName).get(movieId)==null){
                movies.get(movieName).put(movieId,new MovieData(bookingCapacity));
                logger.logger.info("New Slot Added!");
            }
            else{
                movies.get(movieName).get(movieId).capacity += bookingCapacity;
                logger.logger.info("Slot capacity increased!");
            }
//            movies.get(movieName).put(movieId,new webmts.MovieData(bookingCapacity));
            System.out.println(movies);
            System.out.println(movies.get(movieName));
            System.out.println(movies.get(movieName).get(movieId).capacity);

            for (Map.Entry<String, ConcurrentHashMap<String, MovieData>> shows : movies.entrySet()) {
                System.out.println("Movie name: " + shows.getKey());
                Map<String, MovieData> addMap = shows.getValue();

                // Iterate InnerMap
                for (Map.Entry<String, MovieData> show : addMap.entrySet()) {
                    System.out.println(show.getKey() + " :: " + show.getValue().capacity);
                }
            }
            logger.logger.info("Function call completed successfully");
            return "Success";
        }
        catch(Exception ex){
            System.out.println("Exception thrown" + ex.getMessage());
            logger.logger.warning("Exception occurred: " + ex);
            return "Failure";
        }
    }

    @Override
    public String removeMovieSlots(String movieID, String movieName) {

        logger.logger.info("Called function removeMovieSlots with details: " + movieID + " " + movieName);
        if(movies == null || movies.get(movieName) == null || movies.get(movieName).get(movieID) == null) {
            logger.logger.info("Could not remove slots. Bad request.");
            return "Failure";
//            return -1;
        }
        ArrayList<String> customers = movies.get(movieName).get(movieID).customers;
        ConcurrentHashMap<String, Integer> bookings = new ConcurrentHashMap<>();

        //create hashmap which has customerID and ticketsBooked
        for(String c : customers){
            ArrayList<CustomerData> shows = customerBookings.get(c);
            for(CustomerData show: customerBookings.get(c)){
//                System.out.println(show.movieName + " " + show.movieID + " " + show.tickets);
                if(show.movieName.equals(movieName) && show.movieID.equals(movieID)){
                    //insert ticket details in hashmap
                    bookings.put(c, show.tickets);

                    //remove show from customerBookings
                    customerBookings.get(c).remove(show);
                    System.out.println("Show found for customer");
                    break;
                }
            }
        }
        System.out.println("Bookings: " + bookings);
        //hashmap populated

        //get movie details which is to be removed
        int capOfMovieRemoved = movies.get(movieName).get(movieID).capacity;
        String movieToRemove = movieID + " " + capOfMovieRemoved;
        //remove movie slot
        movies.get(movieName).remove(movieID);
        System.out.println("Movie slot removed");
        logger.logger.info("Movie slot removed");

        //get available movie slots in current server
        String shows = listMovieShowsAvailability(movieName, false);
        String[] availableShows = shows.split("_");
        ArrayList<String> avlShows = new ArrayList<>(); //Arrays.asList(availableShows)
        for(String show: availableShows){
            avlShows.add(show.trim());
        }

        if(avlShows.size() == 0){ //availableShows
            System.out.println("No further show to book movie ticket");
            return "Success";
//            return 0;
        }

        //available slots retrieved

        //sort the shows
        ArrayList<String> accessibleShows = new ArrayList<>();
        int index = avlShows.indexOf(movieID);
        if(index == -1) {
            avlShows.add(movieToRemove);
            index = 0;
        }
        Collections.sort(avlShows, new sortAvailableShowListComparator());
        for(int i = avlShows.indexOf(movieToRemove) + 1; i < avlShows.size() ; i++){
            accessibleShows.add(avlShows.get(i));
        }

        //book movie tickets for customers
        for(String customerID : customers){
            if(bookings != null && bookings.get(customerID) != null){
                int numTickets = bookings.get(customerID);
                for(String show : accessibleShows){
                    String result = bookMovieTickets(customerID, show.split(" ")[0], movieName, numTickets);
                    if(result.equals("Success")){
//                    customers.remove(customerID); //to be checked if can be removed
                        break;
                    }
                }
            }

        }
        logger.logger.info("Function executed successfully. Returning 0");
        return "Success";
//        return 0;
    }

    @Override
    public String listMovieShowsAvailability(String movieName, boolean isClientCall) {

        logger.logger.info("Called function listMovieShowsAvailability with params: " + " " + movieName);
        System.out.println("Entered listMovieShowsAvailability for server: " + serverName);
        ArrayList<String> availableShows = new ArrayList<>();
        if(movies == null){
//            System.out.println("Entered if for listMovieShowsAvailability");
//            availableShows = null;
        }
        else if(movies.get(movieName) == null){
//            System.out.println("Entered else if for listMovieShowsAvailability. get movieName == null");
//            availableShows = null;
        }
        else{
//            System.out.println("Entered else statement");
            ConcurrentHashMap<String,MovieData> shows = movies.get(movieName);
            for (Map.Entry<String, MovieData> show : shows.entrySet()) {
                System.out.println(show.getKey().trim() + " " + show.getValue().capacity);
                if(show.getValue().capacity > 0){
                    System.out.println("Adding " + show.getKey());
                    availableShows.add(show.getKey().trim() + " " + show.getValue().capacity);
                }
            }
        }

        if(isClientCall){ //send udp packets
            logger.logger.info("Making UDP call to other servers");
            System.out.println("Entered isClientCall for server: " + serverName);
            ArrayList<Integer> portsList = getPortList();
            DatagramSocket ds1 = null;
            DatagramSocket ds2 = null;
            try{
                System.out.println("Entered try block in isClientCall for server: " + serverName);
                ds1 = new DatagramSocket();
                ds2 = new DatagramSocket();
                String msg = "listMovieShowsAvailability_" + movieName;
                System.out.println("Msg created: " + msg);
                byte[] req = msg.getBytes();
                InetAddress ia=InetAddress.getLocalHost();

                System.out.println("ports:" + getPortList());
                DatagramPacket dp1 = new DatagramPacket(req,req.length,ia,portsList.get(0));
                ds1.send(dp1);
                logger.logger.info("Datagram packet sent to port " + portsList.get(0));
                System.out.println("Packet sent to: " + portsList.get(0));
                DatagramPacket dp2 = new DatagramPacket(req,req.length,ia,portsList.get(1));
                ds2.send(dp2);
                logger.logger.info("Datagram packet sent to port " + portsList.get(1));
                System.out.println("Packet sent to: " + portsList.get(1));

                //create reply data

                byte[] reply1 = new byte[1024];
                byte[] reply2 = new byte[1024];
                DatagramPacket dpreply1 = new DatagramPacket(reply1,reply1.length);
                DatagramPacket dpreply2 = new DatagramPacket(reply2,reply2.length);
                System.out.println();

                //receive data
                ds1.receive(dpreply1);
                ds2.receive(dpreply2);
                String res1 = new String(dpreply1.getData()).trim();
                if(!res1.isEmpty()){
                    System.out.println("Res1: " + res1);
                    for(String show: res1.split("_")){
                        if(!show.trim().isEmpty()){
                            availableShows.add(show.trim());
                        }
                    }
                }

                String res2 = new String(dpreply2.getData()).trim();
                if(!res2.isEmpty()){
                    System.out.println("Res2: " + res2);
                    for(String show: res2.split("_")){
                        availableShows.add(show.trim());
                    }
                }
                ds1.close();
                ds2.close();
                logger.logger.info("Datagram sockets closed");
            }

            catch(Exception ex){
                System.out.println("Exception occurred: " + ex);
                logger.logger.warning("Exception occurred: " + ex);
            }
        }

//        return availableShows;
        String result = "";
        System.out.println("available Shows: " + availableShows);
        if(!availableShows.isEmpty()){
            Collections.sort(availableShows);
            for (String show: availableShows){
                System.out.println("Result: " + result);
                result = result.concat(show.trim() + "_");
            }
        }
        result = result.substring(0,result.length()-1);
        System.out.println("listMovieShowsAvailability returning: " + result);
        logger.logger.info("Function call completed. Returning: " + result);
        return result;
    }

    @Override
    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        logger.logger.info("Called function bookMovieTickets");
        logger.logger.info("Params: " + " " + customerID + " " + movieID + " " + movieName + " " + numberOfTickets);
        System.out.println("Entered bookMovieTickets() for server: " + serverName);
        System.out.println("Details: " + customerID + " " + movieID + " " + movieName + " " + numberOfTickets);
        try{
            System.out.println("Entered try block for bookMovieTickets");
            if(!serverName.substring(0,3).equals(movieID.substring(0,3))){
                logger.logger.info("Making udp call");
                //udp call
                System.out.println("Entered udp call bookMovieTickets");
                String movieLocation = movieID.substring(0,3);
                String customerLocation = customerID.substring(0,3);
                //basic condition for 3 tickets outside own server
                if(numberOfTickets>3){
                    System.out.println("numberOfTickets>3");
                    logger.logger.info("Unsuccessful booking");
                    return "Failure";
                }
                String bookings = getBookingSchedule(customerID, true);
                //condition for 3 tickets outside own server
                String[] bookedShows = bookings.split("_");
                int otherServerTickets = 0;
                if(!bookings.equals("")){
                    for(String show: bookedShows){
                        String []booking = show.trim().split(" ");
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyy");
                        LocalDate alreadyBookedDate = LocalDate.parse(booking[1].substring(4),dateTimeFormatter);
                        LocalDate newShowDate = LocalDate.parse(movieID.substring(4),dateTimeFormatter);
                        LocalDate previousShowDate = newShowDate.minusDays(6);
                        if(alreadyBookedDate.isAfter(previousShowDate) && !booking[1].contains(serverName.substring(0,3))){
                            otherServerTickets += Integer.parseInt(booking[2]);
                        }
                    }
                    if(otherServerTickets + numberOfTickets > 3){
                        System.out.println("otherServerTickets + numberOfTickets > 3 - Unsuccessful call");
                        logger.logger.info("Unsuccessful booking");
                        return "Failure";
//                        return -1;
                    }
                }
//                for(String show: bookedShows){
//                    String []booking = show.trim().split(" ");
//                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyy");
//                    LocalDate alreadyBookedDate = LocalDate.parse(booking[1].substring(4),dateTimeFormatter);
//                    LocalDate newShowDate = LocalDate.parse(movieID.substring(4),dateTimeFormatter);
//                    LocalDate previousShowDate = newShowDate.minusDays(6);
//                    if(alreadyBookedDate.isAfter(previousShowDate) && !booking[1].contains(serverName.substring(0,3))){
//                        otherServerTickets += Integer.parseInt(booking[2]);
//                    }
//                }
//                if(otherServerTickets + numberOfTickets > 3){
//                    System.out.println("otherServerTickets + numberOfTickets > 3 - Unsuccessful call");
//                    logger.logger.info("Unsuccessful booking");
//                    return -1;
//                }

                //Positive scenario
                System.out.println("Entered positive scenario");
                if(movieLocation.equals("ATW")){
                    if(customerLocation.equals("VER")){
                        return convertIntToString(makeUDPCallForBookingTicket(customerID, movieID, movieName, numberOfTickets, verAtwPortNum));
                    }
                    else{ //customerLocation.equals("OUT")
                        return convertIntToString(makeUDPCallForBookingTicket(customerID, movieID, movieName, numberOfTickets, outAtwPortNum));
                    }
                }
                else if(movieLocation.equals("VER")){
                    if(customerLocation.equals("ATW")){
                        return convertIntToString(makeUDPCallForBookingTicket(customerID, movieID, movieName, numberOfTickets, atwVerPortNum));
                    }
                    else{ //customerLocation.equals("OUT")
                        return convertIntToString(makeUDPCallForBookingTicket(customerID, movieID, movieName, numberOfTickets, outVerPortNum));
                    }
                }
                else { //movieLocation = OUT
                    if(customerLocation.equals("VER")){
                        return convertIntToString(makeUDPCallForBookingTicket(customerID, movieID, movieName, numberOfTickets, verOutPortNum));
                    }
                    else{ //customerLocation.equals("ATW")
                        return convertIntToString(makeUDPCallForBookingTicket(customerID, movieID, movieName, numberOfTickets, atwOutPortNum));
                    }
                }
            }
            else{
                System.out.println("try else block");
                if(movies==null || movies.get(movieName) == null || movies.get(movieName).get(movieID) == null){
                    System.out.println("No slot available!");
                    logger.logger.info("Unsuccessful booking");
                    return "Failure";
//                    return -1;
                }
                else if(movies.get(movieName).get(movieID).capacity < numberOfTickets){
                    System.out.println("Required tickets unavailable!");
                    logger.logger.info("Unsuccessful booking");
                    return "Failure";
//                    return -1;
                }
                else if(movies.get(movieName).get(movieID).capacity >= numberOfTickets) {
                    System.out.println("Capacity: " + movies.get(movieName).get(movieID).capacity);
                    System.out.println("Booking tickets.");
                    movies.get(movieName).get(movieID).capacity -= numberOfTickets;
                    if(!movies.get(movieName).get(movieID).customers.contains(customerID)){
                        movies.get(movieName).get(movieID).customers.add(customerID);
                    }

                    System.out.println("Updated details: ");
                    System.out.println(movies.get(movieName).get(movieID).capacity);
                    System.out.println(movies.get(movieName).get(movieID).customers);
                    int res = populateCustomerBookings(customerID, movieID, movieName, numberOfTickets);
                    System.out.println("Returning: " + res);
                    logger.logger.info("Successful booking");
                    return convertIntToString(res);
//                    return res;
                }
                logger.logger.info("Function call completed");
                return "Success";
//                return 0;
            }
        }
        catch(Exception ex){
            System.out.println("Exception thrown" + ex.getMessage());
            logger.logger.warning("Exception thrown" + ex);
            return "Failure";
//                    return -1;
        }

//        return 0;
    }

    @Override
    public String getBookingSchedule(String customerID, boolean isClientCall) {

        logger.logger.info("Called function getBookingSchedule with params: " + customerID);
        ArrayList<String> bookingSchedule = new ArrayList<>();
        System.out.println("Entered getBookingSchedule for server: " + serverName);
        if(customerBookings == null){
//            System.out.println("entered if1");
//            bookingSchedule =  null;
        }
        else if(customerBookings.get(customerID) == null){
//            System.out.println("entered elseif custid == null");
//            bookingSchedule =  null;
        }
        else {
//            System.out.println("entered else");

            ArrayList<CustomerData> shows = customerBookings.get(customerID);
            System.out.println("shows: " + shows);

            for(CustomerData show: customerBookings.get(customerID)){
                bookingSchedule.add(show.movieName + " " + show.movieID + " " + show.tickets);
            }
//            System.out.println("returning bookingSchedule: " + bookingSchedule);
        }

        if(isClientCall) {
            logger.logger.info("Making UDP calls to other servers");
            System.out.println("Entered isClientCall for server: " + serverName);
            ArrayList<Integer> portsList = getPortList();
            DatagramSocket ds1 = null;
            DatagramSocket ds2 = null;
            try{
                System.out.println("Entered try block in isClientCall for server: " + serverName);
                ds1 = new DatagramSocket();
                ds2 = new DatagramSocket();
                String msg = "getBookingSchedule_" + customerID;
                System.out.println("Msg created: " + msg);
                byte[] req = msg.getBytes();
                InetAddress ia=InetAddress.getLocalHost();

                System.out.println("ports:" + getPortList());
                DatagramPacket dp1 = new DatagramPacket(req,req.length,ia,portsList.get(0));
                ds1.send(dp1);
                System.out.println("Packet sent to: " + portsList.get(0));
                logger.logger.info("Datagram Packet sent to port " + portsList.get(0));
                DatagramPacket dp2 = new DatagramPacket(req,req.length,ia,portsList.get(1));
                ds2.send(dp2);
                System.out.println("Packet sent to: " + portsList.get(1));
                logger.logger.info("Datagram Packet sent to port " + portsList.get(1));

                //create reply data

                byte[] reply1 = new byte[1024];
                byte[] reply2 = new byte[1024];
                DatagramPacket dpreply1 = new DatagramPacket(reply1,reply1.length);
                DatagramPacket dpreply2 = new DatagramPacket(reply2,reply2.length);
                System.out.println();

                //receive data
                ds1.receive(dpreply1);
                logger.logger.info("Response received for 1st packet");
                ds2.receive(dpreply2);
                logger.logger.info("Response received for 2nd packet");
                String res1 = new String(dpreply1.getData()).trim();
                if(!res1.isEmpty()){
                    System.out.println("Res1: " + res1);
                    for(String show: res1.split("_")){
                        if(!show.trim().isEmpty()){
                            bookingSchedule.add(show.trim());
                        }
                    }
                }

                String res2 = new String(dpreply2.getData()).trim();
                if(!res2.isEmpty()){
                    System.out.println("Res2: " + res2);
                    for(String show: res2.split("_")){
                        bookingSchedule.add(show.trim());
                    }
                }
                ds1.close();
                ds2.close();
            }

            catch(Exception ex){
                System.out.println("Exception occurred: " + ex);
                logger.logger.warning("Exception occurred: " + ex);
            }
        }
        logger.logger.info("Function call completed successfully");
        String result = "";
        System.out.println("available Shows: " + bookingSchedule);
        if(!bookingSchedule.isEmpty()){
            Collections.sort(bookingSchedule);
            for (String show: bookingSchedule){
                System.out.println("Result: " + result);
                result = result.concat(show.trim() + "_");
            }
            result = result.substring(0,result.length()-1);
        }

        System.out.println("listMovieShowsAvailability returning: " + result);
        logger.logger.info("Function call completed. Returning " + result);
        return result;
//                    return -1;



//        return "success";
    }

    @Override
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        logger.logger.info("Entered function cancelMovieTickets");
        logger.logger.info("Request params: " + customerID + " " + movieID + " " + movieName + " " + numberOfTickets);
        System.out.println("Entered cancelMovieTickets for server: " + serverName);
        System.out.println("Details: " + customerID + " " + movieID + " " + movieName + " " + numberOfTickets);
        try{
            System.out.println("Entered try block");
            if(!serverName.substring(0,3).equals(movieID.substring(0,3))){
                //udp call
                logger.logger.info("Making udp call");
                String movieLocation = movieID.substring(0,3);
                String customerLocation = customerID.substring(0,3);
                if(movieLocation.equals("ATW")){
                    if(customerLocation.equals("VER")){
                        logger.logger.info("Making udp call to ATW server");
                        return convertIntToString(makeUDPCallForCancellingTicket(customerID, movieID, movieName, numberOfTickets, verAtwPortNum));
                    }
                    else{ //customerLocation.equals("OUT")
                        logger.logger.info("Making udp call to ATW server");
                        return convertIntToString(makeUDPCallForCancellingTicket(customerID, movieID, movieName, numberOfTickets, outAtwPortNum));
                    }
                }
                else if(movieLocation.equals("VER")){
                    if(customerLocation.equals("ATW")){
                        logger.logger.info("Making udp call to VER server");
                        return convertIntToString(makeUDPCallForCancellingTicket(customerID, movieID, movieName, numberOfTickets, atwVerPortNum));
                    }
                    else{ //customerLocation.equals("OUT")
                        logger.logger.info("Making udp call to VER server");
                        return convertIntToString(makeUDPCallForCancellingTicket(customerID, movieID, movieName, numberOfTickets, outVerPortNum));
                    }
                }
                else { //movieLocation = OUT
                    if(customerLocation.equals("VER")){
                        logger.logger.info("Making udp call to OUT server");
                        return convertIntToString(makeUDPCallForCancellingTicket(customerID, movieID, movieName, numberOfTickets, verOutPortNum));
                    }
                    else{ //customerLocation.equals("ATW")
                        logger.logger.info("Making udp call to OUT server");
                        return convertIntToString(makeUDPCallForCancellingTicket(customerID, movieID, movieName, numberOfTickets, atwOutPortNum));
                    }
                }
            }
            else{
                if(movies==null || movies.get(movieName) == null || movies.get(movieName).get(movieID) == null){
                    logger.logger.info("Unsuccessful cancellation!");
                    System.out.println("No such show exists!");
                    return "Failure";
//                        return -1;
                }
                else if(customerBookings == null || customerBookings.get(customerID).isEmpty()){
                    logger.logger.info("Unsuccessful cancellation!");
                    System.out.println("else if unsuccessful cancel Line761");
                    return "Failure";
//                        return -1;
                }
                else {
                    System.out.println("Entered else");
                    boolean hasBooked = !customerBookings.get(customerID).isEmpty();
                    System.out.println("hasBooked: " + hasBooked);
                    int totalBookedTickets = 0;
                    ArrayList<CustomerData> allShows = new ArrayList<>();
                    allShows = customerBookings.get(customerID);

                    if(hasBooked){
                        for(CustomerData show: allShows){
//                            System.out.println("");
                            if(show.movieName.equals(movieName) && show.movieID.equals(movieID)){
                                totalBookedTickets = show.tickets;
                                break;
                            }
                        }
                        //case1: tickets to be cancelled > tickets booked
                        if(numberOfTickets > totalBookedTickets){
                            logger.logger.info("Unsuccessful cancellation!");
                            System.out.println("Entered if numberOfTickets > totalBookedTickets");
                            return "Failure";
//                        return -1;
                        }
                        //case2: tickets to be cancelled < tickets booked
                        else if(numberOfTickets < totalBookedTickets){
                            System.out.println("Cancelling some tickets.");
                            movies.get(movieName).get(movieID).capacity += numberOfTickets;
                            allShows = customerBookings.get(customerID);
                            for(int i = 0 ; i < allShows.size(); i++){
                                CustomerData show = allShows.get(i);
                                if(show.movieID.equals(movieID) && show.movieName.equals(movieName)){
                                    customerBookings.get(customerID).get(i).tickets -= numberOfTickets;
                                    logger.logger.info("Successful cancellation of some tickets!");
                                    return "Success";
//                        return 0;
                                }
                            }
                        }
                        //case3: tickets to be cancelled = tickets booked (cancel all)
                        else{
                            System.out.println("Cancelling all tickets.");
                            boolean wasRemoved = movies.get(movieName).get(movieID).customers.remove(customerID);
                            if(wasRemoved){
                                movies.get(movieName).get(movieID).capacity += numberOfTickets;
                                allShows = customerBookings.get(customerID);
                                for(int i = 0 ; i < allShows.size(); i++){
                                    CustomerData show = allShows.get(i);
                                    if(show.movieID.equals(movieID) && show.movieName.equals(movieName)){
                                        customerBookings.get(customerID).remove(i);
                                        if(customerBookings.get(customerID).isEmpty()){ //to be tested. remove all shows of a single customer
                                            customerBookings.remove(customerID);
                                        }
                                        logger.logger.info("Successful cancellation of all tickets!");
                                        return "Success";
//                                        return 0;
                                    }
                                }
                            }
                            else{
                                System.out.println("Could not remove tickets!");
                                logger.logger.info("Unsuccessful cancellation!");
                                return "Failure";
//                          return -1;
                            }
                        }
                    }
                    else{
                        logger.logger.info("Unsuccessful cancellation!");
                        return "Failure";
//                        return -1;
                    }
                }

            }
        }
        catch(Exception ex){
            System.out.println("Exception thrown" + ex.getMessage());
            logger.logger.info("Unsuccessful cancellation! Exception occurred: " + ex);
            return "Failure";
//                        return -1;
        }
        logger.logger.info("Unsuccessful cancellation!");
        return "Failure";
//                        return -1;
    }

    @Override
    public String exchangeTickets(String customerID, String old_movieName, String movieID, String new_movieID, String new_movieName, int numberOfTickets) {
        try{
            logger.logger.info("Entered exchangeTickets() with params: " + "customerID: " + customerID + ", old_movieName: " + old_movieName + ", movieID:  " + movieID + ", new_movieID:  " + new_movieID + ", new_movieName: " + new_movieName + ", numberOfTickets: " + numberOfTickets);
            String oldLocation = movieID.toUpperCase().substring(0,3);
            String newLocation = new_movieID.toUpperCase().substring(0,3);
            String userLocation = customerID.toUpperCase().substring(0,3);
            int isBooked, isCancelled;

            //check if home theater for new show
            if(newLocation.equals(userLocation)){
                //book, cancel, cancel
                isBooked = convertStringToInt(bookMovieTickets(customerID,new_movieID,new_movieName,numberOfTickets));
                if(isBooked == 0){
                    isCancelled = convertStringToInt(cancelMovieTickets(customerID,movieID,old_movieName,numberOfTickets));
                    if(isCancelled == 0){
                        logger.logger.info("Tickets exchanged successfully. Transaction complete");
                        return "Success";
//                        return 0;
                    }
                    else{
                        logger.logger.info("Could not cancel previous ticket.");
                        int res = convertStringToInt(cancelMovieTickets(customerID,new_movieID,new_movieName,numberOfTickets));
                        if(res == 0){
                            return "Failure";
//                        return -1;
                        }
                        return "Failure";
//                        return -1;
                    }
                }
                logger.logger.info("Could not book ticket. Transaction failed");
                return "Failure";
//                        return -1;
            }

            //check if both shows in same location
            if(newLocation.equals(oldLocation)){
                //cancel, book, book
                isCancelled = convertStringToInt(cancelMovieTickets(customerID,movieID,old_movieName,numberOfTickets));
                if(isCancelled == 0){
                    System.out.println("Movie successfully cancelled. Proceeding to book ticket");
                    isBooked = convertStringToInt(bookMovieTickets(customerID,new_movieID,new_movieName,numberOfTickets));
                    if(isBooked == 0){
                        System.out.println("Movie successfully booked. Transaction successful");
                        logger.logger.info("Movie successfully booked. Transaction successful");
                        return "Success";
//                        return 0;
                    }
                    else{
                        int res = convertStringToInt(bookMovieTickets(customerID,movieID,old_movieName,numberOfTickets));
                        if(res == 0){
                            logger.logger.info("Could not book ticket. Transaction reversed.");
                            return "Failure";
//                        return -1;
                        }
                        return "Failure";
//                        return -1;
                    }
                }
                logger.logger.info("Could not cancel ticket. Exchange could not be performed.");
                return "Failure";
//                        return -1;
            }

            //check if both in other servers
            if(!newLocation.equals(userLocation) && !oldLocation.equals(userLocation)){
                //cancel, book, book
                // or
                //get booking schedule, check bookings and num of tickets
                isCancelled = convertStringToInt(cancelMovieTickets(customerID,movieID,old_movieName,numberOfTickets));
                if(isCancelled == 0){
                    isBooked = convertStringToInt(bookMovieTickets(customerID,new_movieID,new_movieName,numberOfTickets));
                    if(isBooked == 0){
                        logger.logger.info("Exchange of tickets successful. Transaction complete.");
                        return "Success";
//                        return 0;
                    }
                    else {
                        int res = convertStringToInt(bookMovieTickets(customerID,movieID,old_movieName,numberOfTickets));
                        if(res == 0){
                            System.out.println("Could not book ticket. Transaction reversed.");
                            logger.logger.info("Could not book ticket. Transaction reversed.");
                            return "Failure";
//                        return -1;
                        }
                        return "Failure";
//                        return -1;
                    }
                }
                logger.logger.info("Could not cancel previous ticket. Transaction failed.");
                return "Failure";
//                        return -1;
            }
            else {
                isCancelled = convertStringToInt(cancelMovieTickets(customerID,movieID,old_movieName,numberOfTickets));
                if(isCancelled == 0){
                    isBooked = convertStringToInt(bookMovieTickets(customerID,new_movieID,new_movieName,numberOfTickets));
                    if(isBooked == 0){
                        logger.logger.info("Exchange of tickets successful. Transaction complete.");
                        return "Success";
//                        return 0;
                    }
                    else {
                        int res = convertStringToInt(bookMovieTickets(customerID,movieID,old_movieName,numberOfTickets));
                        if(res == 0){
                            System.out.println("Could not book ticket. Transaction reversed.");
                            logger.logger.info("Could not book ticket. Transaction reversed.");
                            return "Failure";
//                        return -1;
                        }
                        return "Failure";
//                        return -1;
                    }
                }
                logger.logger.info("Could not cancel previous ticket. Transaction failed.");
                return "Failure";
//                        return -1;
            }

//            System.out.println("Unhandled scenario reached.");
//            logger.logger.warning("Unhandled scenario reached.");

//            //check if one in main server and other in other location
//            int isCancellable = -1;
//            int isBookable = isBooked(customerID, new_movieID, new_movieName, numberOfTickets);
//            if(isBookable == 0){
//                isCancellable = isCancelled(customerID,movieID, old_movieName, numberOfTickets);
//                if(isCancellable == 0){
//                    return 0;
//                }
//                else {
//                    //cancel booked tickets
//                }
//            }
//            return 1;
        }
        catch(Exception ex){
            ex.printStackTrace();
            return "Failure";
//                        return -1;
        }
    }
}
