package com.fein91.core.service;

import com.fein91.core.model.MarketOrderResult;
import com.fein91.dao.InvoiceRepository;
import com.fein91.dao.OrderRequestRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
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

    public MarketOrderResult addAskMarketOrder(LimitOrderBookDecorator lob, Counterparty to, int quantity) {
        List<Invoice> invoices = invoiceRepository.findByTarget(to);

        if (invoices.isEmpty()) {
            throw new IllegalArgumentException("No invoices were found to counterparty: " + to);
        }

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty source = invoice.getSource();
            map.put(source.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        return lob.addAskMarketOrder(to.getId(), map, quantity);
    }

    public MarketOrderResult addBidMarketOrder(LimitOrderBookDecorator lob, Counterparty source, int quantity) {
        List<Invoice> invoices = invoiceRepository.findBySource(source);

        if (invoices.isEmpty()) {
            throw new IllegalArgumentException("No invoices were found source counterparty: " + source);
        }

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty target = invoice.getTarget();
            map.put(target.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        return lob.addBidMarketOrder(source.getId(), map, quantity);
    }

    public void addAskLimitOrder(LimitOrderBookDecorator lob, Counterparty target, int quantity, double price) {
        List<Invoice> invoices = invoiceRepository.findByTarget(target);

        if (invoices.isEmpty()) {
            throw new IllegalArgumentException("No invoices were found target counterparty: " + target);
        }

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty source = invoice.getSource();
            map.put(source.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        lob.addAskLimitOrder(target.getId(), map, quantity, price);
    }

    public void addBidLimitOrder(LimitOrderBookDecorator lob, Counterparty source, int quantity, double price) {
        List<Invoice> invoices = invoiceRepository.findBySource(source);

        if (invoices.isEmpty()) {
            throw new IllegalArgumentException("No invoices were found source counterparty: " + source);
        }

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty target = invoice.getTarget();
            map.put(target.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        lob.addBidLimitOrder(source.getId(), map, quantity, price);
    }
}
