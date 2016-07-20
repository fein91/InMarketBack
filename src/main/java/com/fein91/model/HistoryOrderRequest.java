package com.fein91.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fein91.core.model.OrderSide;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
public class HistoryOrderRequest {

    @Id
    @GeneratedValue
    Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name="counterparty_fk")
    Counterparty counterparty;

    BigDecimal price;

    BigDecimal quantity;

    BigDecimal weightedDiscountPerc;

    Date date;

    int orderSide;

    String historyOrderType;

    @JsonIgnore
    Long originOrderRequestId;

    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumn(name="processing_order_request_fk")
    List<HistoryTrade> historyTrades;

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

    public BigDecimal getWeightedDiscountPerc() {
        return weightedDiscountPerc;
    }

    public void setWeightedDiscountPerc(BigDecimal weightedDiscountPerc) {
        this.weightedDiscountPerc = weightedDiscountPerc;
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

    public HistoryOrderType getHistoryOrderType() {
        return HistoryOrderType.valueOf(historyOrderType);
    }

    public void setHistoryOrderType(HistoryOrderType historyOrderType) {
        this.historyOrderType = historyOrderType.name();
    }

    public Long getOriginOrderRequestId() {
        return originOrderRequestId;
    }

    public void setOriginOrderRequestId(Long originOrderRequestId) {
        this.originOrderRequestId = originOrderRequestId;
    }

    public List<HistoryTrade> getHistoryTrades() {
        return historyTrades;
    }

    public void setHistoryTrades(List<HistoryTrade> historyTrades) {
        this.historyTrades = historyTrades;
    }

    @Override
    public String toString() {
        return "HistoryOrderRequest{" +
                "id=" + id +
                ", counterparty=" + counterparty +
                ", price=" + price +
                ", quantity=" + quantity +
                ", weightedDiscountPerc=" + weightedDiscountPerc +
                ", date=" + date +
                ", orderSide=" + orderSide +
                ", historyOrderType='" + historyOrderType + '\'' +
                ", originOrderRequestId=" + originOrderRequestId +
                ", historyTrades=" + historyTrades +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoryOrderRequest that = (HistoryOrderRequest) o;

        if (orderSide != that.orderSide) return false;
        if (counterparty != null ? !counterparty.equals(that.counterparty) : that.counterparty != null) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (historyTrades != null ? !historyTrades.equals(that.historyTrades) : that.historyTrades != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (historyOrderType != null ? !historyOrderType.equals(that.historyOrderType) : that.historyOrderType != null) return false;
        if (originOrderRequestId != null ? !originOrderRequestId.equals(that.originOrderRequestId) : that.originOrderRequestId != null)
            return false;
        if (price != null ? !price.equals(that.price) : that.price != null) return false;
        if (quantity != null ? !quantity.equals(that.quantity) : that.quantity != null) return false;
        if (weightedDiscountPerc != null ? !weightedDiscountPerc.equals(that.weightedDiscountPerc) : that.weightedDiscountPerc != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (counterparty != null ? counterparty.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (quantity != null ? quantity.hashCode() : 0);
        result = 31 * result + (weightedDiscountPerc != null ? weightedDiscountPerc.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + orderSide;
        result = 31 * result + (historyOrderType != null ? historyOrderType.hashCode() : 0);
        result = 31 * result + (originOrderRequestId != null ? originOrderRequestId.hashCode() : 0);
        result = 31 * result + (historyTrades != null ? historyTrades.hashCode() : 0);
        return result;
    }
}
