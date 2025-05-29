package Client;

import java.net.*;
import java.util.*;

import Shared.MessageSerializator;

public class NotificationReceiver implements Runnable {
    private MessageSerializator serializator;
    private DatagramSocket socket;
    private LinkedList<String> notifications;

    public NotificationReceiver() throws Exception {
        this.socket = new DatagramSocket();
        this.notifications = new LinkedList<>();
        this.serializator = new MessageSerializator();
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];

        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                if(message.equals("SERVER SHUTDOWN")) {
                    System.out.println("\nServer is shutting down");
                    System.exit(0);
                }
                synchronized (notifications) {
                    notifications.add(message);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        socket.close();
        notifications.clear();
    }

    public int getNumber() {
        return notifications.size();
    }

    public int getPort() {
        return socket.getLocalPort();
    }

    public void show() {
        synchronized (notifications) {
            if (notifications.size() > 0) {
                Iterator<String> iterator = notifications.iterator();
                int n = 1;

                while (iterator.hasNext()) {
                    String notification = iterator.next();
                    System.out.println("Notification n." + n + " : " + serializator.getMessage(notification));
                    n++;
                    iterator.remove();
                }
            } else
                System.out.println("La casella e' vuota!");
        }
    }
}
