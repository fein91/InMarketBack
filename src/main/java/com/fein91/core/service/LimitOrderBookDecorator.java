package com.fein91.core.service;

import com.fein91.core.model.*;
import com.fein91.model.OrderRequest;
import com.fein91.model.OrderType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

public class LimitOrderBookDecorator {

    private final static double DEFAULT_TICK_SIZE = 0.1;
    private static final int APR_SCALE = 1;

    protected final OrderBook lob;

    public LimitOrderBookDecorator() {
        this.lob = new OrderBook(DEFAULT_TICK_SIZE);
    }

    public OrderResult addOrder(OrderRequest orderRequest, Map<Integer, List<Integer>> invoicesQtyByGiverId) {
        if (OrderType.LIMIT == orderRequest.getOrderType()) {
            return addLimitOrder(orderRequest.getCounterparty().getId(),
                        orderRequest.getOrderSide(),
                        invoicesQtyByGiverId,
                        orderRequest.getQuantity().intValue(),
                        orderRequest.getPrice().doubleValue());
        } else if (OrderType.MARKET == orderRequest.getOrderType()) {
            return addMarketOrder(orderRequest.getCounterparty().getId(),
                        orderRequest.getOrderSide(),
                        invoicesQtyByGiverId,
                        orderRequest.getQuantity().intValue());
        } else {
            throw new IllegalArgumentException("Unknown orderRequest type: " + orderRequest);
        }
    }

    public OrderResult addLimitOrder(BigInteger counterPartyId,
                              OrderSide orderSide,
                              Map<Integer, List<Integer>> invoicesQtyByGiverId,
                              int quantity,
                              double price) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity can't be 0");
        } else if (price <= 0) {
            throw new IllegalArgumentException("Price can't be 0");
        }

        long time = System.nanoTime();
        Order order = new Order(time, true, quantity, counterPartyId.intValue(), orderSide.getCoreName(), price);
        order.setInvoicesQtyByGiverId(invoicesQtyByGiverId);

        OrderReport orderReport = lob.processOrder(order, false);
        System.out.println(lob);

        int satisfiedDemand = quantity - orderReport.getQtyRemaining();

        BigDecimal apr = calculateAPR(time, satisfiedDemand);

        return new OrderResult(apr, satisfiedDemand);
    }

    public OrderResult addMarketOrder(BigInteger counterPartyId,
                                            OrderSide orderSide,
                                            Map<Integer, List<Integer>> invoicesQtyByGiverId,
                                            int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity can't be 0");
        }

        long time = System.nanoTime();
        Order order = new Order(time, false, quantity, counterPartyId.intValue(), orderSide.getCoreName());
        order.setInvoicesQtyByGiverId(invoicesQtyByGiverId);

        OrderReport orderReport = lob.processOrder(order, false);
        System.out.println(lob);

        int satisfiedDemand = quantity - orderReport.getQtyRemaining();

        BigDecimal apr = calculateAPR(time, satisfiedDemand);

        return new OrderResult(apr, satisfiedDemand);
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
