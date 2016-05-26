package com.fein91.core.service;

import com.fein91.core.model.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

public class LimitOrderBookDecorator {

    private final static double DEFAULT_TICK_SIZE = 0.01;
    private static final int APR_SCALE = 1;

    protected final OrderBook lob;

    public LimitOrderBookDecorator() {
        this.lob = new OrderBook(DEFAULT_TICK_SIZE);
    }

    public void addAskLimitOrder(BigInteger counterPartyId,
                                 Map<Integer, List<Integer>> invoicesQtyByGiverId,
                                 int quantity,
                                 double price) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("ASK quantity can't be 0");
        } else if (price <= 0) {
            throw new IllegalArgumentException("ASK price can't be 0");
        }

        Order order = new Order(System.nanoTime(), true, quantity, counterPartyId.intValue(), OrderType.ASK.getCoreName(), price);
        order.setInvoicesQtyByGiverId(invoicesQtyByGiverId);

        lob.processOrder(order, false);
        System.out.println(lob);
    }

    public void addBidLimitOrder(BigInteger counterPartyId,
                                 Map<Integer, List<Integer>> invoicesQtyByGiverId,
                                 int quantity,
                                 double price) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("ASK quantity can't be 0");
        } else if (price <= 0) {
            throw new IllegalArgumentException("ASK price can't be 0");
        }

        Order order = new Order(System.nanoTime(), true, quantity, counterPartyId.intValue(), OrderType.BID.getCoreName(), price);
        order.setInvoicesQtyByGiverId(invoicesQtyByGiverId);

        lob.processOrder(order, false);
        System.out.println(lob);
    }

    public MarketOrderResult addAskMarketOrder(BigInteger counterPartyId,
                                               Map<Integer, List<Integer>> invoicesQtyByGiverId,
                                               int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("ASK quantity can't be 0");
        }

        long time = System.nanoTime();
        Order order = new Order(time, false, quantity, counterPartyId.intValue(), OrderType.ASK.getCoreName());
        order.setInvoicesQtyByGiverId(invoicesQtyByGiverId);

        OrderReport orderReport = lob.processOrder(order, false);
        System.out.println(lob);

        int satisfiedDemand = quantity - orderReport.getQtyRemaining();

        BigDecimal apr = calculateAPR(time, satisfiedDemand);

        if (orderReport.getQtyRemaining() > 0) {
            System.out.println("Unsatisfied demand was moved to ask limit order!!!");
            //addAskLimitOrder(counterPartyId, orderReport.getQtyRemaining(), apr.doubleValue());
        }

        return new MarketOrderResult(apr, satisfiedDemand);
    }

    public MarketOrderResult addBidMarketOrder(BigInteger counterPartyId,
                                               Map<Integer, List<Integer>> invoicesQtyByGiverId,
                                               int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("BID quantity can't be 0");
        }

        long time = System.nanoTime();

        Order order = new Order(time, false, quantity, counterPartyId.intValue(), OrderType.BID.getCoreName());
        order.setInvoicesQtyByGiverId(invoicesQtyByGiverId);

        OrderReport orderReport = lob.processOrder(order, false);
        System.out.println(lob);

        int satisfiedDemand = quantity - orderReport.getQtyRemaining();

        BigDecimal apr = calculateAPR(time, satisfiedDemand);

        if (orderReport.getQtyRemaining() > 0) {
            System.out.println("Unsatisfied demand was moved to bid limit order!!!");
            //addBidLimitOrder(counterPartyId, orderReport.getQtyRemaining(), apr.doubleValue());
        }

        return new MarketOrderResult(apr, satisfiedDemand);
    }

    protected BigDecimal calculateAPR(long time, int satisfiedDemand) {
        BigDecimal apr = BigDecimal.ZERO;
        for (Trade trade : lob.getTape()) {
            if (trade.getTimestamp() == time) {
                apr = apr.add(BigDecimal.valueOf(trade.getPrice() * trade.getQty() / satisfiedDemand));
            }
        }

        return apr.setScale(APR_SCALE, RoundingMode.HALF_UP);
    }
}
