package com.fein91.core.service;

import com.fein91.builders.OrderBuilder;
import com.fein91.core.model.Order;
import com.fein91.core.model.OrderBook;
import com.fein91.core.model.OrderReport;
import com.fein91.core.model.Trade;
import com.fein91.model.OrderRequest;
import com.fein91.model.OrderResult;
import com.fein91.model.OrderType;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.google.common.base.Preconditions.checkArgument;

@Service
public class LimitOrderBookService {
    private static Logger log = Logger.getLogger(LimitOrderBookService.class);

    private static final int APR_SCALE = 1;

    public OrderResult addOrder(OrderBook lob, OrderRequest orderRequest) {
        BigDecimal quantity = orderRequest.getQuantity();
        BigDecimal price = orderRequest.getPrice();

        checkArgument(quantity.signum() > 0, "Quantity can't be 0");
        if (OrderType.LIMIT == orderRequest.getOrderType()) {
            checkArgument(price.signum() > 0, "Price can't be 0");
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
        log.info(lob);

        BigDecimal satisfiedDemand = quantity.subtract(orderReport.getQtyRemaining());

        BigDecimal apr = calculateAPR(lob, time, satisfiedDemand);
        BigDecimal totalDiscountSum = calculateTotalDiscountSum(lob);
        BigDecimal totalInvoicesSum = calculateTotalInvoicesSum(lob);
        BigDecimal avgDiscountPerc = totalInvoicesSum.signum() > 0 ? totalDiscountSum.divide(totalInvoicesSum, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;

        return new OrderResult(apr, satisfiedDemand, totalDiscountSum, avgDiscountPerc, BigDecimal.ZERO, orderReport.getTrades());
    }

    private BigDecimal calculateTotalInvoicesSum(OrderBook lob) {
        BigDecimal result = BigDecimal.ZERO;
        for (Trade trade : lob.getTape()) {
            result = result.add(trade.getInvoicesSum());
        }
        return result;
    }


    private BigDecimal calculateAPR(OrderBook lob, long time, BigDecimal satisfiedDemand) {
        BigDecimal apr = BigDecimal.ZERO;
        for (Trade trade : lob.getTape()) {
            if (trade.getTimestamp() == time) {
                apr = apr.add(BigDecimal.valueOf(trade.getPrice()).multiply(trade.getQty()).divide(satisfiedDemand, BigDecimal.ROUND_HALF_UP));
            }
        }

        return apr.setScale(APR_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTotalDiscountSum(OrderBook lob) {
        BigDecimal result = BigDecimal.ZERO;
        for (Trade trade : lob.getTape()) {
            result = result.add(trade.getDiscountSum());
        }
        return result;
    }
}
