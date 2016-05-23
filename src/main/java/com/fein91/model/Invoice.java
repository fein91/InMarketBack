package com.fein91.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
    Counterparty counterPartyFrom;

    @ManyToOne
    @JoinColumn(name="counterparty_to_fk")
    Counterparty counterPartyTo;

    BigDecimal value;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public Counterparty getCounterPartyFrom() {
        return counterPartyFrom;
    }

    public void setCounterPartyFrom(Counterparty counterPartyFrom) {
        this.counterPartyFrom = counterPartyFrom;
    }

    public Counterparty getCounterPartyTo() {
        return counterPartyTo;
    }

    public void setCounterPartyTo(Counterparty counterPartyTo) {
        this.counterPartyTo = counterPartyTo;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
