package com.fein91.core.service;

import com.fein91.core.model.*;
import com.fein91.model.OrderResult;
import com.fein91.dao.InvoiceRepository;
import com.fein91.dao.OrderRequestRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import com.fein91.model.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LimitOrderBookService {

    private static final int APR_SCALE = 1;

    @Autowired
    InvoiceRepository invoiceRepository;
    @Autowired
    OrderRequestRepository orderRequestRepository;

    public OrderResult addAskMarketOrder(OrderBook lob, OrderRequest orderRequest) {
        return addAskMarketOrder(lob, orderRequest.getId(), orderRequest.getCounterparty(), orderRequest.getQuantity().intValue());
    }

    public OrderResult addAskMarketOrder(OrderBook lob, BigInteger orderId, Counterparty target, int quantity) {
        List<Invoice> invoices = invoiceRepository.findByTarget(target);

        if (invoices.isEmpty()) {
            throw new IllegalArgumentException("No invoices were found target counterparty: " + target);
        }

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty source = invoice.getSource();
            map.put(source.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        return addMarketOrder(lob, orderId, target.getId(), OrderSide.ASK, map, quantity);
    }

    public OrderResult addBidMarketOrder(OrderBook lob, OrderRequest orderRequest) {
        return addBidMarketOrder(lob, orderRequest.getId(), orderRequest.getCounterparty(), orderRequest.getQuantity().intValue());
    }

    public OrderResult addBidMarketOrder(OrderBook lob, BigInteger orderId, Counterparty source, int quantity) {
        List<Invoice> invoices = invoiceRepository.findBySource(source);

        if (invoices.isEmpty()) {
            throw new IllegalArgumentException("No invoices were found source counterparty: " + source);
        }

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty target = invoice.getTarget();
            map.put(target.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        return addMarketOrder(lob, orderId, source.getId(), OrderSide.BID, map, quantity);
    }

    public OrderResult addAskLimitOrder(OrderBook lob, OrderRequest orderRequest) {
        return addAskLimitOrder(lob, orderRequest.getId(), orderRequest.getCounterparty(), orderRequest.getQuantity().intValue(), orderRequest.getPrice().doubleValue());
    }

    public OrderResult addAskLimitOrder(OrderBook lob, BigInteger orderId, Counterparty target, int quantity, double price) {
        List<Invoice> invoices = invoiceRepository.findByTarget(target);

        if (invoices.isEmpty()) {
            throw new IllegalArgumentException("No invoices were found target counterparty: " + target);
        }

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty source = invoice.getSource();
            map.put(source.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        return addLimitOrder(lob, orderId, target.getId(), OrderSide.ASK, map, quantity, price);
    }

    public OrderResult addBidLimitOrder(OrderBook lob, OrderRequest orderRequest) {
        return addBidLimitOrder(lob, orderRequest.getId(), orderRequest.getCounterparty(), orderRequest.getQuantity().intValue(), orderRequest.getPrice().doubleValue());
    }

    public OrderResult addBidLimitOrder(OrderBook lob, BigInteger orderId, Counterparty source, int quantity, double price) {
        List<Invoice> invoices = invoiceRepository.findBySource(source);

        if (invoices.isEmpty()) {
            throw new IllegalArgumentException("No invoices were found source counterparty: " + source);
        }

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty target = invoice.getTarget();
            map.put(target.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        return addLimitOrder(lob, orderId, source.getId(), OrderSide.BID, map, quantity, price);
    }

    public OrderResult addLimitOrder(OrderBook lob,
                                     BigInteger orderId,
                                     BigInteger counterPartyId,
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
        Order order = new Order(orderId, time, true, quantity, counterPartyId.intValue(), orderSide.getCoreName(), price);
        order.setInvoicesQtyByGiverId(invoicesQtyByGiverId);

        OrderReport orderReport = lob.processOrder(order, false);
        System.out.println(lob);

        int satisfiedDemand = quantity - orderReport.getQtyRemaining();

        BigDecimal apr = calculateAPR(lob, time, satisfiedDemand);

        return new OrderResult(apr, satisfiedDemand);
    }

    public OrderResult addMarketOrder(OrderBook lob,
                                      BigInteger orderId,
                                      BigInteger counterPartyId,
                                      OrderSide orderSide,
                                      Map<Integer, List<Integer>> invoicesQtyByGiverId,
                                      int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity can't be 0");
        }

        long time = System.nanoTime();
        Order order = new Order(orderId, time, false, quantity, counterPartyId.intValue(), orderSide.getCoreName());
        order.setInvoicesQtyByGiverId(invoicesQtyByGiverId);

        OrderReport orderReport = lob.processOrder(order, false);
        System.out.println(lob);

        int satisfiedDemand = quantity - orderReport.getQtyRemaining();

        BigDecimal apr = calculateAPR(lob, time, satisfiedDemand);

        return new OrderResult(apr, satisfiedDemand);
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
