package Replica3;

import java.util.ArrayList;

class ServerData {

    ArrayList<String> clientIDList;
    int capacity;

    ServerData(ArrayList<String> clientIDList, int capacity) {
        this.clientIDList= clientIDList;
        this.capacity= capacity;
    }

    ServerData(int capacity) {
        this.capacity= capacity;
    }
}
