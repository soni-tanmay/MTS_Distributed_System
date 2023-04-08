package Replica1;

import java.util.ArrayList;

public class MovieData {
    public MovieData(int capacity) {
        this.capacity = capacity;
        this.customers = new ArrayList<>();
    }

    public MovieData(int capacity, String customerID) {
        this.capacity = capacity;
        this.customers = new ArrayList<>();
        this.customers.add(customerID);
    }

    int capacity;
    public ArrayList<String> customers;
}