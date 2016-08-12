package com.fein91.model;

import com.fein91.core.model.OrderSide;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

    int side;

    int type;

    @Transient
    Map<Long, Boolean> invoicesChecked = new HashMap<>();

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

    public OrderSide getSide() {
        return OrderSide.valueOf(side);
    }

    public void setSide(OrderSide side) {
        this.side = side.getId();
    }

    public OrderType getType() {
        return OrderType.valueOf(type);
    }

    public void setType(OrderType type) {
        this.type = type.getId();
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
                ", side=" + getSide() +
                ", type=" + getType() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderRequest that = (OrderRequest) o;

        if (side != that.side) return false;
        if (type != that.type) return false;
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
        result = 31 * result + side;
        result = 31 * result + type;
        return result;
    }
}
