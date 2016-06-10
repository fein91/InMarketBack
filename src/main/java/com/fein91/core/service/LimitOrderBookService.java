package com.fein91.core.service;

import com.fein91.builders.OrderBuilder;
import com.fein91.core.model.*;
import com.fein91.model.*;
import com.fein91.dao.InvoiceRepository;
import com.fein91.dao.OrderRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class LimitOrderBookService {

    private static final int APR_SCALE = 1;

    @Autowired
    InvoiceRepository invoiceRepository;
    @Autowired
    OrderRequestRepository orderRequestRepository;

    public OrderResult addOrder(OrderBook lob, OrderRequest orderRequest) {
        BigDecimal quantity = orderRequest.getQuantity();
        BigDecimal price = orderRequest.getPrice();

        if (quantity.signum() <= 0) {
            throw new IllegalArgumentException("Quantity can't be 0");
        } else if (OrderType.LIMIT == orderRequest.getOrderType() && price.signum() <= 0) {
            throw new IllegalArgumentException("Price can't be 0");
        }

        long time = System.nanoTime();
        Order order = new OrderBuilder(orderRequest.getId())
                .timestamp(time)
                .orderSide(orderRequest.getOrderSide())
                .orderType(orderRequest.getOrderType())
                .quantity(quantity)
                .price(price)
                .takerId(orderRequest.getCounterparty().getId())
                .build();

        OrderReport orderReport = lob.processOrder(order, false);
        System.out.println(lob);

        int satisfiedDemand = quantity.intValue() - orderReport.getQtyRemaining();

        BigDecimal apr = calculateAPR(lob, time, satisfiedDemand);

        return new OrderResult(apr, satisfiedDemand, orderReport.getTrades());
    }

    protected BigDecimal calculateAPR(OrderBook lob, long time, int satisfiedDemand) {
        BigDecimal apr = BigDecimal.ZERO;
        for (Trade trade : lob.getTape()) {
            if (trade.getTimestamp() == time) {
                apr = apr.add(BigDecimal.valueOf(trade.getPrice() * trade.getQty() / satisfiedDemand));
            }
        }

        return apr.setScale(APR_SCALE, RoundingMode.HALF_UP);
    }
}
