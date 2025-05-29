package Client;

import java.util.HashMap;
import java.util.Scanner;

import com.google.gson.Gson;

import Shared.Request;

public class RequestMaker {

    String operation;
    String username;
    String password;
    String old_password;
    String new_password;

    String type;
    Integer size;
    Integer price;
    Integer order;
    String month;

    HashMap<String, Object> values;

    Request request;

    Scanner input;

    public RequestMaker(Scanner input) {
        this.input = input;
    }

    public void setOperation(String Operation) {
        this.operation = Operation;
    }

    public void createRequest() {

        this.values = new HashMap<>();

        switch (operation) {

            case "register":

                System.out.print("Insert ID: ");
                this.username = input.nextLine();
                System.out.print("Insert password: ");
                this.password = input.nextLine();

                values.put("username", username);
                values.put("password", password);

                break;
            case "login":

                System.out.print("Inserti ID: ");
                this.username = input.nextLine();
                System.out.print("Insert password: ");
                this.password = input.nextLine();

                values.put("username", username);
                values.put("password", password);

                break;
            case "updateCredentials":

                System.out.print("Insert username: ");
                this.username = input.nextLine();
                System.out.print("Insert old password: ");
                this.old_password = input.nextLine();
                System.out.print("Insert new password: ");
                this.new_password = input.nextLine();

                values.put("username", username);
                values.put("old_password", old_password);
                values.put("new_password", new_password);

                break;

            case "Esci":
                break;

            case "logout":
                break;

            case "showBook":
                break;

            case "showStops":
                break;

            case "insertLimitOrder":

                System.out.print("Insert type (bid/ask): ");
                this.type = input.nextLine();
                System.out.print("Insert size: ");
                this.size = input.nextInt();
                System.out.print("Insert price: ");
                this.price = input.nextInt();

                values.put("type", type);
                values.put("size", size);
                values.put("price", price);

                break;
            case "insertMarketOrder":

                System.out.print("Insert type (bid/ask): ");
                this.type = input.nextLine();
                System.out.print("Insert size: ");
                this.size = input.nextInt();

                values.put("type", type);
                values.put("size", size);

                break;
            case "insertStopOrder":

                System.out.print("Order type (bid/ask): ");
                this.type = input.nextLine();
                System.out.print("Insert size: ");
                this.size = input.nextInt();
                System.out.print("Insert price: ");
                this.price = input.nextInt();

                values.put("type", type);
                values.put("size", size);
                values.put("price", price);

                break;
            case "cancelOrder":

                System.out.print("insert order ID: ");
                this.order = input.nextInt();

                values.put("orderId", order);

                break;
            case "getPriceHistory":

                System.out.print("insert month (ex. FEB2024): ");
                this.month = input.nextLine();

                values.put("month", month);

                break;

            default:
                break;

        }

        this.request = new Request(operation, values);
    }

    public String getRequest() {
        Gson gson = new Gson();
        return gson.toJson(this.request);
    }
}