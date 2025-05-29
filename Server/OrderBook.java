package Server;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import Shared.*;

public class OrderBook {
    private final TreeMap<Integer, Pair<Queue<Order>, Integer>> bidBook;
    private final TreeMap<Integer, Pair<Queue<Order>, Integer>> askBook;
    private final TreeMap<Integer, Pair<Queue<Order>, Integer>> stopAskBook;
    private final TreeMap<Integer, Pair<Queue<Order>, Integer>> stopBidBook;

    private NotificationSender notificationsender;
    private OrderManager ordermanager;

    private static AtomicInteger idCounter;

    public static int generateId() {
        return idCounter.getAndIncrement();
    }

    public OrderBook() {

        idCounter = new AtomicInteger(1);

        bidBook = new TreeMap<>(Comparator.reverseOrder());
        askBook = new TreeMap<>();
        stopAskBook = new TreeMap<>();
        stopBidBook = new TreeMap<>(Comparator.reverseOrder());
    }

    public void setManagers(OrderManager Ordermanager, NotificationSender Notificationsender) {
        ordermanager = Ordermanager;
        notificationsender = Notificationsender;
    }

    // Method to add an order to the book

    public synchronized int addOrder(Order order) {

        // Determine if the order is a buy or sell order

        boolean isBid = order.isBuyOrder();

        // Obtain the appropriate book based on the order type

        TreeMap<Integer, Pair<Queue<Order>, Integer>> book = isBid ? askBook : bidBook;

        if (order.getOrderType().equals("MARKET")) {

            if (!canExecuteMarketOrder(order, book)) {
                return -1;
            }

            int remainingQuantity = order.getQuantity();

            Iterator<Map.Entry<Integer, Pair<Queue<Order>, Integer>>> iterator = book.entrySet().iterator();

            // Execute order if there is enough quantity available in the book
            // If the order is a buy order, iterate through the ask book
            // If the order is a sell order, iterate through the bid book

            while (iterator.hasNext()) {
                Map.Entry<Integer, Pair<Queue<Order>, Integer>> entry = iterator.next();

                int priceCapacity = entry.getValue().getSecond();

                if (priceCapacity <= remainingQuantity) {
                    remainingQuantity -= priceCapacity;

                    for (Order ordine : entry.getValue().getFirst()) {
                        notificationsender.addNotification(ordine);
                    }

                    iterator.remove();

                    if (remainingQuantity == 0)
                        break;

                } else {
                    // If order cannot be fully executed, execute only the remaining part
                    // Iterate through the orders at the current price level

                    for (Order ordine : entry.getValue().getFirst()) {

                        priceCapacity = entry.getValue().getSecond();

                        if (ordine.getQuantity() <= remainingQuantity) {
                            remainingQuantity -= ordine.getQuantity();
                            entry.getValue().setSecond(priceCapacity - ordine.getQuantity());
                            entry.getValue().getFirst().remove(ordine);

                            notificationsender.addNotification(ordine);


                        } else {
                            ordine.setQuantity(ordine.getQuantity() - remainingQuantity);
                            entry.getValue().setSecond(priceCapacity - remainingQuantity);

                            if (order.isStop()) {
                                order.setOrderType("STOP");
                                notificationsender.addNotification(order);
                            }
                            return order.getOrderId();
                        }
                    }
                }
            }
        } else {

            // If the order is a limit order, add it to the appropriate book
            
            if (order.getOrderType().equals("LIMIT")) {

                int remainingQuantity = order.getQuantity();

                Iterator<Map.Entry<Integer, Pair<Queue<Order>, Integer>>> iterator = book.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<Integer, Pair<Queue<Order>, Integer>> entry = iterator.next();

                    if (order.isBuyOrder() && entry.getKey() > order.getPrice()
                            || !order.isBuyOrder() && entry.getKey() < order.getPrice())
                        break;
                    int priceCapacity = entry.getValue().getSecond();

                    // Execute order if there is enough quantity available in the book

                    if (priceCapacity <= remainingQuantity) {

                        remainingQuantity -= priceCapacity;
                        for (Order ordine : entry.getValue().getFirst()) {
                            notificationsender.addNotification(ordine);

                        }

                        iterator.remove();

                        if (remainingQuantity == 0)
                            break;

                            // If the order cannot be fully executed, execute only the remaining part
                    } else {
                        for (Order ordine : entry.getValue().getFirst()) {

                            priceCapacity = entry.getValue().getSecond();

                            if (ordine.getQuantity() <= remainingQuantity) {
                                remainingQuantity -= ordine.getQuantity();
                                entry.getValue().setSecond(priceCapacity - ordine.getQuantity());
                                entry.getValue().getFirst().remove(ordine);
                                notificationsender.addNotification(ordine);

                            } else {

                                ordine.setQuantity(ordine.getQuantity() - remainingQuantity);
                                entry.getValue().setSecond(priceCapacity - remainingQuantity);
                                return order.getOrderId();
                            }
                        }
                    }
                }

                // If the order cannot be fully executed, add it to the book

                if (remainingQuantity > 0) {

                    if (order.isBuyOrder()) {
                        bidBook.putIfAbsent(order.getPrice(),
                                new Pair<Queue<Order>, Integer>(new LinkedList<Order>(), 0));
                        order.setQuantity(remainingQuantity);
                        bidBook.get(order.getPrice()).getFirst().add(order);
                        bidBook.get(order.getPrice())
                                .setSecond(bidBook.get(order.getPrice()).getSecond() + remainingQuantity);
                    } else {
                        askBook.putIfAbsent(order.getPrice(),
                                new Pair<Queue<Order>, Integer>(new LinkedList<Order>(), 0));
                        order.setQuantity(remainingQuantity);
                        askBook.get(order.getPrice()).getFirst().add(order);
                        askBook.get(order.getPrice())
                                .setSecond(askBook.get(order.getPrice()).getSecond() + remainingQuantity);
                    }
                }
                checkStops(order);
            }
        }

        return 0;

    }

    // Method to check if a market order can be executed
    // It checks if the cumulative quantity of orders in the book is greater than or
    // equal to the quantity of the order to be executed
    // If the order is a buy order, it checks the ask book
    // If the order is a sell order, it checks the bid book
    // Returns true if the order can be executed, false otherwise

    private boolean canExecuteMarketOrder(Order order, TreeMap<Integer, Pair<Queue<Order>, Integer>> book) {
        int cumulativeQuantity = 0;

        for (Map.Entry<Integer, Pair<Queue<Order>, Integer>> entry : book.entrySet()) {
            cumulativeQuantity += entry.getValue().getSecond();
            if (cumulativeQuantity >= order.getQuantity())
                break;
        }

        return (cumulativeQuantity >= order.getQuantity()) ? true : false;
    }

    // Method to add a stop order to the book
    // It adds the order to the appropriate stop book based on the order type
    // If the order is a buy order, it adds it to the stop bid book
    // If the order is a sell order, it adds it to the stop ask book
    // Returns the order ID of the added stop order

    public synchronized int addStopOrder(Order order) {
        TreeMap<Integer, Pair<Queue<Order>, Integer>> stopBook = order.isBuyOrder() ? stopBidBook : stopAskBook;

        stopBook.putIfAbsent(order.getPrice(), new Pair<Queue<Order>, Integer>(new LinkedList<Order>(), 0));
        stopBook.get(order.getPrice()).getFirst().add(order);

        return order.getOrderId();
    }

    // Method to check if there are stop orders to be executed

    private void checkStops(Order order) {

        TreeMap<Integer, Pair<Queue<Order>, Integer>> targetBook = order.isBuyOrder() ? stopBidBook : stopAskBook;

        Iterator<Map.Entry<Integer, Pair<Queue<Order>, Integer>>> iterator = targetBook.entrySet().iterator();

        if (order.isBuyOrder()) {
            while (iterator.hasNext()) {
                Map.Entry<Integer, Pair<Queue<Order>, Integer>> entry = iterator.next();

                if (entry.getKey() <= order.getPrice()) {
                    Iterator<Order> order_iterator = entry.getValue().getFirst().iterator();

                    while (order_iterator.hasNext()) {
                        Order stop_order = order_iterator.next();
                        stop_order.setOrderType("MARKET");

                        addOrder(stop_order);

                        order_iterator.remove();
                    }

                    if (entry.getValue().getFirst().size() == 0)
                        iterator.remove();
                }
            }
        } else {
            while (iterator.hasNext()) {
                Map.Entry<Integer, Pair<Queue<Order>, Integer>> entry = iterator.next();

                if (entry.getKey() >= order.getPrice()) {
                    Iterator<Order> order_iterator = entry.getValue().getFirst().iterator();

                    while (order_iterator.hasNext()) {
                        Order stop_order = order_iterator.next();
                        stop_order.setOrderType("MARKET");
                        addOrder(stop_order);
                        order_iterator.remove();
                    }

                    if (entry.getValue().getFirst().size() == 0)
                        iterator.remove();
                }
            }
        }
    }

    // Method to display the order book
    // It returns a string that represents the order book

    public Pair<String, Integer> displayBook() {
        int nlines = 4;

        StringBuilder visualBook = new StringBuilder("===== ORDER BOOK ===========================\n");

        visualBook.append("BIDS:\n");
        for (Map.Entry<Integer, Pair<Queue<Order>, Integer>> entry : bidBook.entrySet()) {
            nlines++;

            Integer price = entry.getKey();
            Pair<Queue<Order>, Integer> pair = entry.getValue();
            Queue<Order> ordersAtPrice = pair.getFirst();
            int totalQuantity = pair.getSecond();

            // Visualizza il prezzo, le quantità e il totale
            visualBook.append("Price: " + price + " | Quantities: [");
            for (Order order : ordersAtPrice) {
                visualBook.append(order.getQuantity() + " ");
            }
            visualBook.append("] | Total: " + totalQuantity);
            visualBook.append("\n");
        }

        visualBook.append("ASKS:\n");
        for (Map.Entry<Integer, Pair<Queue<Order>, Integer>> entry : askBook.entrySet()) {
            nlines++;

            Integer price = entry.getKey();
            Pair<Queue<Order>, Integer> pair = entry.getValue();
            Queue<Order> ordersAtPrice = pair.getFirst();
            int totalQuantity = pair.getSecond();

            // Visualizza il prezzo, le quantità e il totale
            visualBook.append("Price: " + price + " | Quantities: [");
            for (Order order : ordersAtPrice) {
                visualBook.append(order.getQuantity() + " ");
            }
            visualBook.append("] | Total: " + totalQuantity);
            visualBook.append("\n");
        }
        visualBook.append("============================================\n");

        Pair<String, Integer> bookoutput = new Pair<String, Integer>(visualBook.toString(), nlines);
        return bookoutput;
    }

    // Method to display the stop order book
    // It returns a string that represents the stop order book

    public Pair<String, Integer> displayStopBook(String user) {

        StringBuilder visualStopBook = new StringBuilder();
        Integer nlines = 4;

        visualStopBook.append("===== STOP ORDER BOOK ======================\n");

        visualStopBook.append("BIDS:");
        for (Map.Entry<Integer, Pair<Queue<Order>, Integer>> entry : stopBidBook.entrySet()) {
            nlines++;
            Integer price = entry.getKey();
            Pair<Queue<Order>, Integer> pair = entry.getValue();
            Queue<Order> ordersAtPrice = pair.getFirst();

            // Visualizza il prezzo, le quantità e il totale
            visualStopBook.append("Price: " + price + " | Quantities: [");
            for (Order order : ordersAtPrice) {
                visualStopBook.append(order.getQuantity() + " ");
            }
            visualStopBook.append("]");
        }
        visualStopBook.append("\n");

        visualStopBook.append("ASKS:\n");
        for (Map.Entry<Integer, Pair<Queue<Order>, Integer>> entry : stopAskBook.entrySet()) {
            nlines++;
            Integer price = entry.getKey();
            Pair<Queue<Order>, Integer> pair = entry.getValue();
            Queue<Order> ordersAtPrice = pair.getFirst();

            int total_size = 0;

            for (Order order : ordersAtPrice) {
                if (order.getUserId() == user)
                    total_size += order.getQuantity();
            }

            if (total_size > 0) {
                // Visualizza il prezzo, le quantità e il totale
                visualStopBook.append("Price: " + price + " | Quantities: [");
                for (Order order : ordersAtPrice) {
                    if (order.getUserId() == user)
                        visualStopBook.append(order.getQuantity() + " ");
                }
                visualStopBook.append("]");
            }
        }

        visualStopBook.append("============================================\n");

        Pair<String, Integer> bookoutput = new Pair<String, Integer>(visualStopBook.toString(), nlines);
        return bookoutput;
    }

    // Method to remove an order from the book
    
    public synchronized boolean removeOrderById(int orderId, String user) {
        // Cerca e rimuovi l'ordine dall'askBook
        if (removeOrderFromBook(askBook, orderId, user)) {
            System.out.println("Order removed from askBook.");
            return true;
        }

        // Cerca e rimuovi l'ordine dal bidBook
        if (removeOrderFromBook(bidBook, orderId, user)) {
            System.out.println("Order removed from bidBook.");
            return true;
        }

        if (removeOrderFromBook(stopAskBook, orderId, user)) {
            System.out.println("Order removed from bidBook.");
            return true;
        }

        if (removeOrderFromBook(stopBidBook, orderId, user)) {
            System.out.println("Order removed from bidBook.");
            return true;
        }

        // Se non trovato
        System.out.println("Order ID not found.");
        return false;
    }

    // Auxiliary method to remove an order from the book
    // It iterates through the book and checks if the order ID matches
    // If found, it removes the order from the queue and updates the total size

    private boolean removeOrderFromBook(TreeMap<Integer, Pair<Queue<Order>, Integer>> book, int orderId, String user) {
        for (Map.Entry<Integer, Pair<Queue<Order>, Integer>> entry : book.entrySet()) {
            Queue<Order> orders = entry.getValue().getFirst();
            Iterator<Order> iterator = orders.iterator();

            while (iterator.hasNext()) {
                Order order = iterator.next();
                if (order.getOrderId() == orderId) {
                    if (!order.getUserId().equals(user))
                        return false;
                    // Rimuovi l'ordine dalla coda
                    iterator.remove();

                    // Aggiorna la somma delle dimensioni
                    int newSize = entry.getValue().getSecond() - order.getQuantity();
                    entry.setValue(new Pair<>(orders, newSize));

                    // Rimuovi la linea di prezzo se non ha più ordini
                    if (orders.isEmpty()) {
                        book.remove(entry.getKey());
                    }
                    return true;
                }
            }
        }
        return false;
    }

    // Orders persistence
    // It iterates through the bid and ask books and sends each order to the order manager
    // for persistence
    
    public void bookPersistence() {
        System.out.println("Persisting orders...");

        for (Map.Entry<Integer, Pair<Queue<Order>, Integer>> entry : bidBook.entrySet()) {
            for (Order order : entry.getValue().getFirst()) {
                ordermanager.sendBook(order);
            }
        }

        for (Map.Entry<Integer, Pair<Queue<Order>, Integer>> entry : askBook.entrySet()) {
            for (Order order : entry.getValue().getFirst()) {
                ordermanager.sendBook(order);
            }
        }

        for (Map.Entry<Integer, Pair<Queue<Order>, Integer>> entry : stopBidBook.entrySet()) {
            for (Order order : entry.getValue().getFirst()) {
                ordermanager.sendBook(order);
            }
        }

        for (Map.Entry<Integer, Pair<Queue<Order>, Integer>> entry : stopAskBook.entrySet()) {
            for (Order order : entry.getValue().getFirst()) {
                ordermanager.sendBook(order);
            }
        }
    }
}