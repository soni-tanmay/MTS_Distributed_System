package Replica2;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;
import Utils.Log;

class Booking {
    public Booking(int capacity) {
        this.capacity = capacity;
    }
     public Booking(int capacity, String cust_ids) {
         this.capacity = capacity;
         this.cust_ids.add(cust_ids);
     }
     public int capacity;
     public ArrayList<String> cust_ids = new ArrayList<>();
 }

 class Customer {
    public Customer(String movieName, int bookedTickets, String movieId) {
        this.bookedTickets = bookedTickets;
        this.movieName = movieName;
        this.movieId = movieId;
    }
    public int bookedTickets;
    public String movieName;
    public String movieId;

 }

@WebService(endpointInterface="Replica2.BookingInterface")
// @SOAPBinding(style=Style.RPC)
public class BookingImplementation implements BookingInterface{

	ConcurrentHashMap<String, ConcurrentHashMap<String, Booking>> movies = new ConcurrentHashMap<String, ConcurrentHashMap<String, Booking>>();
    ConcurrentHashMap<String, ArrayList<Customer>> customers = new ConcurrentHashMap<String, ArrayList<Customer>>();
	
	String serverId;

    Log logInfo;

	BookingImplementation(String serverId, Log logInfo) {
		super();
		this.serverId = serverId;
		this.logInfo = logInfo;
	}

    @Override
    public String addMovieSlots(String movieID, String movieName, int bookingCapacity) {
        logInfo.logger.info("Adding " + bookingCapacity + " slots for " + movieID + " " + movieName);

         if(!movieID.substring(0, 3).equals(this.serverId)) {
             logInfo.logger.info("You can only add movies in your respective location"); 
             return "Failure";
         }
         if(movies.get(movieName) != null) {

             if(movies.get(movieName).get(movieID) != null) {

                 movies.get(movieName).get(movieID).capacity = movies.get(movieName).get(movieID).capacity + bookingCapacity;
             } else {
                 movies.get(movieName).put(movieID, new Booking((bookingCapacity)));
             }
         } else {
             movies.put(movieName, new ConcurrentHashMap(){{put(movieID, new Booking(bookingCapacity));}});
         }

        //  for (Map.Entry<String, ConcurrentHashMap<String, Booking>> entry : movies.entrySet()) {
        //      String movie = entry.getKey();
        //      System.out.println("------------------------------");
        //      for (Map.Entry<String, Booking> custEntry : entry.getValue().entrySet()) {
        //          String movieId = custEntry.getKey();
        //          Booking booking = custEntry.getValue();
        //          System.out.println(movie + " - " + movieId + " - " + booking.capacity);
        //      }
        //  }
         // logger
         logInfo.logger.info("Added movie slots successfully " + LocalDateTime.now());
         return "Success";
        //  return "Added movie slots successfully! ";

    }

    public String removeMovieSlots(String movieID, String movieName, boolean isClientCall) {
        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
            LocalDate todayDate = LocalDate.now();
            LocalDate movieDate = LocalDate.parse(movieID.substring(4),formatter);
            if(!movieID.contains(serverId) || movies.get(movieName)==null || movies.get(movieName).isEmpty() || movies.get(movieName).get(movieID)== null|| !movieDate.isAfter(todayDate)){
                System.out.println("removeMovieSlots: Unauthorised request");
                logInfo.logger.info(LocalDateTime.now() + " | removeMovieSlots | " + movieID + "_" + movieName + " | completed | " + "Unauthorised request");
                return "Unauthorised request";
            }
            if(movies.get(movieName).get(movieID).cust_ids == null || movies.get(movieName).get(movieID).cust_ids.isEmpty() || movies.get(movieName).get(movieID).cust_ids.size()==0){
                movies.get(movieName).remove(movieID);
            }else {
                for (String e : movies.get(movieName).get(movieID).cust_ids){
                    for (Customer c : customers.get(e)){
                        if(c.movieId.equals(movieID)){
                            String nextMovieSlot= movieID;
                            LocalDate nextMovieDate = movieDate;
                            LocalDate lastMovieSlot = LocalDate.now().plusDays(7);
                            while(lastMovieSlot.isAfter(nextMovieDate)){
                                if(nextMovieSlot.charAt(3)=='M'){
                                    nextMovieSlot = nextMovieSlot.substring(0,3)+"A"+nextMovieSlot.substring(4);
                                } else if (nextMovieSlot.charAt(3)=='A') {
                                    nextMovieSlot = nextMovieSlot.substring(0,3)+"E"+nextMovieSlot.substring(4);
                                }else {
                                    nextMovieDate = nextMovieDate.plusDays(1);
                                    nextMovieSlot = nextMovieSlot.substring(0,3)+"M"+nextMovieDate.format(formatter);
                                }
                                String result = bookMovieTickets(e,nextMovieSlot,movieName,c.bookedTickets, true);
                                if(result.equals("Successfully booked")){
                                    break;
                                }
                            }
                            customers.get(e).remove(c);
                            break;
                        }
                    }
                }
                movies.get(movieName).remove(movieID);
            }
            System.out.println("removeMovieSlots: Removed slot successfully");
            logInfo.logger.info(LocalDateTime.now() + " | removeMovieSlots | " + movieID + "_" + movieName + " | completed | " + "Removed slot successfully");
            return "Removed slot successfully";
        }catch (Exception e){
            System.out.println("removeMovieSlots: "+e);
            logInfo.logger.info(LocalDateTime.now() + " | removeMovieSlots | " + movieID + "_" + movieName + " | failed | " + e);
            return e.toString();
        }
    }

    private ArrayList<String> getPort(String serverId) {
        ArrayList<String> ports = new ArrayList<>();
       switch(serverId){
           case "ATW":
               ports.add("7800");
               ports.add("7700");
               return ports;

           case "OUT":
               ports.add("7600");
               ports.add("7800");
               return ports;

           case "VER":
               ports.add("7600");
               ports.add("7700");
               return ports;
       }
        return null;
    }
    
    @Override
    public String[] listMovieShowsAvailability(String movieName, boolean isClientCall) {
        // String[] result = {"listMovieShowsAvailability!!"};
        // return result;

        logInfo.logger.info("Available Movie shows for" + movieName);
         ArrayList<String> availableShows = new ArrayList<>();
        try {
             ConcurrentHashMap<String, Booking> shows = movies.get(movieName);
             if(shows != null) {
                for (Map.Entry<String, Booking> show : shows.entrySet()) {
                    if(show.getValue().capacity > 0){
                        availableShows.add(show.getKey() + " " +show.getValue().capacity);
                    }
                }
             }

             if(!!isClientCall) {
                 DatagramSocket dsVerdun = new DatagramSocket();

                 DatagramSocket dsOutrement = new DatagramSocket();

                 String functionNamesWithParameter = "listMovieShowsAvailability" + "-" + movieName;

                 byte[]b = functionNamesWithParameter.getBytes();

                 InetAddress ia = InetAddress.getLocalHost();

                 ArrayList<String> ports = new ArrayList<>();

                 ports = getPort(this.serverId);

                 DatagramPacket dpVerdun = new DatagramPacket(b ,b.length, ia, Integer.parseInt(ports.get(0)));

                 DatagramPacket dpOutrement = new DatagramPacket(b ,b.length, ia, Integer.parseInt(ports.get(1)));

                 dsVerdun.send(dpVerdun);

                 dsOutrement.send(dpOutrement);

                 byte[] b1=new byte[1024];

                 byte[] b2=new byte[1024];

                 DatagramPacket dpVerdun1=new DatagramPacket(b1,b1.length);

                 dsVerdun.receive(dpVerdun1);

                 DatagramPacket dpOutrement1=new DatagramPacket(b2,b2.length);

                 dsOutrement.receive(dpOutrement1);

                 ByteArrayInputStream bais1 = new ByteArrayInputStream(dpVerdun1.getData());
                 ByteArrayInputStream bais2 = new ByteArrayInputStream(dpOutrement1.getData());

                 DataInputStream in1 = new DataInputStream(bais1);
                 DataInputStream in2 = new DataInputStream(bais2);

                 while (in1.available() > 0) {
                     String element = in1.readUTF();
                     if(element.equals("")){
                         break;
                     }
                     availableShows.add(element);
                 }

                 while (in2.available() > 0) {
                     String element = in2.readUTF();
                     if(element.equals("")){
                         break;
                     }
                     availableShows.add(element);
                 }
             }

        } catch (Exception e) {
             e.printStackTrace();
             System.out.println("Exception in listMovieShowsAvailability" + e);
        }
        logInfo.logger.info("Available Movie shows for" + movieName + " " + availableShows);
        String[] stockArr = new String[availableShows.size()];
        stockArr = availableShows.toArray(stockArr);
        return stockArr;
    }

    private static String getSpecificPortFromMovieId(ArrayList<String> ports, String movieId) {
        if(movieId.substring(0, 3).equals("ATW")) {
            return "7600";
        } else if(movieId.substring(0, 3).equals("OUT")) {
            return "7700";
        } else if(movieId.substring(0, 3).equals("VER")) {
            return "7800";
        }
        return null;
    }

    private static String bookCancelMovieOnOtherServer(String customerID, String movieID, String movieName, int numberOfTickets, boolean cancel) throws IOException {
        DatagramSocket dsServer1 = new DatagramSocket();
        String functionNamesWithParameter;
        if(cancel){
            functionNamesWithParameter = "cancelMovieTickets" + "-" + customerID + "-" + movieID + "-" + movieName + "-" + numberOfTickets;
        } else {
            functionNamesWithParameter = "bookMovieTickets"  + "-" + customerID + "-" + movieID + "-" + movieName + "-" + numberOfTickets;
        }

        byte[] b = functionNamesWithParameter.getBytes();

        InetAddress ia = InetAddress.getLocalHost();

        ArrayList<String> ports = new ArrayList<>();

        // PORT getSpecificPortFromMovieId
        String specificPort = getSpecificPortFromMovieId(ports, movieID);

        DatagramPacket dpServer1 = new DatagramPacket(b, b.length, ia, Integer.parseInt(specificPort));

        dsServer1.send(dpServer1);

        byte[] b1 = new byte[1024];

        DatagramPacket dpServer11 = new DatagramPacket(b1, b1.length);

        dsServer1.receive(dpServer11);

        return new String(dpServer11.getData()).trim();
    }

	public boolean isExchangePossible(String customerID, String movieID, String movieName, int numberOfTickets, boolean isClientCall, String oldMovieId) {
		ArrayList<String> allBookings = new ArrayList<String>(Arrays.asList(getBookingSchedule(customerID, true)));
        try {

            if (!movieID.substring(0, 3).equals(this.serverId)) {

                int i = 0;
                int targetTickets = 0;

                for (String booking : allBookings){
                    String[] parsedStr = booking.split(" ");
                    DateTimeFormatter f = DateTimeFormatter.ofPattern("ddMMyy");
                    LocalDate alreadyBookedDate = LocalDate.parse(parsedStr[1].substring(4), f);
                    LocalDate bookingDate = LocalDate.parse(movieID.substring(4), f);
                    LocalDate previousDate = bookingDate.minusDays(6);
                    if(alreadyBookedDate.isAfter(previousDate) && !parsedStr[1].contains(serverId) ){
                        i+=Integer.parseInt(parsedStr[2]);
                    }
                    
                    if(parsedStr[1].equals(oldMovieId)) {
                        targetTickets = Integer.parseInt(parsedStr[2]);
                    }
                }
				if(numberOfTickets > 3) {
					logInfo.logger.info("You cannot book more that 3 movie tickets");
                    return false;
				}

                if (numberOfTickets > i ||  numberOfTickets > targetTickets){
                    return false;
                }
                return true;
            }

            if (movies != null && movies.get(movieName) != null && movies.get(movieName).get(movieID) != null) {
                if (numberOfTickets > movies.get(movieName).get(movieID).capacity) {
                    logInfo.logger.info("Sorry! we have only " + movies.get(movieName).get(movieID).capacity + " seats available");
                    return false;
                } else {
                    // todo Test below
                    for(String sched : allBookings) {

                        if(movieName.equals(sched.split(" ")[0]) && movieID.substring(3).equals(sched.split(" ")[1].substring(3))) {
                            logInfo.logger.info("You are already booked for the same slot! ");

                            return false;
                        }
                    }
					return true;
				}
            } else {
                logInfo.logger.info("Sorry! Movie show does not exist");
                return false;
            }
        } catch(Exception e) {
            e.printStackTrace();
            logInfo.logger.info(e.getMessage());
            System.out.println("Error "+ e);

            return false;
        }
	}


    @Override
    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets, boolean isClientCall) {
        // todo same customer same server is working, but different customer on different server
        logInfo.logger.info("Booking "+ numberOfTickets + " tickets for movie" + movieName + "(" + movieID + ")" + "with customer id " + customerID);
        String result;
        // Already booked tickets
        ArrayList<String> allBookings = new ArrayList<String>(Arrays.asList(getBookingSchedule(customerID, true)));
        try {

            if (!movieID.substring(0, 3).equals(this.serverId)) {

                int i = 0;
                for (String booking : allBookings){
                    String[] parsedStr = booking.split(" ");
                    DateTimeFormatter f = DateTimeFormatter.ofPattern("ddMMyy");
                    LocalDate alreadyBookedDate = LocalDate.parse(parsedStr[1].substring(4), f);
                    LocalDate bookingDate = LocalDate.parse(movieID.substring(4), f);
                    LocalDate previousDate = bookingDate.minusDays(6);
                    if(alreadyBookedDate.isAfter(previousDate) && !parsedStr[1].contains(serverId) ){
                        i+=Integer.parseInt(parsedStr[2]);
                    }
                }

                if ((numberOfTickets+i)>3){
                    logInfo.logger.info("You cannot book more that 3 movie tickets");
                    // return "You cannot book more that 3 movie tickets";
                    return "Failure";
                }

                return bookCancelMovieOnOtherServer(customerID, movieID, movieName, numberOfTickets, false);
            }

            if (movies != null && movies.get(movieName) != null && movies.get(movieName).get(movieID) != null) {
                if (numberOfTickets > movies.get(movieName).get(movieID).capacity) {
                    logInfo.logger.info("Sorry! we have only " + movies.get(movieName).get(movieID).capacity + " seats available");
                    return "Sorry! we have only " + movies.get(movieName).get(movieID).capacity + " seats available";
                } else {
                    // todo Test below
                    for(String sched : allBookings) {

                        if(movieName.equals(sched.split(" ")[0]) && movieID.substring(3).equals(sched.split(" ")[1].substring(3)) && !movieID.substring(0, 3).equals(sched.split(" ")[1].substring(0, 3))) {
                            logInfo.logger.info("You are already booked for the same slot! ");

                            // return "You are already booked for the same slot! ";
                            return "Failure";
                        }
                    }
                    movies.get(movieName).get(movieID).capacity = movies.get(movieName).get(movieID).capacity - numberOfTickets;
                    if(!movies.get(movieName).get(movieID).cust_ids.contains(customerID)) {
                        movies.get(movieName).get(movieID).cust_ids.add(customerID);
                    }


                    if (customers.containsKey(customerID)) {
                        boolean movieAlreadyExists = false;
                        for (Customer entry : customers.get(customerID)) {

                            if(entry.movieId.equals(movieID) && entry.movieName.equals(movieName)) {
                                movieAlreadyExists = true;
                                entry.bookedTickets = entry.bookedTickets + numberOfTickets;
                            }
                        }
                        if(!movieAlreadyExists) {
                            customers.get(customerID).add(new Customer(movieName, numberOfTickets, movieID));
                        }
//                         ------------
//                         if(entry.movieId.equals(movieID) && entry.movieName.equals(movieName)) {
//                             entry.bookedTickets = entry.bookedTickets + numberOfTickets;
//                         }

                    } else {
                        customers.put(customerID, new ArrayList<>());
                        customers.get(customerID).add(new Customer(movieName, numberOfTickets, movieID));
                    }

                    for (Map.Entry<String, ConcurrentHashMap<String, Booking>> entry : movies.entrySet()) {
                        String movie = entry.getKey();

                        for (Map.Entry<String, Booking> custEntry : entry.getValue().entrySet()) {
                            String movieId = custEntry.getKey();
                            Booking booking = custEntry.getValue();
                            // System.out.println(movie + " - " + movieId + " - " + booking.capacity);
                        }
                    }

                    result = "Success";
                    logInfo.logger.info(result + LocalDateTime.now());
                }
            } else {
                logInfo.logger.info("Sorry! Movie show does not exist");
                // return "Sorry! Movie show does not exist";
                return "Failure";
            }
            return result;
        } catch(Exception e) {
            e.printStackTrace();
            logInfo.logger.info(e.getMessage());
            System.out.println("Error "+ e);

            return "Exception in bookMovieTickets";
        }
    }

    @Override
    public String[] getBookingSchedule(String customerID, boolean isClientCall) {
        // String[] result = {"getBookingSchedule!!"};
        // return result;

        logInfo.logger.info("Booking Schedule for " + customerID);
         ArrayList<String> bookingSchedule = new ArrayList<>();

         try {
             if(customers != null && !customers.isEmpty() && !customerID.isEmpty()) {
                 customers.entrySet().forEach(entry -> {
                     if(entry.getKey().equals(customerID)) {
                         for(Customer data: entry.getValue()) {
                             bookingSchedule.add(data.movieName + " " + data.movieId + " " + data.bookedTickets);
                         }
                     }
                 });
             } else {
                 System.out.println("No customer in hashmap ");
             }


            if(!!isClientCall) {
                 DatagramSocket dsServer1 = new DatagramSocket();

                 DatagramSocket dsServer2 = new DatagramSocket();

                 String functionNamesWithParameter = "getBookingSchedule" + "-" + customerID;

                 byte[]b = functionNamesWithParameter.getBytes();

                 InetAddress ia = InetAddress.getLocalHost();

                 ArrayList<String> ports = getPort(this.serverId);

                 DatagramPacket dpServer1 = new DatagramPacket(b ,b.length, ia, Integer.parseInt(ports.get(0)));

                 DatagramPacket dpServer2 = new DatagramPacket(b ,b.length, ia, Integer.parseInt(ports.get(1)));

                 dsServer1.send(dpServer1);

                 dsServer2.send(dpServer2);

                 byte[] b1=new byte[1024];

                 byte[] b2=new byte[1024];

                 DatagramPacket dpServer11=new DatagramPacket(b1,b1.length);

                 dsServer1.receive(dpServer11);

                 DatagramPacket dpServer22=new DatagramPacket(b2,b2.length);

                 dsServer2.receive(dpServer22);

                 ByteArrayInputStream bais1 = new ByteArrayInputStream(dpServer11.getData());
                 ByteArrayInputStream bais2 = new ByteArrayInputStream(dpServer22.getData());

                 DataInputStream input1 = new DataInputStream(bais1);
                 DataInputStream input2 = new DataInputStream(bais2);

                 while (input1.available() > 0) {
                     String val = input1.readUTF();
                     if(val.equals("")){
                         break;
                     }
                     bookingSchedule.add(val);
                 }
                 // 2nd loop required ?
                 while (input2.available() > 0) {
                     String val = input2.readUTF();
                     if(val.equals("")){
                         break;
                     }
                     bookingSchedule.add(val);
                 }
             }
         } catch(Exception e) {
             System.out.println("Exception in getBookingSchedule " + e);
         }
        logInfo.logger.info("Booking Schedule for customer with ID " + customerID + " is: " + bookingSchedule);
        String[] stockArr = new String[bookingSchedule.size()];
        stockArr = bookingSchedule.toArray(stockArr);
        return stockArr;
    }

    @Override
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets, boolean isClient) {
        // return "cancelMovieTickets!!";
        logInfo.logger.info("Cancel "+ numberOfTickets + " tickets for movie" + movieName + " with customer id " + customerID + " movieid " + movieID);
         if (!movieID.substring(0, 3).equals(this.serverId)) {
             try {
                return bookCancelMovieOnOtherServer(customerID, movieID, movieName, numberOfTickets, true);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Exception in cancelling movie ticket " + e);
            }
         }
         if(movies.get(movieName) == null) {
             logInfo.logger.info("Movie does not exist! ");
             return "Failure";
         }
         if(movies.get(movieName).get(movieID) == null) {
             logInfo.logger.info("Movie Id does not exist! ");
            return "Failure";
         }
         if(customers.get(customerID) == null) {
             logInfo.logger.info("You dont have any booked ticket! ");
             return "Failure";
         }
         for(Customer data: customers.get(customerID)) {
            if(data.movieName.equals(movieName) && data.movieId.equals(movieID)) {
                break;
            } else {
                logInfo.logger.info("You have not booked this movie - " + movieName + " " + movieID);
                //  return "You have not booked this movie - " + movieName + " " + movieID;
                return "Failure";
            }
            //  if(!data.movieName.equals(movieName) && !data.movieName.equals(movieName)) {
            //      logInfo.logger.info("You have not booked this movie - " + movieName + " " + movieID);
            //      return "You have not booked this movie - " + movieName + " " + movieID;
            //  }
         }


         for(Customer c : customers.get(customerID)) {
             if(numberOfTickets > c.bookedTickets) {
                 logInfo.logger.info("Invalid input! You have booked only " + c.bookedTickets + "tickets");
                //  return "Invalid input! You have booked only " + c.bookedTickets + "tickets";
                return "Failure";
             } else if(numberOfTickets == c.bookedTickets) {
                 movies.get(movieName).get(movieID).capacity = movies.get(movieName).get(movieID).capacity + numberOfTickets;
                 movies.get(movieName).get(movieID).cust_ids.remove(customerID);
                 customers.get(customerID).remove(c);
                 // JUST FOR LOGGING
                 for (Map.Entry<String, ConcurrentHashMap<String, Booking>> entry : movies.entrySet()) {
                     String movie = entry.getKey();
                     for (Map.Entry<String, Booking> custEntry : entry.getValue().entrySet()) {
                         String movieId = custEntry.getKey();
                         Booking booking = custEntry.getValue();
                        //  System.out.println(movie + " - " + movieId + " - " + booking.capacity);
                     }
                 }
                 logInfo.logger.info("Cancelled movie tickets successfully ");

                 return "Success";
             } else if(numberOfTickets < c.bookedTickets) {
                 movies.get(movieName).get(movieID).capacity = movies.get(movieName).get(movieID).capacity + numberOfTickets;
                 c.bookedTickets = c.bookedTickets - numberOfTickets;
                 // JUST FOR LOGGING
                 for (Map.Entry<String, ConcurrentHashMap<String, Booking>> entry : movies.entrySet()) {
                     String movie = entry.getKey();
                     for (Map.Entry<String, Booking> custEntry : entry.getValue().entrySet()) {
                         String movieId = custEntry.getKey();
                         Booking booking = custEntry.getValue();
                     }
                 }
                 logInfo.logger.info("Cancelled movie tickets successfully ");
                 return "Success";
             }
         }
         return "Success";

    }


    @Override
    public String exchangeTickets(String customerID, String old_movieName, String movieID, String new_movieID, String new_movieName, int numberOfTickets) {
        try{
            logInfo.logger.info("Entered exchangeTickets() with params: " + "customerID: " + customerID + ", old_movieName: " + old_movieName + ", movieID:  " + movieID + ", new_movieID:  " + new_movieID + ", new_movieName: " + new_movieName + ", numberOfTickets: " + numberOfTickets);
            String oldLocation = movieID.toUpperCase().substring(0,3);
            String newLocation = new_movieID.toUpperCase().substring(0,3);
            String userLocation = customerID.toUpperCase().substring(0,3);
            String isBooked, isCancelled;

            //check if home theater for new show
            if(newLocation.equals(userLocation)){
                //book, cancel, cancel
                isBooked = bookMovieTickets(customerID,new_movieID,new_movieName,numberOfTickets, true);
                if(isBooked.equals("Success")){
                    isCancelled = cancelMovieTickets(customerID,movieID,old_movieName,numberOfTickets, true);
                    if(isCancelled.equals("Success")){
                        logInfo.logger.info("Tickets exchanged successfully. Transaction complete");
                        return "Success";
//                        return 0;
                    }
                    else{
                        logInfo.logger.info("Could not cancel previous ticket.");
                        String res = cancelMovieTickets(customerID,new_movieID,new_movieName,numberOfTickets, true);
                        if(res.equals("Success")){
                            return "Failure";
//                        return -1;
                        }
                        return "Failure";
//                        return -1;
                    }
                }
                logInfo.logger.info("Could not book ticket. Transaction failed");
                return "Failure";
//                        return -1;
            }

            //check if both shows in same location
            if(newLocation.equals(oldLocation)){
                //cancel, book, book
                isCancelled = cancelMovieTickets(customerID,movieID,old_movieName,numberOfTickets, true);
                if(isCancelled.equals("Success")){
                    System.out.println("Movie successfully cancelled. Proceeding to book ticket");
                    isBooked = bookMovieTickets(customerID,new_movieID,new_movieName,numberOfTickets, true);
                    if(isBooked.equals("Success")){
                        System.out.println("Movie successfully booked. Transaction successful");
                        logInfo.logger.info("Movie successfully booked. Transaction successful");
                        return "Success";
//                        return 0;
                    }
                    else{
                        String res = bookMovieTickets(customerID,movieID,old_movieName,numberOfTickets, true);
                        if(res.equals("Success")){
                            logInfo.logger.info("Could not book ticket. Transaction reversed.");
                            return "Failure";
//                        return -1;
                        }
                        return "Failure";
//                        return -1;
                    }
                }
                logInfo.logger.info("Could not cancel ticket. Exchange could not be performed.");
                return "Failure";
//                        return -1;
            }

            //check if both in other servers
            if(!newLocation.equals(userLocation) && !oldLocation.equals(userLocation)){
                //cancel, book, book
                // or
                //get booking schedule, check bookings and num of tickets
                isCancelled = cancelMovieTickets(customerID,movieID,old_movieName,numberOfTickets, true);
                if(isCancelled.equals("Success")){
                    isBooked = bookMovieTickets(customerID,new_movieID,new_movieName,numberOfTickets, true);
                    if(isBooked.equals("Success")){
                        logInfo.logger.info("Exchange of tickets successful. Transaction complete.");
                        return "Success";
//                        return 0;
                    }
                    else {
                        String res = bookMovieTickets(customerID,movieID,old_movieName,numberOfTickets, true);
                        if(res.equals("Success")){
                            System.out.println("Could not book ticket. Transaction reversed.");
                            logInfo.logger.info("Could not book ticket. Transaction reversed.");
                            return "Failure";
//                        return -1;
                        }
                        return "Failure";
//                        return -1;
                    }
                }
                logInfo.logger.info("Could not cancel previous ticket. Transaction failed.");
                return "Failure";
//                        return -1;
            }
            else {
                isCancelled = cancelMovieTickets(customerID,movieID,old_movieName,numberOfTickets, true);
                if(isCancelled.equals("Success")){
                    isBooked = bookMovieTickets(customerID,new_movieID,new_movieName,numberOfTickets, true);
                    if(isBooked.equals("Success")){
                        logInfo.logger.info("Exchange of tickets successful. Transaction complete.");
                        return "Success";
//                        return 0;
                    }
                    else {
                        String res = bookMovieTickets(customerID,movieID,old_movieName,numberOfTickets, true);
                        if(res.equals("Success")){
                            System.out.println("Could not book ticket. Transaction reversed.");
                            logInfo.logger.info("Could not book ticket. Transaction reversed.");
                            return "Failure";
//                        return -1;
                        }
                        return "Failure";
//                        return -1;
                    }
                }
                logInfo.logger.info("Could not cancel previous ticket. Transaction failed.");
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


    public ArrayList<String> mapFunction(String functionNameWithParameters, Log logInfo) {
        ArrayList<String> result = new ArrayList<>();

        try {
            if(functionNameWithParameters.contains("listMovieShowsAvailability")) {
                String [] params = functionNameWithParameters.split("-");

                result = new ArrayList<String>(Arrays.asList(listMovieShowsAvailability(params[1].trim(), false)));
            } else if(functionNameWithParameters.contains("getBookingSchedule")) {
                String [] params = functionNameWithParameters.split("-");

                result = new ArrayList<String>(Arrays.asList(getBookingSchedule(params[1].trim(), false)));
            } else if(functionNameWithParameters.contains("bookMovieTickets")) {
                String [] params = functionNameWithParameters.split("-");

                String bookedTickets = bookMovieTickets(params[1].trim(), params[2].trim(), params[3].trim(), Integer.parseInt(params[4].trim()), false);
                result.add(bookedTickets);
            } else if(functionNameWithParameters.contains("cancelMovieTickets")){
                String [] params = functionNameWithParameters.split("-");

                String cancelTickets = cancelMovieTickets(params[1].trim(), params[2].trim(), params[3].trim(), Integer.parseInt(params[4].trim()), false);
                result.add(cancelTickets);
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Exception" + e);
        }
        return result;
    };

}
