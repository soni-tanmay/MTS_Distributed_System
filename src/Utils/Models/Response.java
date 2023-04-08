package Utils.Models;

import java.util.ArrayList;

public class Response {
    public int StatusCode;
    public ArrayList<String> body;

    public Response(int StatusCode, ArrayList<String> body) {
        this.StatusCode = StatusCode;
        this.body = body;
    }
}
