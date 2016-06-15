package com.fein91.builders;

import com.fein91.core.model.Order;
import com.fein91.core.model.OrderSide;
import com.fein91.model.OrderType;

import java.math.BigDecimal;

public class OrderBuilder {

    public OrderBuilder(long id) {
        this.id = id;
    }

    private final long id;
    private long timestamp;
    private long takerId;
    private OrderType orderType;
    private BigDecimal quantity;
    private OrderSide orderSide;
    private BigDecimal price;

    public OrderBuilder timestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public OrderBuilder takerId(long takerId) {
        this.takerId = takerId;
        return this;
    }

    public OrderBuilder orderType(OrderType orderType) {
        this.orderType = orderType;
        return this;
    }

    public OrderBuilder quantity(BigDecimal quantity) {
        this.quantity = quantity;
        return this;
    }

    public OrderBuilder price(BigDecimal price) {
        this.price = price;
        return this;
    }

    public OrderBuilder orderSide(OrderSide orderSide) {
        this.orderSide = orderSide;
        return this;
    }

    public Order build() {
        return OrderType.LIMIT == orderType
                ? new Order(id, timestamp, orderType, quantity, takerId, orderSide, price.doubleValue())
                : new Order(id, timestamp, orderType, quantity, takerId, orderSide);
    }
}
