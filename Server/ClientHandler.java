package Server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;

import Shared.*;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;

    ConcurrentHashMap<String, Tuple> usersDatabase;

    OrderBook orderBook;
    OrderManager orderManager;
    UserManager userManager;

    public ClientHandler(Socket clientSocket, MainServer server, ConcurrentHashMap<String, Tuple> usersDatabase,
            OrderBook orderBook,
            NotificationSender notificationsender, UserManager userManager, OrderManager orderManager) {

        this.clientSocket = clientSocket;
        this.usersDatabase = usersDatabase;
        this.orderBook = orderBook;
        this.orderManager = orderManager;
        this.userManager = userManager;
    }

    @Override
    public void run() {
        String user = null;

        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            RequestManager requestManager = new RequestManager();
            ResponseMaker responseMaker = new ResponseMaker();

            String request_json;
            HashMap<String, Object> values;

            String operation;

            do {

                request_json = in.readLine();
                requestManager.setRequest(request_json);
                operation = requestManager.getOperation();
                values = requestManager.getValues();

                switch (operation) {
                    case "register":

                        String username = (String) values.get("username");
                        String password = (String) values.get("password");

                        if (password.length() < 5) {
                            responseMaker.setCode("register", 101); // Password troppo corta
                            out.println(responseMaker.getResponse());
                            break;
                        }

                        Tuple newTuple = new Tuple(password, false, null, 0);
                        Tuple existing = usersDatabase.putIfAbsent(username, newTuple);

                        if (existing != null) {
                            responseMaker.setCode("register", 102); // Username già registrato
                        } else {
                            responseMaker.setCode("register", 100);
                            userManager.send(new User(username, password));
                            System.out.println("User registered: " + username);
                        }

                        out.println(responseMaker.getResponse());
                        break;

                    case "login":

                        username = (String) values.get("username");
                        password = (String) values.get("password");

                        Tuple userTuple = usersDatabase.get(username);

                        if (userTuple == null || !userTuple.getPassword().equals(password)) {
                            responseMaker.setCode("login", 101); // Credenziali errate
                            out.println(responseMaker.getResponse());
                            break;
                        }

                        synchronized (userTuple) {
                            if (userTuple.getStatus()) {
                                responseMaker.setCode("login", 102); // Utente già loggato
                                out.println(responseMaker.getResponse());
                                break;
                            }

                            userTuple.setStatus(true);
                            userTuple.setAddress(clientSocket.getInetAddress());

                            responseMaker.setCode("login", 100);
                            out.println(responseMaker.getResponse());
                            System.out
                                    .println("User " + username + ": logged in from " + clientSocket.getInetAddress());

                            Gson gson = new Gson();
                            int user_port = gson.fromJson(in.readLine(), NotifyPort.class).getPort();
                            userTuple.setPort(user_port);
                        }

                        Order order;
                        user = (String) values.get("username");

                        do {
                            String req = in.readLine();

                            requestManager.setRequest(req);

                            if (!requestManager.getOperation().equals("logout"))
                                values = requestManager.getValues();

                            int size;
                            int price;
                            boolean isBid;

                            switch (requestManager.getOperation()) {
                                case "insertLimitOrder":

                                    System.out.println("User " + user + ": limit order");

                                    size = ((Double) values.get("size")).intValue();
                                    price = ((Double) values.get("price")).intValue();
                                    isBid = values.get("type").equals("bid");
                                    order = new Order(OrderBook.generateId(), user, price, size, isBid, "LIMIT",
                                            false, System.currentTimeMillis());
                                    orderBook.addOrder(order);

                                    responseMaker.setCode(order.getOrderId());
                                    out.println(responseMaker.getResponse());

                                    break;
                                case "insertMarketOrder":

                                    size = ((Double) values.get("size")).intValue();
                                    isBid = values.get("type").equals("bid");
                                    order = new Order(OrderBook.generateId(), user, 0, size, isBid, "MARKET", false,
                                            System.currentTimeMillis());

                                    if (orderBook.addOrder(order) < 0) {
                                        responseMaker.setCode(-1);
                                        System.out.println(
                                                "User " + user + ": market order (failed)");
                                    } else {
                                        responseMaker.setCode(order.getOrderId());

                                        System.out.println(
                                                "User " + user + ": market order (succeded)");
                                    }

                                    out.println(responseMaker.getResponse());

                                    break;

                                case "insertStopOrder":

                                    System.out.println("Utente " + user + ": stop order");

                                    size = ((Double) values.get("size")).intValue();
                                    price = ((Double) values.get("price")).intValue();
                                    isBid = values.get("type").equals("bid");
                                    order = new Order(OrderBook.generateId(), user, price, size, isBid, "STOP",
                                            true, System.currentTimeMillis());
                                    orderBook.addStopOrder(order);
                                    System.out.println("User " + user + ": stop order");

                                    responseMaker.setCode(order.getOrderId());
                                    out.println(responseMaker.getResponse());

                                    break;

                                case "cancelOrder":

                                    Integer orderID = ((Double) values.get("orderId")).intValue();

                                    if (orderBook.removeOrderById(orderID, user)) {
                                        responseMaker.setCode("cancelOrder", 100);
                                        System.out.println("User " + user + " deletes order " + orderID + "(succeded)");
                                    } else {
                                        responseMaker.setCode("cancelOrder", 101);
                                        System.out.println("User " + user + " deletes order " + orderID + "(failed)");
                                    }
                                    out.println(responseMaker.getResponse());

                                    break;

                                case "getPriceHistory":
                                    System.out.println("User " + user + ": price history");

                                    StringBuilder priceHistory = new StringBuilder();

                                    LinkedList<Order> ordersOfMonth = orderManager
                                            .getOrdersOfMonth((String) values.get("month"));

                                    if (ordersOfMonth == null) {
                                        responseMaker.setCode("getPriceHistory", 101);
                                        out.println(responseMaker.getResponse());
                                    } else if (ordersOfMonth.size() == 0) {
                                        responseMaker.setCode("getPriceHistory", 102);
                                        out.println(responseMaker.getResponse());
                                    } else {

                                        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                                        long timestamp;

                                        int min = Integer.MAX_VALUE;
                                        int max = Integer.MIN_VALUE;
                                        int apertura = 0;
                                        int chiusura = 0;

                                        int prec = 1;
                                        int current_day = 1;
                                        Boolean change = false;

                                        for (Order ordine : ordersOfMonth) {
                                            timestamp = ordine.getTime() * 1000;
                                            cal.setTimeInMillis(timestamp);
                                            current_day = cal.get(Calendar.DAY_OF_MONTH);
                                            change = (prec == current_day) ? false : true;

                                            if (!change) {
                                                if (ordine.getPrice() > max)
                                                    max = ordine.getPrice();
                                                if (ordine.getPrice() < min)
                                                    min = ordine.getPrice();
                                                chiusura = ordine.getPrice();
                                            } else {
                                                if (current_day > prec) {
                                                    priceHistory.append("Day " + prec + ": open=" + apertura
                                                            + " close=" + chiusura + " max=" + max + " min="
                                                            + min + "\n");
                                                }

                                                max = ordine.getPrice();
                                                min = ordine.getPrice();
                                                apertura = ordine.getPrice();
                                            }

                                            prec = current_day;
                                        }

                                        responseMaker.setCode("getPriceHistory", 100);
                                        out.println(responseMaker.getResponse());

                                        out.println(priceHistory);
                                    }

                                    break;

                                case "showBook":
                                    System.out.printf("User %s: show book\n", user);

                                    Pair<String, Integer> book_output = orderBook.displayBook();

                                    out.println(book_output.getSecond());
                                    out.println(book_output.getFirst());

                                    break;

                                case "showStops":
                                    System.out.println("User " + user + ": show stops");

                                    Pair<String, Integer> stops_output = orderBook.displayStopBook(user);

                                    out.println(stops_output.getSecond());
                                    out.println(stops_output.getFirst());

                                    break;

                                case "logout":

                                    System.out.println("User " + user + ": logout");
                                    synchronized (usersDatabase) {
                                        usersDatabase.get((String) values.get("username")).setStatus(false);
                                    }

                                    break;

                                default:
                                    break;
                            }

                        } while (!requestManager.getOperation().equals("logout"));

                        break;

                    case "leave":
                        break;
                }

            } while (!operation.equals("leave"));

        } catch (IOException e) {
            System.err.println("User " + user + ": handling error: " + e.getMessage());
        } finally {
            try {
                usersDatabase.get(user).setStatus(false);
                clientSocket.close();
                System.out.println("Connection closed.");
            } catch (IOException e) {
                System.out.println(user + ": Error closing connection: " + e.getMessage());
            }
        }
    }
}