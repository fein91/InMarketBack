package com.fein91.core.model;

import java.math.BigDecimal;

public class MarketOrderResult {
    final BigDecimal apr;
    final int satisfiedDemand;

    public MarketOrderResult(BigDecimal apr, int satisfiedDemand) {
        this.apr = apr;
        this.satisfiedDemand = satisfiedDemand;
    }

    public BigDecimal getApr() {
        return apr;
    }

    public int getSatisfiedDemand() {
        return satisfiedDemand;
    }
}
