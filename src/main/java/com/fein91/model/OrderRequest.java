package com.fein91.model;

import com.fein91.core.model.OrderSide;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Created by olta1014 on 23.05.2016.
 */
@Entity
public class OrderRequest {

    @Id
    BigInteger id;

    @ManyToOne
    @JoinColumn(name="counterparty_fk")
    Counterparty counterparty;

    BigDecimal price;

    BigDecimal quantity;

    Date date;

    int orderSide;

    int orderType;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public Counterparty getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(Counterparty counterparty) {
        this.counterparty = counterparty;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public OrderSide getOrderSide() {
        return OrderSide.valueOf(orderSide);
    }

    public void setOrderSide(OrderSide orderSide) {
        this.orderSide = orderSide.getId();
    }

    public OrderType getOrderType() {
        return OrderType.valueOf(orderType);
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType.getId();
    }
}
