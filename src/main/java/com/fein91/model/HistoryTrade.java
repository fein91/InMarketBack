package com.fein91.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
public class HistoryTrade implements CalculableTrade {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name="invoice_fk")
    Invoice invoice;

    /**
     * ref to existed limit order request from order book which was affected in current trade
     */
    @JsonIgnore
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

    BigDecimal price;

    BigDecimal discountValue;

    BigDecimal periodReturn;

    /**
     * contains unpaid value before order
     */
    BigDecimal unpaidInvoiceValue;

    /**
     * its needed to calculate avg days left to payment date
     */
    @Transient
    @JsonIgnore
    private BigDecimal daysToPaymentMultQtyTraded;

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

    @Override
    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public BigDecimal getPeriodReturn() {
        return periodReturn;
    }

    public void setPeriodReturn(BigDecimal periodReturn) {
        this.periodReturn = periodReturn;
    }

    public BigDecimal getUnpaidInvoiceValue() {
        return unpaidInvoiceValue;
    }

    public void setUnpaidInvoiceValue(BigDecimal unpaidInvoiceValue) {
        this.unpaidInvoiceValue = unpaidInvoiceValue;
    }

    public void setTarget(Counterparty target) {
        this.target = target;
    }

    public Counterparty getTarget() {
        return target;
    }

    @Override
    public BigDecimal getDaysToPaymentMultQtyTraded() {
        return daysToPaymentMultQtyTraded;
    }

    public void setDaysToPaymentMultQtyTraded(BigDecimal daysToPaymentMultQtyTraded) {
        this.daysToPaymentMultQtyTraded = daysToPaymentMultQtyTraded;
    }

    @Override
    public String toString() {
        return "HistoryTrade{" +
                "id=" + id +
                ", invoice=" + invoice +
                ", affectedOrderRequest=" + affectedOrderRequest +
                ", target=" + target +
                ", quantity=" + quantity +
                ", price=" + price +
                ", discountValue=" + discountValue +
                '}';
    }
}
