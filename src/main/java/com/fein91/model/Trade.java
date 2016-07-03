package com.fein91.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
public class Trade {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    Invoice invoice;

    /**
     * ref to new order request which is currently processing
     */
    @ManyToOne
    @JoinColumn(name="processingOrderRequestFk")
    HistoryOrderRequest processingOrderRequest;

    /**
     * ref to existed limit order request from order book which was affected in current trade
     */
    @ManyToOne
    @JoinColumn(name="affectedOrderRequestFk")
    HistoryOrderRequest affectedOrderRequest;

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

    public HistoryOrderRequest getProcessingOrderRequest() {
        return processingOrderRequest;
    }

    public void setProcessingOrderRequest(HistoryOrderRequest processingOrderRequest) {
        this.processingOrderRequest = processingOrderRequest;
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
}
