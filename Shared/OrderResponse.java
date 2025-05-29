package Shared;

public class OrderResponse {
    Integer code;

    public OrderResponse(Integer Code) {
        this.code = Code;
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer Code) {
        this.code = Code;
    }
}