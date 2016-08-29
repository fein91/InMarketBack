package com.fein91.core.service;

import com.fein91.builders.OrderBuilder;
import com.fein91.core.model.Order;
import com.fein91.core.model.OrderBook;
import com.fein91.core.model.OrderReport;
import com.fein91.model.OrderRequest;
import com.fein91.model.OrderResult;
import com.fein91.model.OrderType;
import com.fein91.service.CalculationService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.fein91.Constants.UI_SCALE;
import static com.fein91.Constants.ROUNDING_MODE;

@Service
public class LimitOrderBookService {
    private static Logger log = Logger.getLogger(LimitOrderBookService.class);

    private CalculationService calculationService;

    @Autowired
    public LimitOrderBookService(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    public OrderResult addOrder(OrderBook lob, OrderRequest orderRequest) {
        BigDecimal quantity = orderRequest.getQuantity();
        BigDecimal price = orderRequest.getPrice();

        checkArgument(quantity.signum() > 0, "Quantity can't be 0");
        if (OrderType.LIMIT == orderRequest.getType()) {
            checkArgument(price.signum() > 0, "Price can't be 0");
        }

        long time = System.nanoTime();
        Order order = new OrderBuilder(orderRequest.getId())
                .timestamp(time)
                .orderSide(orderRequest.getSide())
                .orderType(orderRequest.getType())
                .quantity(quantity)
                .price(price)
                .takerId(orderRequest.getCounterparty().getId())
                .build();

        OrderReport orderReport = lob.processOrder(order);
        log.info(lob);

        BigDecimal satisfiedDemand = quantity.subtract(orderReport.getQtyRemaining());

        BigDecimal apr = calculationService.calculateAPR(lob.getTape(), satisfiedDemand);
        BigDecimal totalDiscountSum = calculationService.calculateTotalDiscountSum(lob.getTape());
        BigDecimal totalInvoicesSum = calculationService.calculateTotalInvoicesSum(lob.getTape());
        BigDecimal avgDiscountPerc = calculationService.calculateAvgDiscountPerc(totalDiscountSum, totalInvoicesSum);
        BigDecimal avgDaysToPayment = calculationService.calculateAvgDaysToPayment(lob.getTape());

        return new OrderResult(apr.setScale(UI_SCALE, ROUNDING_MODE),
                satisfiedDemand.setScale(UI_SCALE, ROUNDING_MODE),
                totalDiscountSum.setScale(UI_SCALE, ROUNDING_MODE),
                avgDiscountPerc.setScale(UI_SCALE, ROUNDING_MODE),
                avgDaysToPayment.setScale(UI_SCALE, ROUNDING_MODE),
                orderReport.getTrades());
    }
}
