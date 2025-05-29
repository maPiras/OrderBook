package Server;

import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.concurrent.*;

import Shared.*;

public class MainServer {

    private final ConcurrentHashMap<String, Tuple> usersDatabase;
    private final ExecutorService threadPool;
    private final OrderBook orderBook;

    NotificationSender notificationsender;
    UserManager userManager;
    OrderManager orderManager;

    public MainServer(int maxClients, int timeout) throws SocketException {

        this.usersDatabase = new ConcurrentHashMap<>();
        this.notificationsender = new NotificationSender(usersDatabase);
        this.userManager = new UserManager(usersDatabase);
        this.orderBook = new OrderBook();
        this.orderManager = new OrderManager(orderBook);
        this.orderBook.setManagers(orderManager,notificationsender);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.printf("\nServer is shutting down...\n");
            shutdown();
        }));

        // Cached threadpool
        this.threadPool = new ThreadPoolExecutor(0, maxClients, timeout,
                TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

    }

    public void start(int port) {
        Thread notificationThread = new Thread(notificationsender);
        notificationThread.start();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            // Load users from file
            userManager.getUsers();
            // Load orders from file
            orderManager.retrieveBook();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(new ClientHandler(clientSocket, this, usersDatabase, orderBook, notificationsender,
                        userManager, orderManager));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    private void shutdown() {

        System.out.println("Shutting down thread pool...");
        threadPool.shutdown();

        System.out.println("Saving orders...");
        orderBook.bookPersistence();

        System.out.println("Closing notification sender...");
        notificationsender.close();

        System.out.println("Closing order manager...");
        orderManager.close();

        System.out.println("Closing user manager...");
        userManager.close();

        System.out.println("Server closed.");
        System.out.println("Shutdown complete.");
    }

    public static void main(String[] args) throws SocketException {
        Properties config = new Properties();

        // Default values

        int maxClients = 10;
        int port = 0;
        int timeout = 60;

        // Load configuration from properties file
        
        try (FileInputStream fis = new FileInputStream("Misc/server.properties")) {
            config.load(fis);
            port = Integer.parseInt(config.getProperty("port"));
            maxClients = Integer.parseInt(config.getProperty("maxClients"));
            timeout = Integer.parseInt(config.getProperty("timeout"));

        } catch (IOException | NumberFormatException e) {
            System.err.println("Misconfiguration in config file: " + e.getMessage());
            System.exit(1);
        }

        MainServer server = new MainServer(maxClients, timeout);
        server.start(port);
    }
}