package com.fein91.core.service;

import com.fein91.core.model.OrderResult;
import com.fein91.core.model.OrderSide;
import com.fein91.dao.InvoiceRepository;
import com.fein91.dao.OrderRequestRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import com.fein91.model.OrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LimitOrderBookService {

    @Autowired
    InvoiceRepository invoiceRepository;
    @Autowired
    OrderRequestRepository orderRequestRepository;

    public OrderResult addAskMarketOrder(LimitOrderBookDecorator lob, OrderRequest orderRequest) {
        return addAskMarketOrder(lob, orderRequest.getCounterparty(), orderRequest.getQuantity().intValue());
    }

    public OrderResult addAskMarketOrder(LimitOrderBookDecorator lob, Counterparty target, int quantity) {
        List<Invoice> invoices = invoiceRepository.findByTarget(target);

        if (invoices.isEmpty()) {
            throw new IllegalArgumentException("No invoices were found target counterparty: " + target);
        }

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty source = invoice.getSource();
            map.put(source.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        return lob.addMarketOrder(target.getId(), OrderSide.ASK, map, quantity);
    }

    public OrderResult addBidMarketOrder(LimitOrderBookDecorator lob, OrderRequest orderRequest) {
        return addBidMarketOrder(lob, orderRequest.getCounterparty(), orderRequest.getQuantity().intValue());
    }

    public OrderResult addBidMarketOrder(LimitOrderBookDecorator lob, Counterparty source, int quantity) {
        List<Invoice> invoices = invoiceRepository.findBySource(source);

        if (invoices.isEmpty()) {
            throw new IllegalArgumentException("No invoices were found source counterparty: " + source);
        }

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty target = invoice.getTarget();
            map.put(target.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        return lob.addMarketOrder(source.getId(), OrderSide.BID, map, quantity);
    }

    public OrderResult addAskLimitOrder(LimitOrderBookDecorator lob, OrderRequest orderRequest) {
        return addAskLimitOrder(lob, orderRequest.getCounterparty(), orderRequest.getQuantity().intValue(), orderRequest.getPrice().doubleValue());
    }

    public OrderResult addAskLimitOrder(LimitOrderBookDecorator lob, Counterparty target, int quantity, double price) {
        List<Invoice> invoices = invoiceRepository.findByTarget(target);

        if (invoices.isEmpty()) {
            throw new IllegalArgumentException("No invoices were found target counterparty: " + target);
        }

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty source = invoice.getSource();
            map.put(source.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        return lob.addLimitOrder(target.getId(), OrderSide.ASK, map, quantity, price);
    }

    public OrderResult addBidLimitOrder(LimitOrderBookDecorator lob, OrderRequest orderRequest) {
        return addBidLimitOrder(lob, orderRequest.getCounterparty(), orderRequest.getQuantity().intValue(), orderRequest.getPrice().doubleValue());
    }

    public OrderResult addBidLimitOrder(LimitOrderBookDecorator lob, Counterparty source, int quantity, double price) {
        List<Invoice> invoices = invoiceRepository.findBySource(source);

        if (invoices.isEmpty()) {
            throw new IllegalArgumentException("No invoices were found source counterparty: " + source);
        }

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty target = invoice.getTarget();
            map.put(target.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        return lob.addLimitOrder(source.getId(), OrderSide.BID, map, quantity, price);
    }
}
