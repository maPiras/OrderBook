package Shared;

public class Order {
    private final String userId;
    private final int price;
    private int quantity;
    private String orderType;
    private int orderId;
    private long timestamp;
    private boolean StopOrder;

    private final boolean isBuyOrder;

    public Order(int orderId, String userId, Integer price, int quantity, boolean isBuyOrder, String orderType,
            Boolean StopOrder, long time) {

        this.orderId = orderId;
        this.userId = userId;
        this.price = price;
        this.quantity = quantity;
        this.isBuyOrder = isBuyOrder;
        this.orderType = orderType;
        this.StopOrder = StopOrder;
        this.timestamp = time;

    }

    public int getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer q) {
        this.quantity = q;
    }

    public boolean isBuyOrder() {
        return isBuyOrder;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String Type) {
        this.orderType = Type;
    }

    public boolean isStop() {
        return this.StopOrder;
    }

    public void setTime(long time) {
        this.timestamp = time;
    }

    public long getTime() {
        return this.timestamp;
    }

    public void reduceQuantity(int amount) {
        if (amount > 0 && amount <= quantity) {
            this.quantity -= amount;
        }
    }
}