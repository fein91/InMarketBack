package com.fein91.core.service;

import com.fein91.dao.InvoiceRepository;
import com.fein91.dao.RequestRepository;
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
    RequestRepository requestRepository;

    public void addAskMarketOrder(LimitOrderBookDecorator lob, Counterparty to, int quantity) {
        List<Invoice> invoices = invoiceRepository.findByCounterPartyTo(to);

        if (invoices.isEmpty()) {
            throw new IllegalArgumentException("No invoices were found to counterparty: " + to);
        }

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty from = invoice.getCounterPartyFrom();
            map.put(from.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        lob.addAskMarketOrder(to.getId(), map, quantity);
    }

    public void addBidMarketOrder(LimitOrderBookDecorator lob, Counterparty from, int quantity) {
        List<Invoice> invoices = invoiceRepository.findByCounterPartyFrom(from);

        if (invoices.isEmpty()) {
            throw new IllegalArgumentException("No invoices were found from counterparty: " + from);
        }

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty to = invoice.getCounterPartyTo();
            map.put(to.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        lob.addBidMarketOrder(from.getId(), map, quantity);
    }

    public void addAskLimitOrder(LimitOrderBookDecorator lob, Counterparty to, int quantity, double price) {
        List<Invoice> invoices = invoiceRepository.findByCounterPartyTo(to);

        if (invoices.isEmpty()) {
            throw new IllegalArgumentException("No invoices were found to counterparty: " + to);
        }

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty from = invoice.getCounterPartyFrom();
            map.put(from.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        lob.addAskLimitOrder(to.getId(), map, quantity, price);
    }

    public void addBidLimitOrder(LimitOrderBookDecorator lob, Counterparty from, int quantity, double price) {
        List<Invoice> invoices = invoiceRepository.findByCounterPartyFrom(from);

        if (invoices.isEmpty()) {
            throw new IllegalArgumentException("No invoices were found from counterparty: " + from);
        }

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty to = invoice.getCounterPartyTo();
            map.put(to.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        lob.addBidLimitOrder(from.getId(), map, quantity, price);
    }


//    public MarketOrderResult calculateSupplierApr(LimitOrderBookDecorator lob, Counterparty supplier, int quantity) {
//        List<Invoice> supplierInvoices = invoiceRepository.findByCounterPartyFrom(supplier);
//
//        for (Invoice invoice : supplierInvoices) {
//            Counterparty buyer = invoice.getCounterPartyTo();
//            //TODO find by counterparty and order type == ASK
//            List<Request> buyerOrders = orderRepository.findByCounterparty(buyer);
//
//            for (Request order : buyerOrders) {
//                BigDecimal localOrderValue = order.getQuantity().min(invoice.getValue());
//                lob.addAskLimitOrder(localOrderValue.intValue(), order.getPrice().doubleValue());
//            }
//        }
//        return lob.addBidMarketOrder(quantity);
//    }
}
