package com.fein91.builders;

import com.fein91.core.model.OrderSide;
import com.fein91.model.Counterparty;
import com.fein91.model.OrderRequest;
import com.fein91.model.OrderType;

import java.math.BigDecimal;
import java.util.Date;

public class OrderRequestBuilder {

    public OrderRequestBuilder(Counterparty counterparty) {
        this.counterparty = counterparty;
    }

    Counterparty counterparty;
    BigDecimal price;
    BigDecimal quantity;
    Date date;
    OrderSide orderSide;
    OrderType orderType;

    public OrderRequestBuilder price(BigDecimal price) {
        this.price = price;
        return this;
    }

    public OrderRequestBuilder quantity(BigDecimal quantity) {
        this.quantity = quantity;
        return this;
    }

    public OrderRequestBuilder date(Date date) {
        this.date = date;
        return this;
    }

    public OrderRequestBuilder orderSide(OrderSide orderSide) {
        this.orderSide = orderSide;
        return this;
    }

    public OrderRequestBuilder orderType(OrderType orderType) {
        this.orderType = orderType;
        return this;
    }

    public OrderRequest build() {
        if (orderType == OrderType.LIMIT && price == null) {
            throw new IllegalStateException("Can't build limit order without price");
        }

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCounterparty(counterparty);
        orderRequest.setDate(date);
        orderRequest.setOrderSide(orderSide);
        orderRequest.setOrderType(orderType);
        orderRequest.setPrice(price);
        orderRequest.setQuantity(quantity);
        return orderRequest;
    }
}
