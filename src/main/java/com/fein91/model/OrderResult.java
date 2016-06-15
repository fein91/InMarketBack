package com.fein91.model;

import com.fein91.core.model.Trade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderResult {
    final BigDecimal apr;
    final BigDecimal satisfiedDemand;
    final BigDecimal discountSum;
    final BigDecimal avgDiscountPerc;
    final BigDecimal avgDaysToPayment;
    @Deprecated
    final List<Trade> trades;

    public OrderResult(BigDecimal apr, BigDecimal satisfiedDemand, BigDecimal discountSum, BigDecimal avgDiscountPerc,
                       BigDecimal avgDaysToPayment, List<Trade> trades) {
        this.apr = apr;
        this.satisfiedDemand = satisfiedDemand;
        this.trades = trades;
        this.discountSum = discountSum;
        this.avgDiscountPerc = avgDiscountPerc;
        this.avgDaysToPayment = avgDaysToPayment;
    }

    public BigDecimal getApr() {
        return apr;
    }

    public BigDecimal getSatisfiedDemand() {
        return satisfiedDemand;
    }

    /**
     *
     * @deprecated only used in tests remove it from here
     */
    @Deprecated
    public List<Trade> getTape() {
        return trades;
    }
}
