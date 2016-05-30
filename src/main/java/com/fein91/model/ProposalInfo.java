package com.fein91.model;

import java.math.BigDecimal;

public class ProposalInfo {

    private final BigDecimal asksSum;
    private final BigDecimal bidsSum;
    private final BigDecimal invoicesSum;

    public ProposalInfo(BigDecimal asksSum, BigDecimal bidsSum, BigDecimal invoicesSum) {
        this.asksSum = asksSum;
        this.bidsSum = bidsSum;
        this.invoicesSum = invoicesSum;
    }

    public BigDecimal getAsksSum() {
        return asksSum;
    }

    public BigDecimal getBidsSum() {
        return bidsSum;
    }

    public BigDecimal getInvoicesSum() {
        return invoicesSum;
    }
}
