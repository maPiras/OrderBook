package Shared;

import java.util.*;

public class Request {
    private String operation;
    private HashMap<String, Object> values;

    public Request(String operation, HashMap<String, Object> values) {
        this.operation = operation;
        this.values = values;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public HashMap<String, Object> getValues() {
        return values;
    }

    public void setValues(HashMap<String, Object> values) {
        this.values = values;
    }
}