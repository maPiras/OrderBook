package Client;

import com.google.gson.Gson;

import Shared.OperationResponse;
import Shared.OrderResponse;

public class ResponseManager {
    String response;
    String type;

    public ResponseManager() {

    }

    public void setResponse(String Type, String Response) {
        this.response = Response;
        this.type = Type;
    }

    public int ManageResponse() {
        Gson gson = new Gson();
        int response_code;

        if (type.equals("orderResponse")) {
            OrderResponse order_response = gson.fromJson(response, OrderResponse.class);

            response_code = order_response.getCode();

            if (response_code > 0)
                System.out.printf("Ordine eseguito (ID %d)\n", response_code);
            else
                System.out.printf("Ordine non eseguito (-1)");

            return response_code;

        } else {
            OperationResponse operation_response = gson.fromJson(response, OperationResponse.class);
            System.out.println(operation_response.getMessage());

            return operation_response.getResponse();
        }
    }
}
