package com.fein91.core.service;

import com.fein91.dao.InvoiceRepository;
import com.fein91.dao.OrderRepository;
import com.fein91.model.Counterparty;
import com.fein91.model.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LimitOrderBookService {

    @Autowired
    InvoiceRepository invoiceRepository;
    @Autowired
    OrderRepository orderRepository;

    public void addAskMarketOrder(LimitOrderBookDecorator lob, Counterparty from, int quantity) {
        List<Invoice> invoices = invoiceRepository.findByCounterPartyFrom(from);

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty to = invoice.getCounterPartyTo();
            map.put(to.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        lob.addAskMarketOrder(from.getId(), map, quantity);
    }

    public void addBidMarketOrder(LimitOrderBookDecorator lob, Counterparty to, int quantity) {
        List<Invoice> invoices = invoiceRepository.findByCounterPartyTo(to);

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty from = invoice.getCounterPartyFrom();
            map.put(from.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        lob.addBidMarketOrder(to.getId(), map, quantity);
    }

    public void addAskLimitOrder(LimitOrderBookDecorator lob, Counterparty from, int quantity, double price) {
        List<Invoice> invoices = invoiceRepository.findByCounterPartyFrom(from);

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty to = invoice.getCounterPartyTo();
            map.put(to.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        lob.addAskLimitOrder(from.getId(), map, quantity, price);
    }

    public void addBidLimitOrder(LimitOrderBookDecorator lob, Counterparty to, int quantity, double price) {
        List<Invoice> invoices = invoiceRepository.findByCounterPartyTo(to);

        Map<Integer, List<Integer>> map = new HashMap<>();

        for (Invoice invoice : invoices) {
            Counterparty from = invoice.getCounterPartyFrom();
            map.put(from.getId().intValue(), Collections.singletonList(invoice.getValue().intValue()));
        }

        lob.addBidLimitOrder(to.getId(), map, quantity, price);
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
