package Server;

import java.util.*;
import com.google.gson.Gson;

import Shared.Request;

public class RequestManager {
    String request;
    Request deserialized;

    public RequestManager() {

    }

    public void setRequest(String Request) {
        this.request = Request;

        Gson gson = new Gson();
        this.deserialized = gson.fromJson(request, Request.class);
    }

    public HashMap<String, Object> getValues() {
        return this.deserialized.getValues();
    }

    public String getOperation() {
        return this.deserialized.getOperation();
    }
}
