package com.fein91.model;

import com.fein91.core.model.OrderSide;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by olta1014 on 23.05.2016.
 */
@Entity
public class OrderRequest {

    @Id
    @GeneratedValue
    Long id;

    @ManyToOne
    @JoinColumn(name="counterparty_fk")
    Counterparty counterparty;

    BigDecimal price;

    BigDecimal quantity;

    Date date;

    int orderSide;

    int orderType;

    @Transient
    Map<Long, Boolean> invoicesChecked;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Map<Long, Boolean> getInvoicesChecked() {
        return invoicesChecked;
    }

    public void setInvoicesChecked(Map<Long, Boolean> invoicesChecked) {
        this.invoicesChecked = invoicesChecked;
    }

    @Override
    public String toString() {
        return "OrderRequest{" +
                "id=" + id +
                ", counterparty=" + counterparty +
                ", price=" + price +
                ", quantity=" + quantity +
                ", date=" + date +
                ", orderSide=" + orderSide +
                ", orderType=" + orderType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderRequest that = (OrderRequest) o;

        if (orderSide != that.orderSide) return false;
        if (orderType != that.orderType) return false;
        if (counterparty != null ? !counterparty.equals(that.counterparty) : that.counterparty != null) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (price != null ? !price.equals(that.price) : that.price != null) return false;
        if (quantity != null ? !quantity.equals(that.quantity) : that.quantity != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (counterparty != null ? counterparty.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (quantity != null ? quantity.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + orderSide;
        result = 31 * result + orderType;
        return result;
    }
}
