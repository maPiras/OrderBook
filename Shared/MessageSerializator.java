package Shared;

import java.util.ArrayList;
import com.google.gson.Gson;

public class MessageSerializator {

    public MessageSerializator() {

    }

    public static class Message {
        String notification = "closedTrades";
        ArrayList<Order> trades;

        public Message(Order closedTrade) {
            trades = new ArrayList<>();
            trades.add(closedTrade);
        }

        public Order getTrade() {
            return trades.get(0);
        }
    }

    public String getSerialized(Order order) {
        Gson gson = new Gson();
        Order order_notify = new Order(order.getOrderId(), (String) order.getUserId(), order.getPrice(),
                order.getQuantity(), order.isBuyOrder(), (String) order.getOrderType(), order.isStop(),
                (long) System.currentTimeMillis() / 1000);
        Message message = new Message(order_notify);
        return gson.toJson(message, Message.class);
    }

    public String getMessage(String message) {
        Gson gson = new Gson();
        Message des_message = gson.fromJson(message, Message.class);
        Order trade = des_message.getTrade();
        if (trade.getOrderType() != "STOP") {
            return "Limit order exectuion: ID " + trade.getOrderId() + " size " + trade.getQuantity() + " price "
                    + trade.getPrice();
        } else
            return "Stop order execution: ID " + trade.getOrderId() + " size " + trade.getQuantity() + " price "
                    + trade.getPrice();
    }

}
