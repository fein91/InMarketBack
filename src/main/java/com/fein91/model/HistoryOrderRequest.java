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

    @Deprecated
    BigDecimal avgDiscountPerc;

    BigDecimal avgDaysToPayment;

    Date date;

    int side;

    String type;

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

    @Deprecated
    public BigDecimal getAvgDiscountPerc() {
        return avgDiscountPerc;
    }

    @Deprecated
    public void setAvgDiscountPerc(BigDecimal avgDiscountPerc) {
        this.avgDiscountPerc = avgDiscountPerc;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public OrderSide getSide() {
        return OrderSide.valueOf(side);
    }

    public void setSide(OrderSide side) {
        this.side = side.getId();
    }

    public HistoryOrderType getType() {
        return HistoryOrderType.valueOf(type);
    }

    public void setType(HistoryOrderType type) {
        this.type = type.name();
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

    public BigDecimal getAvgDaysToPayment() {
        return avgDaysToPayment;
    }

    public void setAvgDaysToPayment(BigDecimal avgDaysToPayment) {
        this.avgDaysToPayment = avgDaysToPayment;
    }

    @Override
    public String toString() {
        return "HistoryOrderRequest{" +
                "id=" + id +
                ", counterparty=" + counterparty +
                ", price=" + price +
                ", quantity=" + quantity +
                ", avgDiscountPerc=" + avgDiscountPerc +
                ", date=" + date +
                ", side=" + side +
                ", type='" + type + '\'' +
                ", originOrderRequestId=" + originOrderRequestId +
                ", historyTrades=" + historyTrades +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoryOrderRequest that = (HistoryOrderRequest) o;

        if (side != that.side) return false;
        if (counterparty != null ? !counterparty.equals(that.counterparty) : that.counterparty != null) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (historyTrades != null ? !historyTrades.equals(that.historyTrades) : that.historyTrades != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (originOrderRequestId != null ? !originOrderRequestId.equals(that.originOrderRequestId) : that.originOrderRequestId != null)
            return false;
        if (price != null ? !price.equals(that.price) : that.price != null) return false;
        if (quantity != null ? !quantity.equals(that.quantity) : that.quantity != null) return false;
        if (avgDiscountPerc != null ? !avgDiscountPerc.equals(that.avgDiscountPerc) : that.avgDiscountPerc != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (counterparty != null ? counterparty.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        result = 31 * result + (quantity != null ? quantity.hashCode() : 0);
        result = 31 * result + (avgDiscountPerc != null ? avgDiscountPerc.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + side;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (originOrderRequestId != null ? originOrderRequestId.hashCode() : 0);
        result = 31 * result + (historyTrades != null ? historyTrades.hashCode() : 0);
        return result;
    }
}
