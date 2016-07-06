package com.fein91.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
public class HistoryTrade {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name="invoice_fk")
    Invoice invoice;

    /**
     * ref to existed limit order request from order book which was affected in current trade
     */
    @ManyToOne
    @JoinColumn(name="affectedOrderRequestFk")
    HistoryOrderRequest affectedOrderRequest;

    /**
     * trade source is historyOrderRequest.counterparty
     */
    @OneToOne
    @JoinColumn(name="counterparty_fk")
    Counterparty target;

    BigDecimal quantity;

    BigDecimal discountValue;

    public Long getId() {
        return id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public HistoryOrderRequest getAffectedOrderRequest() {
        return affectedOrderRequest;
    }

    public void setAffectedOrderRequest(HistoryOrderRequest affectedOrderRequest) {
        this.affectedOrderRequest = affectedOrderRequest;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public void setTarget(Counterparty target) {
        this.target = target;
    }

    public Counterparty getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "HistoryTrade{" +
                "id=" + id +
                ", invoice=" + invoice +
                ", affectedOrderRequest=" + affectedOrderRequest +
                ", target=" + target +
                ", quantity=" + quantity +
                ", discountValue=" + discountValue +
                '}';
    }
}
