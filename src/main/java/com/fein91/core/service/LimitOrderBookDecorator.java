package com.fein91.core.service;

import com.fein91.core.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class LimitOrderBookDecorator {

    private final static double DEFAULT_TICK_SIZE = 0.01;

    protected final OrderBook lob;

    public LimitOrderBookDecorator() {
        this.lob = new OrderBook(DEFAULT_TICK_SIZE);
    }

    public void addAskLimitOrder(int quantity, double price) {
        Order order = new Order(System.nanoTime(), true, quantity, new Random(100000).nextInt(), OrderType.ASK.getCoreName(), price);
        lob.processOrder(order, false);
        System.out.println(lob);
    }

    public void addBidLimitOrder(int quantity, double price) {
        Order order = new Order(System.nanoTime(), true, quantity, new Random(100000).nextInt(), OrderType.BID.getCoreName(), price);
        lob.processOrder(order, false);
        System.out.println(lob);
    }

    public MarketOrderResult addAskMarketOrder(int quantity) {
        long time = System.nanoTime();

        Order order = new Order(time, false, quantity, new Random(100000).nextInt(), OrderType.ASK.getCoreName());
        int volumeBefore = lob.volumeOnSide(OrderType.BID.getCoreName());
        lob.processOrder(order, false);
        int volumeAfter = lob.volumeOnSide(OrderType.BID.getCoreName());
        System.out.println(lob);

        int satisfiedDemand = volumeBefore - volumeAfter;

        BigDecimal apr = calculateAPR(time, satisfiedDemand);

        int unsatisfiedDemand = quantity - satisfiedDemand;
        if (unsatisfiedDemand > 0) {
            System.out.println("Unsatisfied demand was moved to ask limit order!!!");
            addAskLimitOrder(unsatisfiedDemand, apr.doubleValue());
        }

        return new MarketOrderResult(apr, satisfiedDemand);
    }

    public MarketOrderResult addBidMarketOrder(int quantity) {
        long time = System.nanoTime();

        Order order = new Order(time, false, quantity, new Random(100000).nextInt(), OrderType.BID.getCoreName());

        int volumeBefore = lob.volumeOnSide(OrderType.ASK.getCoreName());
        lob.processOrder(order, false);
        int volumeAfter = lob.volumeOnSide(OrderType.ASK.getCoreName());

        System.out.println(lob);

        int satisfiedDemand = volumeBefore - volumeAfter;

        BigDecimal apr = calculateAPR(time, satisfiedDemand);

        int unsatisfiedDemand = quantity - satisfiedDemand;
        if (unsatisfiedDemand > 0) {
            System.out.println("Unsatisfied demand was moved to bid limit order!!!");
            addBidLimitOrder(unsatisfiedDemand, apr.doubleValue());
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

        apr.setScale(2, RoundingMode.HALF_UP);
        return apr;
    }
}
