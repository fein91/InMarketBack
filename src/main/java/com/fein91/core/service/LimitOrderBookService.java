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

    private static final int SCALE = 2;

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

        OrderReport orderReport = lob.processOrder(order);
        log.info(lob);

        BigDecimal satisfiedDemand = quantity.subtract(orderReport.getQtyRemaining());

        BigDecimal apr = calculateAPR(lob, satisfiedDemand);
        BigDecimal totalDiscountSum = calculateTotalDiscountSum(lob);
        BigDecimal totalInvoicesSum = calculateTotalInvoicesSum(lob);
        BigDecimal avgDiscountPerc = totalInvoicesSum.signum() > 0
                ? totalDiscountSum.divide(totalInvoicesSum, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        BigDecimal avgDaysToPayment = calculateAvgDaysToPayment(lob);

        return new OrderResult(apr.setScale(SCALE, RoundingMode.HALF_UP),
                satisfiedDemand.setScale(SCALE, RoundingMode.HALF_UP),
                totalDiscountSum.setScale(SCALE, RoundingMode.HALF_UP),
                avgDiscountPerc.setScale(SCALE, RoundingMode.HALF_UP),
                avgDaysToPayment.setScale(SCALE, RoundingMode.HALF_UP),
                orderReport.getTrades());
    }

    /**
     * avgDaysToPayment = (invoice1.getDaysToPayment() * paymentByInvoice1 + ... + invoiceN.getDaysToPayment() * paymentByInvoiceN) / paymentByInvoice1 + ... + paymentByInvoiceN
     * @param lob
     * @return
     */
    private BigDecimal calculateAvgDaysToPayment(OrderBook lob) {
        BigDecimal totalSumInvoicesDaysToPaymentMultQtyTraded = BigDecimal.ZERO;
        BigDecimal paymentsSum = BigDecimal.ZERO;
        for (Trade trade: lob.getTape()) {
            totalSumInvoicesDaysToPaymentMultQtyTraded = totalSumInvoicesDaysToPaymentMultQtyTraded.add(trade.getDaysToPaymentMultQtyTraded());
            paymentsSum = paymentsSum.add(trade.getQty());
        }
        return paymentsSum.signum() > 0 ?
                totalSumInvoicesDaysToPaymentMultQtyTraded.divide(paymentsSum, BigDecimal.ROUND_HALF_UP)
                : BigDecimal.ZERO;
    }

    private BigDecimal calculateTotalInvoicesSum(OrderBook lob) {
        BigDecimal result = BigDecimal.ZERO;
        for (Trade trade : lob.getTape()) {
            result = result.add(trade.getInvoiceValue());
        }
        return result;
    }


    private BigDecimal calculateAPR(OrderBook lob, BigDecimal satisfiedDemand) {
        BigDecimal apr = BigDecimal.ZERO;
        for (Trade trade : lob.getTape()) {
            apr = apr.add(BigDecimal.valueOf(trade.getPrice()).multiply(trade.getQty()).divide(satisfiedDemand, BigDecimal.ROUND_HALF_UP));
        }

        return apr;
    }

    private BigDecimal calculateTotalDiscountSum(OrderBook lob) {
        BigDecimal result = BigDecimal.ZERO;
        for (Trade trade : lob.getTape()) {
            result = result.add(trade.getDiscountValue());
        }
        return result;
    }
}
