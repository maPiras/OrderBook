package Server;

import java.net.*;
import java.util.concurrent.*;

import Shared.*;

public class NotificationSender implements Runnable {
    private DatagramSocket socket;
    private final MessageSerializator serializator;
    private final ConcurrentHashMap<String, Tuple> usersDatabase;

    // Shared queue for notifications
    private static final BlockingQueue<Notification> notificationQueue = new LinkedBlockingQueue<>();

    public NotificationSender(ConcurrentHashMap<String, Tuple> usersDatabase) throws SocketException {
        this.socket = new DatagramSocket(); // Creazione del socket UDP
        this.serializator = new MessageSerializator();
        this.usersDatabase = usersDatabase;
    }

    // Add a notification to the queue
    public void addNotification(Order ordine) {
        Notification orderNotify = new Notification(serializator.getSerialized(ordine),
                usersDatabase.get(ordine.getUserId()).getAddress(), usersDatabase.get(ordine.getUserId()).getPort());
        try {
            notificationQueue.put(orderNotify);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Sends a notification to the client
    // using the DatagramSocket
    // and the DatagramPacket

    public void sendNotification(String message, InetAddress clientAddress, int clientPort) {
        // Serializza
        byte[] buffer = message.getBytes();

        // Create a DatagramPacket
        // with the serialized message
        // and the client's address and port
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
        try {
            // Invia il pacchetto
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Estrae una notifica dalla coda (operazione bloccante)
                Notification notification = notificationQueue.take();
                sendNotification(notification.getMessage(), notification.getClientAddress(),
                        notification.getClientPort());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Internal class to represent a notification

    public static class Notification {
        // Attributes

        private final String message;
        private final InetAddress clientAddress;
        private final int clientPort;

        // Constructor

        public Notification(String message, InetAddress clientAddress, int clientPort) {
            this.message = message;
            this.clientAddress = clientAddress;
            this.clientPort = clientPort;
        }

        public String getMessage() {
            return message;
        }

        public InetAddress getClientAddress() {
            return clientAddress;
        }

        public int getClientPort() {
            return clientPort;
        }
    }

    public void close() {
                // Sends a notification to all active users
        for (Tuple user : usersDatabase.values()) {
            try {
                // Check if the user is active
            // and send the shutdown notification
                if(user.getStatus())
                sendNotification("SERVER SHUTDOWN", user.getAddress(), user.getPort());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        socket.close();
        notificationQueue.clear();
    }
}
