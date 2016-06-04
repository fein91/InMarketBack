package com.fein91.model;

import java.math.BigDecimal;

public class ProposalInfo {

    private final BigDecimal asksSum;
    private final BigDecimal bidsSum;

    public ProposalInfo(BigDecimal asksSum, BigDecimal bidsSum) {
        this.asksSum = asksSum;
        this.bidsSum = bidsSum;
    }

    public BigDecimal getAsksSum() {
        return asksSum;
    }

    public BigDecimal getBidsSum() {
        return bidsSum;
    }

}
