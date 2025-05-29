package Server;

import java.io.*;
import java.util.*;

import com.google.gson.Gson;

import Shared.*;

public class OrderManager {
    OrderBook orderBook;

    FileWriter output;
    FileWriter book;

    Boolean empty_output, empty_book;

    
    public OrderManager(OrderBook orderBook) {
        this.orderBook = orderBook;
        this.empty_output = true;
        this.empty_book = true;

        String orders_filename = "orders_" + System.currentTimeMillis() + ".json";

        try {

            // Opening files and forcing json syntax

            File orders = new File(orders_filename);
            if (!orders.exists()) {
                orders.createNewFile();
            }

            File bookout = new File("current_book.json");
            if (!bookout.exists()) {
                bookout.createNewFile();
            }

            this.output = new FileWriter(orders_filename, true);
            this.book = new FileWriter("current_book.json", true);

            output.write("{\n\"orders\": ["); 
            output.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized public void sendOrder(Order o) {
        Gson gson = new Gson();

        // logging an order forcing json syntax

        try {
            if (empty_output) {
                output.write("\n" + gson.toJson(o));
                empty_output = false;
            } else {
                output.write("\n," + gson.toJson(o));
            }
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendBook(Order o) {
        Gson gson = new Gson();

        // persisting boox forcing json syntax

        try {
            if (empty_book) {
                book.write("{\n\"book\": [");
                book.flush();
                book.write("\n" + gson.toJson(o));
                empty_book = false;
            } else {
                book.write("\n," + gson.toJson(o));
            }
            book.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void retrieveBook() throws IOException {

        // recovering the book from the file
        // and adding the orders to the order book
        // forcing json syntax

        BufferedReader reader = null;
        Gson gson = new Gson();

        reader = new BufferedReader(new FileReader("current_book.json"));

        String line = reader.readLine();
        line = reader.readLine();
        line = reader.readLine();
        int n = 0;

        while (line != null) {
            if (line.equals("]}"))
                break;

            Order u;

            if (n == 0)
                u = gson.fromJson(line, Order.class);
            else
                u = gson.fromJson(line.substring(1), Order.class);

            if (u.isStop()) {
                orderBook.addStopOrder(u);
            } else {
                orderBook.addOrder(u);
            }

            line = reader.readLine();
            n++;
        }

        System.out.println("Book recovered: " + n + " orders.");
        this.book = new FileWriter("current_book.json", false);
        reader.close();
    }

    private String translateMonth(String month) {
        switch (month) {
            case "JAN":
                return "0";
            case "FEB":
                return "1";
            case "MAR":
                return "2";
            case "APR":
                return "3";
            case "MAY":
                return "4";
            case "JUN":
                return "5";
            case "JUL":
                return "6";
            case "AUG":
                return "7";
            case "SEP":
                return "8";
            case "OCT":
                return "9";
            case "NOV":
                return "10";
            case "DEC":
                return "11";
            default:
                return "-1";
        }
    }

    public LinkedList<Order> getOrdersOfMonth(String month) {

        LinkedList<Order> orders = new LinkedList<>();

        BufferedReader reader;

        Gson gson = new Gson();

        if (month.length() == 7) {

            // Withdrawing orders of a specific month
            // from the file storicoOrdini.json
            // forcing json syntax

            String translated_month = translateMonth(month.substring(0, month.length() - 4))
                    + month.substring(month.length() - 4, month.length());

            if (!translateMonth(month.substring(0, month.length() - 4)).equals("-1")) {

                try {
                    reader = new BufferedReader(new FileReader("Misc/storicoOrdini.json"));
                    String line = reader.readLine();

                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

                    line = reader.readLine();
                    line = reader.readLine();

                    while (line != null) {
                        line = reader.readLine();
                        if (line.equals("]}"))
                            break;

                        Order u = gson.fromJson(line.substring(1), Order.class);

                        long timestamp = u.getTime() * 1000;
                        cal.setTimeInMillis(timestamp);

                        String order_data = String.valueOf(cal.get(Calendar.MONTH))
                                + String.valueOf(cal.get(Calendar.YEAR));

                        if (order_data.equals(translated_month)) {
                            orders.add(u);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();

                }

                return orders;
            }
        }
        return null;
    }

    public void close() {

        try {
            output.write("\n]}");
            output.flush();
            book.write("\n]}");
            book.flush();
            output.close();
            book.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}