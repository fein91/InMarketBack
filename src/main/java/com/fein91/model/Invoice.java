package com.fein91.model;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Created by olta1014 on 23.05.2016.
 */
@Entity
public class Invoice {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "counterparty_from_fk")
    private Counterparty source;

    @ManyToOne
    @JoinColumn(name = "counterparty_to_fk")
    private Counterparty target;

    private BigDecimal value;

    private BigDecimal prepaidValue;

    public Invoice() {
        //JPA
    }

    public Invoice(Long id, Counterparty source, Counterparty target, BigDecimal value, BigDecimal prepaidValue) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.value = value;
        this.prepaidValue = prepaidValue;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Counterparty getSource() {
        return source;
    }

    public void setSource(Counterparty source) {
        this.source = source;
    }

    public Counterparty getTarget() {
        return target;
    }

    public void setTarget(Counterparty target) {
        this.target = target;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getPrepaidValue() {
        return prepaidValue;
    }

    public void setPrepaidValue(BigDecimal prepaidValue) {
        this.prepaidValue = prepaidValue;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", source=" + source +
                ", target=" + target +
                ", value=" + value +
                ", prepaidValue=" + prepaidValue +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Invoice invoice = (Invoice) o;

        if (id != null ? !id.equals(invoice.id) : invoice.id != null) return false;
        if (source != null ? !source.equals(invoice.source) : invoice.source != null) return false;
        if (target != null ? !target.equals(invoice.target) : invoice.target != null) return false;
        if (value != null ? !value.equals(invoice.value) : invoice.value != null) return false;
        return prepaidValue != null ? prepaidValue.equals(invoice.prepaidValue) : invoice.prepaidValue == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (prepaidValue != null ? prepaidValue.hashCode() : 0);
        return result;
    }
}
