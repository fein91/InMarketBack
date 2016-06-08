package com.fein91.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Created by olta1014 on 23.05.2016.
 */
@Entity
public class Invoice {

    @Id
    BigInteger id;

    @ManyToOne
    @JoinColumn(name="counterparty_from_fk")
    Counterparty source;

    @ManyToOne
    @JoinColumn(name="counterparty_to_fk")
    Counterparty target;

    BigDecimal value;

    BigDecimal prepaidValue;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
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
}
