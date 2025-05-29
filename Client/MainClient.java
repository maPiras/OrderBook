package Client;

import java.io.*;
import java.net.*;
import java.util.*;

import com.google.gson.Gson;

import Shared.NotifyPort;

public class MainClient {
    public static void main(String[] args) throws Exception {

        Properties config = new Properties();

        String hostname = "localhost"; // Indirizzo del server
        int port = 0; // Porta del server

        try (FileInputStream fis = new FileInputStream("Misc/client.properties")) {
            config.load(fis);
            hostname = config.getProperty("hostname");
            port = Integer.parseInt(config.getProperty("port"));
        } catch (IOException | NumberFormatException e) {
            System.err.println("Errore nel file di configurazione: " + e.getMessage());
            System.exit(1);
        }

        Scanner input = null;

        try (Socket socket = new Socket(hostname, port);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader console = new BufferedReader(new InputStreamReader(System.in))) {

            input = new Scanner(System.in);
            RequestMaker requestMaker = new RequestMaker(input);
            ResponseManager responseManager = new ResponseManager();

            NotificationReceiver receiver = new NotificationReceiver();
            Thread listener = new Thread(receiver);
            listener.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    System.out.println("\nShutting down...");
                    if (socket != null && !socket.isClosed())
                        socket.close();
                    receiver.close();
                    // Chiudi altri stream o risorse
                } catch (IOException e) {
                    System.err.println("Error during resources closure: " + e.getMessage());
                }
            }));

            System.out.println("Connected.");

            System.out.println("\n Choose and option:");
            System.out.println("1. Register (ID and password)");
            System.out.println("2. Login (ID d password)");
            System.out.println("3. Update password (ID, old, new)");
            System.out.println("4. Leave\n");

            String option;
            String logged_option;

            do {

                System.out.print("Insert option: ");
                option = console.readLine();

                switch (option) {

                    case "1":
                        requestMaker.setOperation("register");
                        requestMaker.createRequest();
                        out.println(requestMaker.getRequest());

                        responseManager.setResponse("operationResponse", in.readLine());
                        responseManager.ManageResponse();

                        break;

                    case "2":
                        requestMaker.setOperation("login");
                        requestMaker.createRequest();
                        out.println(requestMaker.getRequest());

                        String response = in.readLine();
                        if (response == null) {
                            System.out.println("Connection closed by the server.");
                            System.exit(0);
                            break;
                        }

                        responseManager.setResponse("operationResponse", response);
                        if (responseManager.ManageResponse() == 100) {

                            NotifyPort notifyPort = new NotifyPort(receiver.getPort());
                            Gson gson = new Gson();

                            out.println(gson.toJson(notifyPort));

                            System.out.println("Welcome!");

                            System.out.println("1. Insert limit order");
                            System.out.println("2. Insert market order");
                            System.out.println("3. Insert stop order");
                            System.out.println("4. Delete order");
                            System.out.println("5. Request order history");
                            System.out.println("6. Show book");
                            System.out.println("7. Show stops");
                            System.out.println("8. Show notifications");
                            System.out.println("9. Logout\n");

                            do {
                                if (receiver.getNumber() > 0)
                                    System.out.println("Received " + receiver.getNumber() + " new notifications.");

                                System.out.print("Insert option: ");
                                logged_option = console.readLine();

                                switch (logged_option) {

                                    case "1":
                                        requestMaker.setOperation("insertLimitOrder");
                                        requestMaker.createRequest();
                                        out.println(requestMaker.getRequest());

                                        responseManager.setResponse("orderResponse", in.readLine());
                                        responseManager.ManageResponse();

                                        break;

                                    case "2":
                                        requestMaker.setOperation("insertMarketOrder");
                                        requestMaker.createRequest();
                                        out.println(requestMaker.getRequest());

                                        responseManager.setResponse("orderResponse", in.readLine());
                                        responseManager.ManageResponse();
                                        break;

                                    case "3":
                                        requestMaker.setOperation("insertStopOrder");
                                        requestMaker.createRequest();
                                        out.println(requestMaker.getRequest());

                                        responseManager.setResponse("orderResponse", in.readLine());
                                        responseManager.ManageResponse();
                                        break;

                                    case "4":
                                        requestMaker.setOperation("cancelOrder");
                                        requestMaker.createRequest();
                                        out.println(requestMaker.getRequest());

                                        responseManager.setResponse("orderResponse", in.readLine());
                                        responseManager.ManageResponse();
                                        break;

                                    case "5":
                                        requestMaker.setOperation("getPriceHistory");
                                        requestMaker.createRequest();
                                        out.println(requestMaker.getRequest());

                                        responseManager.setResponse("operationResponse", in.readLine());

                                        if (responseManager.ManageResponse() == 100) {

                                            String line4;
                                            while ((line4 = in.readLine()).length() != 0) {
                                                System.out.println(line4);
                                            }
                                        }

                                        break;

                                    case "6":
                                        requestMaker.setOperation("showBook");
                                        requestMaker.createRequest();
                                        out.println(requestMaker.getRequest());

                                        String line2;

                                        while ((line2 = in.readLine()).length() != 0) {
                                            System.out.println(line2);
                                        }

                                        break;

                                    case "7":

                                        requestMaker.setOperation("showStops");
                                        requestMaker.createRequest();
                                        out.println(requestMaker.getRequest());

                                        String line3 = in.readLine();
                                        int nlines = Integer.parseInt(line3);

                                        for (int i = 0; i < nlines; i++) {

                                            line3 = in.readLine();
                                            if (line3.length() == 0)
                                                break;
                                            System.out.println(line3);

                                        }

                                        break;

                                    case "8":
                                        receiver.show();
                                        break;

                                    case "9":
                                        requestMaker.setOperation("logout");
                                        requestMaker.createRequest();
                                        out.println(requestMaker.getRequest());

                                        System.exit(0);
                                }

                            } while (!logged_option.equals("9"));

                        }

                        break;

                    case "3":
                        requestMaker.setOperation("updateCredentials");
                        requestMaker.createRequest();
                        out.println(requestMaker.getRequest());

                        responseManager.setResponse("operationResponse", in.readLine());
                        responseManager.ManageResponse();

                        break;

                    case "4":
                        requestMaker.setOperation("leave");
                        requestMaker.createRequest();
                        out.println(requestMaker.getRequest());

                        break;
                }

            } while (!option.equals("4"));

            System.exit(0);

        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + hostname);
        } catch (IOException e) {
            System.err.println("I/O error while communicating with the server: " + e.getMessage());
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

}
