package Server;

import com.google.gson.Gson;

import Shared.OrderResponse;
import Shared.OperationResponse;

public class ResponseMaker {
    OrderResponse order_response;
    OperationResponse operation_response;
    boolean isOperationResponse;

    public ResponseMaker() {
        order_response = null;
        operation_response = null;
        isOperationResponse = false;
    }

    public void setCode(Integer Code) {
        isOperationResponse = false;
        order_response = new OrderResponse(Code);
    }

    public void setCode(String Operation, Integer Code) {
        isOperationResponse = true;
        operation_response = new OperationResponse(Code, Operation);
    }

    public String getResponse() {
        Gson gson = new Gson();
        if (isOperationResponse) {
            return gson.toJson(operation_response);
        }
        return gson.toJson(order_response);
    }

}
