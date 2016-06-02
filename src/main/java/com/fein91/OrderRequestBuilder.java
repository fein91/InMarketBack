package com.fein91;

import com.fein91.core.model.OrderSide;
import com.fein91.model.Counterparty;
import com.fein91.model.OrderRequest;
import com.fein91.model.OrderType;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

public class OrderRequestBuilder {

    public OrderRequestBuilder(BigInteger id, Counterparty counterparty) {
        this.id = id;
        this.counterparty = counterparty;
    }

    BigInteger id;
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
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setId(id);
        orderRequest.setCounterparty(counterparty);
        orderRequest.setDate(date);
        orderRequest.setOrderSide(orderSide);
        orderRequest.setOrderType(orderType);
        orderRequest.setPrice(price);
        orderRequest.setQuantity(quantity);
        return orderRequest;
    }
}
