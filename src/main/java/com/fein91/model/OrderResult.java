package com.fein91.model;

import com.fein91.core.model.Trade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderResult {
    final BigDecimal apr;
    final BigDecimal satisfiedDemand;
    final List<Trade> trades;

    public OrderResult(BigDecimal apr, BigDecimal satisfiedDemand, List<Trade> trades) {
        this.apr = apr;
        this.satisfiedDemand = satisfiedDemand;
        this.trades = trades;
    }

    public BigDecimal getApr() {
        return apr;
    }

    public BigDecimal getSatisfiedDemand() {
        return satisfiedDemand;
    }

    public List<Trade> getTape() {
        return trades;
    }
}
