package Replica3;

import Utils.Log;

import javax.jws.WebService;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebService(endpointInterface="Replica3.ServerInterface")
//@SOAPBinding(style= Style.RPC)
public class ServerImplementation implements ServerInterface{

    static ConcurrentHashMap<String, ArrayList<ClientData>> clientDataList = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, ConcurrentHashMap<String, ServerData>> serverDataList = new ConcurrentHashMap<>();
    String serverName;
    Log myLogger;
    int atwverPort = 7201;
    int atwoutPort = 7202;
    int veratwPort = 7203;
    int veroutPort = 7204;
    int outatwPort = 7205;
    int outverPort = 7206;

    public ServerImplementation(String serverName, Log myLogger) {
        super();
        this.serverName=serverName;
        this.myLogger = myLogger;
    }

    @Override
    public String addMovieSlots(String movieID, String movieName, int bookingCapacity) {
        try{
            if (!movieID.contains(serverName) || (movieID.charAt(3)!='M' && movieID.charAt(3)!='A' && movieID.charAt(3)!='E')){
                System.out.println("addMovieSlots: Unauthorised movie id");
                myLogger.logger.info(LocalDateTime.now() + " | addMovieSlots | " + movieID + "_" + movieName + "_" + bookingCapacity + " | completed | " + "Unauthorised movie id");
//                return "Unauthorised movie id";
                return "Failure";
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
            LocalDate todayDate = LocalDate.now();
            LocalDate weekEndDate = LocalDate.now().plusDays(7);
            LocalDate movieDate = LocalDate.parse(movieID.substring(4),formatter);
            if(!movieDate.isAfter(todayDate) || movieDate.isAfter(weekEndDate)){
                System.out.println("addMovieSlots: Invalid movie date");
                myLogger.logger.info(LocalDateTime.now() + " | addMovieSlots | " + movieID + "_" + movieName + "_" + bookingCapacity + " | completed | " + "Invalid movie date");
//                return "Invalid movie date";
                return "Failure";
            }
            if(serverDataList.get(movieName)==null){
                serverDataList.put(movieName, new ConcurrentHashMap<String, ServerData>() {
                    {
                        put(movieID, new ServerData(bookingCapacity));
                    }
                });
            }else if(serverDataList.get(movieName).get(movieID)==null){
                serverDataList.get(movieName).put(movieID,new ServerData(bookingCapacity));
            }else{
                serverDataList.get(movieName).get(movieID).capacity+=bookingCapacity;
            }
            System.out.println("addMovieSlots: Successfully added");
            myLogger.logger.info(LocalDateTime.now() + " | addMovieSlots | " + movieID + "_" + movieName + "_" + bookingCapacity + " | completed | " + "Successfully added");
//            return "Successfully added";
            return "Success";
        }catch (Exception e){
            System.out.println("addMovieSlots: "+ e);
            myLogger.logger.info(LocalDateTime.now() + " | addMovieSlots | " + movieID + "_" + movieName + "_" + bookingCapacity + " | completed | " + e);
//            return e.toString();
            return "Failure";
        }
    }

    @Override
    public String removeMovieSlots(String movieID, String movieName, boolean isClientCall) {
        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
            LocalDate todayDate = LocalDate.now();
            LocalDate movieDate = LocalDate.parse(movieID.substring(4),formatter);
            if(!movieID.contains(serverName) || serverDataList.get(movieName)==null || serverDataList.get(movieName).isEmpty() || serverDataList.get(movieName).get(movieID)== null|| !movieDate.isAfter(todayDate)){
                System.out.println("removeMovieSlots: Unauthorised request");
                myLogger.logger.info(LocalDateTime.now() + " | removeMovieSlots | " + movieID + "_" + movieName + " | completed | " + "Unauthorised request");
//                return "Unauthorised request";
                return "Failure";
            }

            if(serverDataList.get(movieName).get(movieID).clientIDList == null || serverDataList.get(movieName).get(movieID).clientIDList.isEmpty() || serverDataList.get(movieName).get(movieID).clientIDList.size()==0){
                serverDataList.get(movieName).remove(movieID);
            }else {
                for (String e : serverDataList.get(movieName).get(movieID).clientIDList){
                    for (ClientData c : clientDataList.get(e)){
                        if(c.movieID.equals(movieID)){
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
                                String result = bookMovieTickets(e,nextMovieSlot,movieName,c.tickets);
                                if(result.equals("Successfully booked")){
                                    break;
                                }
                            }
                            clientDataList.get(e).remove(c);
                            break;
                        }
                    }
                }
                serverDataList.get(movieName).remove(movieID);
            }
            System.out.println("removeMovieSlots: Removed slot successfully");
            myLogger.logger.info(LocalDateTime.now() + " | removeMovieSlots | " + movieID + "_" + movieName + " | completed | " + "Removed slot successfully");
//            return "Removed slot successfully";
            return "Success";
        }catch (Exception e){
            System.out.println("removeMovieSlots: "+e);
            myLogger.logger.info(LocalDateTime.now() + " | removeMovieSlots | " + movieID + "_" + movieName + " | failed | " + e);
//            return e.toString();
            return "Failure";
        }
    }

    @Override
    public String listMovieShowsAvailability(String movieName, boolean isClientCall) {
        try{
            ArrayList<String> movieAvailabilityList = new ArrayList<>();
            if(serverDataList.get(movieName.trim())!=null && !serverDataList.get(movieName.trim()).isEmpty())
                for(Map.Entry<String, ServerData> e: serverDataList.get(movieName.trim()).entrySet()){
                    movieAvailabilityList.add(e.getKey()+" "+e.getValue().capacity);
                }
            if (!!isClientCall) {
                ArrayList<Integer> portList = getPortList(serverName);
                DatagramSocket datagramSocket1 = new DatagramSocket();
                DatagramSocket datagramSocket2 = new DatagramSocket();
                byte[] b = ("listMovieShowsAvailability_"+movieName).getBytes();
                InetAddress inetAddress = InetAddress.getLocalHost();
                DatagramPacket datagramPacket1 = new DatagramPacket(b,b.length,inetAddress,portList.get(0));
                DatagramPacket datagramPacket2 = new DatagramPacket(b,b.length,inetAddress,portList.get(1));
                datagramSocket1.send(datagramPacket1);
                datagramSocket2.send(datagramPacket2);

                byte[] b1=new byte[1024];
                byte[] b2=new byte[1024];
                DatagramPacket datagramPacket11=new DatagramPacket(b1,b1.length);
                DatagramPacket datagramPacket22=new DatagramPacket(b2,b2.length);
                datagramSocket1.receive(datagramPacket11);
                datagramSocket2.receive(datagramPacket22);

                String str1 = new String(datagramPacket11.getData());
                String str2 = new String(datagramPacket22.getData());

                if(!str1.trim().isEmpty())
                    for (String s : str1.trim().split("_")){
                        if (!s.trim().isEmpty())
                            movieAvailabilityList.add(s.trim());
                    }

                if(!str2.trim().isEmpty())
                    for (String s : str2.trim().split("_")){
                        if (!s.trim().isEmpty())
                            movieAvailabilityList.add(s.trim());
                    }

                datagramSocket1.close();
                datagramSocket2.close();
            }
            Collections.sort(movieAvailabilityList);
            System.out.println("listMovieShowsAvailability: "+ movieAvailabilityList);
            myLogger.logger.info(LocalDateTime.now() + " | listMovieShowsAvailability | " + movieName + " | completed | " + movieAvailabilityList);

            return String.join("_",movieAvailabilityList);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("listMovieShowsAvailability "+e);
            myLogger.logger.info(LocalDateTime.now() + " | listMovieShowsAvailability | " + movieName + " | failed | " + e);
//            return e.toString();
            return "Failure";
        }
    }

    @Override
    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        try{
            ArrayList<String> bookingSchedule = new ArrayList<>();

            if (!getBookingSchedule(customerID,true).trim().isEmpty())
                for (String s : getBookingSchedule(customerID,true).split("_")){
                    if (!s.trim().isEmpty())
                        bookingSchedule.add(s.trim());
                }

            if(movieID.startsWith(serverName)){

                if(serverDataList.get(movieName)==null || serverDataList.get(movieName).isEmpty() || serverDataList.get(movieName).get(movieID)==null){
                    System.out.println("bookMovieTickets: Movie does not exist");
                    myLogger.logger.info(LocalDateTime.now() + " | bookMovieTickets | " + customerID + "_" + movieID + "_" + movieName + "_" + numberOfTickets + " | completed | " + "Movie does not exist");
//                    return "Movie does not exist";
                    return "Failure";
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
                LocalDate todayDate = LocalDate.now();
                LocalDate movieDate = LocalDate.parse(movieID.substring(4),formatter);

                if (!movieDate.isAfter(todayDate)){
                    System.out.println("bookMovieTickets: Cannot book past movie slots");
                    myLogger.logger.info(LocalDateTime.now() + " | bookMovieTickets | " + customerID + "_" + movieID + "_" + movieName + "_" + numberOfTickets + " | completed | " + "Cannot book past movie slots");
//                    return "Cannot book past movie slots";
                    return "Failure";
                }

                if(serverDataList.get(movieName).get(movieID).capacity < numberOfTickets){
                    System.out.println("bookMovieTickets: Not enough tickets available");
                    myLogger.logger.info(LocalDateTime.now() + " | bookMovieTickets | " + customerID + "_" + movieID + "_" + movieName + "_" + numberOfTickets + " | completed | " + "Not enough tickets available");
//                    return "Not enough tickets available";
                    return "Failure";
                }

                for (String e : bookingSchedule){
                    String[] data = e.split(" ");
                    if(movieName.equals(data[1].trim()) && movieID.substring(3).equals(data[0].trim().substring(3))){
                        System.out.println("bookMovieTickets: Already booked for the same slot");
                        myLogger.logger.info(LocalDateTime.now() + " | bookMovieTickets | " + customerID + "_" + movieID + "_" + movieName + "_" + numberOfTickets + " | completed | " + "Already booked for the same slot");
//                        return "Already booked for the same slot";
                        return "Failure";
                    }
                }
                serverDataList.get(movieName).get(movieID).capacity -= numberOfTickets;
                if(serverDataList.get(movieName).get(movieID).clientIDList==null){
                    serverDataList.get(movieName).get(movieID).clientIDList = new ArrayList<>();
                }
                if(!serverDataList.get(movieName).get(movieID).clientIDList.contains(customerID.trim())){
                    serverDataList.get(movieName).get(movieID).clientIDList.add(customerID);
                }
                if (clientDataList.isEmpty() || clientDataList.get(customerID.trim())==null || clientDataList.get(customerID.trim()).isEmpty()){
                    clientDataList.put(customerID,new ArrayList<>());
                    clientDataList.get(customerID.trim()).add(new ClientData(movieName,movieID,numberOfTickets));
                    System.out.println("bookMovieTickets: Successfully booked");
                    myLogger.logger.info(LocalDateTime.now() + " | bookMovieTickets | " + customerID + "_" + movieID + "_" + movieName + "_" + numberOfTickets + " | completed | " + "Successfully booked");
//                    return "Successfully booked";
                    return "Success";
                }
                for (ClientData e : clientDataList.get(customerID.trim())){
                    if(e.movieID.equals(movieID) && e.movieName.equals(movieName)){
                        e.tickets += numberOfTickets;
                        System.out.println("bookMovieTickets: Successfully booked");
                        myLogger.logger.info(LocalDateTime.now() + " | bookMovieTickets | " + customerID + "_" + movieID + "_" + movieName + "_" + numberOfTickets + " | completed | " + "Successfully booked");
//                        return "Successfully booked";
                        return "Success";
                    }
                }
                clientDataList.get(customerID.trim()).add(new ClientData(movieName,movieID,numberOfTickets));
                System.out.println("bookMovieTickets: Successfully booked");
                myLogger.logger.info(LocalDateTime.now() + " | bookMovieTickets | " + customerID + "_" + movieID + "_" + movieName + "_" + numberOfTickets + " | completed | " + "Successfully booked");
//                return "Successfully booked";
                return "Success";
            }else {
                int count =0;

                for (String e : bookingSchedule){
                    String[] data = e.split(" ");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
                    LocalDate alreadyBookedMovieDate = LocalDate.parse(data[0].trim().substring(4),formatter);
                    LocalDate movieDate = LocalDate.parse(movieID.substring(4),formatter);
                    LocalDate previousWeekEndDate = movieDate.minusDays(7);
                    if(alreadyBookedMovieDate.isAfter(previousWeekEndDate) && !data[0].contains(serverName) ){
                        count+=Integer.parseInt(data[2]);
                    }
                }

                if ((numberOfTickets+count)>3){
                    System.out.println("bookMovieTickets: Cannot book more than 3 tickets");
                    myLogger.logger.info(LocalDateTime.now() + " | bookMovieTickets | " + customerID + "_" + movieID + "_" + movieName + "_" + numberOfTickets + " | completed | " + "Cannot book more than 3 tickets");
//                    return "Cannot book more than 3 tickets";
                    return "Failure";
                }

                DatagramSocket datagramSocket1 = new DatagramSocket();
                byte[] b = ("bookMovieTickets_"+customerID+"_"+movieID+"_"+movieName+"_"+numberOfTickets).getBytes();
                InetAddress inetAddress=InetAddress.getLocalHost();
                int port = getSpecificPort(serverName+movieID.substring(0,3));
                DatagramPacket datagramPacket1 = new DatagramPacket(b,b.length,inetAddress,port);
                datagramSocket1.send(datagramPacket1);

                byte[] b1=new byte[1024];
                DatagramPacket datagramPacket2=new DatagramPacket(b1,b1.length);
                datagramSocket1.receive(datagramPacket2);
                String str=new String(datagramPacket2.getData());
                datagramSocket1.close();

                System.out.println("bookMovieTickets: "+str.trim());
                myLogger.logger.info(LocalDateTime.now() + " | bookMovieTickets | " + customerID + "_" + movieID + "_" + movieName + "_" + numberOfTickets + " | completed | " + str.trim());
                return str.trim();
            }
        }catch (Exception e){
            System.out.println("bookMovieTickets: "+ e);
            myLogger.logger.info(LocalDateTime.now() + " | bookMovieTickets | " + customerID + "_" + movieID + "_" + movieName + "_" + numberOfTickets + " | failed | " + e);
//            return e.toString();
            return "Failure";
        }
    }

    @Override
    public String getBookingSchedule(String customerID, boolean isClientCall) {
        try{
            ArrayList<String> bookingSchedule = new ArrayList<>();
            if(clientDataList!=null && !clientDataList.isEmpty() && clientDataList.get(customerID.trim())!=null && !clientDataList.get(customerID.trim()).isEmpty()) {
                for (ClientData e : clientDataList.get(customerID.trim())) {
                    bookingSchedule.add(e.movieID + " " + e.movieName + " " + e.tickets);
                }
            }
            if(!!isClientCall){
                ArrayList<Integer> portList = getPortList(serverName);
                DatagramSocket datagramSocket1 = new DatagramSocket();
                DatagramSocket datagramSocket2 = new DatagramSocket();
                byte[] b = ("getBookingSchedule_"+customerID).getBytes();
                InetAddress inetAddress=InetAddress.getLocalHost();
                DatagramPacket datagramPacket1 = new DatagramPacket(b,b.length,inetAddress,portList.get(0));
                DatagramPacket datagramPacket2 = new DatagramPacket(b,b.length,inetAddress,portList.get(1));
                datagramSocket1.send(datagramPacket1);
                datagramSocket2.send(datagramPacket2);

                byte[] b1=new byte[1024];
                byte[] b2=new byte[1024];
                DatagramPacket datagramPacket11=new DatagramPacket(b1,b1.length);
                DatagramPacket datagramPacket22=new DatagramPacket(b2,b2.length);
                datagramSocket1.receive(datagramPacket11);
                datagramSocket2.receive(datagramPacket22);

                String str1 = new String(datagramPacket11.getData());
                String str2 = new String(datagramPacket22.getData());

                if(!str1.trim().isEmpty())
                    for (String s : str1.trim().split("_")){
                        if(!s.trim().isEmpty())
                            bookingSchedule.add(s.trim());
                    }

                if(!str2.trim().isEmpty())
                    for (String s : str2.trim().split("_")){
                        if(!s.trim().isEmpty())
                            bookingSchedule.add(s.trim());
                    }

                datagramSocket1.close();
                datagramSocket2.close();
            }

            System.out.println("getBookingSchedule: "+ bookingSchedule);
            myLogger.logger.info(LocalDateTime.now() + " | getBookingSchedule | " + customerID + " | completed | " + bookingSchedule);

            return String.join("_",bookingSchedule);
        }catch (Exception e){
            System.out.println("getBookingSchedule: "+ e);
            myLogger.logger.info(LocalDateTime.now() + " | getBookingSchedule | " + customerID + " | failed | " + e);
//            return e.toString();
            return "Failure";
        }
    }

    @Override
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        try {
            if (movieID.contains(serverName)){
                if(clientDataList.get(customerID)!=null && !clientDataList.get(customerID).isEmpty()){
                    for (ClientData e : clientDataList.get(customerID)){
                        if (e.movieID.equals(movieID) && e.movieName.equals(movieName)){
                            if (numberOfTickets == e.tickets){
                                serverDataList.get(e.movieName).get(e.movieID).capacity +=numberOfTickets;
                                clientDataList.get(customerID).remove(e);
                                serverDataList.get(e.movieName).get(e.movieID).clientIDList.remove(customerID);
                                System.out.println("cancelMovieTickets: Cancelled");
                                myLogger.logger.info(LocalDateTime.now() + " | cancelMovieTickets | " + customerID + "_" + movieID + "_" + movieName + "_" + numberOfTickets + " | completed | " + "Cancelled");
//                                return "Cancelled";
                                return "Success";
                            }else if(numberOfTickets<e.tickets){
                                serverDataList.get(e.movieName).get(e.movieID).capacity +=numberOfTickets;
                                e.tickets -= numberOfTickets;
                                System.out.println("cancelMovieTickets: Cancelled");
                                myLogger.logger.info(LocalDateTime.now() + " | cancelMovieTickets | " + customerID + "_" + movieID + "_" + movieName + "_" + numberOfTickets + " | completed | " + "Cancelled");
//                                return "Cancelled";
                                return "Success";
                            }else {
                                System.out.println("cancelMovieTickets: Invalid number of tickets");
                                myLogger.logger.info(LocalDateTime.now() + " | cancelMovieTickets | " + customerID + "_" + movieID + "_" + movieName + "_" + numberOfTickets + " | completed | " + "Invalid number of tickets");
//                                return "Invalid number of tickets";
                                return "Failure";
                            }
                        }
                    }
                }
                System.out.println("cancelMovieTickets: no tickets found");
                myLogger.logger.info(LocalDateTime.now() + " | cancelMovieTickets | " + customerID + "_" + movieID + "_" + movieName + "_" + numberOfTickets + " | completed | " + "no tickets found");
//                return "no tickets found";
                return "Failure";
            }else {
                DatagramSocket datagramSocket1 = new DatagramSocket();
                byte[] b = ("cancelMovieTickets_"+customerID+"_"+movieID+"_"+movieName+"_"+numberOfTickets).getBytes();
                InetAddress inetAddress = InetAddress.getLocalHost();
                int port = getSpecificPort(serverName+movieID.substring(0,3));
                DatagramPacket datagramPacket1 = new DatagramPacket(b,b.length,inetAddress,port);
                datagramSocket1.send(datagramPacket1);

                byte[] b1=new byte[1024];
                DatagramPacket datagramPacket2=new DatagramPacket(b1,b1.length);
                datagramSocket1.receive(datagramPacket2);
                String str=new String(datagramPacket2.getData());
                datagramSocket1.close();

                System.out.println("cancelMovieTickets: "+ str.trim());
                myLogger.logger.info(LocalDateTime.now() + " | cancelMovieTickets | " + customerID + "_" + movieID + "_" + movieName + "_" + numberOfTickets + " | completed | " + str.trim());
                return str.trim();
            }
        }catch (Exception e){
            System.out.println("cancelMovieTickets: "+ e);
            myLogger.logger.info(LocalDateTime.now() + " | cancelMovieTickets | " + customerID + "_" + movieID + "_" + movieName + "_" + numberOfTickets + " | failed | " + e);
//            return null;
            return "Failure";
        }
    }

    @Override
    public String exchangeTickets(String customerID, String movieID, String old_movieName, String new_movieID, String new_movieName, int numberOfTickets) {
        try{
            String cancelable = isCancelable(customerID,movieID,old_movieName,numberOfTickets);
            String bAvailable = bookingAvailable(customerID,new_movieID,movieID,new_movieName,numberOfTickets);
            if (cancelable.equals("true") && bAvailable.equals("true")){
                String cancelTickets = cancelMovieTickets(customerID,movieID,old_movieName,numberOfTickets);
                String bookTickets = bookMovieTickets(customerID,new_movieID,new_movieName,numberOfTickets);
                if (cancelTickets.equals("Cancelled") && bookTickets.equals("Successfully booked")){
                    System.out.println("exchangeTickets: "+ "Exchange Successful");
                    myLogger.logger.info(LocalDateTime.now() + " | exchangeTickets | " + customerID + "_" + movieID + "_" + new_movieID + "_" + old_movieName + "_" + new_movieName + "_" + numberOfTickets + " | completed | " + "Exchange Successful");
//                    return "Exchange Successful";
                    return "Success";
                }else {
                    System.out.println("exchangeTickets: "+ "Unexpected error");
                    myLogger.logger.info(LocalDateTime.now() + " | exchangeTickets | " + customerID + "_" + movieID + "_" + new_movieID + "_" + old_movieName + "_" + new_movieName + "_" + numberOfTickets + " | completed | " + "Unexpected error");
//                    return "Unexpected error";
                    return "Failure";
                }
            }else {
                if (!cancelable.equals("true")) {
                    System.out.println("exchangeTickets: "+ cancelable);
                    myLogger.logger.info(LocalDateTime.now() + " | exchangeTickets | " + customerID + "_" + movieID + "_" + new_movieID + "_" + old_movieName + "_" + new_movieName + "_" + numberOfTickets + " | completed | " + cancelable);
                    return cancelable;
                }else {
                    System.out.println("exchangeTickets: "+ bAvailable);
                    myLogger.logger.info(LocalDateTime.now() + " | exchangeTickets | " + customerID + "_" + movieID + "_" + new_movieID + "_" + old_movieName + "_" + new_movieName + "_" + numberOfTickets + " | completed | " + bAvailable);
                    return bAvailable;
                }
            }
        }catch (Exception e){
            System.out.println("exchangeTickets: "+ e);
            myLogger.logger.info(LocalDateTime.now() + " | exchangeTickets | " + customerID + "_" + movieID + "_" + new_movieID + "_" + old_movieName + "_" + new_movieName + "_" + numberOfTickets + " | failed | " + e);
//            return e.toString();
            return "Failure";
        }
    }

    String isCancelable (String customerID, String movieID, String old_movieName, int numberOfTickets){
        try {
            if (movieID.contains(serverName)) {
                if (clientDataList.get(customerID) != null && !clientDataList.get(customerID).isEmpty()) {
                    for (ClientData e : clientDataList.get(customerID)) {
                        if (e.movieID.equals(movieID) && e.movieName.equals(old_movieName)) {
                            if (numberOfTickets > e.tickets) {
                                System.out.println("isCancelable: Invalid number of tickets");
//                                return "Invalid number of tickets";
                                return "Failure";
                            } else {
                                System.out.println("isCancelable: true");
                                return "true";
                            }
                        }
                    }
                }
                System.out.println("isCancelable: no tickets found");
//                return "no tickets found";
                return "Failure";
            } else {
                DatagramSocket datagramSocket1 = new DatagramSocket();
                byte[] b = ("isCancelable_" + customerID + "_" + movieID + "_" + old_movieName + "_" + numberOfTickets).getBytes();
                InetAddress inetAddress = InetAddress.getLocalHost();
                int port = getSpecificPort(serverName + movieID.substring(0, 3));
                DatagramPacket datagramPacket1 = new DatagramPacket(b, b.length, inetAddress, port);
                datagramSocket1.send(datagramPacket1);

                byte[] b1 = new byte[1024];
                DatagramPacket datagramPacket2 = new DatagramPacket(b1, b1.length);
                datagramSocket1.receive(datagramPacket2);
                String str = new String(datagramPacket2.getData());
                datagramSocket1.close();

                System.out.println("isCancelable: " + str.trim());
                return str.trim();
            }
        }catch (Exception e){
            System.out.println("isCancelable: "+ e);
//            return e.toString();
            return "Failure";
        }
    }

    String bookingAvailable (String customerID, String new_movieID, String movieID ,String new_movieName, int numberOfTickets){
        try{
            ArrayList<String> bookingSchedule = new ArrayList<>();

            if (!getBookingSchedule(customerID,true).trim().isEmpty())
                for (String s : getBookingSchedule(customerID,true).split("_")){
                    if (!s.trim().isEmpty())
                        bookingSchedule.add(s.trim());
                }

            if(new_movieID.startsWith(serverName)){
                if(serverDataList.get(new_movieName)==null || serverDataList.get(new_movieName).isEmpty() || serverDataList.get(new_movieName).get(new_movieID)==null){
                    System.out.println("bookingAvailable: Movie does not exist");
//                    return "Movie does not exist";
                    return "Failure";
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
                LocalDate todayDate = LocalDate.now();
                LocalDate movieDate = LocalDate.parse(new_movieID.substring(4),formatter);

                if (!movieDate.isAfter(todayDate)){
                    System.out.println("bookingAvailable: Cannot book past movie slots");
//                    return "Cannot book past movie slots";
                    return "Failure";
                }

                if(serverDataList.get(new_movieName).get(new_movieID).capacity < numberOfTickets){
                    System.out.println("bookingAvailable: Not enough tickets available");
//                    return "Not enough tickets available";
                    return "Failure";
                }

                for (String e : bookingSchedule){
                    String[] data = e.split(" ");
                    if(!data[0].trim().equals(movieID)) {
                        if (new_movieName.equals(data[1].trim()) && new_movieID.substring(3).equals(data[0].trim().substring(3))) {
                            System.out.println("bookingAvailable: Already booked for the same slot");
//                            return "Already booked for the same slot";
                            return "Failure";
                        }
                    }
                }

                System.out.println("bookingAvailable: true");
                return "true";
            }else {
                int count =0;

                if(movieID.startsWith(serverName)){
                    for (String e : bookingSchedule){
                        String[] data = e.split(" ");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy");
                        LocalDate alreadyBookedMovieDate = LocalDate.parse(data[0].trim().substring(4),formatter);
                        LocalDate movieDate = LocalDate.parse(new_movieID.substring(4),formatter);
                        LocalDate previousWeekEndDate = movieDate.minusDays(7);
                        if(alreadyBookedMovieDate.isAfter(previousWeekEndDate) && !data[0].contains(serverName) ){
                            count+=Integer.parseInt(data[2]);
                        }
                    }
                }

                if ((numberOfTickets+count)>3){
                    System.out.println("bookingAvailable: Cannot book more than 3 tickets");
//                    return "Cannot book more than 3 tickets";
                    return "Failure";
                }

                DatagramSocket datagramSocket1 = new DatagramSocket();
                byte[] b = ("bookingAvailable_"+customerID+"_"+new_movieID+"_"+movieID+"_"+new_movieName+"_"+numberOfTickets).getBytes();
                InetAddress inetAddress=InetAddress.getLocalHost();
                int port = getSpecificPort(serverName + new_movieID.substring(0,3));
                DatagramPacket datagramPacket1 = new DatagramPacket(b,b.length,inetAddress,port);
                datagramSocket1.send(datagramPacket1);

                byte[] b1=new byte[1024];
                DatagramPacket datagramPacket2=new DatagramPacket(b1,b1.length);
                datagramSocket1.receive(datagramPacket2);
                String str=new String(datagramPacket2.getData());
                datagramSocket1.close();

                System.out.println("bookingAvailable: "+str.trim());
                return str.trim();
            }
        }catch (Exception e){
            System.out.println("bookingAvailable: "+ e);
//            return e.toString();
            return "Failure";
        }
    }

    Integer getSpecificPort(String connectionName) {
        switch (connectionName){
            case "ATWVER":
                return atwverPort;
            case "ATWOUT":
                return atwoutPort;
            case "VERATW":
                return veratwPort;
            case "VEROUT":
                return veroutPort;
            case "OUTATW":
                return outatwPort;
            case "OUTVER":
                return outverPort;
            default:
                return null;
        }
    }

    ArrayList<Integer> getPortList(String serverName) {
        ArrayList<Integer> portNumberList = new ArrayList<>();
        switch (serverName){
            case "ATW":
                portNumberList.add(atwverPort);
                portNumberList.add(atwoutPort);
                return portNumberList;
            case "VER":
                portNumberList.add(veratwPort);
                portNumberList.add(veroutPort);
                return portNumberList;
            case "OUT":
                portNumberList.add(outatwPort);
                portNumberList.add(outverPort);
                return portNumberList;
            default:
                return null;
        }
    }

    byte[] mapMethods(String functionParameters) {
        try{
            String[] parameters = functionParameters.split("_");
            if(functionParameters.contains("listMovieShowsAvailability")){
                String result = listMovieShowsAvailability(parameters[1].trim(), false);
                System.out.println("mapMethods_listMovieShowsAvailability: " + result);
                return String.valueOf(result).getBytes();
            } else if(functionParameters.contains("getBookingSchedule")){
                String result = getBookingSchedule(parameters[1].trim(),false);
                System.out.println("mapMethods_getBookingSchedule: " + result);
                return String.valueOf(result).getBytes();
            }else if(functionParameters.contains("cancelMovieTickets")){
                String result = cancelMovieTickets(parameters[1].trim(),parameters[2].trim(),parameters[3].trim(),Integer.parseInt(parameters[4].trim()));
                System.out.println("mapMethods_cancelMovieTickets: "+ result);
                return String.valueOf(result).getBytes();
            }
            else if(functionParameters.contains("bookMovieTickets")){
                String result = bookMovieTickets(parameters[1].trim(),parameters[2].trim(),parameters[3].trim(),Integer.parseInt(parameters[4].trim()));
                System.out.println("mapMethods_bookMovieTickets: "+ result);
                return String.valueOf(result).getBytes();
            }
            else if(functionParameters.contains("isCancelable")){
                String result = isCancelable(parameters[1].trim(),parameters[2].trim(),parameters[3].trim(),Integer.parseInt(parameters[4].trim()));
                System.out.println("mapMethods_isCancelable: "+ result);
                return String.valueOf(result).getBytes();
            }
            else if(functionParameters.contains("bookingAvailable")){
                String result = bookingAvailable(parameters[1].trim(),parameters[2].trim(),parameters[3].trim(),parameters[4].trim(),Integer.parseInt(parameters[5].trim()));
                System.out.println("mapMethods_bookMovieTickets: "+ result);
                return String.valueOf(result).getBytes();
            }
        }catch (Exception e){
            System.out.println("mapMethods: "+e);
        }
        return null;
    }
}
